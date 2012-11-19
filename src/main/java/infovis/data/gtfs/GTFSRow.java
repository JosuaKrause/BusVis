package infovis.data.gtfs;

/**
 * A row of a GTFS file.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface GTFSRow {

  /**
   * Whether the field name is present.
   * 
   * @param name The name.
   * @return Whether the name is present.
   */
  boolean hasField(String name);

  /**
   * Getter.
   * 
   * @param name The name.
   * @return The value of the field with the given name. If the field is not
   *         present <code>null</code> is returned.
   */
  String getField(String name);

}
