package es.danpintas.tdi.providers;

import javax.inject.Provider;

/**
 * Abstract provider for the injectors.
 *
 * @param <T> Type to instantiate.
 * @author danpintas
 */
public abstract class AbstractProvider<T> implements Provider<T> {
    
    private final Provider<T> provider;
    
    /**
     * Constructor setting the underlying provider.
     *
     * @param provider {@link InstanceProvider}.
     */
    public AbstractProvider(InstanceProvider<T> provider) {
        this.provider = provider;
    }
    
    /**
     * Instances a new object through the underlying {@link InstanceProvider}.
     *
     * @return New injected object.
     */
    protected T instance() {
        return provider.get();
    }
    
}
