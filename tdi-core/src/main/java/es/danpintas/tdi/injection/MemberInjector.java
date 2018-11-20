package es.danpintas.tdi.injection;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Contract for {@link Member} ({@link Field} and {@link Method}) injection.
 * 
 * @author danpintas
 */
public interface MemberInjector {

  /**
   * Injects the {@link Member} for a given instance.
   * 
   * @param instance Object getting the {@link Member} injected.
   */
  void inject(Object instance);

  /**
   * Initializes the {@link Member} injector's {@link javax.inject.Provider}s.
   */
  void providerCheck();

  /**
   * Gets the {@link Member} declaring {@link Class}.
   * 
   * @return {@link Member} declaring {@link Class}.
   */
  Class<?> getDeclaringClass();

  /**
   * Shows if the injector belongs to a {@link Field}.
   * 
   * @return true if {@link Field} injector, false if {@link Member} injector.
   */
  boolean isField();

}
