package infovis.data;

import java.awt.Color;

/**
 * A bus line, having a name and a color.
 * 
 * @author Leo Woerteler
 */
public class BusLine {
  /** Bus line name. */
  private final String name;

  /** Bus color. */
  private final Color color;

  /**
   * Constructor taking the name and the color.
   * 
   * @param name bus line name
   * @param color bus line color
   */
  BusLine(final String name, final Color color) {
    if(name == null || color == null) throw new NullPointerException();
    this.name = name;
    this.color = color;
  }

  /**
   * Getter.
   * 
   * @return bus line name
   */
  public String getName() {
    return name;
  }

  /**
   * Getter.
   * 
   * @return bus line color
   */
  public Color getColor() {
    return color;
  }

  @Override
  public String toString() {
    return String.format("%s[%s, %08X]", getClass().getSimpleName(), name, color.getRGB());
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof BusLine && name.equals(((BusLine) obj).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
