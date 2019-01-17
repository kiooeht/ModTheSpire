package org.clapper.util.io;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.clapper.util.text.TextUtil;

/**
 * <p>The <tt>JustifyTextWriter</tt> class is a filter class. A
 * <tt>JustifyTextWriter</tt> object wraps a <tt>Writer</tt> or
 * <tt>OutputStream</tt> object, filtering output to the wrapped object so
 * that output lines are right-justified, left-justified or centered within
 * a field. (Strictly speaking, there's no need for this class to support
 * left-justifying lines, since that's the default. It's supported here
 * solely for completeness.)</p>
 *
 * <p>For instance, given the following input:</p>
 *
 * <blockquote><pre>
 * This is the first line.
 * This, a longer line, is the second.
 * </pre></blockquote>
 *
 * <p>A <tt>JustifyTextWriter</tt> that's right-justifying lines in a
 * 50-character field would produce:</p>
 *
 * <blockquote><pre> *
 *                           This is the first line.| &lt;--
 *               This, a longer line, is the second.| &lt;--
 * </pre></blockquote>
 *
 * <p>(The arrows and vertical bars would obviously not be output. They're
 * there to show where the field ends.)</p>
 *
 * <p>A <tt>JustifyTextWriter</tt> that's centering lines in a
 * 50-character field would produce:</p>
 *
 * <blockquote><pre>
 *              This is the first line.             | &lt;--
 *        This, a longer line, is the second.       | &lt;--
 * </pre></blockquote>
 *
 * <p>For an interesting effect, consider wrapping a <tt>JustifyTextWriter</tt>
 * inside a {@link WordWrapWriter} to get word-wrapped, justified output. For
 * instance, to wrap words on a 60-character boundary, and then center them
 * in the same field, use code like this:</p>
 *
 * <blockquote><pre>
 * final int WIDTH = 60;
 * WordWrapWriter out = new WordWrapWriter (new JustifyWriter (JustifyWriter.CENTER, WIDTH), WIDTH);
 * </pre></blockquote>
 *
 * <p><b>Notes</b></p>
 *
 * <ol>
 *   <li> The class does not do any special processing of tab characters.
 *        Embedded tab characters and newline characters can have surprising
 *        (and unwanted) effects on the rendered output.
 *   <li> Technically, this class probably ought to extend the
 *        <tt>java.io.FilterWriter</tt> class, since performs filtering
 *        of output written to another underlying <tt>Writer</tt> or
 *        <tt>OutputStream</tt>. However, I wanted <tt>JustifyTextWriter</tt>
 *        to be polymorphically compatible with <tt>java.io.PrintWriter</tt>,
 *        so it could be passed to methods expecting a <tt>PrintWriter</tt>
 *        object; there are more methods that expect a <tt>PrintWriter</tt>
 *        than there are methods that expect a <tt>FilterWriter</tt>.
 * </ol>
 *
 * @see WordWrapWriter
 * @see TextUtil#rightJustifyString(String,int)
 * @see TextUtil#leftJustifyString(String,int)
 * @see TextUtil#centerString(String,int)
 * @see java.io.Writer
 * @see java.io.PrintWriter
 */
public class JustifyTextWriter extends PrintWriter
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * The default line length.
     */
    public static final int DEFAULT_LINE_LENGTH =
                                       WordWrapWriter.DEFAULT_LINE_LENGTH;

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Where the output is really going.
     */
    private PrintWriter writer = null;

    /**
     * The line length to use.
     */
    private int lineLength = DEFAULT_LINE_LENGTH;

    /**
     * Current line being assembled. println() consumes this buffer.
     */
    private StringBuffer buffer = new StringBuffer();

    /**
     * Justification type
     */
    private JustifyStyle justification = JustifyStyle.LEFT_JUSTIFY;

    /*----------------------------------------------------------------------*\
                               Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Build an <tt>JustifyTextWriter</tt> object that will write its
     * output to the specified <tt>Writer</tt> object, using the default
     * line length of 79.
     *
     * @param output         Where the output goes.
     * @param justification  How to justify the output
     *
     * @see #DEFAULT_LINE_LENGTH
     * @see #JustifyTextWriter(Writer,JustifyStyle,int)
     * @see #JustifyTextWriter(PrintWriter,JustifyStyle)
     * @see #JustifyTextWriter(OutputStream,JustifyStyle)
     * @see JustifyStyle
     * @see java.io.Writer
     */
    public JustifyTextWriter (Writer output, JustifyStyle justification)
    {
        this (new PrintWriter (output), justification, DEFAULT_LINE_LENGTH);
    }

    /**
     * Build an <tt>JustifyTextWriter</tt> object that will write its
     * output to the specified <tt>PrintWriter</tt> object, using the default
     * line length of 79.
     *
     * @param output         Where the output goes.
     * @param justification  How to justify the output
     *
     * @see #DEFAULT_LINE_LENGTH
     * @see #JustifyTextWriter(Writer,JustifyStyle)
     * @see #JustifyTextWriter(PrintWriter,JustifyStyle,int)
     * @see #JustifyTextWriter(OutputStream,JustifyStyle)
     * @see JustifyStyle
     * @see java.io.Writer
     */
    public JustifyTextWriter (PrintWriter  output,
                              JustifyStyle justification)
    {
        this (output, justification, DEFAULT_LINE_LENGTH);
    }

    /**
     * Build an <tt>JustifyTextWriter</tt> object that will write its
     * output to the specified <tt>OutputStream</tt> object, using the
     * default line length of 79.
     *
     * @param output         Where the output goes.
     * @param justification  How to justify the output
     *
     * @see #DEFAULT_LINE_LENGTH
     * @see #JustifyTextWriter(OutputStream,JustifyStyle,int)
     * @see #JustifyTextWriter(Writer,JustifyStyle)
     * @see #JustifyTextWriter(PrintWriter,JustifyStyle)
     * @see JustifyStyle
     * @see java.io.OutputStream
     */
    public JustifyTextWriter (OutputStream output,
                              JustifyStyle justification)
    {
        this (output, justification, DEFAULT_LINE_LENGTH);
    }

    /**
     * Build an <tt>JustifyTextWriter</tt> object that will write its
     * output to the specified <tt>Writer</tt> object, using the specified
     * line length.
     *
     * @param output         Where the output goes.
     * @param justification  How to justify the output
     * @param lineLength     The desired line length.
     *
     * @see #DEFAULT_LINE_LENGTH
     * @see #JustifyTextWriter(Writer,JustifyStyle)
     * @see #JustifyTextWriter(PrintWriter,JustifyStyle,int)
     * @see #JustifyTextWriter(OutputStream,JustifyStyle,int)
     * @see JustifyStyle
     * @see java.io.Writer
     */
    public JustifyTextWriter (Writer       output,
                              JustifyStyle justification,
                              int          lineLength)
    {
        this (new PrintWriter (output), justification, lineLength);
    }

    /**
     * Build an <tt>JustifyTextWriter</tt> object that will write its
     * output to the specified <tt>PrintWriter</tt> object, using the
     * specified line length.
     *
     * @param output         Where the output goes.
     * @param justification  How to justify the output
     * @param lineLength     The desired line length.
     *
     * @see #DEFAULT_LINE_LENGTH
     * @see #JustifyTextWriter(PrintWriter,JustifyStyle)
     * @see #JustifyTextWriter(Writer,JustifyStyle,int)
     * @see #JustifyTextWriter(OutputStream,JustifyStyle,int)
     * @see JustifyStyle
     * @see java.io.Writer
     */
    public JustifyTextWriter (PrintWriter  output,
                              JustifyStyle justification,
                              int          lineLength)
    {
        super (output);
        writer = output;
        setLineLength (lineLength);
        setJustification (justification);
    }

    /**
     * Build an <tt>JustifyTextWriter</tt> object that will write its
     * output to the specified <tt>OutputStream</tt> object, using the
     * specified line length.
     *
     * @param output         Where the output goes.
     * @param justification  How to justify the output
     * @param lineLength     The desired line length.
     *
     * @see #DEFAULT_LINE_LENGTH
     * @see #JustifyTextWriter(OutputStream,JustifyStyle)
     * @see #JustifyTextWriter(Writer,JustifyStyle,int)
     * @see #JustifyTextWriter(PrintWriter,JustifyStyle,int)
     * @see JustifyStyle
     * @see java.io.Writer
     */
    public JustifyTextWriter (OutputStream output,
                              JustifyStyle justification,
                              int          lineLength)
    {
        super (output);
        writer = new PrintWriter (output);
        setLineLength (lineLength);
        setJustification (justification);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Flush the stream and check its error state.
     */
    public boolean checkError()
    {
        return super.checkError();
    }

    /**
     * Close the stream, flushing it first. Closing a previously-closed
     * stream has no effect.
     */
    public void close()
    {
        super.close();
    }

    /**
     * Flush the stream. If the stream has saved any characters from the
     * various write() methods in a buffer, write them immediately to their
     * intended destination. Then, if that destination is another character
     * or byte stream, flush it. Thus one flush() invocation will flush all
     * the buffers in a chain of Writers and OutputStreams.
     */
    public void flush()
    {
        flushBufferedLine();
        writer.flush();
    }

    /**
     * Retrieve the current justification style.
     *
     * @return The current justification style
     *
     * @see #setJustification
     */
    public JustifyStyle getJustification()
    {
        return justification;
    }


    /**
     * Set or change the current justification style.
     *
     * @param style The new justification style
     *
     * @see #setJustification
     */
    public void setJustification (JustifyStyle style)
    {
        justification = style;
    }

    /**
     * Retrieve the current line length.
     *
     * @return The line length.
     */
    public int getLineLength()
    {
        return lineLength;
    }

    /**
     * Set the line length.
     *
     * @param newLineLength The new line length to use. A value of 0
     *                      disables wrapping.
     *
     * @throws IndexOutOfBoundsException the value is negative
     */
    public void setLineLength (int newLineLength)
        throws IndexOutOfBoundsException
    {
        if (newLineLength < 0)
        {
            throw new IndexOutOfBoundsException ("Line length of " +
                                                 newLineLength +
                                                 " is negative.");
        }

        lineLength = newLineLength;
    }

    /**
     * Print a boolean.
     *
     * @param b  The boolean to print
     */
    public void print (boolean b)
    {
        Boolean B = new Boolean (b);

        write (B.toString());
    }

    /**
     * Print a character.
     *
     * @param c  The character to print
     */
    public void print (char c)
    {
        write (c);
    }

    /**
     * Print an array of characters.
     *
     * @param s  The array of characters to print
     */
    public void print (char s[])
    {
        write (s, 0, s.length);
    }

    /**
     * Print a double.
     *
     * @param d  The double floating point number to print
     */
    public void print (double d)
    {
        Double D = new Double (d);

        write (D.toString());
    }

    /**
     * Print a float.
     *
     * @param f  The floating point number to print
     */
    public void print (float f)
    {
        Float F = new Float (f);

        write (F.toString());
    }

    /**
     * Print an integer.
     *
     * @param i  The integer to print
     */
    public void print (int i)
    {
        Integer I = new Integer (i);

        write (I.toString());
    }

    /**
     * Print a long.
     *
     * @param l  The long to print
     */
    public void print (long l)
    {
        Long L = new Long (l);

        write (L.toString());
    }

    /**
     * Print a short.
     *
     * @param s  The short to print
     */
    public void print (short s)
    {
        Short S = new Short (s);

        write (S.toString());
    }

    /**
     * Print a String.
     *
     * @param s  The String to print.
     */
    public void print (String s)
    {
        write (s);
    }

    /**
     * Print an Object.
     *
     * @param x The object to print.
     */
    public void print (Object x)
    {
        write (x.toString());
    }

    /**
     * End the current line.
     */
    public void println()
    {
        flushBufferedLine();
        writer.println();
    }

    /**
     * Print a boolean and finish the line.
     *
     * @param b  The boolean to print
     */
    public void println (boolean b)
    {
        Boolean B = new Boolean (b);

        println (B.toString());
    }

    /**
     * Print a character and finish the line.
     *
     * @param c  The character to print
     */
    public void println (char c)
    {
        println (c);
    }

    /**
     * Print an array of characters.
     *
     * @param s  The array of characters to print
     */
    public void println (char s[])
    {
        for (int i = 0; i < s.length; i++)
            print (s[i]);
        println();
    }

    /**
     * Print a double and finish the line.
     *
     * @param d  The double floating point number to print
     */
    public void println (double d)
    {
        Double D = new Double (d);

        println (D.toString());
    }

    /**
     * Print a float and finish the line.
     *
     * @param f  The floating point number to print
     */
    public void println (float f)
    {
        Float F = new Float (f);

        println (F.toString());
    }

    /**
     * Print an integer.
     *
     * @param i  The integer to print
     */
    public void println (int i)
    {
        Integer I = new Integer (i);

        println (I.toString());
    }

    /**
     * Print a long and finish the line.
     *
     * @param l  The long to print
     */
    public void println (long l)
    {
        Long L = new Long (l);

        println (L.toString());
    }

    /**
     * Print a short and finish the line.
     *
     * @param s  The short to print
     */
    public void println (short s)
    {
        Short S = new Short (s);

        println (S.toString());
    }

    /**
     * Print a String and finish the line.
     *
     * @param s  The String to print.
     */
    public void println (String s)
    {
        print (s);
        println();
    }

    /**
     * Print an Object and finish the line.
     *
     * @param x The object to print.
     */
    public void println (Object x)
    {
        println (x.toString());
    }

    /**
     * Write a single character. This method is called by all other
     * <tt>write()</tt>, <tt>print()</tt> and <tt>println()</tt> methods.
     * Thus, subclasses that wish to preprocess (or intercept) the output
     * only have to override this method.
     *
     * @param c Character to write
     */
    public void write (int c)
    {
        buffer.append ((char) c);
    }

    /**
     * Write a portion of an array of characters to the underlying
     * output object. Assumes the characters represent the start of
     * a new line. Each line is indented according to this object's
     * defined indentation level.
     *
     * @param cbuf Array of characters
     * @param off  Offset from which to start writing characters
     * @param len  Number of characters to write
     */
    public void write (char cbuf[], int off, int len)
    {
        for (; (off < cbuf.length) && (len > 0); len--, off++)
            write (cbuf[off]);
    }

    /**
     * Write a portion of a String of characters to the underlying
     * output object. Assumes the characters represent the start of
     * a new line. Each line is indented according to this object's
     * defined indentation level.
     *
     * @param s    String from which to write
     * @param off  Offset from which to start writing characters
     * @param len  Number of characters to write
     */
    public void write (String s, int off, int len)
    {
        char[] cbuf = s.toCharArray();

        this.write (cbuf, off, len);
    }

    /**
     * Write a string.
     *
     * @param s String to write
     */
    public void write (String s)
    {
        char[] cbuf = s.toCharArray();

        this.write (cbuf, 0, cbuf.length);
    }

    /**
     * Write an array of characters.
     *
     * @param cbuf Array of characters to write
     */
    public void write (char[] cbuf)
    {
        this.write (cbuf, 0, cbuf.length);
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Flushes any characters in the buffered output line, then clears the
     * buffer.
     */
    private synchronized void flushBufferedLine()
    {
        if (buffer.length() > 0)
        {
            String s = buffer.toString();

            switch (justification)
            {
                case LEFT_JUSTIFY:
                    writer.print (s);
                    break;

                case RIGHT_JUSTIFY:
                    writer.print (TextUtil.rightJustifyString (s, lineLength));
                    break;

                case CENTER:
                    writer.print (TextUtil.centerString (s, lineLength));
                    break;
            }

            buffer.setLength (0);
        }
    }
}
