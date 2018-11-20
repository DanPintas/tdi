package es.danpintas.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

/**
 * Generic info holder for {@link Type}.
 *
 * @param <T> Type the object refers to.
 * @author danpintas
 */
public class TypeData<T> {
    
    private static final String NOT_SUPERTYPE_CONSTRUCTOR = "%s does not construct a supertype of %s";
    private static final String NOT_SUPERTYPE = "%s is not defined by a supertype of %s";
    
    private final Class<? super T> rawType;
    private final Type type;
    private final int hashCode;
    
    @SuppressWarnings("unchecked")
    private TypeData(Type type) {
        this.type = Types.canonicalize(type);
        this.rawType = (Class<? super T>) Types.getRawType(this.type);
        this.hashCode = this.type.hashCode();
    }
    
    /**
     * Gets type data for the given {@code Type} instance.
     *
     * @param type Underlying {@link Type}.
     */
    public static TypeData get(Type type) {
        return new TypeData<>(type);
    }
    
    /**
     * Gets type data for the given {@code Class} instance.
     *
     * @param type Underlying {@link Type} as {@link Class}.
     */
    public static <T> TypeData<T> get(Class<T> type) {
        return new TypeData<>(type);
    }
    
    /**
     * Returns the raw (non-generic) type for this type.
     */
    public final Class<? super T> getRawType() {
        return rawType;
    }
    
    /**
     * Gets underlying {@code Type} instance.
     */
    public final Type getType() {
        return type;
    }
    
    @Override
    public final int hashCode() {
        return this.hashCode;
    }
    
    @Override
    public final boolean equals(Object o) {
        return o instanceof TypeData<?> && Types.typeEquals(type, ((TypeData<?>) o).type);
    }
    
    @Override
    public final String toString() {
        return Types.typeToString(type);
    }
    
    private List<TypeData> resolveAll(Type[] types) {
        TypeData<?>[] result = new TypeData<?>[types.length];
        for (int t = 0; t < types.length; t++) {
            result[t] = resolve(types[t]);
        }
        return Arrays.asList(result);
    }
    
    private TypeData<?> resolve(Type toResolve) {
        return TypeData.get(resolveType(toResolve));
    }
    
    private Type resolveType(Type r) {
        Type toResolve = r;
        while (true) {
            if (toResolve instanceof TypeVariable) {
                TypeVariable<?> original = (TypeVariable<?>) toResolve;
                toResolve = Types.resolveTypeVariable(type, rawType, original);
                if (toResolve == original) {
                    return toResolve;
                }
            } else if (toResolve instanceof GenericArrayType) {
                return resolveGenericArrayType((GenericArrayType) toResolve);
            } else if (toResolve instanceof ParameterizedType) {
                return resolveParameterizedType((ParameterizedType) toResolve);
            } else if (toResolve instanceof WildcardType) {
                return resolveWildcardType((WildcardType) toResolve);
            } else {
                return toResolve;
            }
        }
    }
    
    private Type resolveGenericArrayType(GenericArrayType toResolve) {
        Type componentType = toResolve.getGenericComponentType();
        Type newComponentType = resolveType(componentType);
        return componentType == newComponentType ? toResolve : Types.arrayOf(newComponentType);
    }
    
    private Type resolveParameterizedType(ParameterizedType toResolve) {
        Type ownerType = toResolve.getOwnerType();
        Type newOwnerType = resolveType(ownerType);
        boolean changed = newOwnerType != ownerType;
        
        Type[] args = toResolve.getActualTypeArguments();
        for (int t = 0, length = args.length; t < length; t++) {
            Type resolvedTypeArgument = resolveType(args[t]);
            if (resolvedTypeArgument != args[t]) {
                if (!changed) {
                    args = args.clone();
                    changed = true;
                }
                args[t] = resolvedTypeArgument;
            }
        }
        
        return changed ? Types.parameterizedTypeWithOwner(newOwnerType, toResolve.getRawType(), args) : toResolve;
    }
    
    private Type resolveWildcardType(WildcardType toResolve) {
        Type[] originalLowerBound = toResolve.getLowerBounds();
        Type[] originalUpperBound = toResolve.getUpperBounds();
        
        if (originalLowerBound.length == 1) {
            Type lowerBound = resolveType(originalLowerBound[0]);
            if (lowerBound != originalLowerBound[0]) {
                return Types.supertypeOf(lowerBound);
            }
        } else if (originalUpperBound.length == 1) {
            Type upperBound = resolveType(originalUpperBound[0]);
            if (upperBound != originalUpperBound[0]) {
                return Types.subtypeOf(upperBound);
            }
        }
        return toResolve;
    }
    
    /**
     * Gets the type data of a {@code supertype}. <br/>
     * For example, if this is {@code ArrayList<Integer>}, this returns {@code List<Integer>} given
     * the input {@code List.class}.
     *
     * @param supertype Superclass of, or interface implemented by, this.
     * @return {@code TypeData} of the given supertype.
     */
    public TypeData getSupertype(Class<?> supertype) {
        Validations.checkArgument(supertype.isAssignableFrom(rawType), "%s is not a supertype of %s",
                supertype, this.type);
        return resolve(Types.getGenericSupertype(type, rawType, supertype));
    }
    
    /**
     * Gets the type data of {@code field}.
     *
     * @param field {@link Field} defined by this or any supertype.
     * @return {@code TypeData} of the given field.
     */
    public TypeData getFieldType(Field field) {
        checkAssignableMember(field);
        return resolve(field.getGenericType());
    }
    
    /**
     * Returns the type data of {@code method}.
     *
     * @param method {@link Method} defined by this or any supertype.
     * @return {@code TypeData} of the given method return.
     */
    public TypeData getReturnType(Method method) {
        checkAssignableMember(method);
        return resolve(method.getGenericReturnType());
    }
    
    /**
     * Gets the type data associated to the parameter types of {@code executable}.
     *
     * @param executable {@link Executable} defined by this or any supertype.
     * @return {@link List} with the {@code TypeData} of the given executable's parameters.
     */
    public List<TypeData> getParameterTypes(Executable executable) {
        checkAssignableMember(executable);
        return resolveAll(executable.getGenericParameterTypes());
    }
    
    /**
     * Gets the {@code TypeData} associated to the exception types of {@code executable}.
     *
     * @param executable {@link Executable} defined by this or any supertype.
     * @return {@link List} with the {@code TypeData} of the given executable's exceptions.
     */
    public List<TypeData> getExceptionTypes(Executable executable) {
        checkAssignableMember(executable);
        return resolveAll(executable.getGenericExceptionTypes());
    }
    
    private void checkAssignableMember(Member member) {
        Validations.checkArgument(member.getDeclaringClass().isAssignableFrom(rawType),
                () -> getErrorTemplate(member.getClass()), member, type);
    }
    
    private String getErrorTemplate(Class<?> memberClass) {
        return Constructor.class == memberClass ? NOT_SUPERTYPE_CONSTRUCTOR : NOT_SUPERTYPE;
    }
    
}

