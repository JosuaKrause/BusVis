package infovis.embed;

/**
 * The embedder technique, ie all subclasses of {@link AbstractEmbedder}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public enum Embedders {

  /** Radial positioning technique. */
  CIRCULAR("Radial Positioning"),

  /** Edge based technique. */
  EDGE("Edge Based Positioning"),

  /**
   * Spring embedding technique.
   * 
   * @deprecated The use of this technique is now deprecated.
   */
  @Deprecated
  SPRING("Spring Embedder"),

  /* end of declaration */;

  /**
   * The human readable name.
   */
  private String name;

  /**
   * Creates a technique with a name.
   * 
   * @param name The name of the technique.
   */
  private Embedders(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Creates an embedder for the given technique.
   * 
   * @param embed The technique.
   * @param drawer The drawer.
   * @param weighter The weighter.
   * @return The embedder.
   */
  @SuppressWarnings("deprecation")
  public static AbstractEmbedder createFor(final Embedders embed,
      final NodeDrawer drawer, final Weighter weighter) {
    switch(embed) {
      case EDGE:
        return new EdgeEmbedder(weighter, drawer);
      case CIRCULAR:
        return new CircularEmbedder(weighter, drawer);
      case SPRING:
        return new SpringEmbedder(weighter, drawer);
    }
    throw new InternalError();
  }

}
