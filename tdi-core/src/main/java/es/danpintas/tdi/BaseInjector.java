package es.danpintas.tdi;

import javax.inject.Singleton;

import es.danpintas.tdi.keys.ScopeKey;

/**
 * Simple {@link Injector} implementation, with the following scopes:
 * <ul>
 * <li>Prototype (default)</li>
 * <li>{@link Singleton}</li>
 * </ul>
 * 
 * @author danpintas
 */
public class BaseInjector extends AbstractInjector {

  /**
   * Constructor.
   * 
   * @param module First {@link Module}, at least one is required.
   * @param modules Additional Modules.
   */
  public BaseInjector(Module module, Module... modules) {
    super(module, modules);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> destroy()));
  }

  @Override
  protected ScopeKey[] getScopes() {
    return new ScopeKey[0];
  }

}
