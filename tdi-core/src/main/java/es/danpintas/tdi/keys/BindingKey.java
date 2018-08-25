package es.danpintas.tdi.keys;

import java.lang.annotation.Annotation;

import es.danpintas.reflect.TypeData;

/**
 * Paired key for identifying injection bindings.
 * 
 * @author danpintas
 *
 * @param <T> Type asociated to the binding.
 */
public final class BindingKey<T> extends AbstractKey<TypeData<T>, Annotation> {

  /**
   * Constructor.
   * 
   * @param typeData {@link TypeData} for the binding class.
   * @param annotation Qualifier {@link Annotation}.
   */
  public BindingKey(TypeData<T> typeData, Annotation annotation) {
    super(typeData, annotation);
  }

}
