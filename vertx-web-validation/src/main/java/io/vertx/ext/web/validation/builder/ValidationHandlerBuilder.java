package io.vertx.ext.web.validation.builder;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.builder.impl.ValidationHandlerBuilderImpl;

/**
 * Builder for a {@link ValidationHandler}. <br/>
 *
 * For more info look the docs
 */
public interface ValidationHandlerBuilder {

  /**
   * Add a parameter given the location and the processor
   *
   * @param location
   * @param processor
   * @return
   */
  @Fluent
  ValidationHandlerBuilder parameter(ParameterLocation location, ParameterProcessor processor);

  @Fluent
  ValidationHandlerBuilder queryParameter(StyledParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder queryParameter(ParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder pathParameter(ParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder cookieParameter(StyledParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder cookieParameter(ParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder headerParameter(ParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder body(BodyProcessorFactory bodyProcessor);

  @Fluent
  ValidationHandlerBuilder body(BodyProcessor bodyProcessor);

  @Fluent
  ValidationHandlerBuilder predicate(RequestPredicate predicate);

  /**
   * Build the {@link ValidationHandler} from this builder
   *
   * @return
   */
  ValidationHandler build();

  static ValidationHandlerBuilder create(SchemaParser parser) {
    return new ValidationHandlerBuilderImpl(parser);
  }

}
