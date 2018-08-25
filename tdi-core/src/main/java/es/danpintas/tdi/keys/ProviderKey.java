package es.danpintas.tdi.keys;

import java.lang.annotation.Annotation;

import es.danpintas.reflect.TypeData;

/**
 * Paired key for identifying providers.
 * 
 * @author danpintas
 *
 * @param <T> Type asociated to the binding.
 */
public final class ProviderKey<T> extends AbstractKey<TypeData<T>, Class<? extends Annotation>> {

  /**
   * Constructor.
   * 
   * @param typeData {@link TypeData} for the provider.
   * @param scope Scope {@link Annotation}.
   */
  public ProviderKey(TypeData<T> typeData, Class<? extends Annotation> scope) {
    super(typeData, scope);
  }

}
