package infovis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * I/O utility methods.
 * 
 * @author Leo Woerteler
 */
public final class IOUtil {
  /** Hidden default constructor. */
  private IOUtil() {
    // never used
  }

  /**
   * Gets the {@link URL} of a resource.
   * 
   * @param local path to local resources directory
   * @param resource resource to get
   * @return the URL
   * @throws IOException if the resource can't be found
   */
  public static URL getURL(final String local, final String resource) throws IOException {
    final URL url = IOUtil.class.getResource('/' + resource);
    if(url != null) return url;
    final File f = new File(local, resource);
    if(!f.canRead()) throw new IOException(f.getName() + " doesn't exist");
    return f.toURI().toURL();
  }

  /**
   * Gets an {@link InputStream} for a resource.
   * 
   * @param local local resource directory
   * @param resource resource
   * @return input stream
   * @throws IOException if the resource can't be found
   */
  public static InputStream getResource(final String local, final String resource)
      throws IOException {
    final InputStream in = IOUtil.class.getResourceAsStream('/' + resource);
    if(in != null) return in;
    final File f = new File(local, resource);
    if(!f.canRead()) throw new IOException(f.getName() + " doesn't exist");
    return new FileInputStream(f);
  }

  /**
   * Creates a {@link BufferedReader} from the given {@link InputStream} that
   * interprets the incoming bytes using the given charset.
   * 
   * @param in input stream
   * @param charset charset
   * @return reader
   * @throws UnsupportedEncodingException if the encoding isn't supported
   */
  public static BufferedReader charsetReader(final InputStream in, final String charset)
      throws UnsupportedEncodingException {
    return new BufferedReader(new InputStreamReader(in, charset));
  }
}
