package es.danpintas.tdi;

/**
 * Contract to define dependency configuration in a separate class.
 * 
 * @author danpintas
 */
@FunctionalInterface
public interface Module {

  /**
   * Declares dependency configuration for a given {@code Binder}.
   * 
   * @param binder {@link Binder} to configure dependencies for.
   */
  void install(Binder binder);

}
