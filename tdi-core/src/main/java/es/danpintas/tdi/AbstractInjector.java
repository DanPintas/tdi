package es.danpintas.tdi;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.inject.Provider;
import javax.inject.Singleton;

import es.danpintas.reflect.TypeData;
import es.danpintas.tdi.bindings.Binding;
import es.danpintas.tdi.bindings.BindingConfig;
import es.danpintas.tdi.injection.MemberInjector;
import es.danpintas.tdi.keys.BindingKey;
import es.danpintas.tdi.keys.ProviderKey;
import es.danpintas.tdi.keys.ScopeKey;
import es.danpintas.tdi.providers.InstanceProvider;
import es.danpintas.tdi.providers.PrototypeProvider;
import es.danpintas.tdi.providers.SingletonProvider;

/**
 * Abstract implementation for {@link Injector}.
 * 
 * @author danpintas
 */
public abstract class AbstractInjector implements Injector {

  private static final Function<InstanceProvider, Provider> PROTOTYPE = PrototypeProvider::new;
  private static final Function<InstanceProvider, Provider> SINGLETON = SingletonProvider::new;

  private final List<Binding<?>> builders = new LinkedList<>();
  private final Map<BindingKey<?>, Provider<?>> bindings = new HashMap<>();

  private final Map<TypeData<?>, InstanceProvider<?>> instanceProviders = new HashMap<>();
  private final List<MemberInjector> staticMembers = new LinkedList<>();
  private final List<Runnable> preDestroy = new LinkedList<>();

  private final Map<Class<? extends Annotation>, Function<InstanceProvider, Provider>> scopes =
      new HashMap<>();
  private final Map<ProviderKey<?>, Provider<?>> providers = new HashMap<>();

  /**
   * Constructor.
   * 
   * @param module First {@link Module}, at least one is required.
   * @param modules Additional Modules.
   */
  public AbstractInjector(Module module, Module... modules) {
    scopes.put(Singleton.class, SINGLETON);
    for (ScopeKey scope : getScopes())
      scopes.put(scope.getAnnotation(), scope.getProvision());
    Module base = i -> i.bind(Injector.class).to(this);
    base.install(this::bindingBuild);
    module.install(this::bindingBuild);
    for (Module m : modules)
      m.install(this::bindingBuild);
    build();
  }

  /**
   * Fetches the available scopes.
   * 
   * @return ScopeKey[]
   */
  protected abstract ScopeKey[] getScopes();

  private <T> BindingConfig<T> bindingBuild(TypeData<T> typeData) {
    Binding<T> builder = new Binding<>(typeData, this::getOrBuildProvider);
    builders.add(builder);
    return builder;
  }

  @SuppressWarnings("unchecked")
  private <T> Provider<T> getOrBuildProvider(TypeData<? extends T> implementation,
      Class<? extends Annotation> scope) {
    ProviderKey<?> key = new ProviderKey<>(implementation, scope);
    Provider<?> provider = providers.get(key);
    return (Provider<T>) (provider != null ? provider
        : buildAndRegisterProvider(implementation, scope, key));
  }

  private Provider<?> buildAndRegisterProvider(TypeData<?> implementation,
      Class<? extends Annotation> scope, ProviderKey<?> key) {
    Provider<?> provider =
        getProvision(implementation, scope).apply(getInstanceProvider(implementation));
    providers.put(key, provider);
    return provider;
  }

  private Function<InstanceProvider, Provider> getProvision(TypeData<?> implementation,
      Class<? extends Annotation> scope) {
    for (Entry<Class<? extends Annotation>, Function<InstanceProvider, Provider>> entry : scopes
        .entrySet())
      if (implementation.getRawType().isAnnotationPresent(entry.getKey())
          || entry.getKey().equals(scope))
        return entry.getValue();
    return PROTOTYPE;
  }

  @SuppressWarnings("unchecked")
  private <T> InstanceProvider<T> getInstanceProvider(TypeData<T> implementation) {
    InstanceProvider<?> provider = instanceProviders.get(implementation);
    return (InstanceProvider<T>) (provider != null ? provider
        : buildInstanceProvider(implementation));
  }

  private <T> InstanceProvider<T> buildInstanceProvider(TypeData<? extends T> implementation) {
    InstanceProvider<T> provider = new InstanceProvider<>(implementation, this::getProvider,
        this::addStaticMembers, this::addPreDestroy);
    instanceProviders.put(implementation, provider);
    return provider;
  }

  /**
   * Initializes the providers of the declared dependencies. <br/>
   * Should be called only once.
   */
  private void build() {

    for (Binding<?> builder : builders)
      bindings.put(builder.key(), builder.provider());
    builders.clear();

    for (Entry<TypeData<?>, InstanceProvider<?>> entry : instanceProviders.entrySet())
      entry.getValue().initProviders();

    staticMembers.sort(this::compareMembers);
    for (MemberInjector staticMember : staticMembers)
      staticMember.inject(null);

  }

  private int compareMembers(MemberInjector a, MemberInjector b) {
    int val;
    Class<?> aClass = a.getDeclaringClass();
    Class<?> bClass = a.getDeclaringClass();
    boolean distinctClass = !aClass.equals(bClass);

    if (distinctClass && a.getDeclaringClass().isAssignableFrom(b.getDeclaringClass()))
      val = 1;
    else if (distinctClass && b.getDeclaringClass().isAssignableFrom(a.getDeclaringClass()))
      val = -1;
    else if (a.isField() && !b.isField())
      val = 1;
    else if (!b.isField() && a.isField())
      val = -1;
    else
      val = 0;
    return val;
  }

  private void addStaticMembers(MemberInjector[] array) {
    for (MemberInjector member : array)
      if (!staticMembers.contains(member)) {
        member.providerCheck();
        staticMembers.add(member);
      }
  }

  private void addPreDestroy(Runnable action) {
    preDestroy.add(0, action);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Provider<T> getProvider(BindingKey<T> key) {
    return (Provider<T>) bindings.get(key);
  }

  @Override
  public void destroy() {
    for (Runnable r : preDestroy)
      r.run();
  }

}
