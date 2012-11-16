package jkit.io.csv;

/**
 * An empty csv handler adapter. Ignores every event.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CSVAdapter implements CSVHandler {

  @Override
  public void start(final CSVContext ctx) {
    // nothing to do
  }

  @Override
  public void colTitle(final CSVContext ctx, final String title) {
    // nothing to do
  }

  @Override
  public void rowTitle(final CSVContext ctx, final String title) {
    // nothing to do
  }

  @Override
  public void cell(final CSVContext ctx, final String content) {
    // nothing to do
  }

  @Override
  public void row(final CSVContext ctx) {
    // nothing to do
  }

  @Override
  public void end(final CSVContext ctx) {
    // nothing to do
  }

}
