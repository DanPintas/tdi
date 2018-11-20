package es.danpintas.tdi.providers;

import javax.inject.Singleton;

/**
 * {@link Singleton} scoped provider, always returning the same instance.
 *
 * @param <T> Type to instantiate.
 * @author danpintas
 */
public class SingletonProvider<T> extends AbstractProvider<T> {
    
    private T instance;
    
    /**
     * Constructor
     *
     * @param provider Underlying {@link InstanceProvider}
     */
    public SingletonProvider(InstanceProvider<T> provider) {
        super(provider);
    }
    
    @Override
    public synchronized T get() {
        if (instance == null)
            instance = instance();
        return instance;
    }
    
}
