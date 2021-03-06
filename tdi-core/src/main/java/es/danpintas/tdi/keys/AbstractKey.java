package es.danpintas.tdi.keys;

import java.util.Objects;

/**
 * Abstract implementation for a paired key.
 *
 * @param <A> First element type.
 * @param <B> Second element type.
 * @author danpintas
 */
public abstract class AbstractKey<A, B> {
    
    protected final A a;
    protected final B b;
    
    /**
     * Default constructor.
     *
     * @param a First element.
     * @param b Second element.
     */
    public AbstractKey(A a, B b) {
        this.a = a;
        this.b = b;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + ((b == null) ? 0 : b.hashCode());
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
        AbstractKey<?, ?> other = (AbstractKey<?, ?>) obj;
        return Objects.equals(a, other.a) && Objects.equals(b, other.b);
    }
    
    @Override
    public String toString() {
        return "ProviderKey [a=" + a + ", b=" + b + "]";
    }
    
}
