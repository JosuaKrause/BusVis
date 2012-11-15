package jkit.io.csv;

import java.util.HashMap;
import java.util.Map;

public final class CSVRow {

  private final Map<String, String> map = new HashMap<String, String>();

  private String[] indexed;

  private int maxIndex;

  public CSVRow(final int numCols) {
    indexed = new String[numCols];
  }

  protected void addCell(final int index, final String name, final String value) {
    if(index >= indexed.length) {
      final String[] tmp = indexed;
      indexed = new String[index + 1];
      System.arraycopy(tmp, 0, indexed, 0, tmp.length);
    }
    maxIndex = Math.max(index, maxIndex);
    indexed[index] = value;
    if(name != null) {
      map.put(name, value);
    }
  }

  public boolean has(final String name) {
    return map.containsKey(name);
  }

  public boolean hasIndex(final int i) {
    return i <= maxIndex;
  }

  public int highestIndex() {
    return maxIndex;
  }

  public String get(final int index) {
    if(index > maxIndex || index < 0) throw new IndexOutOfBoundsException(
        "max index: " + maxIndex + " index: " + index);
    return indexed[index];
  }

  public String get(final String name) {
    return map.get(name);
  }

}
