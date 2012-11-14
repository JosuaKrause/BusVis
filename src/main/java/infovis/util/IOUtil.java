package infovis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import au.com.bytecode.opencsv.CSVReader;

/**
 * I/O utility methods.
 * 
 * @author Leo Woerteler
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class IOUtil {

  /** Hidden default constructor. */
  private IOUtil() {
    // never used
  }

  /** The CP-1252 character set for Excel compatibility. */
  public static final Charset CP1252 = Charset.forName("CP1252");

  /** The UTF-8 character set. */
  public static final Charset UTF8 = Charset.forName("UTF-8");

  /** Resource path. */
  public static final String RESOURCES = "src/main/resources";

  public static String getParent(final String path) {
    String f = path;
    while(endsWithDelim(f)) {
      f = parent(f);
    }
    return parent(f);
  }

  private static String parent(final String path) {
    final int i = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
    return path.substring(0, i + 1);
  }

  private static boolean endsWithDelim(final String path) {
    final char end = path.charAt(path.length() - 1);
    return end == '/' || end == '\\';
  }

  public static File directFile(final String path) {
    return new File(path);
  }

  public static File directFile(final String path, final String file) {
    if(endsWithDelim(path)) return directFile(path + file);
    return directFile(path + "/" + file);
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

  /**
   * Creates a {@link CSVReader} suitable for Microsoft Excel CSV files.
   * 
   * @param local The local resource path or <code>null</code> if a direct path
   *          is specified.
   * @param path sub-directory inside the resource directory
   * @param file CSV file
   * @param cs The charset.
   * @return reader or <code>null</code> if not found.
   * @throws IOException I/O exception
   */
  public static CSVReader readerFor(final String local, final String path,
      final String file, final Charset cs) throws IOException {
    final URL url = IOUtil.getURL(local, path + '/' + file);
    if(!IOUtil.hasContent(url)) return null;
    return new CSVReader(IOUtil.charsetReader(url.openStream(), cs), ';');
  }

}
