package es.danpintas.tdi.keys;

import java.lang.annotation.Annotation;

import es.danpintas.reflect.TypeData;

/**
 * Paired key for identifying injection bindings.
 *
 * @param <T> Type associated to the binding.
 * @author danpintas
 */
public final class BindingKey<T> extends AbstractKey<TypeData<T>, Annotation> {
    
    /**
     * Constructor.
     *
     * @param typeData   {@link TypeData} for the binding class.
     * @param annotation Qualifier {@link Annotation}.
     */
    public BindingKey(TypeData<T> typeData, Annotation annotation) {
        super(typeData, annotation);
    }
    
}
