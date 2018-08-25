package es.danpintas.tdi.providers;

/**
 * Default provider, always returning a new instance.
 * 
 * @author danpintas
 *
 * @param <T> Type to instantiate.
 */
public class PrototypeProvider<T> extends AbstractProvider<T> {

  /**
   * Constructor
   * 
   * @param provider Underlying {@link InstanceProvider}.
   */
  public PrototypeProvider(InstanceProvider<T> provider) {
    super(provider);
  }

  @Override
  public T get() {
    return instance();
  }

}
