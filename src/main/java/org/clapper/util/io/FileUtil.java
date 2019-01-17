package org.clapper.util.io;

import java.io.*;

/**
 * Static class containing miscellaneous file utility methods.
 */
public class FileUtil
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private FileUtil()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Determine whether a string represents an absolute path. On Unix, an
     * absolute path must start with a "/". On Windows, it must begin
     * with one of the machine's valid drive letters.
     *
     * @param path  the path to check
     *
     * @return <tt>true</tt> if it's absolute, <tt>false</tt> if not
     *
     * @throws IOException  on error
     */
    public static boolean isAbsolutePath(String path)
        throws IOException
    {
        // It's important not to use  java.util.File.listRoots(), for two
        // reasons:
        //
        // 1. On Windows, the floppy can be one of the roots. If there isn't
        //    a disk in the floppy drive, some versions of Windows will issue
        //    an "Abort/Continue/Retry" pop-up.
        // 2. If a security manager is installed, listRoots() can return
        //    a null or empty array.
        //
        // So, this version analyzes the pathname textually.

        boolean  isAbsolute = false;
        String   fileSep = System.getProperty("file.separator");

        if (fileSep.equals("/"))
        {
            // Unix.

            isAbsolute = path.startsWith("/");
        }

        else if (fileSep.equals("\\"))
        {
            // Windows. Must start with something that looks like a drive
            // letter.

            isAbsolute = (Character.isLetter(path.charAt(0))) &&
                         (path.charAt(1) == ':') &&
                         (path.charAt(2) == '\\');
        }

        else
        {
            throw new IOException("Can't determine operating system from " +
                                  "file separator \"" + fileSep + "\"");
        }

        return isAbsolute;
    }

    /**
     * Copy an <tt>InputStream</tt> to an <tt>OutputStream</tt>. If either
     * stream is not already buffered, then it's wrapped in the corresponding
     * buffered stream (i.e., <tt>BufferedInputStream</tt> or
     * <tt>BufferedOutputStream</tt>) before copying. Calling this method
     * is equivalent to:
     *
     * <blockquote><pre>copyStream (src, dst, 8192);</pre></blockquote>
     *
     * @param is    the source <tt>InputStream</tt>
     * @param os    the destination <tt>OutputStream</tt>
     *
     * @return total number of bytes copied
     *
     * @throws IOException  on error
     *
     * @see #copyStream(InputStream,OutputStream,int)
     * @see #copyReader(Reader,Writer)
     * @see #copyFile(File,File)
     */
    public static int copyStream(InputStream is, OutputStream os)
        throws IOException
    {
        return copyStream(is, os, -1);
    }

    /**
     * Copy an <tt>InputStream</tt> to an <tt>OutputStream</tt>. If either
     * stream is not already buffered, then it's wrapped in the corresponding
     * buffered stream (i.e., <tt>BufferedInputStream</tt> or
     * <tt>BufferedOutputStream</tt>) before copying.
     *
     * @param src        the source <tt>InputStream</tt>
     * @param dst        the destination <tt>OutputStream</tt>
     * @param bufferSize the buffer size to use, or -1 for a default
     *
     * @return total number of bytes copied
     *
     * @throws IOException  on error
     *
     * @see #copyReader(Reader,Writer,int)
     * @see #copyStream(InputStream,OutputStream)
     * @see #copyFile(File,File)
     */
    public static int copyStream(InputStream  src,
                                 OutputStream dst,
                                 int          bufferSize)
        throws IOException
    {
        int totalCopied = 0;

        if (! (src instanceof BufferedInputStream))
        {
            if (bufferSize > 0)
                src = new BufferedInputStream(src, bufferSize);
            else
                src = new BufferedInputStream(src);
        }

        if (! (dst instanceof BufferedOutputStream))
        {
            if (bufferSize > 0)
                dst = new BufferedOutputStream(dst, bufferSize);
            else
                dst = new BufferedOutputStream(dst);
        }

        int b;

        while ((b = src.read()) != -1)
        {
            dst.write(b);
            totalCopied++;
        }

        dst.flush();

        return totalCopied;
    }

    /**
     * Copy characters from a reader to a writer. If the reader is not
     * already buffered, then it's wrapped in a <tt>BufferedReader</tt>,
     * using the specified buffer size. Similarly, buffered stream (i.e.,
     * <tt>BufferedInputStream</tt> or If the writer is not already
     * buffered, then it's wrapped in a <tt>BufferedWriter</tt>, using the
     * specified buffer size.
     *
     * @param reader     where to read from
     * @param writer     where to write to
     * @param bufferSize buffer size to use, if reader and writer are not
     *                   already buffered, or -1 to use a default size.
     *
     * @return total number of characters copied
     *
     * @throws IOException on error
     *
     * @see #copyReader(Reader,Writer)
     * @see #copyStream(InputStream,OutputStream,int)
     * @see #copyStream(InputStream,OutputStream)
     * @see #copyFile(File,File)
     */
    public static int copyReader(Reader reader, Writer writer, int bufferSize)
        throws IOException
    {
        if (! (reader instanceof BufferedReader))
        {
            if (bufferSize > 0)
                reader = new BufferedReader(reader, bufferSize);
            else
                reader = new BufferedReader(reader);
        }

        if (! (writer instanceof BufferedWriter))
        {
            if (bufferSize > 0)
                writer = new BufferedWriter(writer, bufferSize);
            else
                writer = new BufferedWriter(writer);
        }

        int ch;
        int total = 0;

        while ((ch = reader.read()) != -1)
        {
            writer.write(ch);
            total++;
        }

        writer.flush();

        return total;
    }

    /**
     * Copy characters from a reader to a writer, using a default buffer size.
     *
     * @param reader  where to read from
     * @param writer  where to write to
     *
     * @return total number of characters copied
     *
     * @throws IOException on error
     *
     * @see #copyReader(Reader,Writer)
     * @see #copyStream(InputStream,OutputStream,int)
     * @see #copyStream(InputStream,OutputStream)
     * @see #copyFile(File,File)
     */
    public static int copyReader(Reader reader, Writer writer)
        throws IOException
    {
        return copyReader(reader, writer, -1);
    }

    /**
     * Copy one file to another. This method simply copies bytes and
     * performs no character set conversion. If you want character set
     * conversions, use {@link #copyTextFile(File,String,File,String)}.
     *
     * @param src  The file to copy
     * @param dst  Where to copy it. Can be a directory or a file.
     *
     * @return total number of bytes copied
     *
     * @throws IOException on error
     *
     * @see #copyTextFile(File,String,File,String)
     * @see #copyReader(Reader,Writer,int)
     * @see #copyReader(Reader,Writer)
     * @see #copyStream(InputStream,OutputStream,int)
     * @see #copyStream(InputStream,OutputStream)
     */
    public static int copyFile(File src, File dst) throws IOException
    {
        int totalCopied = 0;

        if (dst.isDirectory())
            dst = new File(dst, src.getName());

        InputStream   from = null;
        OutputStream  to   = null;

        try
        {
            from = new FileInputStream(src);
            to   = new FileOutputStream(dst);

            totalCopied = copyStream(from, to);
        }

        finally
        {
            if (from != null)
                from.close();

            if (to != null)
                to.close();
        }

        return totalCopied;
    }

    /**
     * Copy one file to another, character by character, possibly doing
     * character set conversions
     *
     * @param src         the file to copy
     * @param srcEncoding the character set encoding for the source file,
     *                    or null to assume the default
     * @param dst         Where to copy it. Can be a directory or a file.
     * @param dstEncoding the character set encoding for the destination file,
     *                    or null to assume the default
     *
     * @return total number of characters copied
     *
     * @throws IOException on error
     *
     * @see #copyFile(File,File)
     * @see #copyReader(Reader,Writer,int)
     * @see #copyReader(Reader,Writer)
     * @see #copyStream(InputStream,OutputStream,int)
     * @see #copyStream(InputStream,OutputStream)
     */
    public static int copyTextFile(File   src,
                                   String srcEncoding,
                                   File   dst,
                                   String dstEncoding)
        throws IOException
    {
        if (dst.isDirectory())
            dst = new File(dst, src.getName());

        Reader reader;
        Writer writer;

        if (srcEncoding != null)
        {
            reader = new InputStreamReader(new FileInputStream(src),
                srcEncoding);
        }

        else
        {
            reader = new FileReader(src);
        }

        if (dstEncoding != null)
        {
            writer = new OutputStreamWriter(new FileOutputStream(dst),
                dstEncoding);
        }

        else
        {
            writer = new FileWriter(dst);
        }

        int total = copyReader(reader, writer);

        reader.close();
        writer.close();

        return total;
    }

    /**
     * Get the virtual machine's default encoding.
     *
     * @return the default encoding
     */
    public static String getDefaultEncoding()
    {
        return java.nio.charset.Charset.defaultCharset().name();
    }

    /**
     * Get the extension for a path or file name. Does not include the ".".
     *
     * @param file the file
     *
     * @return the extension, or null if there isn't one
     */
    public static String getFileNameExtension(File file)
    {
        return getFileNameExtension(file.getName());
    }

    /**
     * Get the extension for a path or file name. Does not include the ".".
     *
     * @param path  the file or path name
     *
     * @return the extension, or null if there isn't one
     */
    public static String getFileNameExtension(String path)
    {
        String ext = null;
        int    i   = path.lastIndexOf('.');

        if ((i != -1) && (i != (path.length() - 1)))
            ext = path.substring(i + 1);

        return ext;
    }

    /**
     * Get the name of a file without its extension. Does not remove
     * any parent directory components. Uses <tt>File.getAbsolutePath()</tt>,
     * not <tt>File.getCanonicalPath()</tt> to get the path.
     *
     * @param file  the file
     *
     * @return the path without the extension
     */
    public static String getFileNameNoExtension(File file)
    {
        return getFileNameNoExtension(file.getAbsolutePath());
    }

    /**
     * Get the name of a file without its extension. Does not remove
     * any parent directory components.
     *
     * @param path  the path
     *
     * @return the path without the extension
     */
    public static String getFileNameNoExtension(String path)
    {
        int i = path.lastIndexOf('.');

        if (i != -1)
            path = path.substring(0, i);

        return path;
    }

    /**
     * Get the name of a file's parent directory. This is the directory
     * part of the filename. For instance, "/home/foo.zip" would return
     * "/home". This method uses the file's absolute path.
     *
     * @param fileName the file name
     *
     * @return directory name part of the file's absolute pathname
     *
     * @see #dirname(File)
     * @see #basename(String)
     */
    public static String dirname(String fileName)
    {
        return dirname(new File(fileName));
    }

    /**
     * Get the name of a file's parent directory. This is the directory
     * part of the filename. For instance, "/home/foo.zip" would return
     * "/home". This method uses the file's absolute path.
     *
     * @param file  the file whose parent directory is to be returned
     *
     * @return directory name part of the file's absolute pathname
     *
     * @see #dirname(String)
     * @see #basename(File)
     */
    public static String dirname(File file)
    {
        String  absName = file.getAbsolutePath();
        String  fileSep = System.getProperty("file.separator");
        int     lastSep = absName.lastIndexOf(fileSep);

        return absName.substring(0, lastSep);
    }

    /**
     * Get the base (i.e., simple file) name of a file. This is the file
     * name stripped of any directory information. For instance,
     * "/home/foo.zip" would return "foo.zip".
     *
     * @param fileName name of the file to get the basename for
     *
     * @return file name part of the file
     *
     * @see #dirname(String)
     */
    public static String basename(String fileName)
    {
        String  fileSep = System.getProperty("file.separator");
        int     lastSep = fileName.lastIndexOf(fileSep);

        return (lastSep == -1) ? fileName
                               : fileName.substring(lastSep + 1);
    }

    /**
     * Get the base (i.e., simple file) name of a file. This is the file
     * name stripped of any directory information. For instance,
     * "/home/foo.zip" would return "foo.zip".
     *
     * @param file  the file to get the basename for
     *
     * @return file name part of the file
     *
     * @see #basename(String)
     * @see #dirname(File)
     */
    public static String basename (File file)
    {
        return basename(file.getName());
    }
}
