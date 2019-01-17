package org.clapper.util.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;

import java.util.HashSet;
import java.util.Set;

import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>The <tt>Zipper</tt> class provides a convenient mechanism for writing
 * zip and jar files; it's a simplifying layer that sits on top of the
 * existing Zip and Jar classes provided by the JDK. A <tt>Zipper</tt>
 * object behaves like a container into which a caller can drop
 * <tt>File</tt> objects, <tt>InputStream</tt> objects, <tt>Writer</tt>
 * objects, <tt>URLs</tt> and pathnames. The objects that are dropped into
 * a <tt>Zipper</tt> container are written to the actual underlying zip or
 * jar file at the moment they're added to the <tt>Zipper</tt>
 * container.</p>
 *
 * <p>A <tt>Zipper</tt> object will write directories as well as files, and
 * it'll either preserve pathnames or flatten the paths down to single
 * components. When preserving pathnames, a <tt>Zipper</tt> object converts
 * absolute paths to relative paths by stripping any leading "file system
 * mount points." On Unix, this means stripping the leading "/"; on
 * Windows, it means stripping any leading drive letter and the leading
 * "\". (See <tt>java.io.File.listRoots()</tt> for more information.)</p>
 *
 * <p>A <tt>Zipper</tt> object will write a jar file if the associated file
 * name ends in ".jar"; otherwise, it'll write a Zip file.</p>
 *
 * <p><b>Note:</b> The <tt>Zipper</tt> class currently provides no support
 * for storing uncompressed (i.e., fully inflated) entries. All data stored
 * in the underlying zip or jar file is compressed, even though the
 * JDK-supplied zip and Jar classes support both compressed and uncompressed
 * entries. If necessary, the <tt>Zipper</tt> class can be extended to support
 * storing uncompressed data.</p>
 *
 * <h2>Example</h2>
 *
 * <p>The following code fragment adds three files and the contents of a URL
 * to a zip file.</p>
 *
 * <blockquote>
 * <pre>
 * try
 * {
 *     URL    url = new URL ("http://www.fulltilt.com/index.htm");
 *     File   f1  = new File ("fred.java");
 *     String f2  = "c:\temp\foobar.exe";
 *
 *     // create with "flatten" disabled, so we're preserving paths
 *     Zipper zipper = new Zipper ("c:\temp\myfile.zip", false);
 *     zipper.put (url, "msds.pdf");
 *     zipper.put (f1);
 *     zipper.put (f2);
 *     zipper.close();
 * }
 *
 * catch (Exception ex)
 * {
 *     ex.printStackTrace();
 *     System.exit (1);
 * }
 * </pre>
 * </blockquote>
 *
 * <p>Assuming the above code fragment runs without I/O errors, it'll
 * produce file <tt>C:\TEMP\MYFILE.ZIP</tt> containing the following
 * entries, in order:</p>
 *
 * <blockquote>
 * <pre>
 * index.htm
 * fred.java
 * temp/foobar.exe
 * </pre>
 * </blockquote>
 */
public class Zipper
{
    /*----------------------------------------------------------------------*\
                            Private Constants
    \*----------------------------------------------------------------------*/

    /**
     * Output buffer size, in bytes.
     */
    private static final int OUTPUT_BUF_SIZE = 8192;

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The zip or jar file being written.
     */
    private File zipFile = null;

    /**
     * True if it's a jar file; false otherwise.
     */
    private boolean isJar = false;

    /**
     * The output stream for writing to the zip or jar file. If isJar is
     * true, this object is really a JarOutputStream.
     */
    private ZipOutputStream zipStream = null;

    /**
     * The Manifest to associate with the jar file. Null for zip files.
     */
    private Manifest manifest = null;

    /**
     * Whether to flatten  (true) to preserve path information.
     */
    private boolean flatten = false;

    /**
     * Total number of entries written so far.
     */
    private int totalEntriesWritten = 0;

    /**
     * Table of contents. The set contains the names already written to the
     * zip file.
     */
    private Set<String> tableOfContents = new HashSet<String>();

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>Zipper</tt> object that will write the specified
     * zip or jar file.
     *
     * @param path      The pathname of the zip or jar file to write. If
     *                  the file already exists, it'll be overwritten.
     * @param flatten   If <tt>true</tt>, path names are flattened (i.e.,
     *                  all but the base file names are removed) in the Zip
     *                  or jar file; otherwise, paths are left intact (after
     *                  removal of any leading "/" character).
     *
     * @throws IOException  The specified file is a directory or isn't writable
     *
     * @see #Zipper(String,Manifest,boolean)
     * @see #Zipper(File,boolean)
     * @see #Zipper(File,Manifest,boolean)
     */
    public Zipper (String path, boolean flatten) throws IOException
    {
        this (new File (path), flatten);
    }

    /**
     * Create a new <tt>Zipper</tt> object that will write the specified
     * zip or jar file.
     *
     * @param path      The pathname of the zip or jar file to write. If the
     *                  file already exists, it'll be overwritten.
     * @param manifest  Optional <tt>Manifest</tt> to associate with the Jar
     *                  file. Ignored if <tt>file</tt> doesn't specify a Jar
     *                  file. May be null.
     * @param flatten   If <tt>true</tt>, path names are flattened (i.e.,
     *                  all but the base file names are removed) in the Zip
     *                  or jar file; otherwise, paths are left intact (after
     *                  removal of any leading "/" character).
     *
     * @throws IOException  The specified file is a directory or isn't writable
     *
     * @see #Zipper(String,boolean)
     * @see #Zipper(File,boolean)
     * @see #Zipper(File,Manifest,boolean)
     */
    public Zipper (String   path,
                   Manifest manifest,
                   boolean  flatten) throws IOException
    {
        this (new File (path), manifest, flatten);
    }

    /**
     * Create a new <tt>Zipper</tt> object that will write the specified
     * zip or jar file. The <tt>Zipper</tt> object is created in unbuffered
     * mode.
     *
     * @param file      The <tt>File</tt> object that specifies the zip or Jar
     *                  file to write. If the file already exists, it'll be
     *                  overwritten.
     * @param flatten   If <tt>true</tt>, path names are flattened (i.e.,
     *                  all but the base file names are removed) in the Zip
     *                  or jar file; otherwise, paths are left intact (after
     *                  removal of any leading "/" character).
     *
     * @throws IOException  The specified file is a directory or isn't writable
     *
     * @see #Zipper(String,boolean)
     * @see #Zipper(String,Manifest,boolean)
     * @see #Zipper(File,Manifest,boolean)
     */
    public Zipper (File file, boolean flatten) throws IOException
    {
        this (file, null, flatten);
    }

    /**
     * Create a new <tt>Zipper</tt> object that will write the specified
     * zip or jar file.
     *
     * @param file      The <tt>File</tt> object that specifies the zip or Jar
     *                  file to write. If the file already exists, it'll be
     *                  overwritten.
     * @param manifest  Optional <tt>Manifest</tt> to associate with the Jar
     *                  file. Ignored if <tt>file</tt> doesn't specify a Jar
     *                  file. May be null.
     * @param flatten   If <tt>true</tt>, path names are flattened (i.e.,
     *                  all but the base file names are removed) in the Zip
     *                  or jar file; otherwise, paths are left intact (after
     *                  removal of any leading "/" character).
     *
     * @throws IOException  The specified file is a directory or isn't writable
     *
     * @see #Zipper(String,boolean)
     * @see #Zipper(String,Manifest,boolean)
     * @see #Zipper(File,boolean)
     */
    public Zipper (File     file,
                   Manifest manifest,
                   boolean  flatten) throws IOException
    {
        if (file.isDirectory())
        {
            throw new IOException ("File \"" + file.getPath() +
                                   "\" is a directory.");
        }

        this.zipFile = file;
        this.flatten = flatten;

        String ext = FileUtil.getFileNameExtension (file.getName());
        if (ext.equals (".jar"))
        {
            isJar = true;
            this.manifest = manifest;
        }
    }

    /*----------------------------------------------------------------------*\
                                Destructor
    \*----------------------------------------------------------------------*/

    /**
     * Destroys the object, closing the underlying zip or jar file, if it's
     * open.
     */
    protected void finalize()
    {
        try
        {
            close();
        }

        catch (IOException ex)
        {
        }
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Close the <tt>Zipper</tt> object, flushing any changes to and closing
     * the underlying zip or jar file.
     *
     * @throws IOException   error closing Zip/jar file
     */
    public synchronized void close() throws IOException
    {
        if (zipStream != null)
        {
            zipStream.close();
            zipStream = null;
        }
    }

    /**
     * Determine whether an entry with the specified name has been written
     * to the zip file. The name will be flattened if this object was
     * created with flattening enabled.
     *
     * @param name  The name to check
     *
     * @return <tt>true</tt> if that name was written to the zip file;
     *         <tt>false</tt> otherwise
     */
    public synchronized boolean containsEntry (String name)
    {
        return tableOfContents.contains (convertName (name));
    }

    /**
     * Get the <tt>File</tt> object that describes the underlying Jar or
     * zip file to which this <tt>Zipper</tt> object is writing. It is legal
     * to call this method even after the <tt>Zipper</tt> has been closed.
     *
     * @return the underlying zip or jar file
     */
    public File getFile()
    {
        return zipFile;
    }

    /**
     * Get the total number of entries written to the zip or jar file so
     * far. It's legal to call this method even after the <tt>Zipper</tt>
     * object has been closed.
     *
     * @return the total number of entries written to this object
     *
     * @see #put(File)
     * @see #put(String)
     * @see #put(InputStream,String)
     * @see #close()
     */
    public int getTotalEntries()
    {
        return totalEntriesWritten;
    }

    /**
     * Put a <tt>File</tt> object to the zip or jar file. The file's
     * contents will be placed in the zip or jar file immediately following
     * the last item that was written.
     *
     * @param file     The <tt>File</tt> to be added to the zip or jar file.
     *                 The specified file can be a file or a directory.
     *
     * @throws IOException  The specified file doesn't exist or isn't readable.
     *
     * @see #put(File,String)
     * @see #put(String)
     * @see #put(InputStream,String)
     */
    public void put (File file) throws IOException
    {
        if (! file.exists())
        {
            throw new IOException ("File \"" + file.getPath() +
                                   "\" does not exist.");
        }

        if ((! file.isDirectory()) && (! file.canRead()))
        {
            throw new IOException ("Cannot read file \"" + file.getPath() +
                                   "\".");
        }

        if (file.isDirectory())
            writeDirectory (file);
        else
            write (file);
    }

    /**
     * Put a <tt>File</tt> object to the zip or jar file, but using a
     * specified Zip entry name, rather than the name of the file itself.
     * The file's contents will be placed in the zip or jar file
     * immediately following the last item that was written.
     *
     * @param file     The <tt>File</tt> to be added to the zip or jar file.
     *                 The specified file can be a file or a directory.
     * @param name     The name to use for the Zip entry
     *
     * @throws IOException  The specified file doesn't exist or isn't readable.
     *
     * @see #put(File)
     * @see #put(String)
     * @see #put(InputStream,String)
     */
    public void put (File file, String name) throws IOException
    {
        FileInputStream in = new FileInputStream (file);
        write (in, name);
        in.close();
    }

    /**
     * Put a named file to the zip or jar file. The file's contents will be
     * placed in the zip or jar file immediately following the last item
     * that was written.
     *
     * @param path     The path to the file to be added to the zip or jar file.
     *                 The specified file can be a file or a directory.
     *
     * @throws IOException  The specified file doesn't exist or isn't readable.
     *
     * @see #put(File,String)
     * @see #put(File)
     * @see #put(InputStream,String)
     */
    public void put (String path) throws IOException
    {
        put (new File (path));
    }

    /**
     * Put an <tt>InputStream</tt> object to the zip or jar file. The
     * <tt>InputStream</tt> will be read until EOF, and the stream of bytes
     * will be placed in the zip or jar file immediately following the
     * last item that was written.
     *
     * @param istream  The <tt>InputStream</tt> to be added to the zip or
     *                 jar file.
     * @param name     The name to give the entry in the zip or jar file.
     *
     * @throws IOException  The specified file doesn't exist or isn't readable.
     *
     * @see #put(File,String)
     * @see #put(File)
     * @see #put(String)
     */
    public void put (InputStream istream, String name) throws IOException
    {
        write (istream, name);
    }

    /**
     * Put an array of bytes to the zip or jar file. The stream of bytes
     * will be placed in the zip or jar file immediately following the last
     * item that was written.
     *
     * @param bytes    The bytes to be added to the zip or jar file.
     * @param name     The name to give the entry in the zip or jar file.
     *
     * @throws IOException  The specified file doesn't exist or isn't readable.
     *
     * @see #put(File,String)
     * @see #put(File)
     * @see #put(String)
     */
    public void put (byte[] bytes, String name) throws IOException
    {
        write (bytes, name);
    }

    /**
     * Open a URL, read its contents, and store the contents in the
     * underlying zip or jar file. The URL's contents will be placed in the
     * zip or jar file immediately following the last item that was
     * written.
     *
     * @param url  The URL to be downloaded and stored in the zip or jar file
     * @param name The name to give the entry in the zip or jar file
     *
     * @throws IOException failed to open or read URL
     */
    public void put (URL url, String name) throws IOException
    {
        write (url.openConnection().getInputStream(), name);
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Write the contents of a <tt>File</tt> object to the underlying
     * Jar or zip file. The <tt>File</tt> must not specify a directory.
     *
     * @param file  The <tt>File</tt> to write
     *
     * @throws IOException  on I/O error
     */
    private void write (File file) throws IOException
    {
        FileInputStream in = new FileInputStream (file);
        write (in, file.getPath());
        in.close();
    }

    /**
     * Write the contents of an <tt>InputStream</tt> object to the
     * underlying Jar or zip file.
     *
     * @param istream  The <tt>InputStream</tt> to write. The stream is
     *                 read until end of file.
     * @param name     The name to assign to the Zip entry
     *
     * @throws IOException  on I/O error
     */
    private void write (InputStream istream, String name) throws IOException
    {
        open();

        name = convertName (name);
        ZipEntry zipEntry = getZipEntry (name);
        zipStream.putNextEntry (zipEntry);

        byte buf[] = new byte[OUTPUT_BUF_SIZE];
        int total;

        while ( (total = istream.read (buf, 0, buf.length)) > 0 )
            zipStream.write (buf, 0, total);

        zipStream.closeEntry();
        totalEntriesWritten++;
        tableOfContents.add (name);
    }

    /**
     * Write an array of bytes underlying Jar or zip file.
     *
     * @param bytes    The array of bytes
     * @param name     The name to assign to the Zip entry
     *
     * @throws IOException  on I/O error
     */
    private void write (byte[] bytes, String name) throws IOException
    {
        open();

        name = convertName (name);
        ZipEntry zipEntry = getZipEntry (name);
        zipStream.putNextEntry (zipEntry);
        zipStream.write (bytes, 0, bytes.length);
        zipStream.closeEntry();
        totalEntriesWritten++;
        tableOfContents.add (name);
    }

    /**
     * Write a directory to the underlying Jar or zip file.
     *
     * @param file  The <tt>File</tt> representing the directory to write
     *
     * @throws IOException on I/O error
     */
    public void writeDirectory (File file) throws IOException
    {
        open();

        String name = convertName (file.getPath());

        if (! name.endsWith ("/"))
            name = name + "/";

        ZipEntry zipEntry = getZipEntry (name);
        zipStream.putNextEntry (zipEntry);
        zipStream.closeEntry();
        totalEntriesWritten++;
        tableOfContents.add (name);
    }

    /**
     * Convert a string name into an appropriate zip file entry, taking
     * flattening into account if it's enabled.
     *
     * @param name  the name
     *
     * @return the possibly-adjusted name
     */
    private String convertName (String name)
    {
        if (flatten)
            name = (new File (name)).getName();

        else
        {
            int   i;
            File  fsRoots[];

            // Nuke any leading file system roots. This will nail the
            // leading "/" on Unix, and any drive letters on Windows.

            fsRoots = File.listRoots();
            for (i = 0; i < fsRoots.length; i++)
            {
                String root = fsRoots[i].getPath();
                if (name.toLowerCase().startsWith (root.toLowerCase()))
                {
                    name = name.substring (root.length());
                    break;
                }
            }

            // If this is Windows, convert any Windows file separators
            // to Unix-style. The Zip protocol seems to prefer "/"
            // separators.

            if (File.separatorChar != '/')
            {
                // Replace all instances of the file separator.

                name = name.replace (File.separatorChar, '/');
            }
        }

        return name;
    }

    /**
     * Get a ZipEntry for a name. Returns a ZipEntry or a JarEntry,
     * depending on the underlying file type.
     *
     * @param name  The name to associate with the ZipEntry
     *
     * @return the ZipEntry object
     */
    private ZipEntry getZipEntry (String name)
    {
        return (isJar ? (new ZipEntry (name)) : (new JarEntry (name)));
    }

    /**
     * Ensure that the underlying zip or jar file is open.
     *
     * @throws IOException  on error
     */
    private synchronized void open() throws IOException
    {
        if (zipStream == null)
        {
            OutputStream out =
                new BufferedOutputStream (new FileOutputStream (zipFile));
            if (isJar)
            {
                if (manifest != null)
                    zipStream = new JarOutputStream (out, manifest);
                else
                    zipStream = new JarOutputStream (out);
            }

            else
            {
                zipStream = new ZipOutputStream (out);
            }
        }
    }
}
