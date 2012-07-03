package infovis.data;

import java.awt.Color;

/**
 * A bus line, having a name and a color.
 * 
 * @author Leo Woerteler
 */
public final class BusLine implements Comparable<BusLine> {
  /** Bus line used for walked sections of a route. */
  public static final BusLine WALK = new BusLine("Walk", Color.BLACK);

  /** Bus line name. */
  private final String name;

  /** Bus color. */
  private final Color color;

  /** The hidden sorting name. */
  private final String hiddenSortingName;

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
    final String tmp = "0000000000" + name;
    final int l = tmp.length();
    hiddenSortingName = tmp.substring(l - 10, l);
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
   * @return The full bus line name.
   */
  public String getFullName() {
    return (this == WALK ? "" : "Line ") + getName();
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

  @Override
  public int compareTo(final BusLine o) {
    return -hiddenSortingName.compareTo(o.hiddenSortingName);
  }

}
