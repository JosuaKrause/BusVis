/**
 * 
 */
package jkit.io.csv;

/**
 * A csv handler receives events from the csv reader. {@link CSVAdapter} can be
 * used as adapter class.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface CSVHandler {

  /**
   * Is called at the beginning of the csv file.
   * 
   * @param ctx The context.
   */
  void start(CSVContext ctx);

  /**
   * Is called per column before the first row.
   * 
   * @param ctx The context.
   * @param title The name of the current column.
   */
  void colTitle(CSVContext ctx, String title);

  /**
   * Is called per row before the first column.
   * 
   * @param ctx The context.
   * @param title The name of the current row.
   */
  void rowTitle(CSVContext ctx, String title);

  /**
   * Is called on every cell in the table.
   * 
   * @param ctx The context.
   * @param content The content of the current cell.
   */
  void cell(CSVContext ctx, String content);

  /**
   * Is called when a new row starts. It is called before the first cell.
   * 
   * @param ctx The context.
   */
  void row(CSVContext ctx);

  /**
   * Is called at the end of the csv file.
   * 
   * @param ctx The context.
   */
  void end(CSVContext ctx);

}
