package infovis.layout;

import infovis.busvis.NodeDrawer;
import infovis.busvis.Weighter;

/**
 * The embedder technique, ie all subclasses of {@link AbstractLayouter}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public enum Layouts {

  /** Radial positioning technique. */
  CIRCULAR("Radial Positioning"),

  /** Edge based technique. */
  EDGE("Edge Based Positioning"),

  /** Stress based technique. */
  STRESS("Stress Based Positioning"),

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
  private Layouts(final String name) {
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
  public static AbstractLayouter createFor(final Layouts embed,
      final NodeDrawer drawer, final Weighter weighter) {
    switch(embed) {
      case EDGE:
        return new EdgeLayouter(weighter, drawer);
      case CIRCULAR:
        return new CircularLayouter(weighter, drawer);
      case STRESS:
        return new StressLayout(weighter, drawer);
      case SPRING:
        return new SpringLayout(weighter, drawer);
    }
    throw new InternalError();
  }

}
