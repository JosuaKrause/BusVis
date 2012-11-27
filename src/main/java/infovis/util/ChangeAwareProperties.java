package infovis.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

/**
 * A property implementation that is aware of software side changes.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class ChangeAwareProperties extends Properties {

  /** Whether the properties have changed. */
  private boolean hasChanged = false;

  @Override
  public synchronized void clear() {
    super.clear();
    hasChanged = true;
  }

  @Override
  public synchronized void load(final InputStream inStream) throws IOException {
    super.load(inStream);
    hasChanged = false;
  }

  @Override
  public synchronized void load(final Reader reader) throws IOException {
    super.load(reader);
    hasChanged = false;
  }

  @Override
  public synchronized void loadFromXML(final InputStream in)
      throws IOException, InvalidPropertiesFormatException {
    super.loadFromXML(in);
    hasChanged = false;
  }

  @Override
  public synchronized Object put(final Object key, final Object value) {
    Objects.requireNonNull(value);
    Objects.requireNonNull(key);
    final Object ref = get(key);
    if(ref != null && ref.equals(value)) return ref;
    hasChanged = true;
    return super.put(key, value);
  }

  @Override
  public synchronized void putAll(final Map<? extends Object, ? extends Object> t) {
    for(final java.util.Map.Entry<? extends Object, ? extends Object> e : t.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public synchronized Object remove(final Object key) {
    if(!containsKey(key)) return null;
    hasChanged = true;
    return super.remove(key);
  }

  /**
   * Stores the property to the resource if it has been changed.
   * 
   * @param r The resource to store the properties to.
   * @param comments Comments for the file.
   * @return Whether the file was written.
   * @throws IOException I/O Exception.
   */
  public boolean storeIfChanged(final Resource r,
      final String comments) throws IOException {
    if(!hasChanged) return false;
    final Writer out = r.writer();
    if(out == null) return false;
    store(out, comments);
    return true;
  }

  /**
   * Setter.
   * 
   * @param hasChanged Overwrites the changed status of this property object.
   */
  public void setChangeStatus(final boolean hasChanged) {
    this.hasChanged = hasChanged;
  }

  /**
   * Getter.
   * 
   * @return Whether the properties have been changed.
   */
  public boolean hasChanged() {
    return hasChanged;
  }

}
