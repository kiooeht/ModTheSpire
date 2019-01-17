package org.clapper.util.misc;

import org.clapper.util.io.FileUtil;
import org.clapper.util.logging.Logger;
import org.clapper.util.text.TextUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import java.net.URLConnection;
import java.net.FileNameMap;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The <tt>MIMETypeUtil</tt> class provides some general purpose MIME type
 * utilities not found in the JDK. Among other methods, this class provides the
 * {@link #fileExtensionForMIMEType fileExtensionForMIMEType()}
 * method, which converts a MIME type to a file extension. That method uses
 * a traditional <tt>mime.types</tt> files, similar to the file shipped
 * with with web servers such as Apache. It looks for a suitable file in
 * the following locations:
 *
 * <ol>
 *   <li> First, it looks for the file <tt>.mime.types</tt> in the user's
 *        home directory.
 *   <li> Next, it looks for <tt>mime.types</tt> (no leading ".") in all the
 *        directories in the CLASSPATH
 *   <li> Last, it loads a default set of mappings shipped with this library
 * </ol>
 *
 * <p>It loads all the matching files it finds; the first mapping found for
 * a given MIME type is the one that is used. The files are only loaded once
 * within a given running Java VM.</p>
 *
 * <p>The syntax of the file follows the classic <tt>mime.types</tt>
 * syntax:</p>
 *
 * <pre>
 * # The format is &lt;mime type&gt; &lt;space separated file extensions&gt;
 * # Comments begin with a '#'
 *
 * text/plain             txt text TXT
 * text/html              html htm HTML HTM
 * ...
 * </pre>
 *
 * <p>When mapping a MIME type to an extension,
 * {@link #fileExtensionForMIMEType fileExtensionForMIMEType()}
 * uses the first extension it finds in the <tt>mime.types</tt> file.
 * MIME types that cannot be found in the file are mapped to extension
 * ".dat".</p>
 */
public class MIMETypeUtil
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Default MIME type, when a MIME type cannot be determined from a file's
     * extension.
     *
     * @see #MIMETypeForFile
     * @see #MIMETypeForFileName
     */
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /**
     * Resource bundle containing MIME type mappings
     */
    private static final String MIME_MAPPINGS_BUNDLE =
                                    "org.clapper.util.misc.MIMETypes";

    /*----------------------------------------------------------------------*\
                               Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * Table for converting MIME type strings to file extensions. The table
     * is initialized the first time fileExtensionForMIMEType() is
     * called.
     */
    private static Map<String, String> mimeTypeToExtensionMap = null;

    /**
     * Reverse lookup table, by extension.
     */
    private static Map<String, String> extensionToMIMETypeMap = null;

    /**
     * For issuing log messages
     */
    private static final Logger log = new Logger (MIMETypeUtil.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private MIMETypeUtil()
    {
        // Can't be instantiated
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get an appropriate extension for a MIME type.
     *
     * @param mimeType  the String MIME type
     *
     * @return the appropriate file name extension, or a default extension
     *         if not found. The extension will not have the leading "."
     *         character.
     */
    public static String fileExtensionForMIMEType (String mimeType)
    {
        loadMappings();

        String ext = (String) mimeTypeToExtensionMap.get (mimeType);

        if (ext == null)
            ext = "dat";

        return ext;
    }

    /**
     * Get the MIME type for a filename extension.
     *
     * @param extension       the extension, without the "."
     *
     * @return the MIME type, or a default MIME type if there's no mapping
     *         for the extension
     *
     * @see #MIMETypeForFileExtension(String,String)
     * @see #MIMETypeForFile(File)
     * @see #MIMETypeForFile(File,String)
     * @see #MIMETypeForFileName(String)
     * @see #MIMETypeForFileName(String,String)
     */
    public static String MIMETypeForFileExtension (String extension)  // NOPMD
    {
        return MIMETypeForFileExtension (extension, DEFAULT_MIME_TYPE);
    }

    /**
     * Get the MIME type for a filename extension.
     *
     * @param extension       the extension, without the "."
     * @param defaultMIMEType the default MIME type to use if one cannot
     *                        be determined from the extension, or null to
     *                        use {@link #DEFAULT_MIME_TYPE}
     *
     * @return the MIME type, or the default MIME type
     *
     * @see #MIMETypeForFileExtension(String)
     * @see #MIMETypeForFile(File)
     * @see #MIMETypeForFile(File,String)
     * @see #MIMETypeForFileName(String)
     * @see #MIMETypeForFileName(String,String)
     */
    public static String MIMETypeForFileExtension (String extension,   // NOPMD
                                                   String defaultMIMEType)
    {
        return MIMETypeForFileName ("test." + extension, defaultMIMEType);
    }

    /**
     * Get the MIME type for a file. This method is simply a convenient
     * front-end for <tt>java.net.FileNameMap.getContentTypeFor()</tt>,
     * but it applies a consistent default when <tt>getContentTypeFor()</tt>
     * returns null (which can happen).
     *
     * @param file   the file
     *
     * @return the MIME type to use
     *
     * @see #MIMETypeForFile(File,String)
     * @see #MIMETypeForFileName(String)
     * @see #MIMETypeForFileExtension(String)
     * @see #MIMETypeForFileExtension(String,String)
     * @see #DEFAULT_MIME_TYPE
     */
    public static String MIMETypeForFile (File file)                   // NOPMD
    {
        return MIMETypeForFileName (file.getName(), DEFAULT_MIME_TYPE);
    }

    /**
     * Get the MIME type for a file. This method is simply a convenient
     * front-end for <tt>java.net.FileNameMap.getContentTypeFor()</tt>,
     * but it applies the supplied default when <tt>getContentTypeFor()</tt>
     * returns null (which can happen).
     *
     * @param file            the file
     * @param defaultMIMEType the default MIME type to use if one cannot
     *                        be determined from the file's name, or null to
     *                        use {@link #DEFAULT_MIME_TYPE}
     *
     * @return the MIME type to use
     *
     * @see #MIMETypeForFile(File)
     * @see #MIMETypeForFileName(String,String)
     * @see #MIMETypeForFileExtension(String)
     * @see #MIMETypeForFileExtension(String,String)
     * @see #DEFAULT_MIME_TYPE
     */
    public static String MIMETypeForFile (File   file,                // NOPMD
                                          String defaultMIMEType)
    {
        return MIMETypeForFileName (file.getName(), defaultMIMEType);
    }

    /**
     * Get the MIME type for a name file. This method is simply a convenient
     * front-end for <tt>java.net.FileNameMap.getContentTypeFor()</tt>,
     * but it applies a consistent default when <tt>getContentTypeFor()</tt>
     * returns null (which can happen).
     *
     * @param fileName   the file name
     *
     * @return the MIME type to use
     *
     * @see #MIMETypeForFile(File)
     * @see #MIMETypeForFileName(String,String)
     * @see #DEFAULT_MIME_TYPE
     */
    public static String MIMETypeForFileName (String fileName)         // NOPMD
    {
        return MIMETypeForFileName (fileName, DEFAULT_MIME_TYPE);
    }

    /**
     * Get the MIME type for a file name. This method is simply a convenient
     * front-end for <tt>java.net.FileNameMap.getContentTypeFor()</tt>,
     * but it applies the supplied default when <tt>getContentTypeFor()</tt>
     * returns null (which can happen).
     *
     * @param fileName        the file name
     * @param defaultMIMEType the default MIME type to use if one cannot
     *                        be determined from the file name, or null to
     *                        use {@link #DEFAULT_MIME_TYPE}
     *
     * @return the MIME type to use
     *
     * @see #MIMETypeForFile(File,String)
     * @see #MIMETypeForFileName(String)
     * @see #MIMETypeForFileExtension(String)
     * @see #MIMETypeForFileExtension(String,String)
     * @see #DEFAULT_MIME_TYPE
     */
    public static String MIMETypeForFileName (String fileName,         // NOPMD
                                              String defaultMIMEType)
    {
        String mimeType = null;
        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        // Check ours first.

        loadMappings();

        String extension = FileUtil.getFileNameExtension (fileName);
        mimeType = (String) extensionToMIMETypeMap.get (extension);

        if (mimeType == null)
        {
            // Check the system one.

            mimeType = fileNameMap.getContentTypeFor (fileName);
        }

        if (mimeType != null)
        {
            if (mimeType.equals (DEFAULT_MIME_TYPE) &&
                (defaultMIMEType != null))
            {
                // Substitute the caller's default, if there is one, on the
                // assumption that it'll be more useful.

                mimeType = defaultMIMEType;
            }
        }

        else
        {
            mimeType = (defaultMIMEType == null) ? DEFAULT_MIME_TYPE
                                                 : defaultMIMEType;
        }

        return mimeType;
    }

    /**
     * <p>This method parses an HTTP-style "<tt>Content-type</tt>" header into
     * its constituent pieces. The HTTP specification (RFC 2616) defines
     * the <tt>Content-type</tt> header as:</p>
     *
     * <blockquote><pre>"Content-type" ":" &lt;media-type&gt;</pre></blockquote>
     *
     * <p>A <i>media-type</i> is a MIME type ("type/subtype"), with optional
     * name=value pair parameters. The parameters are separated from the
     * media type, and from each other, by ";" characters.</p>
     *
     * <p>A common example of a <tt>Content-type</tt> header is:</p>
     *
     * <blockquote><pre>Content-type: text/html; charset=ISO-8859-1</pre>
     * </blockquote>
     *
     * <p>This method parses apart the MIME type and the parameters, saving
     * each one separately in caller-supplied objects.</p>
     *
     * @param contentTypeHeader  the header value (without the
     *                           <tt>Content-type:</tt> prefix). This value
     *                           is what's typically returned by
     *                           <tt>URLConnection.getContentType()</tt>.
     * @param mimeType           a <tt>StringBuffer</tt> to receive the
     *                           MIME type, or null if you don't care about
     *                           the MIME type. This method clears the string
     *                           buffer before storing anything in it.
     * @param parameters         a <tt>Map</tt> to receive the parameters,
     *                           or null if you don't care about the
     *                           parameters. The parameter names are used
     *                           as the map's keys. This method clears the
     *                           map before storing anything in it.
     */
    public static void parseContentTypeHeader (String contentTypeHeader,
                                               StringBuffer mimeType,
                                               Map<String, String> parameters)
    {
        // ContentType header is required, and either the MIME type or the
        // parameters (or both) must be set. Otherwise, we do nothing.

        if ( ((mimeType != null) || (parameters != null))
                                 &&
             (! TextUtil.stringIsEmpty (contentTypeHeader)) )
        {
            if (mimeType == null)
                mimeType = new StringBuffer();

            if (parameters == null)
                parameters = new HashMap<String, String>();

            String[] tokens = TextUtil.split(contentTypeHeader, " ;");
            mimeType.setLength(0);
            mimeType.append(tokens[0]);

            parameters.clear();
            for (int i = 1; i < tokens.length; i++)
            {
                String[] nv = TextUtil.split(tokens[i], "=");

                parameters.put(nv[0], (nv.length == 1) ? "" : nv[1]);
            }
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Load the MIME type mappings into memory.
     */
    private static synchronized void loadMappings()
    {
        if (mimeTypeToExtensionMap != null)
            return;

        mimeTypeToExtensionMap = new HashMap<String, String>();
        extensionToMIMETypeMap = new HashMap<String, String>();

        // First, check the user's home directory.

        String fileSep = System.getProperty ("file.separator");
        StringBuffer buf = new StringBuffer();

        buf.append (System.getProperty ("user.home"));
        buf.append (fileSep);
        buf.append (".mime.types");

        loadMIMETypesFile (buf.toString());

        // Now, check every directory in the classpath.

        String   pathSep = System.getProperty ("path.separator");
        String   classPath = System.getProperty ("java.class.path");
        String[] pathComponents = TextUtil.split (classPath, pathSep);
        int      i;

        for (i = 0; i < pathComponents.length; i++)
        {
            buf.setLength (0);
            buf.append (pathComponents[i]);
            buf.append (fileSep);
            buf.append ("mime.types");

            loadMIMETypesFile (buf.toString());
        }

        // Finally, load the resource bundle.

        ResourceBundle bundle = ResourceBundle.getBundle(MIME_MAPPINGS_BUNDLE);
        for (Enumeration e = bundle.getKeys(); e.hasMoreElements(); )
        {
            String type = (String) e.nextElement();
            try
            {
                String[] extensions = TextUtil.split (bundle.getString (type));

                if (mimeTypeToExtensionMap.get (type) == null)
                {
                    log.debug ("Internal: " + type + " -> \"" + extensions[0] +
                               "\"");
                    mimeTypeToExtensionMap.put (type, extensions[0]);
                }

                for (i = 0; i < extensions.length; i++)
                {
                    if (extensionToMIMETypeMap.get (extensions[i]) == null)
                    {
                        log.debug ("Internal: " + "\"" + extensions[i] +
                                   "\" -> " + type);
                        extensionToMIMETypeMap.put (extensions[i], type);
                    }
                }
            }

            catch (MissingResourceException ex)
            {
                log.error ("While reading internal bundle \"" +
                           MIME_MAPPINGS_BUNDLE +
                           "\", got unexpected error on key \"" +
                           type + "\"",
                           ex);
            }
        }
    }

    /**
     * Attempt to load a MIME types file. Throws no exceptions.
     *
     * @param path  path to the file
     * @param map   map to load
     */
    private static void loadMIMETypesFile (String path)
    {
        try
        {
            File f = new File (path);

            log.debug ("Attempting to load MIME types file \"" + path + "\"");
            if (! (f.exists() && f.isFile()))
                log.debug ("Regular file \"" + path + "\" does not exist.");

            else
            {
                LineNumberReader r = new LineNumberReader (new FileReader (f));
                String line;

                while ((line = r.readLine()) != null)
                {
                    line = line.trim();

                    if ((line.length() == 0) || (line.startsWith ("#")))
                        continue;

                    String[] fields = TextUtil.split (line);

                    // Skip lines without at least two tokens.

                    if (fields.length < 2)
                        continue;

                    // Special case: Scan the extensions, and make sure we
                    // have at least one valid extension. Some .mime.types
                    // files have entries like this:
                    //
                    // mime/type  desc="xxx" exts="jnlp"
                    //
                    // We don't handle those.

                    List<String> extensions = new ArrayList<String>();

                    for (int i = 1; i < fields.length; i++)
                    {
                        if (fields[i].indexOf ('=') != -1)
                            continue;
                        if (fields[i].indexOf ('"') != -1)
                            continue;

                        // Treat as valid. Remove any leading "."

                        if (fields[i].startsWith ("."))
                        {
                            if (fields[i].length() == 1)
                                continue;

                            fields[i] = fields[i].substring (1);
                        }

                        extensions.add (fields[i]);
                    }

                    if (extensions.size() == 0)
                        continue;

                    // If the MIME type doesn't have a "/", skip it

                    String mimeType = fields[0];
                    String extension;

                    if (mimeType.indexOf ('/') == -1)
                        continue;

                    // The first field is the preferred extension. Keep any
                    // existing mapping for the MIME type.

                    if (mimeTypeToExtensionMap.get (mimeType) == null)
                    {
                        extension = (String) extensions.get (0);
                        log.debug ("File \"" + path + "\": " + mimeType +
                                   " -> \"" + extension + "\"");

                        mimeTypeToExtensionMap.put (mimeType, extension);
                    }

                    // Map the extensions back to the MIME type

                    for (Iterator it = extensions.iterator(); it.hasNext(); )
                    {
                        extension = (String) it.next();
                        if (extensionToMIMETypeMap.get (extension) == null)
                        {
                            log.debug ("File \"" + path + "\": \"" +
                                       extension + "\" -> " + mimeType);
                            extensionToMIMETypeMap.put (extension, mimeType);
                        }
                    }
                }

                r.close();
            }
        }

        catch (IOException ex)
        {
            log.debug ("Error reading \"" + path + "\"", ex);
        }
    }
}
