/**
 * 
 */
package jkit.io.csv;

/**
 * The context of an csv event.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface CSVContext {

  /**
   * Getter.
   * 
   * @return The csv reader.
   */
  CSVReader reader();

  /**
   * Getter.
   * 
   * @return The name of the current column.
   */
  String colName();

  /**
   * Getter.
   * 
   * @return The name of the current row.
   */
  String rowName();

  /**
   * Getter.
   * 
   * @return The current row.
   */
  int row();

  /**
   * Getter.
   * 
   * @return The current column.
   */
  int col();

}
