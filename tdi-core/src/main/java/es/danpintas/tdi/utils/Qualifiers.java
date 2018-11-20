package es.danpintas.tdi.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Qualifier;

/**
 * {@link Qualifier} related utilities.
 *
 * @author danpintas
 */
public final class Qualifiers {
    
    @SuppressWarnings("all")
    private static final class NamedImpl implements Named {
        
        private final String value;
        
        private NamedImpl(String value) {
            this.value = value;
        }
        
        @Override
        public Class<? extends Annotation> annotationType() {
            return Named.class;
        }
        
        @Override
        public String value() {
            return value;
        }
        
        @Override
        public int hashCode() {
            return (127 * "value".hashCode()) ^ value.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Named))
                return false;
            Named other = (Named) o;
            return value.equals(other.value());
        }
        
        @Override
        public String toString() {
            return "@" + Named.class.getName() + "(value=" + value + ")";
        }
        
    }
    
    private Qualifiers() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a {@code Named} annotation with the given value
     *
     * @param name Value for the annotation.
     * @return {@link Named} annotation.
     */
    public static Named named(String name) {
        return new NamedImpl(name);
    }
    
    /**
     * Instantiates an {@code Annotation} from a given class.
     *
     * @param annotationClass {@link Class} to instance.
     * @return {@link Annotation} instance.
     */
    public static <T extends Annotation> T from(Class<T> annotationClass) {
        final Map<String, Object> members = resolveMembers(annotationClass);
        return annotationClass.cast(
                Proxy.newProxyInstance(annotationClass.getClassLoader(), new Class<?>[]{annotationClass},
                        (proxy, method, args) -> invoke(annotationClass, members, method, args)));
    }
    
    private static Map<String, Object> resolveMembers(Class<? extends Annotation> annotationType) {
        Map<String, Object> result = new HashMap<>();
        for (Method method : annotationType.getDeclaredMethods())
            result.put(method.getName(), method.getDefaultValue());
        return result;
    }
    
    private static <T extends Annotation> Object invoke(Class<T> annotationClass,
                                                        final Map<String, Object> members, Method method, Object[] args)
            throws IllegalAccessException, InvocationTargetException {
        switch (method.getName()) {
            case "annotationType":
                return annotationClass;
            case "toString":
                return annotationToString(annotationClass);
            case "hashCode":
                return annotationHashCode(annotationClass, members);
            case "equals":
                return annotationEquals(annotationClass, members, args[0]);
            default:
                return members.get(method.getName());
        }
    }
    
    private static boolean annotationEquals(Class<? extends Annotation> type,
                                            Map<String, Object> members, Object other)
            throws IllegalAccessException, InvocationTargetException {
        if (!type.isInstance(other)) {
            return false;
        }
        for (Method method : type.getDeclaredMethods()) {
            String name = method.getName();
            if (!Arrays.deepEquals(new Object[]{method.invoke(other)}, new Object[]{members.get(name)}))
                return false;
        }
        return true;
    }
    
    private static int annotationHashCode(Class<? extends Annotation> type,
                                          Map<String, Object> members) {
        int result = 0;
        for (Method method : type.getDeclaredMethods()) {
            String name = method.getName();
            Object value = members.get(name);
            result += (127 * name.hashCode()) ^ (Arrays.deepHashCode(new Object[]{value}) - 31);
        }
        return result;
    }
    
    private static String annotationToString(Class<? extends Annotation> type) {
        return "@" + type.getName() + "()";
    }
    
}
