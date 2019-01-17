package org.clapper.util.io;

import org.clapper.util.logging.Logger;

import org.clapper.util.text.UnixShellVariableSubstituter;
import org.clapper.util.text.VariableDereferencer;
import org.clapper.util.text.VariableSubstitutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import java.util.zip.GZIPOutputStream;

import java.text.DecimalFormat;

/**
 * <p>A <tt>RollingFileWriter</tt> is similar to the JDK-supplied
 * <tt>FileOutputStream</tt> class: It provides an output stream for
 * writing data to a <tt>File</tt>. It differs from a normal
 * <tt>FileOutputStream</tt> in that it provides support for rolling, or
 * versioned, output files.</p>
 *
 * <p>A <tt>RollingFileWriter</tt> object can be constructed so that
 * it will automatically roll a file over when it exceeds a certain size.
 * If automatic rollover is not selected, then rollover occurs only at the
 * time the file is first opened. Automatic rollover is controlled by two
 * parameters: the maximum size for any one file and the maximum number of
 * rolled-over files. If automatic rollover is enabled, whenever a file
 * exceeds the configured maximum size, the
 * <tt>RollingFileWriter</tt> closes the file, renames it so that it
 * has a numeric suffix and reopens the (now nonexistent) original file. It
 * also shifts previously-rolled files out of the way. The numeric suffix
 * always starts at 0, but the number of digits varies, depending on the
 * maximum number of files. Because automatic rollover normally occurs when
 * the maximum number of bytes has been exceeded, it's possible for the file
 * to roll in the middle of a line of output.</p>
 *
 * <p>Backup files can optionally be compressed, via the gzip algorithm,
 * at the time of roll-over.</p>
 *
 * <p>When a <tt>RollingFileWriter</tt> is instantiated with an appropriate
 * file pattern (see below), it first looks for an existing instance of the
 * named file. If it finds one, it rotates the existing file and any other
 * existing backups out of the way, by renaming them. The new name is based
 * on the primary name, with a version number (e.g., 0, 1, 2, etc.), or index,
 * inserted into it. The file pattern controls where the index is inserted
 * in the file names.</p>
 *
 * <p>The filename or pathname passed to the constructor must be a pattern
 * that contains a special marker, the string "${n}", indicating where the
 * file version number is to be placed. If the filename does not contain
 * such a marker, it is invalid. When a version number must be inserted,
 * the marker is replaced with a period (".") and an index number. The
 * number is zero-filled, if necessary, depending on the maximum number of
 * backup files. For instance, if the maximum number of backup files is 7,
 * then the backup algorithm will use ".0", ".1", ..., ".6". If the maximum
 * number of backup files is 20, the backup algorithm will use ".00",
 * ".01", etc.) The current (or most recent file) has no extension at all;
 * this file is called the <i>primary</i> file. Backup files are ordered by
 * reverse timestamp. The newest backup file has the lowest-numbered index,
 * and the oldest backup file has the highest-numbered index.</p>
 *
 * <p>Examples of valid file name patterns follow:</p>
 *
 * <table>
 *   <caption>Patterns</caption>
 *   <tr>
 *     <th>Pattern</th>
 *     <th>Maximum number of backup files</th>
 *     <th>Primary file</th>
 *     <th>Backup files</th>
 *  </tr>
 *
 *  <tr>
 *    <td><tt>/tmp/foo${n}.txt</tt></td>
 *    <td>3</td>
 *    <td><tt>/tmp/foo.txt</tt></td>
 *    <td><tt>/tmp/foo.0.txt</tt><br>
 *        <tt>/tmp/foo.1.txt</tt><br>
 *        <tt>/tmp/foo.2.txt</tt></td>
 *  </tr>
 *
 *  <tr>
 *    <td><tt>C:\temp\mumble${n}.log</tt></td>
 *    <td>11</td>
 *    <td><tt>C:\temp\mumble.log</tt></td>
 *    <td><tt>C:\temp\mumble.00.log</tt><br>
 *        <tt>C:\temp\mumble.01.log</tt><br>
 *        <tt>C:\temp\mumble.02.log</tt><br>
 *        <tt>C:\temp\mumble.03.log</tt><br>
 *        <tt>C:\temp\mumble.04.log</tt><br>
 *        <tt>C:\temp\mumble.05.log</tt><br>
 *        <tt>C:\temp\mumble.06.log</tt><br>
 *        <tt>C:\temp\mumble.07.log</tt><br>
 *        <tt>C:\temp\mumble.08.log</tt><br>
 *        <tt>C:\temp\mumble.09.log</tt></td>
 *  </tr>
 *
 *  <tr>
 *    <td><tt>/tmp/mumble.log${n}</tt></td>
 *    <td>5</td>
 *    <td><tt>/tmp/mumble.log</tt></td>
 *    <td><tt>/tmp/mumble.log.0</tt><br>
 *        <tt>/tmp/mumble.log.1</tt><br>
 *        <tt>/tmp/mumble.log.2</tt><br>
 *        <tt>/tmp/mumble.log.3</tt><br>
 *        <tt>/tmp/mumble.log.4</tt></td>
 *  </tr>
 * </table>
 *
 * <p>When a caller opens a <tt>RollingFileWriter</tt> object, it can also
 * register a callback object that will be invoked at the moment of roll-over,
 * to retrieve a roll-over message. This message is then written to the end
 * of the just rolled-over file and the beginning of the new file, and can
 * help users to determine whether a file is one of a chain of files. These
 * special callback objects can be instances of any class that implements
 * the <tt>RollingFileWriter.RolloverCallback</tt> interface.</p>
 *
 * <h3>Caveats</h3>
 *
 * <ol>
 *   <li>When automatic rollover is enabled, be careful not to choose a
 *       the maximum file size value that's too small.
 *
 *   <li>If you instantiate a <tt>RollingFileWriter</tt> handler for a given
 *       file and you specify a different maximum number of rolled-over
 *       files than the previous time you opened the file, the
 *       <tt>RollingFileWriter</tt> object may ignore previously rolled
 *       over files. For instance, if you instantiate a
 *       <tt>RollingFileWriter</tt> object to write to <tt>error.log{$n}</tt>,
 *       and you elect to retain 10 files, you'll eventually end up with
 *       <tt>error.log</tt>, <tt>error.log.0</tt>, <tt>...</tt>,
 *       <tt>error.log.9</tt>. If, in a later invocation of your
 *       application, you instantiate a <tt>RollingFileWriter</tt> object
 *       to write to <tt>error.log</tt>, but you elect to retain 100 files,
 *       the second <tt>RollingFileWriter</tt> object will never notice
 *       the rolled-over files created by the first application run,
 *       because it'll be using a 2-digit numeric extension (and,
 *       therefore, looking for files <tt>error.log.00</tt>,
 *       <tt>error.log.01</tt>, <tt>...</tt>, <tt>error.log.99</tt>).
 *
 *   <li>This class doesn't check for roll-over until one of the
 *       <tt>println()</tt> methods is called.
 *
 *   <li>A rolled-over file can be a little larger than the actual maximum
 *       size, depending on the length of the line that triggered the
 *       roll-over.
 * </ol>
 *
 *
 * @see java.io.File
 * @see java.io.PrintWriter
 */
public class RollingFileWriter extends PrintWriter
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * The pattern to substitute. Useful for classes that have to insert
     * this pattern into a string.
     */
    public static final String INDEX_PATTERN = "${n}";

    /*----------------------------------------------------------------------*\
                           Public Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Defines the interface to a callback object containing methods that a
     * <tt>RollingFileWriter</tt> can invoke during processing. Currently,
     * the only capability that is defined is the retrieval of a roll-over
     * message. When a caller opens a <tt>RollingFileWriter</tt> object, it
     * can also register a callback object that will be invoked at the
     * moment of roll-over, to retrieve a roll-over message. This message
     * is then written to the end of the just rolled-over file and the
     * beginning of the new file, and can help users to determine whether a
     * file is one of a chain of files.
     */
    public interface RolloverCallback
    {
        /**
         * Return a message to be written to the rolled-over file and the
         * newly-opened primary file at the moment of roll-over.
         *
         * @return the message, or null if there isn't one.
         */
        public String getRollOverMessage();
    }

    /**
     * An enumeration of constants defining whether or not to compress
     * backup files. (Easier to read than a boolean.)
     */
    public enum Compression
    {
        COMPRESS_BACKUPS,
        DONT_COMPRESS_BACKUPS
    }

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String GZIP_EXTENSION = ".gz";

    /*----------------------------------------------------------------------*\
                           Private Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Used to substitute the backup file index, as well as to check the
     * validity of a pattern.
     */
    private static class BackupIndexDereferencer
        implements VariableDereferencer
    {
        private Integer        index;
        private DecimalFormat  indexFormat;
        private boolean        legal = false;

        BackupIndexDereferencer (Integer index, int maxRolledOverFiles)
        {
            this.index       = index;
            this.indexFormat = indexFormat;

            // Have to create a decimal format we can use to construct the
            // suffix with the appropriate number of digits. The simplest
            // solution is to take the max number of files value, subtract
            // 1, and figure out how many digits that number has. A few
            // examples will illustrate that this works:
            //
            // maxRolledOverFiles  maxRolledOverFiles  # of
            //     value            value minus 1  digits    extensions
            // ---------------------------------------------------------------
            //        2                  1           1       0, 1
            //        9                  8           1       0, 1, 2, ..., 7
            //       10                  9           1       0, 1, 2, ..., 9
            //       25                 24           2       00, 01, ..., 24
            //      100                 99           2       00, 01, ..., 99
            //      101                100           3       000, 001, ..., 100

            String        digitCount = String.valueOf (maxRolledOverFiles - 1);
            StringBuilder format     = new StringBuilder();

            for (int i = 0; i < digitCount.length(); i++)
                format.append ('0');

            indexFormat = new DecimalFormat (format.toString());
        }

        public String getVariableValue (String varName, Object context)
            throws VariableSubstitutionException
        {
            if (! varName.equals ("n"))
            {
                throw new VariableSubstitutionException
                    (Package.BUNDLE_NAME,
                     "RollingFileWriter.unknownVariable",
                     "Unknown variable \"{0}\" in file pattern \"{1}\"",
                     new Object[] {varName, context});
            }

            legal = true;
            StringBuilder buf = new StringBuilder();

            if (index != null)
            {
                buf.append (".");
                buf.append (indexFormat.format (index));
            }

            return buf.toString();
        }

        boolean patternIsLegal()
        {
            return legal;
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The primary file being written to.
     */
    private File primaryFile = null;

    /**
     * The file pattern
     */
    private String filePattern = null;

    /**
     * The character set name passed to the constructor, or null
     */
    private String charsetName = null;

    /**
     * Maximum rolled file size. If set to 0 (the default), automatic
     * file roll-over is disabled.
     */
    private long maxRolledFileSize = 0;

    /**
     * Maximum number of rolled-over files. If set to 0 (the default),
     * automatic file roll-over is disabled.
     */
    private int maxRolledOverFiles = 0;

    /**
     * Compression type
     */
    private Compression compressionType = Compression.COMPRESS_BACKUPS;

    /**
     * Callback object, if any.
     */
    private RolloverCallback callback = null;

    /**
     * Line separator sequence, for issuing newlines.
     */
    private static String newline = System.getProperty ("line.separator");

    /**
     * For logging
     */
    private static Logger log = new Logger (RollingFileWriter.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructs a <tt>RollingFileWriter</tt> that does not do automatic
     * file roll-over. Roll-over of any existing files <i>does</i> occur at
     * the instant the file is opened (i.e., by this constructor), but does
     * not occur on the fly while the file is being written. Backups are not
     * compressed.
     *
     * @param fileNamePattern  The name pattern for the file to open
     *
     * @throws IOExceptionExt  Failed to open file
     *
     * @see #RollingFileWriter(String,Compression)
     * @see #RollingFileWriter(String,String,Compression)
     * @see #RollingFileWriter(String,long,int,Compression)
     * @see #RollingFileWriter(String,String,long,int,Compression,RolloverCallback)
     */
    public RollingFileWriter (String fileNamePattern)
        throws IOExceptionExt
    {
        this (fileNamePattern, null, Compression.DONT_COMPRESS_BACKUPS);
    }

    /**
     * Constructs a <tt>RollingFileWriter</tt> that does not do automatic
     * file roll-over. Roll-over of any existing files <i>does</i> occur at
     * the instant the file is opened (i.e., by this constructor), but does
     * not occur on the fly while the file is being written.
     *
     * @param fileNamePattern The name pattern for the file to open
     * @param compressionType {@link Compression#COMPRESS_BACKUPS} to
     *                        compress backups,
     *                        {@link Compression#DONT_COMPRESS_BACKUPS} to
     *                        leave backups uncompressed
     *
     * @throws IOExceptionExt  Failed to open file
     *
     * @see #RollingFileWriter(String)
     * @see #RollingFileWriter(String,String,Compression)
     * @see #RollingFileWriter(String,long,int,Compression)
     * @see #RollingFileWriter(String,String,long,int,Compression,RolloverCallback)
     */
    public RollingFileWriter (String      fileNamePattern,
                              Compression compressionType)
        throws IOExceptionExt
    {
        this (fileNamePattern, null, compressionType);
    }

    /**
     * Constructs a <tt>RollingFileWriter</tt> that does not do automatic
     * file roll-over. Roll-over of any existing files <i>does</i> occur at
     * the instant the file is opened (i.e., by this constructor), but does
     * not occur on the fly while the file is being written.
     *
     * @param fileNamePattern The name pattern for the file to open
     * @param charsetName     The name of the character encoding to use for
     *                        the output, or null for the default
     * @param compressionType {@link Compression#COMPRESS_BACKUPS} to
     *                        compress backups,
     *                        {@link Compression#DONT_COMPRESS_BACKUPS} to
     *                        leave backups uncompressed
     *
     * @throws IOExceptionExt  Failed to open file
     *
     * @see #RollingFileWriter(String)
     * @see #RollingFileWriter(String,Compression)
     * @see #RollingFileWriter(String,long,int,Compression)
     * @see #RollingFileWriter(String,String,long,int,Compression,RolloverCallback)
     */
    public RollingFileWriter (String      fileNamePattern,
                              String      charsetName,
                              Compression compressionType)
        throws IOExceptionExt
    {
        this (fileNamePattern, charsetName, 0, 0, compressionType, null);
    }

    /**
     * Create a new <tt>RollingFileWriter</tt> that will write to the
     * specified file, optionally automatically rolling the file over when
     * it exceeds a specified maximum size. No
     * {@link RollingFileWriter.RolloverCallback} object will be registered,
     * and rolled files will not be compressed.
     *
     * @param fileNamePattern   The name pattern for the file to open
     * @param charsetName       The name of the character encoding to use for
     *                          the output, or null for the default
     * @param maxRolledFileSize The maximum size, in bytes, that the file can
     *                          be before it is rolled over, or 0 for no
     *                          maximum.
     * @param maxRolledFiles    The maximum number of rolled-over log files
     *                          to retain, or 0 for no maximum.
     *
     * @throws IOExceptionExt Failed to open file.
     *
     * @see #RollingFileWriter(String)
     * @see #RollingFileWriter(String,Compression)
     * @see #RollingFileWriter(String,String,Compression)
     * @see #RollingFileWriter(String,String,long,int,Compression,RolloverCallback)
     */
    public RollingFileWriter (String  fileNamePattern,
                              String  charsetName,
                              long    maxRolledFileSize,
                              int     maxRolledFiles)
        throws IOExceptionExt
    {
        this (fileNamePattern,
              charsetName,
              maxRolledFileSize,
              maxRolledFiles,
              Compression.DONT_COMPRESS_BACKUPS,
              null);
    }

    /**
     * Create a new <tt>RollingFileWriter</tt> that will write to the
     * specified file, optionally automatically rolling the file over when
     * it exceeds a specified maximum size. No
     * {@link RollingFileWriter.RolloverCallback} object will be registered.
     *
     * @param fileNamePattern   The name pattern for the file to open
     * @param maxRolledFileSize The maximum size, in bytes, that the file can
     *                          be before it is rolled over, or 0 for no
     *                          maximum.
     * @param maxRolledFiles    The maximum number of rolled-over log files
     *                          to retain, or 0 for no maximum.
     * @param compressionType   {@link Compression#COMPRESS_BACKUPS} to
     *                          compress backups,
     *                          {@link Compression#DONT_COMPRESS_BACKUPS} to
     *                          leave backups uncompressed
     *
     * @throws IOExceptionExt Failed to open file.
     *
     * @see #RollingFileWriter(String)
     * @see #RollingFileWriter(String,Compression)
     * @see #RollingFileWriter(String,String,Compression)
     * @see #RollingFileWriter(String,String,long,int,Compression,RolloverCallback)
     */
    public RollingFileWriter (String      fileNamePattern,
                              long        maxRolledFileSize,
                              int         maxRolledFiles,
                              Compression compressionType)
        throws IOExceptionExt
    {
        this (fileNamePattern,
              null,
              maxRolledFileSize,
              maxRolledFiles,
              compressionType,
              null);
    }

    /**
     * Create a new <tt>RollingFileWriter</tt> that will write to the
     * specified file, optionally automatically rolling the file over when
     * it exceeds a specified maximum size.
     *
     * @param fileNamePattern    The name pattern for the file to open
     * @param charsetName        The name of the character encoding to use for
     *                           the output, or null for the default
     * @param maxRolledFileSize  The maximum size, in bytes, that the file can
     *                           be before it is rolled over, or 0 for no
     *                           maximum.
     * @param maxRolledOverFiles The maximum number of rolled-over log files
     *                           to retain, or 0 for no maximum.
     * @param compressionType    {@link Compression#COMPRESS_BACKUPS} to
     *                           compress backups,
     *                           {@link Compression#DONT_COMPRESS_BACKUPS} to
     *                           leave backups uncompressed
     * @param callback           The callback object to invoke on roll-over,
     *                           or null for none
     *
     * @throws IOExceptionExt Failed to open file.
     *
     * @see #RollingFileWriter(String)
     * @see #RollingFileWriter(String,Compression)
     * @see #RollingFileWriter(String,String,Compression)
     * @see #RollingFileWriter(String,long,int,Compression)
     */
    public RollingFileWriter (String           fileNamePattern,
                              String           charsetName,
                              long             maxRolledFileSize,
                              int              maxRolledOverFiles,
                              Compression      compressionType,
                              RolloverCallback callback)
        throws IOExceptionExt
    {
        super (openPrimaryFile (fileNamePattern,
                                charsetName,
                                maxRolledOverFiles,
                                compressionType,
                                callback),
               true);

        this.filePattern = fileNamePattern;
        this.compressionType = compressionType;
        this.primaryFile = resolveFilePattern (fileNamePattern,
                                               null,
                                               maxRolledOverFiles,
                                               null);
        this.callback = callback;
        this.charsetName = charsetName;
        this.compressionType = compressionType;
        this.maxRolledFileSize  = maxRolledFileSize;
        this.maxRolledOverFiles = maxRolledOverFiles;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the path name of the file being written to.
     *
     * @return The file's name
     */
    public String getPathName()
    {
        return this.primaryFile.getPath();
    }

    /**
     * Flush the stream. If the stream has saved any characters from the
     * various write() methods in a buffer, write them immediately to their
     * intended destination. Then, if that destination is another character
     * or byte stream, flush it. Thus one flush() invocation will flush all
     * the buffers in a chain of Writers and OutputStreams. This method
     * does not check for roll-over, because it's possible to flush the
     * object in the middle of a line, and roll-over should only occur at
     * the end of a line.
     */
    public synchronized void flush()
    {
        super.flush();
    }

    /**
     * Finish the current line, rolling the file if necessary.
     */
    public synchronized void println()
    {
        super.println();
        try
        {
            checkForRollOver();
        }

        catch (Exception ex)
        {
        }
    }

    /**
     * Print a boolean and finish the line, rolling the file if necessary.
     *
     * @param b  The boolean to print
     */
    public synchronized void println (boolean b)
    {
        print (b);
        println();
    }

    /**
     * Print a character and finish the line, rolling the file if necessary.
     *
     * @param c  The character to print
     */
    public synchronized void println (char c)
    {
        print (c);
        println();
    }

    /**
     * Print an array of characters and finish the line, rolling the file
     * if necessary.
     *
     * @param s  The array of characters to print
     */
    public synchronized void println (char s[])
    {
        print (s);
        println();
    }

    /**
     * Print a double and finish the line, rolling the file if necessary.
     *
     * @param d  The double floating point number to print
     */
    public synchronized void println (double d)
    {
        print (d);
        println();
    }

    /**
     * Print a float and finish the line, rolling the file if necessary.
     *
     * @param f  The floating point number to print
     */
    public synchronized void println (float f)
    {
        print (f);
        println();
    }

    /**
     * Print an integer and finish the line, rolling the file if necessary.
     *
     * @param i  The integer to print
     */
    public synchronized void println (int i)
    {
        print (i);
        println();
    }

    /**
     * Print a long and finish the line, rolling the file if necessary.
     *
     * @param l  The long to print
     */
    public synchronized void println (long l)
    {
        super.print (l);
        println();
    }

    /**
     * Print a short and finish the line, rolling the file if necessary.
     *
     * @param s  The short to print
     */
    public synchronized void println (short s)
    {
        super.print (s);
        println();
    }

    /**
     * Print a String and finish the line, rolling the file if necessary.
     *
     * @param s  The String to print.
     */
    public synchronized void println (String s)
    {
        super.print (s);
        println();
    }

    /**
     * Print an Object and finish the line, rolling the file if necessary.
     *
     * @param o The object to print.
     */
    public synchronized void println (Object o)
    {
        super.print (o);
        println();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Print a line to the file without rolling, even if it goes over
     * the file's limit. Newline implicitly added
     *
     * @param line the line to write
     */
    private void printlnNoRoll (String line)
    {
        // Be sure to invoke only methods in the parent class that
        // don't resolve back down to methods in this class! Otherwise,
        // we'll cause nested recursion.

        super.write (line);
        super.write (newline);
        super.flush ();
    }

    /**
     * Determines whether the log file needs to be rolled over and, if so,
     * rolls it over. If roll-over isn't enabled, this method returns without
     * doing anything.
     *
     * @throws IOExceptionExt  On error
     */
    private synchronized void checkForRollOver() throws IOExceptionExt
    {
        if ( (maxRolledFileSize > 0) && (maxRolledOverFiles > 0) )
        {
            // Rollover is enabled. If the log file is empty, we don't
            // roll it over (obviously), even if writing the message would
            // cause the log file to exceed the maximum size. (That's one big
            // message...)

            long fileSize = this.primaryFile.length();

            if (fileSize >= maxRolledFileSize)
            {
                // Must roll over.

                log.debug ("fileSize=" +
                           fileSize +
                           ", maxSize=" +
                            maxRolledFileSize +
                           " -> must roll files over.");
                super.out = rollFilesOver (this.primaryFile,
                                           this.filePattern,
                                           this.charsetName,
                                           this.maxRolledOverFiles,
                                           this.compressionType,
                                           this,
                                           this.callback);
            }
        }
    }

    /**
     * Move a file, converting any IOExceptions into IOExceptions.
     *
     * @param sourceFile      the file to move
     * @param targetFile      where it should go
     *
     * @throws IOExceptionExt  on error
     */
    private static void renameFile (File sourceFile, File targetFile)
        throws IOExceptionExt
    {
        log.debug ("Moving file \"" +
                   sourceFile.getName() +
                   "\" to \""   +
                   targetFile.getName() +
                   "\"");
        try
        {
            if (! sourceFile.renameTo (targetFile))
            {
                throw new IOExceptionExt (Package.BUNDLE_NAME,
                                          "RollingFileWriter.cantMoveFile",
                                          "Unable to move file \"{0}\" to " +
                                          "\"{1}\"",
                                          new Object[]
                                          {
                                              sourceFile.getPath(),
                                              targetFile.getPath()
                                          });
            }
        }

        catch (SecurityException ex)
        {
            throw new IOExceptionExt (Package.BUNDLE_NAME,
                                      "RollingFileWriter.cantMoveFile",
                                      "Unable to move file \"{0}\" to \"{1}\"",
                                      new Object[]
                                      {
                                          sourceFile.getPath(),
                                          targetFile.getPath()
                                      },
                                      ex);
        }
    }

    /**
     * Resolve the primary file name pattern, save the resulting File
     * object in the primaryFile instance variable, and open the file.
     * If the primary file exists, it's rolled over (backed up) and
     * a new one is opened.
     *
     * @param fileNamePattern    the file name pattern
     * @param charsetName        the name of the encoding to use, or null
     * @param maxRolledOverFiles max number of rolled-over files
     * @param compressionType    {@link Compression#COMPRESS_BACKUPS} to
     *                           compress backups,
     *                           {@link Compression#DONT_COMPRESS_BACKUPS} to
     *                           leave backups uncompressed
     * @param callback           callback to invoke on roll-over, or null
     *
     * @return an open Writer object
     *
     * @throws IOExceptionExt on error
     */
    private static Writer openPrimaryFile (String           fileNamePattern,
                                           String           charsetName,
                                           int              maxRolledOverFiles,
                                           Compression      compressionType,
                                           RolloverCallback callback)
        throws IOExceptionExt
    {
        File primaryFile = resolveFilePattern (fileNamePattern,
                                               null,
                                               maxRolledOverFiles,
                                               null);
        log.debug ("primaryFile=" + primaryFile.getPath());

        Writer w = null;

        if (primaryFile.exists())
        {
            log.debug ("Primary file exists. Rolling...");
            w = rollFilesOver (primaryFile,
                               fileNamePattern,
                               charsetName,
                               maxRolledOverFiles,
                               compressionType,
                               null,
                               callback);
        }

        else
        {
            log.debug ("Primary file does not exist.");
            w = openFile (primaryFile, charsetName);
        }

        return w;
    }

    /**
     * Open the specified log file for writing. Sets the "writer"
     * instance variable on success.
     *
     * @param file         The file to open
     * @param charsetName  the name of the encoding to use, or null
     *
     * @return the open file
     *
     * @throws IOExceptionExt  Failed to open file
     */
    private static Writer openFile (File file, String charsetName)
        throws IOExceptionExt
    {
        Writer result = null;

        try
        {
            if (charsetName != null)
            {
                result = new OutputStreamWriter (new FileOutputStream (file),
                                                 charsetName);
            }

            else
            {
                result = new FileWriter (file);
            }
        }

        catch (IOException ex)
        {
            throw new IOExceptionExt (Package.BUNDLE_NAME,
                                      "RollingFileWriter.cantOpenFile",
                                      "Unable to open file \"{0}\"",
                                      new Object[] {file.getPath()});
        }

        return result;
    }

    /**
     * Resolve a file pattern into a file.
     *
     * @param fileNamePattern    the pattern
     * @param index              the file index, or null for the primary file
     * @param maxRolledOverFiles max number of rolled-over files
     * @param compressionType    compression type, or null
     *
     * @return the File object
     *
     * @throws IOExceptionExt on error
     */
    private static File resolveFilePattern (String        fileNamePattern,
                                            Integer       index,
                                            int           maxRolledOverFiles,
                                            Compression   compressionType)
        throws IOExceptionExt
    {
        try
        {
            // Validate the pattern by expanding it to its primary file name.

            BackupIndexDereferencer deref =
                new BackupIndexDereferencer (index, maxRolledOverFiles);
            UnixShellVariableSubstituter sub =
                new UnixShellVariableSubstituter();

            sub.setHonorEscapes(false);
            String fileName = sub.substitute(fileNamePattern,
                                             deref,
                                             null,
                                             fileNamePattern);
            if (! deref.patternIsLegal())
            {
                throw new IOExceptionExt(Package.BUNDLE_NAME,
                                         "RollingFileWriter.badPattern",
                                         "File pattern \"{0}\" is missing " +
                                         "the \"$'{n}'\" marker.",
                                         new Object[] {fileNamePattern});
            }

            if (compressionType == Compression.COMPRESS_BACKUPS)
                fileName = fileName + GZIP_EXTENSION;

            return new File (fileName);
        }

        catch (VariableSubstitutionException ex)
        {
            throw new IOExceptionExt (ex);
        }
    }

    /**
     * Rolls the files over.
     *
     * @param primaryFile        primary file name
     * @param fileNamePattern    file name pattern
     * @param charsetName        encoding to use, or null
     * @param maxRolledOverFiles max number of rolled-over files
     * @param compressionType    {@link Compression#COMPRESS_BACKUPS} to
     *                           compress backups,
     *                           {@link Compression#DONT_COMPRESS_BACKUPS} to
     *                           leave backups uncompressed
     * @param rollingFileWriter  the open writer for the file being rolled
     * @param callback           callback to invoke on roll-over, or null
     *
     * @throws IOExceptionExt On error.
     */
    private static Writer rollFilesOver (File              primaryFile,
                                         String            fileNamePattern,
                                         String            charsetName,
                                         int               maxRolledOverFiles,
                                         Compression       compressionType,
                                         RollingFileWriter rollingFileWriter,
                                         RolloverCallback  callback)
        throws IOExceptionExt
    {
        log.debug ("rolling \"" + primaryFile.getPath() + "\"");

        // Ultimately, we're looking to roll over the current file, so
        // we may need to shift other rolled-over files out of the way.
        // It's possible to have gaps in the sequence (e.g., if someone
        // removed a file). For instance:
        //
        //    error.log error.log-0 error.log-1 error.log-3 error.log-5 ...
        //
        // We don't coalesce all the gaps at once. Instead, we just roll
        // files over until we fill the nearest gap. In the example above,
        // we'd move "error.log-1" to "error.log-2" (filling in the gap),
        // then move "error.log-0" to "error.log-1" and "error.log" to
        // "error.log-0". This is the most efficient approach, especially
        // if the number of saved files is large. Eventually, all the gaps
        // will fill in.

        int  firstGap       = -1;
        int  lastLegalIndex = maxRolledOverFiles - 1;
        int  i;

        for (i = 0; i < maxRolledOverFiles; i++)
        {
            File f = resolveFilePattern (fileNamePattern,
                                         i,
                                         maxRolledOverFiles,
                                         compressionType);

            if (! f.exists())
            {
                firstGap = i;
                break;
            }
        }

        log.debug ("firstGap(1)=" + firstGap);

        // At this point, we know whether there are any gaps. If there aren't,
        // we have to shift all files down by one (possibly tossing the
        // last one). If there are gaps, we just have to shift the files
        // ahead of the first gap.

        if (firstGap == -1)
            firstGap = lastLegalIndex;

        log.debug ("firstGap(2)=" + firstGap);

        for (i = firstGap - 1; i >= 0; i--)
        {
            File targetFile = resolveFilePattern (fileNamePattern,
                                                  i + 1,
                                                  maxRolledOverFiles,
                                                  compressionType);
            File sourceFile = resolveFilePattern (fileNamePattern,
                                                  i,
                                                  maxRolledOverFiles,
                                                  compressionType);

            // Target file can exist if there was no gap (i.e., we're at
            // the end of all the possible files).

            if (targetFile.exists())
            {
                log.debug ("Removing file \"" + targetFile.getPath() + "\"");

                try
                {
                    targetFile.delete();
                }

                catch (SecurityException ex)
                {
                    throw new IOExceptionExt
                        (Package.BUNDLE_NAME,
                         "RollingFileWriter.cantDeleteFile",
                         "Can't delete file \"{0}\"",
                         new Object[] {targetFile.getPath()});
                }
            }

            // Attempt to move the source file to the target slot.

            renameFile (sourceFile, targetFile);
        }

        String rollOverMsg = null;

        if (rollingFileWriter != null)
        {
            // Close the current file, and rename it to the 0th rolled-over
            // file. If there's a callback defined, use it to get a
            // rollover message, and write that message first.

            if (callback != null)
            {
                rollOverMsg = callback.getRollOverMessage();
                if (rollOverMsg != null)
                {
                    log.debug ("Appending roll-over message \"" +
                               rollOverMsg +
                               "\" to full primary file \"" +
                               primaryFile +
                               "\"");

                    // Calling super.println (anything) will fail, because
                    // we've overridden the methods. But we haven't
                    // overridden the write() methods, so we can use them
                    // to do what we want.

                    rollingFileWriter.printlnNoRoll (rollOverMsg);
                }
            }

            log.debug ("Closing full primary file \"" + primaryFile + "\".");
            rollingFileWriter.flush();
            rollingFileWriter.close();
        }

        File targetFile = resolveFilePattern (fileNamePattern,
                                              0,
                                              maxRolledOverFiles,
                                              null);
        renameFile (primaryFile, targetFile);

        if (compressionType == Compression.COMPRESS_BACKUPS)
            gzipFile (targetFile);

        // Finally, open the file. Add the same 'rolled over' message to
        // the top of this one.

        log.debug ("Reopening \"" + primaryFile + "\"");
        Writer result = openFile (primaryFile, charsetName);
        if (rollOverMsg != null)
        {
            try
            {
                log.debug ("Writing roll-over message \"" +
                           rollOverMsg +
                           "\" to top of new primary file \"" +
                           primaryFile +
                           "\"");
                result.write (rollOverMsg);
                result.write (newline);
                result.flush();
            }

            catch (IOException ex)
            {
                throw new IOExceptionExt (ex);
            }
        }

        return result;
    }

    /**
     * Gzip a file.
     *
     * @param file  the file to gzip
     *
     * @throws IOExceptionExt on error
     */
    private static void gzipFile (File file)
        throws IOExceptionExt
    {
        try
        {
            InputStream is = new FileInputStream (file);
            OutputStream os = new GZIPOutputStream
                                   (new FileOutputStream (file.getPath() +
                                                          GZIP_EXTENSION));
            FileUtil.copyStream (is, os);
            is.close();
            os.close();

            if (! file.delete())
            {
                throw new IOExceptionExt (Package.BUNDLE_NAME,
                                          "RollingFileWriter.cantDeleteFile",
                                          "Can't delete file \"{0}\"",
                                          new Object[] {file.getPath()});
            }
        }

        catch (IOException ex)
        {
            throw new IOExceptionExt (Package.BUNDLE_NAME,
                                      "RollingFileWriter.cantGzipFile",
                                      "Can't gzip file \"{0}\"",
                                      new Object[] {file.getPath()});
        }
    }
}
