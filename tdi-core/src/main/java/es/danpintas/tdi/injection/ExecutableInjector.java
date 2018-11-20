package es.danpintas.tdi.injection;

import static es.danpintas.tdi.utils.TypeUtils.getQualifierAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Function;

import javax.inject.Provider;

import es.danpintas.reflect.TypeData;
import es.danpintas.tdi.keys.BindingKey;

/**
 * Abstract implementation for {@link Executable} ({@link Constructor} and {@link Method})
 * injection.
 * 
 * @author danpintas
 */
public abstract class ExecutableInjector {

  private final TypeData<?> typeData;
  private final Function<BindingKey<?>, Provider<?>> fun;
  private final Executable executable;
  private Provider<?>[] providers;

  /**
   * Constructor.
   * 
   * @param typeData {@link TypeData} for the constructor class.
   * @param fun {@link Function} retrieving the {@link Provider} for a {@link BindingKey}.
   * @param executable {@link Executable} to inject.
   */
  public ExecutableInjector(TypeData<?> typeData, Function<BindingKey<?>, Provider<?>> fun,
      Executable executable) {
    this.typeData = typeData;
    this.fun = fun;
    this.executable = executable;
    if (!Modifier.isPublic(executable.getDeclaringClass().getModifiers())
        || !Modifier.isPublic(executable.getModifiers()))
      executable.setAccessible(true);
  }

  /**
   * Initializes the {@link Member} injector's {@link Provider}s.
   */
  public void providerCheck() {
    if (providers == null) {
      Parameter[] parameters = executable.getParameters();
      List<TypeData> parameterTypes = typeData.getParameterTypes(executable);
      providers = new Provider<?>[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        Parameter parameter = parameters[i];
        TypeData<?> parameterType = parameterTypes.get(i);
        Annotation qualifier = getQualifierAnnotation(parameter);
        BindingKey<?> key;
        if (Provider.class.equals(parameter.getType())) {
          ParameterizedType type = (ParameterizedType) parameterType.getType();
          TypeData<?> providerType = TypeData.get(type.getActualTypeArguments()[0]);
          key = new BindingKey<>(providerType, qualifier);
          providers[i] = () -> fun.apply(key);
        } else {
          key = new BindingKey<>(parameterType, qualifier);
          providers[i] = fun.apply(key);
        }
      }
    }
  }

  protected Object[] injectArgs() {
    Object[] args = new Object[providers.length];
    for (int i = 0; i < providers.length; i++)
      if (providers[i] != null)
        args[i] = providers[i].get();
    return args;
  }

}
