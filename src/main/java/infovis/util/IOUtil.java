package infovis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

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
   * @param local local resource path or <code>null</code> if a direct path is
   *          specified.
   * @param resource resource to get
   * @return the URL
   * @throws IOException if the resource can't be found
   */
  public static URL getURL(final String local, final String resource) throws IOException {
    if(local == null) return new File(resource).toURI().toURL();
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
    return getURL(local, resource).openStream();
  }

  /**
   * Creates a {@link BufferedReader} from the given {@link InputStream} that
   * interprets the incoming bytes using the given charset.
   * 
   * @param in input stream
   * @param charset charset
   * @return reader
   */
  public static BufferedReader charsetReader(final InputStream in, final Charset charset) {
    return new BufferedReader(new InputStreamReader(in, charset));
  }

  /**
   * Checks whether the given URL has content. This method does not guarantee
   * whether the URL has content the next time the stream to it is opened,
   * though.
   * 
   * @param url The URL.
   * @return If non empty content could be obtained.
   */
  public static boolean hasContent(final URL url) {
    InputStream in = null;
    try {
      in = url.openStream();
      final boolean content = in.read() >= 0;
      in.close();
      return content;
    } catch(final IOException e) {
      if(in != null) {
        try {
          in.close();
        } catch(final IOException _) {
          // ignore
        }
      }
      return false;
    }
  }

}
