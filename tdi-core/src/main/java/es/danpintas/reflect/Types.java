package es.danpintas.reflect;

import static es.danpintas.reflect.Validations.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import javax.inject.Provider;

/**
 * {@link Type} related utils.
 * 
 * @author danpintas
 */
public final class Types {

  @FunctionalInterface
  private interface CompositeType {

    boolean isFullySpecified();

  }

  private static final class ParameterizedTypeImpl
      implements ParameterizedType, Serializable, CompositeType {

    private static final long serialVersionUID = 0;

    private final transient Type ownerType;
    private final transient Type rawType;
    private final transient Type[] typeArguments;

    private ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
      ensureOwnerType(ownerType, rawType);
      this.ownerType = ownerType == null ? null : canonicalize(ownerType);
      this.rawType = canonicalize(rawType);
      this.typeArguments = typeArguments.clone();
      for (int t = 0; t < this.typeArguments.length; t++) {
        checkNotPrimitive(this.typeArguments[t], "type parameters");
        this.typeArguments[t] = canonicalize(this.typeArguments[t]);
      }
    }

    @Override
    public Type[] getActualTypeArguments() {
      return typeArguments.clone();
    }

    @Override
    public Type getRawType() {
      return rawType;
    }

    @Override
    public Type getOwnerType() {
      return ownerType;
    }

    @Override
    public boolean isFullySpecified() {
      if (ownerType != null && !Types.isFullySpecified(ownerType)) {
        return false;
      }

      if (!Types.isFullySpecified(rawType)) {
        return false;
      }

      for (Type type : typeArguments) {
        if (!Types.isFullySpecified(type)) {
          return false;
        }
      }

      return true;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof ParameterizedType
          && Types.typeEquals(this, (ParameterizedType) other);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(typeArguments) ^ rawType.hashCode() ^ hashCodeOrZero(ownerType);
    }

    private static int hashCodeOrZero(Object o) {
      return o != null ? o.hashCode() : 0;
    }

    @Override
    public String toString() {
      StringBuilder stringBuilder = new StringBuilder(30 * (typeArguments.length + 1));
      stringBuilder.append(typeToString(rawType));
      if (typeArguments.length == 0) {
        return stringBuilder.toString();
      }
      stringBuilder.append("<").append(typeToString(typeArguments[0]));
      for (int i = 1; i < typeArguments.length; i++) {
        stringBuilder.append(", ").append(typeToString(typeArguments[i]));
      }
      return stringBuilder.append(">").toString();
    }

    private static void ensureOwnerType(Type ownerType, Type rawType) {
      if (rawType instanceof Class<?>) {
        Class<?> rawTypeAsClass = (Class<?>) rawType;
        checkArgument(ownerType != null || rawTypeAsClass.getEnclosingClass() == null,
            "No owner type for enclosed %s", rawType);
        checkArgument(ownerType == null || rawTypeAsClass.getEnclosingClass() != null,
            "Owner type for unenclosed %s", rawType);
      }
    }

  }

  private static final class GenericArrayTypeImpl
      implements GenericArrayType, Serializable, CompositeType {

    private static final long serialVersionUID = 0;

    private final transient Type componentType;

    private GenericArrayTypeImpl(Type componentType) {
      this.componentType = canonicalize(componentType);
    }

    @Override
    public Type getGenericComponentType() {
      return componentType;
    }

    @Override
    public boolean isFullySpecified() {
      return Types.isFullySpecified(componentType);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof GenericArrayType && Types.typeEquals(this, (GenericArrayType) o);
    }

    @Override
    public int hashCode() {
      return componentType.hashCode();
    }

    @Override
    public String toString() {
      return typeToString(componentType) + "[]";
    }

  }

  private static final class WildcardTypeImpl implements WildcardType, Serializable, CompositeType {

    private static final long serialVersionUID = 0;

    private final transient Type upperBound;
    private final transient Type lowerBound;

    private WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
      checkArgument(lowerBounds.length <= 1, "Must have at most one lower bound.");
      checkArgument(upperBounds.length == 1, "Must have exactly one upper bound.");

      if (lowerBounds.length == 1) {
        requireNonNull(lowerBounds[0], "lowerBound");
        checkNotPrimitive(lowerBounds[0], "wildcard bounds");
        checkArgument(upperBounds[0] == Object.class, "bounded both ways");
        this.lowerBound = canonicalize(lowerBounds[0]);
        this.upperBound = Object.class;

      } else {
        requireNonNull(upperBounds[0], "upperBound");
        checkNotPrimitive(upperBounds[0], "wildcard bounds");
        this.lowerBound = null;
        this.upperBound = canonicalize(upperBounds[0]);
      }
    }

    @Override
    public Type[] getUpperBounds() {
      return new Type[] {upperBound};
    }

    @Override
    public Type[] getLowerBounds() {
      return lowerBound != null ? new Type[] {lowerBound} : new Type[] {};
    }

    @Override
    public boolean isFullySpecified() {
      return Types.isFullySpecified(upperBound)
          && (lowerBound == null || Types.isFullySpecified(lowerBound));
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof WildcardType && Types.typeEquals(this, (WildcardType) other);
    }

    @Override
    public int hashCode() {
      return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
    }

    @Override
    public String toString() {
      String toString;
      if (lowerBound != null) {
        toString = "? super " + typeToString(lowerBound);
      } else if (upperBound == Object.class) {
        toString = "?";
      } else {
        toString = "? extends " + typeToString(upperBound);
      }
      return toString;
    }

  }

  private Types() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a new {@code ParameterizedType} with the given type arguments.
   * 
   * @param rawType Main {@link Type} .
   * @param typeArguments {@link Type} arguments for {@code rawType}.
   * @return
   */
  public static ParameterizedType parameterizedType(Type rawType, Type... typeArguments) {
    return parameterizedTypeWithOwner(null, rawType, typeArguments);
  }

  /**
   * Creates a new {@code ParameterizedType} with the given type arguments.
   * 
   * @param rawType Owner {@link Type}.
   * @param ownerType Main {@link Type}.
   * @param typeArguments {@link Type} arguments for {@code rawType}.
   * @return
   */
  public static ParameterizedType parameterizedTypeWithOwner(Type ownerType, Type rawType,
      Type... typeArguments) {
    return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
  }

  /**
   * Creates a new {@code GenericArrayType} with the given type argument.
   * 
   * @param ownerType Argument {@link Type}.
   * @return {@link GenericArrayType} of {@code rawType}.
   */
  public static GenericArrayType arrayOf(Type componentType) {
    return new GenericArrayTypeImpl(componentType);
  }

  /**
   * Creates a new {@code WildcardType} with the given type argument.
   * 
   * @param bound Argument {@link Type}.
   * @return {@link WildcardType} of <{@code ?} extends {@code bound}>.
   */
  public static WildcardType subtypeOf(Type bound) {
    return new WildcardTypeImpl(new Type[] {bound}, new Type[] {});
  }

  /**
   * Creates a new {@code WildcardType} with the given type argument.
   * 
   * @param ownerType Argument {@link Type}.
   * @return {@link WildcardType} of <{@code ?} super {@code bound}>.
   */
  public static WildcardType supertypeOf(Type bound) {
    return new WildcardTypeImpl(new Type[] {Object.class}, new Type[] {bound});
  }

  /**
   * Creates a new {@code List} {@code ParameterizedType} with the given type argument.
   * 
   * @param elementType Argument {@link Type}.
   * @return {@link ParameterizedType} of {@link List}<{@code elementType}>.
   */
  public static ParameterizedType listOf(Type elementType) {
    return parameterizedType(List.class, elementType);
  }

  /**
   * Creates a new {@code Collection} {@code ParameterizedType} with the given type argument.
   * 
   * @param elementType Argument {@link Type}.
   * @return {@link ParameterizedType} of {@link Collection}<{@code elementType}>.
   */
  public static ParameterizedType collectionOf(Type elementType) {
    return parameterizedType(Collection.class, elementType);
  }

  /**
   * Creates a new {@code Set} {@code ParameterizedType} with the given type argument.
   * 
   * @param elementType Argument {@link Type}.
   * @return {@link ParameterizedType} of {@link Set}<{@code elementType}>.
   */
  public static ParameterizedType setOf(Type elementType) {
    return parameterizedType(Set.class, elementType);
  }

  /**
   * Creates a new {@code Map} {@code ParameterizedType} with the given type arguments.
   * 
   * @param keyType Argument {@link Type}.
   * @param valueType Argument {@link Type}.
   * @return {@link ParameterizedType} of {@link Map}<{@code keyType},{@code valueType}>.
   */
  public static ParameterizedType mapOf(Type keyType, Type valueType) {
    return parameterizedType(Map.class, keyType, valueType);
  }

  /**
   * Creates a new {@code Provider} {@code ParameterizedType} with the given type argument.
   * 
   * @param providedType Argument {@link Type}.
   * @return {@link ParameterizedType} of {@link Provider}<{@code providedType}>.
   */
  public static ParameterizedType providerOf(Type providedType) {
    return parameterizedType(Provider.class, providedType);
  }


  private static boolean isFullySpecified(Type type) {
    boolean ret;
    if (type instanceof Class) {
      ret = true;
    } else if (type instanceof CompositeType) {
      ret = ((CompositeType) type).isFullySpecified();
    } else if (type instanceof TypeVariable) {
      ret = false;
    } else {
      ret = ((CompositeType) canonicalize(type)).isFullySpecified();
    }
    return ret;
  }

  /**
   * Returns a functionally equal type, but not necessarily equal according to
   * {@link Object#equals(Object) equals()}.
   * 
   * @param type {@code Type} to canonicalize.
   * @return canonicalized {@code Type}.
   */
  public static Type canonicalize(Type type) {
    Type ret;
    if (type == null) {
      ret = null;
    } else if (type instanceof Class) {
      Class<?> c = (Class<?>) type;
      ret = c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;
    } else if (type instanceof CompositeType) {
      ret = type;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType p = (ParameterizedType) type;
      ret = new ParameterizedTypeImpl(p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());
    } else if (type instanceof GenericArrayType) {
      GenericArrayType g = (GenericArrayType) type;
      ret = new GenericArrayTypeImpl(g.getGenericComponentType());
    } else if (type instanceof WildcardType) {
      WildcardType w = (WildcardType) type;
      ret = new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
    } else {
      ret = type;
    }
    return ret;
  }

  /**
   * Gets the {@code Class} associated to a {@code Type}
   * 
   * @param type {@link Type} to convert.
   * @return {@link Class} associated to {@code yype}.
   */
  public static Class<?> getRawType(Type type) {
    Class<?> ret;
    if (type == null) {
      ret = null;
    } else if (type instanceof Class<?>) {
      ret = (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      ret = (Class<?>) parameterizedType.getRawType();

    } else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      ret = Array.newInstance(getRawType(componentType), 0).getClass();
    } else if (type instanceof TypeVariable) {
      ret = Object.class;
    } else {
      throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
          + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }
    return ret;
  }

  /**
   * Checks if two {@code Types} are functionally equal.
   * 
   * @param a First {@link Type}.
   * @param b Second {@link Type}.
   * @return true if a and b are functionally equal, false otherwise.
   */
  public static boolean typeEquals(Type a, Type b) {
    return a == b || equalsAsType(a, b) || equalsAsGeneric(a, b);
  }

  private static boolean equalsAsType(Type a, Type b) {
    return a instanceof Class && a.equals(b);
  }

  private static boolean equalsAsGeneric(Type a, Type b) {
    return equalsAsParameterizedType(a, b) || equalsAsGenericArrayType(a, b)
        || equalsAsWildcardType(a, b) || equalsAsTypeVariable(a, b);
  }

  private static boolean equalsAsParameterizedType(Type a, Type b) {
    return a instanceof ParameterizedType && b instanceof ParameterizedType
        && parameterizedTypeEquals((ParameterizedType) a, (ParameterizedType) b);
  }

  private static boolean parameterizedTypeEquals(ParameterizedType pa, ParameterizedType pb) {
    boolean ret;
    ret = Objects.equals(pa.getOwnerType(), pb.getOwnerType())
        && pa.getRawType().equals(pb.getRawType())
        && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());
    return ret;
  }

  private static boolean equalsAsGenericArrayType(Type a, Type b) {
    return a instanceof GenericArrayType && b instanceof GenericArrayType
        && genericArrayTypeEquals((GenericArrayType) a, (GenericArrayType) b);
  }

  private static boolean genericArrayTypeEquals(GenericArrayType ga, GenericArrayType gb) {
    boolean ret;
    ret = typeEquals(ga.getGenericComponentType(), gb.getGenericComponentType());
    return ret;
  }

  private static boolean equalsAsWildcardType(Type a, Type b) {
    return a instanceof WildcardType && b instanceof WildcardType
        && wildcardTypeEquals((WildcardType) a, (WildcardType) b);
  }

  private static boolean wildcardTypeEquals(WildcardType wa, WildcardType wb) {
    return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
        && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());
  }

  private static boolean equalsAsTypeVariable(Type a, Type b) {
    return a instanceof TypeVariable && b instanceof TypeVariable
        && typeVariableEquals((TypeVariable<?>) a, (TypeVariable<?>) b);
  }

  private static boolean typeVariableEquals(TypeVariable<?> va, TypeVariable<?> vb) {
    return va.getGenericDeclaration().equals(vb.getGenericDeclaration())
        && va.getName().equals(vb.getName());
  }

  /**
   * Returns a {@code String} representation of a give {@code Type}.
   * 
   * @param type {@link Type} to stringify.
   * @return {@link String} representing {@code type}.
   */
  public static String typeToString(Type type) {
    return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
  }

  /**
   * Gets the generic supertype (interface or class) for a given {@code Type}.<br/>
   * For example, given a class {@code IntegerSet}, the result for when supertype is
   * {@code Set.class} is {@code Set<Integer>} and the result when the supertype is
   * {@code Collection.class} is {@code Collection<Integer>}.
   * 
   * @param type {@link Type} to get supertype for.
   * @param rawType {@link Class} to get supertype for.
   * @param toResolve supertype {@link Class}.
   * @return {@code toResolve} {@link Type}
   */
  public static Type getGenericSupertype(Type type, Class<?> rawType, Class<?> toResolve) {
    Type ret = toResolve == rawType ? type : null;
    ret = ret != null || !toResolve.isInterface() ? ret
        : getInterfaceGenericSupertype(rawType, toResolve);
    ret = ret != null || rawType.isInterface() ? ret
        : getHierarchyGenericSupertype(rawType, toResolve);
    return ret != null ? ret : toResolve;
  }

  private static Type getInterfaceGenericSupertype(Class<?> rawType, Class<?> toResolve) {
    Class<?>[] interfaces = rawType.getInterfaces();
    for (int i = 0, length = interfaces.length; i < length; i++) {
      if (interfaces[i] == toResolve) {
        return rawType.getGenericInterfaces()[i];
      } else if (toResolve.isAssignableFrom(interfaces[i])) {
        return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
      }
    }
    return null;
  }

  private static Type getHierarchyGenericSupertype(Class<?> rType, Class<?> toResolve) {
    Class<?> rawType = rType;
    while (rawType != Object.class) {
      Class<?> rawSupertype = rawType.getSuperclass();
      if (rawSupertype == toResolve) {
        return rawType.getGenericSuperclass();
      } else if (toResolve.isAssignableFrom(rawSupertype)) {
        return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
      }
      rawType = rawSupertype;
    }
    return null;
  }

  /**
   * Resolves a {@code TypeVariable} for a given {@code type}.
   * 
   * @param type {@link Type} to resolve {@code type} for.
   * @param rawType {@link Class} to resolve {@code type} for.
   * @param unknown {@link TypeVariable} to resolve.
   * @return {@link Type} of resolved {@link unknown}
   */
  public static Type resolveTypeVariable(Type type, Class<?> rawType, TypeVariable<?> unknown) {
    Class<?> declaredByRaw = declaringClassOf(unknown);
    if (declaredByRaw == null) {
      return unknown;
    }
    Type declaredBy = getGenericSupertype(type, rawType, declaredByRaw);
    if (declaredBy instanceof ParameterizedType) {
      int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
      return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
    }
    return unknown;
  }

  private static int indexOf(Object[] array, Object toFind) {
    for (int i = 0; i < array.length; i++) {
      if (toFind.equals(array[i])) {
        return i;
      }
    }
    throw new NoSuchElementException();
  }

  private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
    GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
    return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
  }

  private static void checkNotPrimitive(Type type, String use) {
    checkArgument(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive(),
        "Primitive types are not allowed in %s: %s", use, type);
  }

  /**
   * Gets a {@code List} with the {@code Class} hierarchy of a given {@code Class}.
   * 
   * @param type {@link Class} to get hierarchy for.
   * @return {@link List} with the descending {@link Class} hierarchy for {@code type}.
   */
  public static List<Class<?>> getTypeHierarchy(Class<?> type) {
    List<Class<?>> hierarchy = new LinkedList<>();
    Class<?> member = type;
    do {
      hierarchy.add(0, member);
      member = member.getSuperclass();
    } while (member != null && !Object.class.equals(member));
    return hierarchy;
  }

  /**
   * Gets a {@code List} with the declared methods of a given {@code Class}.
   * 
   * @param type {@link Class} to get methods for.
   * @return {@link List} with any declared {@link Method} for {@code type}.
   */
  public static List<Method> getMethods(Class<?> type) {
    Method[] array = type.getDeclaredMethods();
    List<Method> methods = new ArrayList<>(type.getDeclaredMethods().length);
    for (Method method : array)
      methods.add(method);
    return methods;
  }

  /**
   * Declares if a given method is overridden by another.
   * 
   * @param method {@link Method} to check for override.
   * @param sub Potentially overriding {@link Method} .
   * @return true if {@code sub} overrides {@method}, false otherwise.
   */
  public static boolean isOverride(Method method, Method sub) {
    return isSameMethodName(method, sub) && isSameReturnType(method, sub)
        && areSameParameterTypes(method, sub) && areValidOverrideModifiers(method, sub);
  }

  private static boolean isSameMethodName(Method method, Method sub) {
    return sub.getName().equals(method.getName());
  }

  private static boolean isSameReturnType(Method method, Method sub) {
    return method.getReturnType().isAssignableFrom(sub.getReturnType());
  }

  private static boolean areSameParameterTypes(Method method, Method sub) {
    return Arrays.equals(method.getParameterTypes(), sub.getParameterTypes());
  }

  private static boolean areValidOverrideModifiers(Method method, Method sub) {
    int methodModifiers = method.getModifiers();
    int subModifiers = sub.getModifiers();
    boolean samePackage = isSamePackage(method, sub);
    boolean ret;
    if (Modifier.isPublic(methodModifiers))
      ret = Modifier.isPublic(subModifiers);
    else if (Modifier.isProtected(methodModifiers))
      ret = Modifier.isPublic(subModifiers) || Modifier.isProtected(subModifiers);
    else if (Modifier.isPrivate(methodModifiers))
      ret = false;
    else if (Modifier.isPrivate(subModifiers) && !samePackage)
      ret = false;
    else
      ret = samePackage;
    return ret;
  }

  private static boolean isSamePackage(Method method, Method sub) {
    return method.getDeclaringClass().getPackage().equals(sub.getDeclaringClass().getPackage());
  }

}

