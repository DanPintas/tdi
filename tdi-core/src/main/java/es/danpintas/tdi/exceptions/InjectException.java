package es.danpintas.tdi.exceptions;

/**
 * Wrapper Exception for the library.
 * 
 * @author danpintas
 */
@SuppressWarnings("serial")
public class InjectException extends RuntimeException {

  /**
   * Constructor for message.
   * 
   * @param message Exception message.
   */
  public InjectException(String message) {
    super(message);
  }

  /**
   * Constructor for cause.
   * 
   * @param cause Exception cause.
   */
  public InjectException(Throwable cause) {
    super(cause);
  }

}
