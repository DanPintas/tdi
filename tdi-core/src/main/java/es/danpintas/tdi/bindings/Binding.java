package es.danpintas.tdi.bindings;

import java.lang.annotation.Annotation;
import java.util.function.BiFunction;

import javax.inject.Provider;

import es.danpintas.reflect.TypeData;
import es.danpintas.tdi.keys.BindingKey;
import es.danpintas.tdi.utils.Qualifiers;

/**
 * Binding data holder.
 *
 * @param <T> Binding type.
 * @author danpintas
 */
public final class Binding<T> implements BindingConfig<T> {
    
    private static final class ProviderImpl<T> implements Provider<T> {
        
        private final T instance;
        
        private ProviderImpl(T instance) {
            this.instance = instance;
        }
        
        @Override
        public T get() {
            return instance;
        }
        
    }
    
    private final TypeData<T> typeData;
    private final BiFunction<TypeData<? extends T>, Class<? extends Annotation>, Provider<T>> providerBuilder;
    private Annotation qualifier;
    private TypeData<? extends T> implementation;
    private Class<? extends Annotation> scope;
    private Provider<T> provider;
    
    /**
     * Constructor.
     *
     * @param typeData        Binding {@link TypeData}.
     * @param providerBuilder {@link BiFunction} defining how to generate a {@link Provider} from
     *                        implementation {@link TypeData} and an (optional) annotation {@link Class}.
     */
    public Binding(TypeData<T> typeData,
                   BiFunction<TypeData<? extends T>, Class<? extends Annotation>, Provider<T>> providerBuilder) {
        this.typeData = typeData;
        this.implementation = typeData;
        this.providerBuilder = providerBuilder;
    }
    
    @Override
    public Binding<T> annotated(Class<? extends Annotation> annotationClass) {
        return annotatedWith(Qualifiers.from(annotationClass));
    }
    
    @Override
    public Binding<T> named(String name) {
        return annotatedWith(Qualifiers.named(name));
    }
    
    private Binding<T> annotatedWith(Annotation annotation) {
        this.qualifier = annotation;
        return this;
    }
    
    @Override
    public Binding<T> scoped(Class<? extends Annotation> scope) {
        this.scope = scope;
        this.provider = null;
        return this;
    }
    
    @Override
    public Binding<T> to(TypeData<? extends T> implementation) {
        this.implementation = implementation;
        this.provider = null;
        return this;
    }
    
    @Override
    public Binding<T> to(Provider<T> provider) {
        this.provider = provider;
        this.scope = null;
        this.implementation = null;
        return this;
    }
    
    @Override
    public Binding<T> to(T instance) {
        return to(new ProviderImpl<>(instance));
    }
    
    /**
     * Returns the binding {@code BindingKey}.
     *
     * @return Binding {@link BindingKey}.
     */
    public BindingKey<T> key() {
        return new BindingKey<>(typeData, qualifier);
    }
    
    /**
     * Returns the binding {@code Provider}.
     *
     * @return Binding {@link Provider}.
     */
    public Provider<T> provider() {
        return provider != null ? provider : providerBuilder.apply(implementation, scope);
    }
    
}
