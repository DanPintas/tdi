package es.danpintas.tdi;

import es.danpintas.reflect.TypeData;
import es.danpintas.tdi.bindings.BindingConfig;

/**
 * Contract for injection configuration though bindings.
 *
 * @author danpintas
 */
@FunctionalInterface
public interface Binder {
    
    /**
     * Starts binding configuration for the given {@code Class}.
     *
     * @param clazz {@link Class} to bind.
     * @return Instantiated {@link BindingConfig}.
     */
    default <T> BindingConfig<T> bind(Class<T> clazz) {
        return bind(TypeData.get(clazz));
    }
    
    /**
     * Starts binding configuration for the given {@code TypeData}.
     *
     * @param typeData {@link TypeData} to bind.
     * @return Instantiated {@link BindingConfig}.
     */
    <T> BindingConfig<T> bind(TypeData<T> typeData);
    
}
