package es.danpintas.tdi.keys;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import javax.inject.Provider;

import es.danpintas.tdi.providers.InstanceProvider;

/**
 * Paired key for declaring scopes.
 * 
 * @author danpintas
 */
public class ScopeKey
    extends AbstractKey<Class<? extends Annotation>, Function<InstanceProvider, Provider>> {

  /**
   * Constructor.
   * 
   * @param annotation Scope.{@link Annotation}.
   * @param provision {@link Function} defining how to wrap a {@link Provider}.
   */
  public ScopeKey(Class<? extends Annotation> annotation,
      Function<InstanceProvider, Provider> provision) {
    super(annotation, provision);
  }

  /**
   * Gets the scope.
   * 
   * @return Scope.{@link Annotation}.
   */
  public Class<? extends Annotation> getAnnotation() {
    return a;
  }

  /**
   * Gets the provision {@link Function}.
   * 
   * @return {@link Function} defining how to wrap a {@link Provider}.
   */
  public Function<InstanceProvider, Provider> getProvision() {
    return b;
  }

}
