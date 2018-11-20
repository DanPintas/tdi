package es.danpintas.tdi.keys;

import java.lang.annotation.Annotation;

import es.danpintas.reflect.TypeData;

/**
 * Paired key for identifying providers.
 *
 * @param <T> Type associated to the binding.
 * @author danpintas
 */
public final class ProviderKey<T> extends AbstractKey<TypeData<T>, Class<? extends Annotation>> {
    
    /**
     * Constructor.
     *
     * @param typeData {@link TypeData} for the provider.
     * @param scope    Scope {@link Annotation}.
     */
    public ProviderKey(TypeData<T> typeData, Class<? extends Annotation> scope) {
        super(typeData, scope);
    }
    
}
