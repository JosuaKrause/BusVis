package infovis.layout;

import infovis.busvis.NodeDrawer;
import infovis.busvis.Weighter;

/**
 * The layout technique, ie all subclasses of {@link AbstractLayouter}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public enum Layouts {

  /** Radial positioning technique. */
  CIRCULAR("Radial Layout"),

  /** Stress based technique. */
  STRESS("Stress Layout"),

  /* end of declaration */;

  /** The human readable name. */
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
   * Creates a layout for the given technique.
   * 
   * @param embed The technique.
   * @param drawer The drawer.
   * @param weighter The weighter.
   * @return The layout.
   */
  public static AbstractLayouter createFor(final Layouts embed,
      final NodeDrawer drawer, final Weighter weighter) {
    switch(embed) {
      case CIRCULAR:
        return new CircularLayouter(weighter, drawer);
      case STRESS:
        return new StressLayout(weighter, drawer);
    }
    throw new InternalError();
  }

}
