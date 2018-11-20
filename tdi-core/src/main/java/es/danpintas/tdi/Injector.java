package es.danpintas.tdi;

import javax.annotation.PreDestroy;
import javax.inject.Provider;

import es.danpintas.reflect.TypeData;
import es.danpintas.tdi.keys.BindingKey;

/**
 * Contract for retrieving dependencies.
 * 
 * @author danpintas
 */
public interface Injector {

  /**
   * Gets the provider associated to a given {@code Class}.
   * 
   * @param type {@link Class} to look provider for.
   * @return {@link Provider} for the given {@link Class}, if it exists.
   */
  default <T> Provider<T> getProvider(Class<T> type) {
    return getProvider(new BindingKey<>(TypeData.get(type), null));
  }

  /**
   * Gets the provider associated to a given {@code TypeData}.
   * 
   * @param typeData {@link TypeData} to look provider for.
   * @return {@link Provider} for the given {@link TypeData}, if it exists.
   */
  default <T> Provider<T> getProvider(TypeData<T> typeData) {
    return getProvider(new BindingKey<>(typeData, null));
  }

  /**
   * Gets the provider associated to a given {@code BindingKey}.
   * 
   * @param key {@link BindingKey} to look provider for.
   * @return {@link Provider} for the given {@link BindingKey}, if it exists.
   */
  <T> Provider<T> getProvider(BindingKey<T> key);

  /**
   * Gets an instance of a given {@code Class}.
   * 
   * @param type {@link Class} to get instance for.
   * @return Instance for the given {@link Class}.
   */
  default <T> T getInstance(Class<T> type) {
    return getProvider(new BindingKey<>(TypeData.get(type), null)).get();
  }

  /**
   * Gets an instance of a given {@code TypeData}.
   * 
   * @param typeData {@link TypeData} to get instance for.
   * @return Instance for the given {@link TypeData}.
   */
  default <T> T getInstance(TypeData<T> typeData) {
    return getProvider(new BindingKey<>(typeData, null)).get();
  }

  /**
   * Gets an instance of a given {@code BindingKey}.
   * 
   * @param key {@link BindingKey} to get instance for.
   * @return Instance for the given {@link BindingKey}.
   */
  default <T> T getInstance(BindingKey<T> key) {
    return getProvider(key).get();
  }

  /**
   * Destroys any declared dependencies, calling {@link PreDestroy} methods.
   */
  void destroy();

}
