package org.clapper.util.text;

/**
 * An <tt>XStringBuffer</tt> objects wraps a standard Java
 * <tt>StringBuffer</tt> object, providing a superset of
 * <tt>StringBuffer</tt>'s functionality. (<tt>XStringBuffer</tt>
 * cannot actually subclass <tt>StringBuffer</tt>, since
 * <tt>StringBuffer</tt> is final.) Among the additional methods that this
 * class provides are:
 *
 * <ul>
 *   <li> A set of {@link #split()} methods, to split the contents of the
 *        buffer on a delimiter
 *   <li> A {@link #delete(String) delete()} method that deletes the first
 *        occurrence of a substring. (<tt>StringBuffer</tt> only provides
 *        a <tt>delete()</tt> method that takes a starting and ending index.)
 *   <li> A {@link #replace(String,String) replace()} method that replaces
 *        the first occurrence of a substring. (<tt>StringBuffer</tt> only
 *        provides a <tt>replace()</tt> method that takes a starting and
 *        ending index.)
 *   <li> A {@link #replaceAll(String,String) replaceAll()} method to
 *        replace all occurrences of a substring with something else
 *   <li> Methods to encode and decode metacharacter sequences in place.
 *        (See {@link #encodeMetacharacters()} and
 *        {@link #decodeMetacharacters()}.)
 * </ul>
 *
 * <p>Because <tt>XStringBuffer</tt> wraps a <tt>StringBuffer</tt>, it is
 * thread-safe, but it can also be slower than a <tt>StringBuilder</tt>.
 * For applications or methods that don't have to worry about thread safety,
 * consider using the {@link XStringBuilder} class, instead.
 *
 * @see java.lang.StringBuffer
 * @see XStringBuilder
 */
public class XStringBuffer extends XStringBufBase
{
    /*----------------------------------------------------------------------*\
                             Private Variables
    \*----------------------------------------------------------------------*/

    /**
     * The underlying string buffer.
     */
    private StringBuffer buf = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct an empty <tt>XStringBuffer</tt> object with a default
     * initial capacity (the same initial capacity as an empty
     * <tt>StringBuffer</tt> object).
     */
    public XStringBuffer()
    {
        super();
        buf = new StringBuffer();
    }

    /**
     * Construct an empty <tt>XStringBuffer</tt> object with the specified
     * initial capacity.
     *
     * @param length  The initial capacity
     */
    public XStringBuffer (int length)
    {
        super();
        buf = new StringBuffer (length);
    }

    /**
     * Construct a <tt>XStringBuffer</tt> object so that it represents the
     * same sequence of characters as the <tt>String</tt> argument. (The
     * <tt>String</tt> contrents are copied into the <tt>XStringBuffer</tt>
     * object.)
     *
     * @param initialContents  The initial contents
     */
    public XStringBuffer (String initialContents)
    {
        super();
        if (initialContents == null)
            buf = new StringBuffer ();
        else
            buf = new StringBuffer (initialContents);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Return the current capacity of the buffer (i.e., the number of
     * characters left before truncation will occur).
     *
     * @return The capacity. A 0 means no more room is left, and all further
     *         inserted or appended characters will cause truncation.
     *
     * @see #length
     */
    public int capacity()
    {
        return buf.capacity();
    }

    /**
     * Remove the character at the specified position in this object,
     * shortening this object by one character.
     *
     * @param index  Index of the character to remove
     *
     * @return This object
     *
     * @throws StringIndexOutOfBoundsException if <tt>index</tt> is negative,
     *                                         or greater than or equal to
     *                                         <tt>length()</tt>.
     */
    public XStringBuffer deleteCharAt (int index)
        throws StringIndexOutOfBoundsException
    {
        buf.deleteCharAt (index);
        return this;
    }

    /**
     * Ensure that the capacity of the buffer is at least equal to the
     * specified minimum. If the current capacity of this string buffer is
     * less than the argument, then a new internal buffer is allocated with
     * greater capacity. The new capacity is the larger of:
     *
     * <ul type="disc">
     *   <li>The <tt>minimumCapacity</tt> argument
     *   <li>Twice the old capacity, plus 2.
     * </ul>
     *
     * If the <tt>minimumCapacity</tt> argument is non-positive, this
     * method returns without doing anything.
     *
     * @param minimumCapacity  The minimum desirec capacity
     */
    public void ensureCapacity (int minimumCapacity)
    {
        buf.ensureCapacity (minimumCapacity);
    }

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
    public void getChars (int   srcBegin,
                          int   srcEnd,
                          char  dst[],
                          int   dstBegin)
        throws IndexOutOfBoundsException
    {
        buf.getChars (srcBegin, srcEnd, dst, dstBegin);
    }

    /**
     * Reverse the contents of the buffer.
     */
    public void reverse()
    {
        buf.reverse();
    }

    /**
     * Set the character at a specified index. The <tt>index</tt>
     * parameter must be greater than or equal to 0, and less than the
     * length of this string buffer.
     *
     * @param index  The index at which to set the value.
     * @param ch     The character to store
     *
     * @throws StringIndexOutOfBoundsException index out of range
     */
    public void setCharAt (int index, char ch)
        throws StringIndexOutOfBoundsException
    {
        buf.setCharAt (index, ch);
    }

    /**
     * Set the length of this <tt>XStringBuffer</tt>. This string
     * buffer is altered to represent a new character sequence whose length
     * is specified by the argument. If the <tt>newLength</tt> argument
     * is less than the current length of the string buffer, the string
     * buffer is truncated to contain exactly the number of characters
     * given by the <tt>newLength</tt> argument. If the
     * <tt>newLength</tt> argument is greater than the current length
     * of the string buffer, the buffer is padded with null characters out
     * to the new length.
     *
     * @param newLength   the new length of the buffer
     *
     * @throws IndexOutOfBoundsException  <tt>newLength</tt> is negative
     */
    public void setLength (int newLength) throws IndexOutOfBoundsException
    {
        buf.setLength (newLength);
    }

    /**
     * Return a standard <tt>StringBuffer</tt> containing a copy of the
     * contents of this buffer.
     *
     * @return The string buffer
     */
    public StringBuffer toStringBuffer()
    {
        return new StringBuffer (buf.toString());
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the underlying buffer (e.g., <tt>StringBuffer</tt>,
     * <tt>StringBuilder</tt>) as an <tt>Appender</tt> object.
     *
     * @return the <tt>Appender</tt>
     */
    protected Appendable getBufferAsAppendable()
    {
        return buf;
    }

    /**
     * Get a new instance of the underlying buffer type (e.g.,
     * <tt>StringBuffer</tt>, <tt>StringBuilder</tt>) as a
     * <tt>CharSequence</tt> object.
     *
     * @return the <tt>Appendable</tt>
     */
    protected CharSequence newBufferAsCharSequence()
    {
        return new StringBuffer();
    }

    /**
     * Get the underlying buffer (e.g., <tt>StringBuffer</tt>,
     * <tt>StringBuilder</tt>) as a <tt>CharSequence</tt> object.
     *
     * @return the <tt>Appendable</tt>
     */
    protected CharSequence getBufferAsCharSequence()
    {
        return buf;
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
     * @throws IndexOutOfBoundsException  if <tt>start</tt> is negative,
     *                                    greater than <tt>length()</tt>,
     *                                    or greater than <tt>end</tt>
     */
    protected void deleteCharacters (int start, int end)
        throws IndexOutOfBoundsException
    {
        buf.delete (start, end);
    }

    /**
     * Insert a single character into the buffer at a specified position.
     * Note that an insertion operation may push characters off the end of
     * the buffer.
     *
     * @param index  Where to start inserting in the string buffer
     * @param ch     The character to insert
     */
    protected void insertCharacter (int index, char ch)
    {
        buf.insert (index, ch);
    }
 
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
    protected synchronized void insertCharacters (int  index,
                                                  char chars[],
                                                  int  offset,
                                                  int  len)
    {
        buf.insert (index, chars, offset, len);
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
     * @throws IndexOutOfBoundsException if <tt>start</tt> is negative,
     *                                   or greater than
     *                                   <tt>length()</tt>, or greater
     *                                   than <tt>end</tt>
     */
    protected void replaceString (int start, int end, String str)
        throws IndexOutOfBoundsException
    {
        buf.replace (start, end, str);
    }
}
