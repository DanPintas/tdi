package es.danpintas.tdi.bindings;

import java.lang.annotation.Annotation;

import javax.inject.Named;
import javax.inject.Provider;

import es.danpintas.reflect.TypeData;

/**
 * Contract for binding configuration.
 *
 * @param <T> Type for the BindingConfig.
 * @author danpintas
 */
public interface BindingConfig<T> {
    
    /**
     * Defines the qualifier annotation applying to the binding.
     *
     * @param annotationClass Qualifier annotation {@link Class}.
     * @return this, after setting.
     */
    BindingConfig<T> annotated(Class<? extends Annotation> annotationClass);
    
    /**
     * Defines the value of the {@link Named} annotation applying to the binding.
     *
     * @param name String value for a {@link Named} annotation.
     * @return this, after setting.
     */
    BindingConfig<T> named(String name);
    
    /**
     * Defines the scope annotation applying to the binding.
     *
     * @param scope Scope annotation {@link Class}.
     * @return this, after setting.
     */
    BindingConfig<T> scoped(Class<? extends Annotation> scope);
    
    /**
     * Defines the implementation {@link Class}.
     *
     * @param implementation {@link Class} for the dependency.
     * @return this, after setting.
     */
    default BindingConfig<T> to(Class<? extends T> implementation) {
        return to(TypeData.get(implementation));
    }
    
    /**
     * Defines the implementation {@link TypeData}.
     *
     * @param implementation {@link TypeData} for the dependency.
     * @return this, after setting.
     */
    BindingConfig<T> to(TypeData<? extends T> implementation);
    
    /**
     * Defines the implementation {@link Provider}.
     *
     * @param provider {@link Provider} for the dependency.
     * @return this, after setting.
     */
    BindingConfig<T> to(Provider<T> provider);
    
    /**
     * Defines the implementation instance.
     *
     * @param instance Instance for the dependency.
     * @return this, after setting.
     */
    BindingConfig<T> to(T instance);
    
}
