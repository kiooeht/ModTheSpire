package org.clapper.util.text;

import java.util.Collection;

import java.io.StringReader;
import java.io.PushbackReader;
import java.io.IOException;

/**
 * Abstract base class for <tt>XStringBuffer</tt> and <tt>XStringBuilder</tt>.
 * This class exists to share common functionality, pushing appropriate details
 * to the underlying implementation.
 *
 * @see XStringBuffer
 * @see XStringBuilder
 * @see java.lang.StringBuffer
 * @see java.lang.StringBuilder
 */
public abstract class XStringBufBase implements CharSequence, Appendable
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * The character that denotes the start of a metacharacter sequence.
     */
    public static final char METACHAR_SEQUENCE_START = '\\';

    /*----------------------------------------------------------------------*\
                             Private Variables
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct an empty <tt>XStringBuffer</tt> object with a default
     * initial capacity (the same initial capacity as an empty
     * <tt>StringBuffer</tt> object).
     */
    XStringBufBase()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                          Abstract Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Copy the some or all of the contents of the buffer into a character
     * array. The first character to be copied is at index
     * <tt>srcBegin</tt>; the last character to be copied is at index
     * (<tt>srcEnd - 1</tt>). The total number of characters to be
     * copied is (<tt>srcEnd - srcBegin</tt>). The characters are
     * copied into the subarray of <tt>dst</tt> starting at index
     * <tt>dstBegin</tt> and ending at index
     * <tt>(dstBegin + (srcEnd - srcBegin) - 1)</tt>.
     *
     * @param srcBegin  Start copy from this offset in the string buffer
     * @param srcEnd    Stop copy from this offset in the string buffer
     * @param dst       Where to copy the characters.
     * @param dstBegin  Offset into <tt>dst</tt>
     *
     * @throws IndexOutOfBoundsException invalid index
     */
    public abstract void getChars (int   srcBegin,
                                   int   srcEnd,
                                   char  dst[],
                                   int   dstBegin)
        throws IndexOutOfBoundsException;

    /*----------------------------------------------------------------------*\
                        Abstract Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the underlying buffer (e.g., <tt>StringBuffer</tt>,
     * <tt>StringBuilder</tt>) as an <tt>Appender</tt> object.
     *
     * @return the <tt>Appender</tt>
     */
    protected abstract Appendable getBufferAsAppendable();

    /**
     * Get the underlying buffer (e.g., <tt>StringBuffer</tt>,
     * <tt>StringBuilder</tt>) as a <tt>CharSequence</tt> object.
     *
     * @return the <tt>Appendable</tt>
     */
    protected abstract CharSequence getBufferAsCharSequence();

    /**
     * Get a new instance of the underlying buffer type (e.g.,
     * <tt>StringBuffer</tt>, <tt>StringBuilder</tt>) as a
     * <tt>CharSequence</tt> object.
     *
     * @return the <tt>Appendable</tt>
     */
    protected abstract CharSequence newBufferAsCharSequence();

    /**
     * Remove the characters in a substring of this
     * <tt>XStringBuffer</tt>. The substring begins at the specified
     * <tt>start</tt> and extends to the character at index
     * <tt>end - 1</tt>, or to the end of the string, if no such
     * character exists. If <tt>start</tt> is equal to <tt>end</tt>,
     * no changes are made.
     *
     * @param start  The beginning index, inclusive
     * @param end    The ending index, exclusive
     *
     * @throws IndexOutOfBoundsException  if <tt>start</tt> is negative,
     *                                    greater than <tt>length()</tt>,
     *                                    or greater than <tt>end</tt>
     */
    protected abstract void deleteCharacters (int start, int end)
        throws IndexOutOfBoundsException;

    /**
     * Insert a single character into the buffer at a specified position.
     * Note that an insertion operation may push characters off the end of
     * the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param ch     The character to insert
     */
    protected abstract void insertCharacter (int index, char ch);

    /**
     * Insert characters from a character array into the buffer at a
     * specified position. Note that an insertion operation may push
     * characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param chars  The character(s) to insert
     * @param offset The starting position in the <tt>chars</tt> array
     * @param len    The number of characters to insert
     */
    protected abstract void insertCharacters (int  index,
                                              char chars[],
                                              int  offset,
                                              int  len);

    /**
     * Replace the characters in a substring of this buffer with
     * characters in the specified <tt>String</tt>. The substring
     * begins at the specified <tt>start</tt> and extends to the
     * character at index <tt>end - 1</tt>, or to the end of the
     * <tt>XStringBuffer</tt> if no such character exists. First the
     * characters in the substring are removed and then the specified
     * <tt>String</tt> is inserted at <tt>start</tt>. (The
     * <tt>XStringBuffer</tt> will be lengthened to accommodate the
     * specified <tt>String</tt> if necessary.)
     *
     * @param start  The beginning index, inclusive
     * @param end    The ending index, exclusive
     * @param str    The string that will replace the previous contents
     *
     * @throws IndexOutOfBoundsException if <tt>start</tt> is negative,
     *                                   or greater than
     *                                   <tt>length()</tt>, or greater
     *                                   than <tt>end</tt>
     */
    protected abstract void replaceString (int start, int end, String str)
        throws IndexOutOfBoundsException;

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Append the string representation of a <tt>boolean</tt> value to
     * the buffer.
     *
     * @param val  The boolean value.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (boolean val)
    {
        try
        {
            getBufferAsAppendable().append (String.valueOf (val));
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Append the specified character to the buffer. If the buffer is already
     * at its maximum length, the character is silently ignored.
     *
     * @param c  The character to append.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (char c)
    {
        try
        {
            getBufferAsAppendable().append (c);
        }

        catch (IOException ex)
        {
        }

        return this;
    }

    /**
     * Append the specified array of characters to the buffer. This method
     * appends only as much of the string as will fit without causing the
     * buffer to exceed its maximum length.
     *
     * @param chars  The characters to append.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (char chars[])
    {
        for (int i = 0; i < chars.length; i++)
            append (chars[i]);

        return this;
    }

    /**
     * Append the specified characters in a character array to the buffer.
     * This method appends only as much of the string as will fit without
     * causing the buffer to exceed its maximum length.
     *
     * @param chars  The characters to append.
     * @param offset The index of the first character to append
     * @param len    The maximum number of characters to append
     *
     * @return a reference to this object
     */
    public XStringBufBase append (char chars[], int offset, int len)
    {
        Appendable buf = getBufferAsAppendable();

        while (offset < len)
        {
            try
            {
                buf.append (chars[offset++]);
            }

            catch (IOException ex)
            {
                // Shouldn't happen here.
            }
        }

        return this;
    }

    /**
     * Append the string representation of a <tt>double</tt> value to
     * the buffer.
     *
     * @param val  The double value.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (double val)
    {
        try
        {
            getBufferAsAppendable().append (String.valueOf (val));
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Append the string representation of a <tt>float</tt> value to
     * the buffer.
     *
     * @param val  The float value.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (float val)
    {
        try
        {
            getBufferAsAppendable().append (String.valueOf (val));
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Append the string representation of a <tt>int</tt> value to
     * the buffer.
     *
     * @param val  The int value.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (int val)
    {
        try
        {
            getBufferAsAppendable().append (String.valueOf (val));
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Append the string representation of a <tt>long</tt> value to
     * the buffer.
     *
     * @param val  The long value.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (long val)
    {
        try
        {
            getBufferAsAppendable().append (String.valueOf (val));
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Append the string representation of an object to the buffer.
     *
     * @param obj  The object whose string value is to be appended.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (Object obj)
    {
        try
        {
            getBufferAsAppendable().append (obj.toString());
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Append the string representation of a <tt>short</tt> value to
     * the buffer.
     *
     * @param val  The short value.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (short val)
    {
        try
        {
            getBufferAsAppendable().append (String.valueOf (val));
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Append the specified string to the buffer. This method appends only
     * as much of the string as will fit without causing the buffer to
     * exceed its maximum length.
     *
     * @param s  The string to append.
     *
     * @return a reference to this object
     */
    public XStringBufBase append (String s)
    {
        try
        {
            getBufferAsAppendable().append (s);
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Append the entire contents of the specified <tt>CharSequence</tt> to
     * the buffer. This method appends only as much of the
     * <tt>CharSequence</tt> as will fit without causing the buffer to
     * exceed its maximum length.
     *
     * @param csq  The <tt>CharSequence</tt> to append
     *
     * @return a reference to this object
     *
     * @see #append(CharSequence,int,int)
     */
    public XStringBufBase append (CharSequence csq)
    {
        try
        {
            getBufferAsAppendable().append (csq);
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Append a subsequence of the specified <tt>CharSequence</tt> to the
     * buffer. This method appends only as much of the subsequence as will
     * fit without causing the buffer to exceed its maximum length.
     *
     * @param csq    The <tt>CharSequence</tt> to append
     * @param start  The starting index of the subsequence
     * @param end    One past the ending index of the subsequence
     *
     * @return a reference to this object
     *
     * @see #append(CharSequence)
     */
    public XStringBufBase append (CharSequence csq, int start, int end)
    {
        try
        {
            getBufferAsAppendable().append (csq, start, end);
        }

        catch (IOException ex)
        {
            // Shouldn't happen here.
        }

        return this;
    }

    /**
     * Return the character at a specified index in the buffer. Indexes begin
     * at 0. (i.e., The first character in the buffer has index 0.)
     *
     * @param index  The index of the character to return
     *
     * @return  The character at the specified index.
     *
     * @throws IndexOutOfBoundsException if <tt>index</tt> is negative or
     *                                   greater than or equal to
     *                                   <tt>length()</tt>
     */
    public char charAt (int index)
        throws IndexOutOfBoundsException
    {
        return getBufferAsCharSequence().charAt (index);
    }

    /**
     * Delete the first occurrence of a given substring in the buffer.
     * with another substring.
     *
     * @param substring   The substring to find and replace
     *
     * @return <tt>true</tt> if the replacement succeeded,
     *         <tt>false</tt> otherwise.
     */
    public boolean delete (String substring)
    {
        int      i;
        boolean  deleted = false;

        i = indexOf (substring);
        if (i > -1)
        {
            deleteCharacters (i, i + substring.length());
            deleted = true;
        }

        return deleted;
    }

    /**
     * Removes all characters from the buffer leaving it empty.
     */
    public void clear()
    {
        delete (0, length());
    }

    /**
     * Remove the characters in a substring of this
     * <tt>XStringBuffer</tt>. The substring begins at the specified
     * <tt>start</tt> and extends to the character at index
     * <tt>end - 1</tt>, or to the end of the string, if no such
     * character exists. If <tt>start</tt> is equal to <tt>end</tt>,
     * no changes are made.
     *
     * @param start  The beginning index, inclusive
     * @param end    The ending index, exclusive
     *
     * @return This object
     *
     * @throws IndexOutOfBoundsException  if <tt>start</tt> is negative,
     *                                    greater than <tt>length()</tt>,
     *                                    or greater than <tt>end</tt>
     */
    public XStringBufBase delete (int start, int end)
        throws IndexOutOfBoundsException
    {
        deleteCharacters (start, end);
        return this;
    }

    /**
     * <p>Replaces certain characters in the string buffer with Java
     * metacharacter ("backslash") sequences.</p>
     *
     * <ul>
     *    <li> A horizontal tab is replaced with <tt>\t</tt>.
     *    <li> A line feed is replaced with <tt>\n</tt>.
     *    <li> A carriage return is replaced with <tt>\r</tt>.
     *    <li> A form feed is replaced with <tt>\f</tt>.
     *    <li> A backslash is replaced by two backslashes.
     *    <li> Nonprintable characters are replaced with a
     *         <tt>&#92;u</tt><i>xxxx</i> sequence.
     * </ul>
     *
     * <p>This method uses the same definition of "non-printable" as
     * {@link TextUtil#isPrintable}.</p>
     *
     * @param start  The beginning index, inclusive
     * @param end    The ending index, exclusive
     *
     * @throws StringIndexOutOfBoundsException if <tt>start</tt> is negative,
     *                                         or greater than
     *                                         <tt>length()</tt>, or greater
     *                                         than <tt>end</tt>
     * @throws IOException                     I/O exception
     *
     * @see #encodeMetacharacters()
     * @see #decodeMetacharacters(int, int)
     */
    public void encodeMetacharacters (int start, int end)
        throws IndexOutOfBoundsException,
               IOException
    {
        char           chars[] = toString().toCharArray();
        int            i       = 0;
        StringBuilder  scratch = new StringBuilder();

        clear();
        try
        {
            while (i < start)
                append (chars[i++]);

            while (i < end)
                append (encodeOneMetacharacter (chars[i++], scratch));

            while (i < chars.length)
                append (chars[i++]);
        }

        catch (ArrayIndexOutOfBoundsException ex)
        {
            throw new StringIndexOutOfBoundsException (String.valueOf (i));
        }
    }

    /**
     * A version of {@link #encodeMetacharacters(int,int) encodeMetacharacters}
     * that processes the entire string buffer. Calling this method is
     * equivalent to:
     *
     * <blockquote>
     * <pre>
     * buf.encodeMetacharacters (0, buf.length())
     * </pre>
     * </blockquote>
     *
     * @see #encodeMetacharacters(int, int)
     * @see #decodeMetacharacters()
     */
    public void encodeMetacharacters()
    {
        try
        {
            encodeMetacharacters (0, this.length());
        }

        catch (IndexOutOfBoundsException ex)
        {
            // Should never happen
        }

        catch (IOException ex)
        {
            // Should never happen
        }
    }

    /**
     * Replaces any metacharacter sequences in a portion of the string
     * buffer (such as those produced by {@link #encodeMetacharacters()}
     * with their actual characters.
     *
     * @param start  The beginning index, inclusive
     * @param end    The ending index, exclusive
     *
     * @throws StringIndexOutOfBoundsException if <tt>start</tt> is negative,
     *                                         or greater than
     *                                         <tt>length()</tt>, or greater
     *                                         than <tt>end</tt>
     *
     * @see #decodeMetacharacters()
     * @see #encodeMetacharacters(int,int)
     */
    public void decodeMetacharacters (int start, int end)
        throws StringIndexOutOfBoundsException
    {
        char           chars[] = toString().toCharArray();
        int            i       = 0;
        StringBuilder  newBuf  = new StringBuilder();

        try
        {
            // Copy verbatim the region of characters prior to "start"

            while (i < start)
                newBuf.append (chars[i++]);

            // Process the region. First, allocate a PushbackReader than
            // can handle up to 5 characters (4 characters for a Unicode
            // code, plus the preceding "u").

            String         region = new String (chars, i, end - i);
            StringReader   sr     = new StringReader (region);
            PushbackReader pb     = new PushbackReader (sr, 5);

            // Now, process the region.

            for (;;)
            {
                int c;

                if ((c = pb.read()) == -1)
                    break;

                if (c == METACHAR_SEQUENCE_START)
                {
                    if ((c = pb.read()) == -1)
                    {
                        // Incomplete metacharacter sequence at end of
                        // region. Just pass along the backslash as is.

                        newBuf.append ((char) c);
                    }

                    else
                    {
                        c = decodeMetacharacter (c, pb);
                        if (c == -1)
                            break;

                        if (c == -2) // Bad unicode sequence
                            newBuf.append (METACHAR_SEQUENCE_START);
                        else
                            newBuf.append ((char) c);
                    }
                }

                else
                {
                    newBuf.append ((char) c);
                }
            }

            // Copy verbatim the region of characters after "end"

            i = end;
            while (i < chars.length)
                newBuf.append (chars[i++]);
        }

        catch (ArrayIndexOutOfBoundsException ex)
        {
            throw new StringIndexOutOfBoundsException (String.valueOf (i));
        }

        catch (IOException ex)
        {
            throw new StringIndexOutOfBoundsException();
        }

        clear();
        append (newBuf.toString());
    }

    /**
     * A version of {@link #decodeMetacharacters(int,int) decodeMetacharacters}
     * that processes the entire string buffer. Calling this method is
     * equivalent to:
     *
     * <blockquote>
     * <pre>
     * buf.decodeMetacharacters (0, buf.length())
     * </pre>
     * </blockquote>
     *
     * @see #encodeMetacharacters(int, int)
     * @see #decodeMetacharacters()
     */
    public void decodeMetacharacters()
    {
        try
        {
            decodeMetacharacters (0, this.length());
        }

        catch (StringIndexOutOfBoundsException ex)
        {
            // Should never happen
        }
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified character. If a character with value <tt>ch</tt> occurs in
     * the character sequence represented by this object, then the
     * index (in Unicode code units) of the first such occurrence is
     * returned.
     *
     * @param ch  the cahracter (Unicode code point)
     *
     * @return the index of the first occurrence of the character, or
     *         -1 if not found
     */
    public int indexOf (int ch)
    {
        return toString().indexOf (ch);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring in this sequence, starting at position 0.
     *
     * @param str  the string to find
     *
     * @return the index of the first occurrence of the substring, or
     *         -1 if not found
     */
    public int indexOf (String str)
    {
        return toString().indexOf (str);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring in this sequence, with the search starting at the
     * specified position.
     *
     * @param str   the string to find
     * @param start the index at which to start the search
     *
     * @return the index of the first occurrence of the substring, or
     *         -1 if not found
     */
    public int indexOf (String str, int start)
    {
        return toString().indexOf (str, start);
    }

    /**
     * Insert the string representation of a <tt>boolean</tt>value into
     * the buffer at a specified position. Note that an insertion operation
     * may push characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param val    The <tt>boolean</tt> value
     *
     * @return this object
     */
    public XStringBufBase insert (int index, boolean val)
    {
        return insert (index, String.valueOf (val));
    }

    /**
     * Insert a single character at a specified position in the buffer.
     * Note that an insertion operation may push * characters off the end
     * of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param ch     The character to insert.
     *
     * @return this object
     */
    public XStringBufBase insert (int index, char ch)
    {
        insertCharacter (index, ch);
        return this;
    }

    /**
     * Insert the contents of a character array into the buffer at a
     * specified position. Note that an insertion operation may push
     * characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param chars  The character array.
     *
     * @return this object
     */
    public XStringBufBase insert (int index, char chars[])
    {
        insertCharacters (index, chars, 0, chars.length);
        return this;
    }

    /**
     * Insert characters from a character array into the buffer at a
     * specified position. Note that an insertion operation may push
     * characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param chars  The character array.
     * @param offset The index of the first character to insert
     * @param len    The maximum number of characters to insert
     *
     * @return this object
     */
    public XStringBufBase insert (int  index,
                                  char chars[],
                                  int  offset,
                                  int  len)
    {
        insertCharacters (index, chars, offset, len);
        return this;
    }

    /**
     * Insert the string representation of a <tt>double</tt>value into
     * the buffer at a specified position. Note that an insertion operation
     * may push characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param val    The <tt>double</tt> value
     *
     * @return this object
     */
    public XStringBufBase insert (int index, double val)
    {
        return insert (index, String.valueOf (val));
    }

    /**
     * Insert the string representation of a <tt>float</tt>value into
     * the buffer at a specified position. Note that an insertion operation
     * may push characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param val    The <tt>float</tt> value
     *
     * @return this object
     */
    public XStringBufBase insert (int index, float val)
    {
        return insert (index, String.valueOf (val));
    }

    /**
     * Insert the string representation of a <tt>int</tt>value into
     * the buffer at a specified position. Note that an insertion operation
     * may push characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param val    The <tt>int</tt> value
     *
     * @return this object
     */
    public XStringBufBase insert (int index, int val)
    {
        return insert (index, String.valueOf (val));
    }

    /**
     * Insert the string representation of a <tt>long</tt>value into
     * the buffer at a specified position. Note that an insertion operation
     * may push characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param val    The <tt>long</tt> value
     *
     * @return this object
     */
    public XStringBufBase insert (int index, long val)
    {
        return insert (index, String.valueOf (val));
    }

    /**
     * Insert the string representation of a <tt>short</tt>value into
     * the buffer at a specified position. Note that an insertion operation
     * may push characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param val    The <tt>short</tt> value
     *
     * @return this object
     */
    public XStringBufBase insert (int index, short val)
    {
        return insert (index, String.valueOf (val));
    }

    /**
     * Insert the string representation of an arbitrary object into the
     * buffer at a specified position. Note that an insertion operation may
     * push characters off the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param obj    The object whose string representation is to be inserted
     *
     * @return this object
     */
    public XStringBufBase insert (int index, Object obj)
    {
        return insert (index, obj.toString());
    }

    /**
     * Insert the contents of a string into the buffer at a specified
     * position. Note that an insertion operation may push characters off
     * the end of the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param s      The string to insert
     *
     * @return this object
     */
    public XStringBufBase insert (int index, String s)
    {
        insertCharacters (index, s.toCharArray(), 0, s.length());
        return this;
    }

    /**
     * Return the number of characters currently in the buffer.
     *
     * @return The number of characters in the buffer.
     */
    public int length()
    {
        return getBufferAsCharSequence().length();
    }

    /**
     * Replace the characters in a substring of this buffer with
     * characters in the specified <tt>String</tt>. The substring
     * begins at the specified <tt>start</tt> and extends to the
     * character at index <tt>end - 1</tt>, or to the end of the
     * <tt>XStringBuffer</tt> if no such character exists. First the
     * characters in the substring are removed and then the specified
     * <tt>String</tt> is inserted at <tt>start</tt>. (The
     * <tt>XStringBuffer</tt> will be lengthened to accommodate the
     * specified <tt>String</tt> if necessary.)
     *
     * @param start  The beginning index, inclusive
     * @param end    The ending index, exclusive
     * @param str    The string that will replace the previous contents
     *
     * @return This object
     *
     * @throws StringIndexOutOfBoundsException if <tt>start</tt> is negative,
     *                                         or greater than
     *                                         <tt>length()</tt>, or greater
     *                                         than <tt>end</tt>
     */
    public XStringBufBase replace (int start, int end, String str)
        throws StringIndexOutOfBoundsException
    {
        replaceString (start, end, str);
        return this;
    }

    /**
     * Replace the first occurrence of a given substring in the buffer
     * with another substring.
     *
     * @param substring   The substring to find and replace
     * @param replacement The replacement string
     *
     * @return <tt>true</tt> if the replacement succeeded,
     *         <tt>false</tt> otherwise.
     */
    public boolean replace (String substring, String replacement)
    {
        int      i;
        boolean  replaced = false;

        i = indexOf (substring);
        if (i > -1)
        {
            replaceString (i, i + substring.length(), replacement);
            replaced = true;
        }

        return replaced;
    }

    /**
     * Replace the first occurrence of a given substring in the buffer
     * with a given character
     *
     * @param substring   The substring to find and replace
     * @param replacement The replacement char
     *
     * @return <tt>true</tt> if the replacement succeeded,
     *         <tt>false</tt> otherwise.
     */
    public boolean replace (String substring, char replacement)
    {
        int      i;
        boolean  replaced = false;

        i = indexOf (substring);
        if (i > -1)
        {
            deleteCharacters (i, i + substring.length());
            insert (i, replacement);
            replaced = true;
        }

        return replaced;
    }

    /**
     * Replace the all occurrences of a given substring in the buffer
     * with another substring. This method avoids recursion; that is, it's
     * safe even if the replacement string contains the source string.
     *
     * @param substring   The substring to find and replace
     * @param replacement The replacement string
     *
     * @return the number of replacements made
     */
    public int replaceAll (String substring, String replacement)
    {
        StringBuilder  buf = new StringBuilder (getBufferAsCharSequence());
        int            i;
        int            start;
        int            total = 0;

        start = 0;
        while ( (start < buf.length()) &&
                ((i = buf.toString().indexOf (substring, start)) >= 0) )
        {
            buf.replace (i, i + substring.length(), replacement);
            total++;
            start = i + replacement.length();
        }

        this.clear();
        this.append (buf.toString());

        return total;
    }

    /**
     * Replace the all occurrences of a given character in the buffer
     * with another character.
     *
     * @param ch          The character to find and replace
     * @param replacement The replacement character
     *
     * @return the number of replacements made
     */
    public int replaceAll (char ch, char replacement)
    {
        int    len   = length();
        char[] chars = new char[len];
        int    i;
        int    total = 0;

        getChars (0, len, chars, 0);
        for (i = 0; i < len; i++)
        {
            if (chars[i] == ch)
            {
                chars[i] = replacement;
                total++;
            }
        }

        if (total > 0)
        {
            clear();
            append (chars);
        }

        return total;
    }

    /**
     * Replace the all occurrences of a given character in the buffer
     * with string. This method avoids recursion; that is, it's
     * safe even if the replacement string contains the character being
     * replaced.
     *
     * @param ch          The character to find and replace
     * @param replacement The replacement string
     *
     * @return the number of replacements made
     */
    public int replaceAll (char ch, String replacement)
    {
        return replaceAll ("" + ch, replacement);
    }

    /**
     * Removes all existing characters from the buffer and loads the
     * string into the buffer.
     *
     *  @param str <tt>String</tt> object to be loaded into the cleared buffer.
     */
    public void reset (String str)
    {
        clear();
        append (str);
    }

    /**
     * Split the contents of a buffer on white space, and return the
     * resulting strings. This method is a convenient front-end to
     * {@link TextUtil#split(String)}.
     *
     * @return an array of <tt>String</tt> objects
     * @see #split(String)
     * @see TextUtil#split(String,char)
     */
    public String[] split()
    {
        return TextUtil.split (this.toString());
    }

    /**
     * Split the contents of a buffer on a delimiter, and return the
     * resulting strings. This method is a convenient front-end to
     * {@link TextUtil#split(String,char)}.
     *
     * @param delim the delimiter
     * @return an array of <tt>String</tt> objects
     * @see #split(String)
     * @see TextUtil#split(String,char)
     */
    public String[] split (char delim)
    {
        return TextUtil.split (this.toString(), delim);
    }

    /**
     * Split the contents of a buffer on a delimiter, and return the
     * resulting strings. This method is a convenient front-end to
     * {@link TextUtil#split(String,String)}
     *
     * @param delimSet the delimiter set
     * @return an array of <tt>String</tt> objects
     * @see #split(char)
     * @see TextUtil#split(String,String)
     */
    public String[] split (String delimSet)
    {
        return TextUtil.split (this.toString(), delimSet);
    }

    /**
     * Split the contents of a buffer on a delimiter, and store the
     * resulting strings in a specified <tt>Collection</tt>. This method
     * is a convenient front-end for
     * {@link TextUtil#split(String,char,Collection)}.
     *
     * @param delim      the delimiter
     * @param collection where to store the resulting strings
     * @return the number of strings added to the collection
     * @see #split(String,Collection)
     * @see #split(char)
     * @see TextUtil#split(String,char)
     * @see TextUtil#split(String,String,Collection)
     */
    public int split (char delim, Collection<String> collection)
    {
        return TextUtil.split (this.toString(), delim, collection);
    }

    /**
     * Split the contents of a buffer on a delimiter, and store the
     * resulting strings in a specified <tt>Collection</tt>. This method
     * is a convenient front-end for
     * {@link TextUtil#split(String,char,Collection)}.
     *
     * @param delimSet   the set of delimiters
     * @param collection where to store the resulting strings
     * @return the number of strings added to the collection
     * @see #split(char,Collection)
     * @see #split(String)
     * @see TextUtil#split(String,String)
     * @see TextUtil#split(String,char,Collection)
     */
    public int split (String delimSet, Collection<String> collection)
    {
        return TextUtil.split (this.toString(), delimSet, collection);
    }

    /**
     * Return a new <tt>String</tt> that contains a subsequence of
     * characters currently contained in this buffer. The substring
     * begins at the specified index and extends to the end of the
     * StringBuffer.
     *
     * @param index  The beginning index, inclusive
     *
     * @return the substring
     *
     * @throws StringIndexOutOfBoundsException index out of range
     */
    public String substring (int index)
        throws StringIndexOutOfBoundsException
    {
        return substring (index, length());
    }

    /**
     * Return a new <tt>String</tt> that contains a subsequence of
     * characters currently contained in this buffer. The substring begins
     * at the specified <tt>start</tt> and extends to the character at
     * index <tt>end - 1</tt>.
     *
     * @param start  The beginning index, inclusive
     * @param end    The beginning index, exclusive
     *
     * @return the substring
     *
     * @throws StringIndexOutOfBoundsException if <tt>start</tt> is negative,
     *                                         greater than <tt>length()</tt>,
     *                                         or greater than <tt>end</tt>
     *
     * @see #subSequence
     */
    public String substring (int start, int end)
        throws StringIndexOutOfBoundsException
    {
        return subSequence (start, end).toString();
    }

    /**
     * Return a new <tt>CharSequence</tt> object (really, another
     * <tt>XStringBuffer</tt>0 that contains a subsequence of
     * characters currently contained in this buffer. The substring begins
     * at the specified <tt>start</tt> and extends to the character at
     * index <tt>end - 1</tt>.
     *
     * @param start  The beginning index, inclusive
     * @param end    The beginning index, exclusive
     *
     * @return the subsequence
     *
     * @throws IndexOutOfBoundsException if <tt>start</tt> is negative,
     *                                   greater than <tt>length()</tt>,
     *                                   or greater than <tt>end</tt>
     */
    public CharSequence subSequence (int start, int end)
        throws IndexOutOfBoundsException
    {
        return getBufferAsCharSequence().subSequence (start, end);
    }

    /**
     * Return the <tt>String</tt> representation of this buffer.
     *
     * @return The string.
     */
    public String toString()
    {
        return getBufferAsCharSequence().toString();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Escape a specific character, if it's non-printable. See the
     * documentation for encodeMetacharacters() for the definition of
     * non-printable.
     *
     * @param c    the character to escape
     * @param buf  a scratch buffer, to avoid allocating a new one on each call
     *
     * @return the escape string (which might contain only the character passed
     *         in, if the character is printable)
     */
    private static String encodeOneMetacharacter (char c, StringBuilder buf)
    {
        StringBuilder result = new StringBuilder();

        if (TextUtil.isPrintable(c))
        {
            if (c == METACHAR_SEQUENCE_START)
            {
                // Have to escape the escape character.

                result.append(METACHAR_SEQUENCE_START);
                result.append(METACHAR_SEQUENCE_START);
            }

            else
            {
                result.append(c);
            }
        }

        else
        {
            // Assume it's non-printable and translate it.

            switch (c)
            {
                case '\r':
                    result.append("\\r");
                    break;

                case '\n':
                    result.append("\\n");
                    break;

                case '\t':
                    result.append("\\t");
                    break;

                case '\f':
                    result.append("\\f");
                    break;

                default:
                    result.append(toUnicodeEscape(c, buf));
            }
        }

        return result.toString();
    }

    /**
     * Convert a character to a Unicode escape string.
     *
     * @param c    the character to escape
     * @param buf  a scratch buffer, to avoid allocating a new one on each call
     *
     * @return the Unicode escape string
     */
    private static String toUnicodeEscape (char c, StringBuilder buf)
    {
        buf.setLength (0);
        return TextUtil.charToUnicodeEscape(c, buf);
    }

    /**
     * Decode a metacharacter sequence.
     *
     * @param c   the character after the backslash
     * @param pb  a PushbackReader representing the remainder of the region
     *            being processed (necessary for Unicode sequences)
     *
     * @return the decoded metacharacter, -1 on EOF, -2 for unknown or
     *         bad sequence
     *
     * @throws IOException read error
     */
    private int decodeMetacharacter (int c, PushbackReader pb)
        throws IOException
    {
        switch (c)
        {
            case 't':
                c = '\t';
                break;

            case 'n':
                c = '\n';
                break;

            case 'r':
                c = '\r';
                break;

            case METACHAR_SEQUENCE_START:
                c = METACHAR_SEQUENCE_START;
                break;

            case 'u':
                c = decodeUnicodeSequence (pb);
                if (c == -2)
                {
                    pb.unread ('u');
                    c = METACHAR_SEQUENCE_START;
                }
                break;

            default:
                // An escaped "regular" character is just the character.
                break;
        }

        return c;
    }

    /**
     * Parse the next four characters and attempt to decode them as a Unicode
     * character code.
     *
     * @param pb  a PushbackReader representing the remainder of the region
     *            being processed (necessary for Unicode sequences)
     *
     * @return the decoded character, -1 on EOF, -2 for a bad Unicode sequence.
     *         If -2 is returned, the 4-character Unicode code is pushed
     *         back on the input stream. (The leading backslash and "u" are
     *         not pushed back, however).
     *
     * @throws IOException  on error
     */
    private int decodeUnicodeSequence (PushbackReader pb)
        throws IOException
    {
        int            c          = -1;
        boolean        incomplete = false;
        StringBuilder  buf        = new StringBuilder();

        // Read four characters, each of which represents a single hex
        // digit.

        for (int i = 0; i < 4; i++)
        {
            if ( (c = pb.read()) == -1 )
            {
                // Incomplete Unicode escape sequence at EOF. Just swallow
                // it.

                incomplete = true;
                break;
            }

            buf.append ((char) c);
        }

        if (incomplete)
        {
            // Push the entire buffered sequence back onto the input
            // stream.

            unread (buf.toString(), pb);
        }

        else
        {
            int      code = 0;
            boolean  error = false;

            try
            {
                code = Integer.parseInt (buf.toString(), 16);
                if (code < 0)
                    throw new NumberFormatException();
            }

            catch (NumberFormatException ex)
            {
                // Bad hexadecimal value in Unicode escape sequence. Push
                // it all back.

                unread (buf.toString(), pb);
                error = true;
            }

            if (! Character.isDefined ((char) code))
            {
                // Invalid Unicode character. Push it all back.

                unread (buf.toString(), pb);
                error = true;
            }

            c = (error ? -2 : ((char) code));
        }

        return c;
    }

    /**
     * Push a string back on the input stream.
     *
     * @param s  the string
     * @param pb the PushbackReader onto which to push the characters
     *
     * @throws IOException if an I/O error occurs or if the pushback buffer is
     *                     full
     */
    private void unread (String s, PushbackReader pb)
        throws IOException
    {
        for (int i = s.length() - 1; i >= 0; i--)
            pb.unread (s.charAt (i));
    }
}
