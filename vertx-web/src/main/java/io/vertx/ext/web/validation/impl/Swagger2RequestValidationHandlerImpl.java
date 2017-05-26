package io.vertx.ext.web.validation.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Operation;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.*;
import io.vertx.ext.web.validation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@Deprecated
public class Swagger2RequestValidationHandlerImpl extends HTTPOperationRequestValidationHandlerImpl<Operation> implements Swagger2RequestValidationHandler {

  public Swagger2RequestValidationHandlerImpl(Operation operation) {
    super(operation);
  }

  /**
   * Function that parse the operation specification and generate validation rules
   */
  @Override
  public void parseOperationSpec() {
    // Extract from path spec parameters description
    for (Parameter opParameter : this.pathSpec.getParameters()) {
      this.parseParameter(opParameter);
    }
  }

  private void loadValidationRule(String location, ParameterValidationRule rule) {
    switch (location) {
      case "header":
        this.addHeaderParamRule(rule);
        break;
      case "query":
        this.addQueryParamRule(rule);
        break;
      case "path":
        this.addPathParamRule(rule);
        break;
      case "formData":
        this.addFormParamRule(rule);
        break;
    }
  }

  private ParameterLocation parseLocation(AbstractSerializableParameter parameter) {
    switch (parameter.getIn()) {
      case "header":
        return ParameterLocation.HEADER;
      case "query":
        return ParameterLocation.QUERY;
      case "path":
        return ParameterLocation.PATH;
      case "formData":
        return ParameterLocation.BODY_FORM;
      case "body":
        return ParameterLocation.BODY;
      default:
        return null;
    }
  }

  private List<String> convertEnum(List numberEnum) {
    List<String> stringEnum = new ArrayList<>();
    numberEnum.forEach(number -> stringEnum.add(number.toString()));
    return stringEnum;
  }

  private String retrieveCollectionFormat(String collectionFormat) {
    return (collectionFormat != null) ? collectionFormat : "csv";
  }

  // "file" and "array" type are not managed inside this function!
  private ParameterTypeValidator resolveTypeValidatorFromProperty(Property property) {
    switch (property.getType()) {
      /* Can't use this code, missing collectionFormat 😠
      case "array":
        ArrayProperty arrayProperty = (ArrayProperty) property;
        return ParameterTypeValidator.createArrayTypeValidator(this.resolveTypeValidatorFromProperty(((ArrayProperty) property).getItems()), arrayProperty. TODO WHERE IS THIS FIELD );*/
      case "integer":
        IntegerProperty integerProperty = (IntegerProperty) property;
        if (integerProperty.getEnum() != null && integerProperty.getEnum().size() != 0)
          return ParameterTypeValidator.createEnumTypeValidator(this.convertEnum(integerProperty.getEnum()));
        else {
          if (integerProperty.getFormat() != null && integerProperty.getFormat().equals("int64")) {
            return ParameterTypeValidator.createLongTypeValidator(((integerProperty.getExclusiveMaximum() != null) ? integerProperty.getExclusiveMaximum() : false), integerProperty.getMaximum(), ((integerProperty.getExclusiveMinimum() != null) ? integerProperty.getExclusiveMinimum() : false), integerProperty.getMinimum(), null /* Where is multipleOf ?! */);
          } else {
            return ParameterTypeValidator.createIntegerTypeValidator(((integerProperty.getExclusiveMaximum() != null) ? integerProperty.getExclusiveMaximum() : false), integerProperty.getMaximum(), ((integerProperty.getExclusiveMinimum() != null) ? integerProperty.getExclusiveMinimum() : false), integerProperty.getMinimum(), null /* Where is multipleOf ?! */);
          }
        }
      case "number":
        AbstractNumericProperty numericProperty = (AbstractNumericProperty) property;
        if (numericProperty.getFormat().equals("float"))
          return ParameterTypeValidator.createFloatTypeValidator(((numericProperty.getExclusiveMaximum() != null) ? numericProperty.getExclusiveMaximum() : false), numericProperty.getMaximum(), ((numericProperty.getExclusiveMinimum() != null) ? numericProperty.getExclusiveMinimum() : false), numericProperty.getMinimum(), null /* Where is multipleOf ?! */);
        else
          return ParameterTypeValidator.createDoubleTypeValidator(((numericProperty.getExclusiveMaximum() != null) ? numericProperty.getExclusiveMaximum() : false), numericProperty.getMaximum(), ((numericProperty.getExclusiveMinimum() != null) ? numericProperty.getExclusiveMinimum() : false), numericProperty.getMinimum(), null /* Where is multipleOf ?! */);
      case "boolean":
        return ParameterType.BOOL.getValidationMethod();
      case "string":
        // First check if parameter is an enum
        AbstractProperty abstractStringProperty = (AbstractProperty) property;
        // Then resolve various string formats
        if (abstractStringProperty.getFormat() != null)
          switch (abstractStringProperty.getFormat()) {
            case "byte":
              return ParameterType.BASE64.getValidationMethod();
            case "date":
              return ParameterType.DATE.getValidationMethod();
            case "date-time":
              return ParameterType.DATETIME.getValidationMethod();
          }
        StringProperty stringProperty = (StringProperty) property;
        return ParameterTypeValidator.createStringTypeValidator(stringProperty.getPattern(), stringProperty.getMinLength(), stringProperty.getMaxLength());
    }
    return ParameterType.GENERIC_STRING.getValidationMethod();
  }

  // "file"  type is not managed inside this function!
  private ParameterTypeValidator resolveTypeValidatorFromParameter(AbstractSerializableParameter parameter) {
    if (parameter.getEnum() != null && parameter.getEnum().size() != 0)
      return ParameterTypeValidator.createEnumTypeValidator(parameter.getEnum());
    switch (parameter.getType()) {
      case "array":
        return ParameterTypeValidator.createArrayTypeValidator(this.resolveTypeValidatorFromProperty(parameter.getItems()), this.retrieveCollectionFormat(parameter.getCollectionFormat()), parameter.getMaxItems(), parameter.getMinItems());
      case "integer":
        if (parameter.getFormat() != null && parameter.getFormat().equals("int64")) {
          return ParameterTypeValidator.createLongTypeValidator(parameter.isExclusiveMaximum(), parameter.getMaximum(), parameter.isExclusiveMinimum(), parameter.getMinimum(), (parameter.getMultipleOf() != null) ? parameter.getMultipleOf().doubleValue() : null);
        } else {
          return ParameterTypeValidator.createIntegerTypeValidator(parameter.isExclusiveMaximum(), parameter.getMaximum(), parameter.isExclusiveMinimum(), parameter.getMinimum(), (parameter.getMultipleOf() != null) ? parameter.getMultipleOf().doubleValue() : null);
        }
      case "number":
        if (parameter.getFormat().equals("float"))
          return ParameterTypeValidator.createFloatTypeValidator(parameter.isExclusiveMaximum(), parameter.getMaximum(), parameter.isExclusiveMinimum(), parameter.getMinimum(), (parameter.getMultipleOf() != null) ? parameter.getMultipleOf().doubleValue() : null);
        else
          return ParameterTypeValidator.createDoubleTypeValidator(parameter.isExclusiveMaximum(), parameter.getMaximum(), parameter.isExclusiveMinimum(), parameter.getMinimum(), (parameter.getMultipleOf() != null) ? parameter.getMultipleOf().doubleValue() : null);
      case "boolean":
        return ParameterType.BOOL.getValidationMethod();
      case "string":
        // Then resolve various string formats
        if (parameter.getFormat() != null)
          switch (parameter.getFormat()) {
            case "byte":
              return ParameterType.BASE64.getValidationMethod();
            case "date":
              return ParameterType.DATE.getValidationMethod();
            case "date-time":
              return ParameterType.DATETIME.getValidationMethod();
          }
        return ParameterTypeValidator.createStringTypeValidator(parameter.getPattern(), parameter.getMinLength(), parameter.getMaxLength());

    }
    return ParameterType.GENERIC_STRING.getValidationMethod();
  }

  private void parseParameter(Parameter parameter) {
    if (parameter.getIn().equals("body")) {
      ObjectMapper mapper = new ObjectMapper();
      try {
        this.setJsonSchema(mapper.writeValueAsString(((BodyParameter) parameter).getSchema()));
        return;
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    } else {
      AbstractSerializableParameter serializableParameter = (AbstractSerializableParameter) parameter;
      if (serializableParameter.getType().equals("file")) {
        this.addFileUploadName(serializableParameter.getName());
      } else {
        this.loadValidationRule(serializableParameter.getIn(),
          ParameterValidationRule.createValidationRuleWithCustomTypeValidator(serializableParameter.getName(),
            this.resolveTypeValidatorFromParameter(serializableParameter),
            !serializableParameter.getRequired(),
            this.parseLocation(serializableParameter)));
      }
    }
  }

}
