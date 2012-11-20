package infovis.util;

/**
 * Methods to help handling objects.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class Objects {

  /** No instance. */
  private Objects() {
    throw new AssertionError();
  }

  /**
   * Requires a non-null argument. This can be used in constructors to enforce
   * non-null arguments.
   * 
   * @param <T> The type of the argument.
   * @param obj The argument.
   * @return The argument.
   * @throws IllegalArgumentException When the argument is <code>null</code>.
   */
  public static <T> T requireNonNull(final T obj) {
    if(obj == null) throw new IllegalArgumentException("null argument");
    return obj;
  }

  /**
   * Requires a null argument.
   * 
   * @param obj The argument.
   * @throws IllegalArgumentException When the argument is <code>null</code>.
   */
  public static void requireNull(final Object obj) {
    if(obj != null) throw new IllegalArgumentException("non-null argument: " + obj);
  }

  /**
   * Requires a <code>true</code> statement.
   * 
   * @param value The statement that needs to be <code>true</code>.
   * @throws IllegalStateException When the statement is <code>false</code>.
   */
  public static void requireTrue(final boolean value) {
    if(!value) throw new IllegalStateException("expected true");
  }

  /**
   * Returns the first argument if it is non-null otherwise the second argument
   * is returned. This is a short-hand for:
   * <code>(obj != null ? obj : or)</code>
   * 
   * @param <T> The type of the arguments.
   * @param obj The object.
   * @param or The alternative.
   * @return The first argument if it is non-null or else the second argument.
   */
  public static <T> T nonNull(final T obj, final T or) {
    return obj != null ? obj : or;
  }

}
