package org.clapper.util.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * <tt>XMLWriter</tt> is a filtering <tt>Writer</tt> class, designed
 * to be used to write XML output. Basically, it performs some simple-minded
 * indentation, to make the written XML a little more readable.
 *
 * @see java.io.Writer
 */
public class XMLWriter extends Writer
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The actual writer, wrapped in a PrintWriter
     */
    private PrintWriter out;

    /**
     * The previously written character, for keeping track of state
     */
    private char prev = '\0';

    /**
     * The indentation level
     */
    private int indentation = 0;

    /**
     * Whether the last element written was an XML processing instruction
     * or not. We don't indent after writing in XML PI.
     */
    private boolean lastWasPI = true;

    /*----------------------------------------------------------------------*\
                            Public Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>XMLWriter</tt> that will write its output to the
     * specified <tt>Writer</tt> object.
     *
     * @param out  where the output should really go
     */
    public XMLWriter (Writer out)
    {
        this.out = new PrintWriter (out);
    }
    /**
     * Construct a new <tt>XMLWriter</tt> that will write its output to the
     * specified <tt>PrintWriter</tt> object.
     *
     * @param out  where the output should really go
     */
    public XMLWriter (PrintWriter out)
    {
        this.out = out;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Close this <tt>Writer</tt>
     *
     * @throws IOException I/O error
     */
    public void close()
        throws IOException
    {
        out.close();
    }

    /**
     * Flush this <tt>Writer</tt>
     *
     * @throws IOException I/O error
     */
    public void flush()
        throws IOException
    {
        out.flush();
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param ch   array of characters
     * @param off  offset from which to start writing characters
     * @param len  number of characters to write
     *
     * @throws IOException  I/O error
     */
    public void write (char[] ch, int off, int len)
        throws IOException
    {
        while (off < len)
        {
            char c = ch[off++];

            switch (c)
            {
                case '<':
                    // Don't write here. It's buffered until the
                    // "default" case. How we indent depends on the
                    // character that follows the '<'.
                    break;

                case '>':
                    if (prev == '/')
                        indentation--;

                    lastWasPI = (prev == '?');
                    out.write (c);
                    out.write ('\n');
                    break;

                default:
                    if (prev == '<')
                    {
                        if (c == '/') // Closing element. Un-indent.
                            indentation--;
                        else if (! lastWasPI)
                            indentation++;

                        indent (indentation);
                        out.write (prev);
                    }

                    out.write (c);
                    break;
            }

            prev = c;
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void indent (int indentation)
        throws IOException
    {
        while (indentation-- > 0)
            out.write ("    ");
    }
}
