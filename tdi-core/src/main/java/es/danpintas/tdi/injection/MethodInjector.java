package es.danpintas.tdi.injection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import javax.inject.Provider;

import es.danpintas.reflect.TypeData;
import es.danpintas.tdi.exceptions.InjectException;
import es.danpintas.tdi.keys.BindingKey;

/**
 * {@link Method} injector.
 * 
 * @author danpintas
 */
public final class MethodInjector extends ExecutableInjector implements MemberInjector {

  private final Method method;

  /**
   * Constructor.
   * 
   * @param typeData {@link TypeData} for the constructor class.
   * @param fun {@link Function} retrieving the {@link Provider} for a {@link BindingKey}.
   * @param method {@link Method} to inject.
   */
  public MethodInjector(TypeData<?> typeData, Function<BindingKey<?>, Provider<?>> fun,
      Method method) {
    super(typeData, fun, method);
    this.method = method;
  }

  @Override
  public void inject(Object instance) {
    try {
      method.invoke(instance, injectArgs());
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new InjectException(e);
    }
  }

  @Override
  public Class<?> getDeclaringClass() {
    return method.getDeclaringClass();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((method == null) ? 0 : method.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MethodInjector other = (MethodInjector) obj;
    if (method == null) {
      if (other.method != null)
        return false;
    } else if (!method.equals(other.method))
      return false;
    return true;
  }

  @Override
  public boolean isField() {
    return false;
  }

}
