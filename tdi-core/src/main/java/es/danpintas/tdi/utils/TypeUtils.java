package es.danpintas.tdi.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Qualifier;

import es.danpintas.tdi.exceptions.InjectException;

public final class TypeUtils {
    
    private TypeUtils() {
        // utils class
    }
    
    /**
     * Gets a {@code List} with the {@code Inject} annotated methods of a given {@code Class}.
     *
     * @param type {@link Class} to get fields for.
     * @return {@link List} with any {@link Inject} {@link Method} for {@code type}.
     */
    public static List<Field> getInjectFields(Class<?> type) {
        Field[] array = type.getDeclaredFields();
        List<Field> fields = new ArrayList<>(array.length);
        for (Field field : array)
            if (field.getDeclaredAnnotation(Inject.class) != null)
                fields.add(field);
        return fields;
    }
    
    /**
     * Gets one (and only one) {@code PostConstruct} annotated method for a given {@code Type}.
     *
     * @param type {@link Type} to get method for.
     * @return {@link PostConstruct} annotated {@link Method}.
     */
    public static Method getPostConstructMethod(Class<?> type) {
        return getOnlyNoArgAnnotatedMethod(type, PostConstruct.class);
    }
    
    /**
     * Gets one (and only one) {@code PreDestroy} annotated method for a given {@code Type}.
     *
     * @param type {@link Type} to get method for.
     * @return {@link PreDestroy} annotated {@link Method}.
     */
    public static Method getPreDestroyMethod(Class<?> type) {
        return getOnlyNoArgAnnotatedMethod(type, PreDestroy.class);
    }
    
    private static Method getOnlyNoArgAnnotatedMethod(Class<?> type,
                                                      Class<? extends Annotation> annotation) {
        Method[] array = type.getDeclaredMethods();
        Method postConstruct = null;
        for (Method method : array) {
            if (method.getDeclaredAnnotation(annotation) != null) {
                if (method.getParameterTypes().length > 0)
                    throw new InjectException(type.getName() + " @" + annotation.getSimpleName() + " method "
                            + method.getName() + " has arguments");
                else if (postConstruct != null)
                    throw new InjectException(
                            type.getName() + " has than one @" + annotation.getSimpleName() + " method");
                else
                    postConstruct = method;
            }
        }
        return postConstruct;
    }
    
    /**
     * Gets one (and only one) {@code Inject} annotated constructor for a given {@code Type}.
     *
     * @param type {@link Type} to get constructor for.
     * @return {@link Inject} annotated {@link Constructor}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getInjectConstructor(Class<T> type) {
        List<Constructor<?>> constructors = new ArrayList<>();
        Constructor<?>[] dc = type.getDeclaredConstructors();
        for (Constructor<?> c : dc)
            if (c.isAnnotationPresent(Inject.class))
                constructors.add(c);
        switch (constructors.size()) {
            case 0:
                return null;
            case 1:
                return (Constructor<T>) constructors.get(0);
            default:
                throw new InjectException(
                        "More than one constructor annotated with @Inject for " + type.getName());
        }
    }
    
    /**
     * Gets one (and only one) {@code Qualifier} annotation for a given {@code AnnotatedElement}.
     *
     * @param element {@link AnnotatedElement} to register.
     * @return {@link Qualifier} annotated {@link Annotation}.
     */
    public static Annotation getQualifierAnnotation(AnnotatedElement element) {
        List<Annotation> qualifiers = new LinkedList<>();
        for (Annotation q : element.getDeclaredAnnotations())
            for (Annotation a : q.annotationType().getAnnotations())
                if (a instanceof Qualifier)
                    qualifiers.add(q);
        switch (qualifiers.size()) {
            case 0:
                return null;
            case 1:
                return qualifiers.get(0);
            default:
                throw new InjectException("More than one @Qualifier annotating element " + element);
        }
    }
    
}
