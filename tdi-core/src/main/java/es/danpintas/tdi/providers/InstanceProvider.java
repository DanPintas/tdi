package es.danpintas.tdi.providers;


import static es.danpintas.reflect.Types.getMethods;
import static es.danpintas.reflect.Types.getTypeHierarchy;
import static es.danpintas.reflect.Types.isOverride;
import static es.danpintas.tdi.utils.TypeUtils.getInjectConstructor;
import static es.danpintas.tdi.utils.TypeUtils.getInjectFields;
import static es.danpintas.tdi.utils.TypeUtils.getPostConstructMethod;
import static es.danpintas.tdi.utils.TypeUtils.getPreDestroyMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;

import es.danpintas.reflect.TypeData;
import es.danpintas.tdi.exceptions.InjectException;
import es.danpintas.tdi.injection.ConstructorInjector;
import es.danpintas.tdi.injection.FieldInjector;
import es.danpintas.tdi.injection.MemberInjector;
import es.danpintas.tdi.injection.MethodInjector;
import es.danpintas.tdi.keys.BindingKey;

/**
 * Underlying {@link Provider}, always initializing an instance.
 * 
 * @author danpintas
 *
 * @param <T> Type to instantiate.
 */
public final class InstanceProvider<T> implements Provider<T> {

  private final ConstructorInjector<T> constructorInjector;
  private final MemberInjector[] staticMemberInjectors;
  private final MemberInjector[] memberInjectors;
  private final Method postConstruct;
  private final Method preDestroy;
  private final Consumer<Runnable> destroyer;

  private final Consumer<MemberInjector[]> staticInjector;

  /**
   * Constructor.
   * 
   * @param typeData {@link TypeData} for the instantiated {@link Class}.
   * @param fun {@link Function} defining how to get a {@link Provider} from a {@link BindingKey}.
   * @param staticInjector {@link Consumer} adding the static members to the injector context.
   * @param destroyer {@link Consumer} registering a {@link PreDestroy} handle.
   */
  public InstanceProvider(TypeData<? extends T> typeData, Function<BindingKey<?>, Provider<?>> fun,
      Consumer<MemberInjector[]> staticInjector, Consumer<Runnable> destroyer) {

    @SuppressWarnings("unchecked")
    Class<? extends T> rawType = (Class<? extends T>) typeData.getRawType();

    this.constructorInjector = initConstructor(rawType, typeData, fun);
    this.staticInjector = staticInjector;
    this.destroyer = destroyer;

    List<Class<?>> typeHierarchy = getTypeHierarchy(rawType);
    int hierarchySize = typeHierarchy.size();
    List<List<Field>> tempFields = new ArrayList<>(typeHierarchy.size());
    List<List<Method>> tempMethods = new ArrayList<>(typeHierarchy.size());
    for (Iterator<Class<?>> it = typeHierarchy.iterator(); it.hasNext();)
      parseType(it.next(), tempFields, tempMethods);
    removeOverridden(tempMethods);
    removeNotInject(tempMethods);
    List<MemberInjector> staticInjectorsList = new LinkedList<>();
    List<MemberInjector> injectorsList = new LinkedList<>();
    for (int i = 0; i < hierarchySize; i++) {
      addFields(typeData, fun, tempFields.get(i), staticInjectorsList, injectorsList);
      addInjectors(typeData, fun, tempMethods.get(i), staticInjectorsList, injectorsList);
    }

    staticMemberInjectors =
        staticInjectorsList.toArray(new MemberInjector[staticInjectorsList.size()]);
    memberInjectors = injectorsList.toArray(new MemberInjector[injectorsList.size()]);

    postConstruct = getPostConstructMethod(rawType);
    if (postConstruct != null
        && (!Modifier.isPublic(postConstruct.getDeclaringClass().getModifiers())
            || !Modifier.isPublic(postConstruct.getModifiers())))
      postConstruct.setAccessible(true);
    preDestroy = getPreDestroyMethod(rawType);
    if (preDestroy != null && (!Modifier.isPublic(preDestroy.getDeclaringClass().getModifiers())
        || !Modifier.isPublic(preDestroy.getModifiers())))
      preDestroy.setAccessible(true);
  }

  private void parseType(Class<?> subType, List<List<Field>> tempFields,
      List<List<Method>> tempMethods) {
    tempFields.add(getInjectFields(subType));
    tempMethods.add(getMethods(subType));
  }

  private ConstructorInjector<T> initConstructor(Class<? extends T> type,
      TypeData<? extends T> typeData, Function<BindingKey<?>, Provider<?>> fun) {
    try {
      Constructor<? extends T> c = getInjectConstructor(type);
      if (c == null)
        c = type.getDeclaredConstructor();
      return new ConstructorInjector<>(typeData, fun, c);
    } catch (NoSuchMethodException t) {
      throw new InjectException(t);
    }
  }

  private void removeOverridden(List<List<Method>> tempMethods) {
    List<Method> included = new LinkedList<>();
    for (int i = tempMethods.size() - 1; i >= 0; i--)
      removeOverridenSub(included, tempMethods.get(i));
  }

  private void removeOverridenSub(List<Method> included, List<Method> subList) {
    for (Iterator<Method> iterator = subList.iterator(); iterator.hasNext();) {
      Method method = iterator.next();
      boolean overridden = false;
      for (Method sub : included) {
        if (isOverride(method, sub)) {
          overridden = true;
          break;
        }
      }
      if (overridden)
        iterator.remove();
      else
        included.add(method);
    }
  }

  private void removeNotInject(List<List<Method>> tempMethods) {
    for (int i = 0; i < tempMethods.size(); i++) {
      List<Method> subList = tempMethods.get(i);
      for (Iterator<Method> iterator = subList.iterator(); iterator.hasNext();) {
        Method method = iterator.next();
        if (method.getAnnotation(Inject.class) == null)
          iterator.remove();
      }
    }
  }

  private void addFields(TypeData<? extends T> typeData, Function<BindingKey<?>, Provider<?>> fun,
      List<Field> subTemp, List<MemberInjector> staticInjectorsList,
      List<MemberInjector> injectorsList) {
    for (Iterator<Field> subIt = subTemp.iterator(); subIt.hasNext();) {
      Field field = subIt.next();
      FieldInjector fInjector = new FieldInjector(typeData, fun, field);
      if (Modifier.isStatic(field.getModifiers()))
        staticInjectorsList.add(fInjector);
      else
        injectorsList.add(fInjector);
    }
  }

  private void addInjectors(TypeData<? extends T> typeData,
      Function<BindingKey<?>, Provider<?>> fun, List<Method> subTemp,
      List<MemberInjector> staticInjectorsList, List<MemberInjector> injectorsList) {
    for (Iterator<Method> subIt = subTemp.iterator(); subIt.hasNext();) {
      Method method = subIt.next();
      MethodInjector mInjector = new MethodInjector(typeData, fun, method);
      if (Modifier.isStatic(method.getModifiers()))
        staticInjectorsList.add(mInjector);
      else
        injectorsList.add(mInjector);
    }
  }

  @Override
  public T get() {
    T instance = constructorInjector.inject();
    if (instance != null) {
      inject(instance);
      if (postConstruct != null)
        postConstruct(instance);
      if (preDestroy != null) {
        destroyer.accept(() -> preDestroy(instance));
      }
    }
    return instance;
  }

  private void inject(T instance) {
    for (MemberInjector memberInjector : memberInjectors)
      memberInjector.inject(instance);
  }

  private void postConstruct(T instance) {
    try {
      postConstruct.invoke(instance);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new InjectException(e);
    }
  }

  private void preDestroy(T instance) {
    try {
      preDestroy.invoke(instance);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new InjectException(e);
    }
  }

  /**
   * Initializes the dependency injection context.
   */
  public void initProviders() {
    constructorInjector.providerCheck();
    for (MemberInjector memberInjector : memberInjectors)
      memberInjector.providerCheck();
    staticInjector.accept(staticMemberInjectors);
  }

}
