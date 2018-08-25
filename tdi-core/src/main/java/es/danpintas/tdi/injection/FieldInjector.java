package es.danpintas.tdi.injection;

import static es.danpintas.tdi.utils.TypeUtils.getQualifierAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

import javax.inject.Provider;

import es.danpintas.reflect.TypeData;
import es.danpintas.tdi.exceptions.InjectException;
import es.danpintas.tdi.keys.BindingKey;

/**
 * {@link Field} injector.
 * 
 * @author danpintas
 */
public final class FieldInjector implements MemberInjector {

  private final TypeData<?> typeData;
  private final Function<BindingKey<?>, Provider<?>> fun;
  private final Field field;
  private Provider<?> provider;

  /**
   * Constructor.
   * 
   * @param typeData {@link TypeData} for the constructor class.
   * @param fun {@link Function} retrieving the {@link Provider} for a {@link BindingKey}.
   * @param field {@link Field} to inject.
   */
  public FieldInjector(TypeData<?> typeData, Function<BindingKey<?>, Provider<?>> fun,
      Field field) {
    this.typeData = typeData;
    this.fun = fun;
    if (!Modifier.isPublic(field.getModifiers())
        || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
      field.setAccessible(true);
    }
    this.field = field;
  }

  @Override
  public void inject(Object instance) {
    try {
      if (provider != null)
        field.set(instance, provider.get());
    } catch (IllegalAccessException e) {
      throw new InjectException(e);
    }
  }

  @Override
  public void providerCheck() {
    if (provider == null) {
      TypeData<?> fieldType = typeData.getFieldType(field);
      Annotation qualifier = getQualifierAnnotation(field);
      BindingKey<?> key;
      if (Provider.class.equals(field.getType())) {
        ParameterizedType type = (ParameterizedType) fieldType.getType();
        TypeData<?> providerType = TypeData.get(type.getActualTypeArguments()[0]);
        key = new BindingKey<>(providerType, qualifier);
        provider = () -> fun.apply(key);
      } else {
        key = new BindingKey<>(fieldType, qualifier);
        provider = fun.apply(key);
      }
    }
  }

  @Override
  public Class<?> getDeclaringClass() {
    return field.getDeclaringClass();
  }

  @Override
  public boolean isField() {
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((field == null) ? 0 : field.hashCode());
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
    FieldInjector other = (FieldInjector) obj;
    if (field == null) {
      if (other.field != null)
        return false;
    } else if (!field.equals(other.field))
      return false;
    return true;
  }

}
