package es.danpintas.tdi.keys;

/**
 * Abstract implementation for a paired key.
 * 
 * @author danpintas
 *
 * @param <A> First element type.
 * @param <B> Second element type.
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
    if (a == null) {
      if (other.a != null)
        return false;
    } else if (!a.equals(other.a))
      return false;
    if (b == null) {
      if (other.b != null)
        return false;
    } else if (!b.equals(other.b))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ProviderKey [a=" + a + ", b=" + b + "]";
  }

}
