package es.danpintas.reflect;

import java.util.function.Supplier;

/**
 * Validation utils.
 * 
 * @author danpintas
 */
public final class Validations {

  private Validations() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an exception with formatted message if an expression is false.
   * 
   * @param expression {@link Boolean} to check.
   * @param template Error message template.
   * @param args Error message arguments.
   */
  public static void checkArgument(boolean expression, String template, Object... args) {
    if (!expression)
      throw new IllegalArgumentException(String.format(template, args));
  }

  /**
   * Throws an exception with formatted message if an expression is false.
   * 
   * @param expression {@link Boolean} to check.
   * @param template Error message template supplier, to postpone calculation.
   * @param args Error message arguments.
   */
  public static void checkArgument(boolean expression, Supplier<String> template, Object... args) {
    if (!expression)
      throw new IllegalArgumentException(String.format(template.get(), args));
  }

}
