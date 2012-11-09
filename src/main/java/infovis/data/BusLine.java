package infovis.data;

import infovis.util.Objects;

import java.awt.Color;

/**
 * A bus line, having a name and a color.
 * 
 * @author Leo Woerteler
 */
public final class BusLine implements Comparable<BusLine> {

  /** Bus line used for walked sections of a route. */
  public static final BusLine WALK = new BusLine("Walk", null, Color.BLACK);

  /** Bus line name. */
  private final String name;

  /** Long bus line name. */
  private final String longName;

  /** Bus color. */
  private final Color color;

  /** The hidden sorting name. */
  private final String hiddenSortingName;

  /**
   * Constructor taking the name and the color.
   * 
   * @param name Bus line name,
   * @param longName Long bus line name, may be <code>null</code>.
   * @param color Bus line color.
   */
  BusLine(final String name, final String longName, final Color color) {
    this.name = Objects.requireNonNull(name);
    this.longName = Objects.nonNull(longName, name);
    this.color = Objects.requireNonNull(color);
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
    return longName;
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

  /**
   * Checks for equality with other bus lines.
   * 
   * @param line The other line.
   * @return If both are equal and non-null.
   */
  public boolean equals(final BusLine line) {
    return line != null && (this == line || name.equals(line.name));
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
