package org.clapper.util.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Static class containing miscellaneous text utility methods.
 */
public final class TextUtil
{
    /*----------------------------------------------------------------------*\
                             Private Classes
    \*----------------------------------------------------------------------*/

    private static class RomanNumberTableEntry
    {
        final String romanString;
        final int    quantity;

        RomanNumberTableEntry(String s, int num)
        {
            this.romanString = s;
            this.quantity = num;
        }
    }

    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Set of digits suitable for encoding a number as a hexadecimal string.
     *
     * @see #hexadecimalForNumber(int,StringBuilder)
     * @see #hexadecimalForNumber(short,StringBuilder)
     * @see #hexadecimalForNumber(long,StringBuilder)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     * @see #encodeNumber(long,int,char[],StringBuilder)
     * @see #encodeNumber(int,int,char[],StringBuilder)
     * @see #encodeNumber(short,int,char[],StringBuilder)
     */
    public static final char[] HEXADECIMAL_DIGITS = new char[]
    {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /**
     * For Roman number conversion
     */
    private static final RomanNumberTableEntry[] ROMAN_NUMBER_TABLE =
        new RomanNumberTableEntry[]
    {
        new RomanNumberTableEntry("M", 1000),
        new RomanNumberTableEntry("CM", 900),
        new RomanNumberTableEntry("D", 500),
        new RomanNumberTableEntry("CD", 400),
        new RomanNumberTableEntry("C", 100),
        new RomanNumberTableEntry("XC", 90),
        new RomanNumberTableEntry("L", 50),
        new RomanNumberTableEntry("XL", 40),
        new RomanNumberTableEntry("X", 10),
        new RomanNumberTableEntry("IX", 9),
        new RomanNumberTableEntry("V", 5),
        new RomanNumberTableEntry("IV", 4),
        new RomanNumberTableEntry("I", 1)
    };

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private TextUtil()
    {
        // Can't be instantiated
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Convert a boolean string to a boolean value. This method is more
     * generous than <tt>java.lang.Boolean.booleanValue()</tt>. The following
     * strings (in upper or lower case) are recognized as <tt>true</tt>:
     * "1", "true", "yes", "y". The following
     * strings (in upper or lower case) are recognized as <tt>false</tt>:
     * "0", "false", "no", "n".
     *
     * @param s   string to convert
     *
     * @return <tt>true</tt> or <tt>false</tt>
     *
     * @throws IllegalArgumentException  string isn't a boolean
     */
    public static boolean booleanFromString(String s)
        throws IllegalArgumentException
    {
        boolean result;

        s = s.toLowerCase();

        if (s.equals("true") ||
            s.equals("1")    ||
            s.equals("yes")  ||
            s.equals("y"))
        {
            result = true;
        }

        else if (s.equals("false") ||
                 s.equals("0")     ||
                 s.equals("no")    ||
                 s.equals("n"))
        {
            result = false;
        }

        else
        {
            throw new IllegalArgumentException("Bad boolean string: \"" +
                                               s + "\"");
        }

        return result;
    }

    /**
     * <p>Split a string on white space, into one or more strings. This
     * method is intended to be reminiscent of the corresponding perl or
     * awk <i>split()</i> function, though without regular expression
     * support. This version of <tt>split()</tt> does not preserve
     * empty strings. That is, the string "a:b::c", when split with a
     * ":" delimiter, yields three fields ("a", "b", "c"), since the
     * two adjacent ":" characters are treated as one delimiter. To
     * preserve empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s  the string to split
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,boolean)
     * @see #split(String,String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split(String s)
    {
        return split(s, (String) null);
    }

    /**
     * <p>Split a string on white space, into one or more strings. This
     * method is intended to be reminiscent of the corresponding perl or
     * awk <i>split()</i> function, though without regular expression
     * support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split(String s, boolean preserveEmptyFields)
    {
        return split(s, (String) null, preserveEmptyFields);
    }

    /**
     * <p>Split a string on white space, into one or more strings. This
     * method is intended to be reminiscent of the corresponding perl or
     * awk <i>split()</i> function, though without regular expression
     * support. This version of <tt>split()</tt> does not preserve
     * empty strings. That is, the string "a:b::c", when split with a
     * ":" delimiter, yields three fields ("a", "b", "c"), since the
     * two adjacent ":" characters are treated as one delimiter. To
     * preserve empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,Collection,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s          the string to split
     * @param collection where to store the split strings
     *
     * @return the number of strings added to the collection
     *
     * @see #split(String,Collection,boolean)
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,char)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static int split(String s, Collection<String> collection)
    {
        return split(s, collection, false);
    }

    /**
     * <p>Split a string on white space, into one or more strings. This
     * method is intended to be reminiscent of the corresponding perl or
     * awk <tt>split()</tt> function, though without regular expression
     * support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param collection          where to store the split strings
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return the number of strings added to the collection
     *
     * @see #split(String,Collection)
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,char)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static int split(String             s,
                            Collection<String> collection,
                            boolean            preserveEmptyFields)
    {
        return split(s, (String) null, collection, preserveEmptyFields);
    }

    /**
     * <p>Split a string into one or more strings, based on a delimiter.
     * This method is intended to be reminiscent of the corresponding perl
     * or awk <i>split()</i> function, though without regular expression
     * support. This version of <tt>split()</tt> does not preserve empty
     * strings. That is, the string "a:b::c", when split with a ":"
     * delimiter, yields three fields ("a", "b", "c"), since the two
     * adjacent ":" characters are treated as one delimiter. To preserve
     * empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,char,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s     the string to split
     * @param delim the delimiter
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,char,boolean)
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split(String s, char delim)
    {
        return split(s, "" + delim, false);
    }

    /**
     * <p>Split a string into one or more strings, based on a delimiter.
     * This method is intended to be reminiscent of the corresponding perl
     * or awk <i>split()</i> function, though without regular expression
     * support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param delim               the delimiter
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,char)
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split(String  s,
                                 char    delim,
                                 boolean preserveEmptyFields)
    {
        return split(s, String.valueOf(delim), preserveEmptyFields);
    }

    /**
     * <p>Split a string into one or more strings, based on a set of
     * delimiter. This method is intended to be reminiscent of the
     * corresponding perl or awk <i>split()</i> function, though without
     * regular expression support. This version of <tt>split()</tt> does
     * not preserve empty strings. That is, the string "a:b::c", when split
     * with a ":" delimiter, yields three fields ("a", "b", "c"), since the
     * two adjacent ":" characters are treated as one delimiter. To
     * preserve empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,String,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s         the string to split
     * @param delimSet  set of delimiters, or null to use white space
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,String,boolean)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split(String s, String delimSet)
    {
        return split(s, delimSet, false);
    }

    /**
     * <p>Split a string into one or more strings, based on a set of
     * delimiter. This method is intended to be reminiscent of the
     * corresponding perl or awk <i>split()</i> function, though without
     * regular expression support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param delimSet            set of delimiters, or null to use white space
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,String)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split(String  s,
                                 String  delimSet,
                                 boolean preserveEmptyFields)
    {
        String[]           result = null;
        StringTokenizer    tok;
        Collection<String> temp = new ArrayList<String>();

        if (delimSet == null)
            delimSet = " \t\n\r";

        tok = new StringTokenizer(s, delimSet, preserveEmptyFields);

        // Assume we'll never see the delimiter unless preserveEmptyFields is
        // set.

        boolean lastWasDelim = true;
        while (tok.hasMoreTokens())
        {
            String token = tok.nextToken();

            if (preserveEmptyFields &&
                (token.length() == 1) &&
                (delimSet.indexOf(token.charAt(0)) != -1))
            {
                if (lastWasDelim)
                    token = "";
                else
                {
                    lastWasDelim = true;
                    continue;
                }
            }

            else
            {
                lastWasDelim = false;
            }

            temp.add (token);
        }

        result = new String[temp.size()];
        temp.toArray(result);
        return result;
    }

    /**
     * <p>Split a string into one or more strings, based on a delimiter.
     * This method is intended to be reminiscent of the corresponding perl
     * or awk <i>split()</i> function, though without regular expression
     * support. This version of <tt>split()</tt> does not preserve empty
     * strings. That is, the string "a:b::c", when split with a ":"
     * delimiter, yields three fields ("a", "b", "c"), since the two
     * adjacent ":" characters are treated as one delimiter. To preserve
     * empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,char,Collection,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s          the string to split
     * @param delim      the delimiter
     * @param collection where to store the split strings
     *
     * @return the number of <tt>String</tt> objects added to the collection
     *
     * @see #split(String,char,Collection,boolean)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,String)
     * @see #split(String,String,Collection)
     */
    public static int split(String             s,
                            char               delim,
                            Collection<String> collection)
    {
        return split(s, String.valueOf(delim), collection);
    }

    /**
     * <p>Split a string into one or more strings, based on a delimiter.
     * This method is intended to be reminiscent of the corresponding perl
     * or awk <tt>split()</tt> function, though without regular expression
     * support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param delim               the delimiter
     * @param collection          where to store the split strings
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return the number of <tt>String</tt> objects added to the collection
     *
     * @see #split(String,char,Collection)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,String)
     * @see #split(String,String,Collection)
     */
    public static int split(String             s,
                            char               delim,
                            Collection<String> collection,
                            boolean            preserveEmptyFields)
    {
        return split(s,
                     String.valueOf (delim),
                     collection,
                     preserveEmptyFields);
    }

    /**
     * <p>Split a string into one or more strings, based on a set of
     * delimiter. This method is intended to be reminiscent of the
     * corresponding perl or awk <i>split()</i> function, though without
     * regular expression support. This version of <tt>split()</tt> does
     * not preserve empty strings. That is, the string "a:b::c", when split
     * with a ":" delimiter, yields three fields ("a", "b", "c"), since the
     * two adjacent ":" characters are treated as one delimiter. To
     * preserve empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,String,Collection,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class. This
     * method does not use regular expressions. This version of
     * <tt>split()</tt> does not preserve empty strings. That is, the
     * string "a:b::c", when split with a ":" delimiter, yields three
     * fields ("a", "b", "c"), since the two adjacent ":" characters are
     * treated as one delimiter. To preserve empty strings, pass
     * <tt>true</tt> as the <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,String,Collection,boolean)} method.</p>
     *
     * @param s          the string to split
     * @param delimSet   set of delimiters
     * @param collection where to store the split strings
     *
     * @return the number of <tt>String</tt> objects added to the collection
     *
     * @see #split(String,String,Collection,boolean)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,String)
     * @see #split(String,char,Collection)
     */
    public static int split(String             s,
                            String             delimSet,
                            Collection<String> collection)
    {
        return split(s, delimSet, collection, false);
    }

    /**
     * <p>Split a string into one or more strings, based on a set of
     * delimiter. This method is intended to be reminiscent of the
     * corresponding perl or awk <i>split()</i> function, though without
     * regular expression support. This method uses a
     * <tt>StringTokenizer</tt> to do the actual work.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class. This
     * method does not use regular expressions. This version of
     * <tt>split()</tt> does not preserve empty strings. That is, the
     * string "a:b::c", when split with a ":" delimiter, yields three
     * fields ("a", "b", "c"), since the two adjacent ":" characters are
     * treated as one delimiter. To preserve empty strings, pass
     * <tt>true</tt> as the <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,String,Collection,boolean)} method.</p>
     *
     * @param s                   the string to split
     * @param delimSet            set of delimiters
     * @param collection          where to store the split strings
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return the number of <tt>String</tt> objects added to the collection
     *
     * @see #split(String,String,Collection)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,String)
     * @see #split(String,char,Collection)
     */
    public static int split(String             s,
                            String             delimSet,
                            Collection<String> collection,
                            boolean            preserveEmptyFields)
    {
        String[] strs = split(s, delimSet, preserveEmptyFields);

        for (int i = 0; i < strs.length; i++)
            collection.add(strs[i]);

        return strs.length;
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings.
     *
     * @param strings  the strings to be joined
     * @param delim    the delimiter string
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @see #split(String,String)
     * @see #join(String[],char)
     */
    public static String join(String[] strings, String delim)
    {
        StringBuilder result = new StringBuilder();
        String        sep    = "";

        for (int i = 0; i < strings.length; i++)
        {
            result.append(sep);
            result.append(strings[i]);
            sep = delim;
        }

        return result.toString();
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings. This version of <tt>join()</tt> supports the
     * new Java variable argument syntax.
     *
     * @param delim    the delimiter string
     * @param strings  the strings to be joined
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join(String delim, String... strings)
    {
        return join(strings, delim);
    }

    /**
     * Join an array of strings into one string, putting the specified
     * delimiter between adjacent strings.
     *
     * @param strings  the strings to be joined
     * @param delim    the delimiter character
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join(String[] strings, char delim)
    {
        return join(strings, "" + delim);
    }

    /**
     * Join an array of strings into one string, putting the specified
     * delimiter between adjacent strings, starting at a specified index.
     *
     * @param strings  the strings to be joined
     * @param start    starting index
     * @param end      one past the ending index
     * @param delim    the delimiter character
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @throws ArrayIndexOutOfBoundsException bad value for <tt>start</tt>
     *                                        or <tt>end</tt>
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join(String[] strings,
                              int      start,
                              int      end,
                              char     delim)
    {
        return join(strings, start, end, "" + delim);
    }

    /**
     * Join an array of strings into one string, putting the specified
     * delimiter between adjacent strings, starting at a specified index.
     *
     * @param strings  the strings to be joined
     * @param start    starting index
     * @param end      one past the ending index
     * @param delim    the delimiter string
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @throws ArrayIndexOutOfBoundsException bad value for <tt>start</tt>
     *                                        or <tt>end</tt>
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join(String[] strings,
                              int      start,
                              int      end,
                              String   delim)
    {
        StringBuilder result = new StringBuilder();
        String        sep    = "";

        while (start < end)
        {
            result.append(sep);
            result.append(strings[start]);
            sep = delim;
            start++;
        }

        return result.toString();
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings. This version of <tt>join()</tt> supports the
     * new Java variable argument syntax.
     *
     * @param delim    the delimiter character
     * @param strings  the strings to be joined
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join(char delim, String... strings)
    {
        return join(strings, "" + delim);
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings.
     *
     * @param objects  A collection the items to be joined. This collection
     *                 can contain objects of any type; each object's
     *                 <tt>toString()</tt> method is called to produce the
     *                 string to be joined.
     * @param delim    the delimiter string
     *
     * @return the joined string, or "" if the collection is empty.
     *
     * @see #split(String,String,Collection)
     * @see #join(Collection,char)
     */
    public static String join(Collection<? extends Object> objects,
                              String delim)
    {
        String result = "";

        if (objects.size() > 0)
        {
            String[] array;
            int      i;
            Iterator it;

            i = 0;
            for (it = objects.iterator(); it.hasNext();)
            {
                Object o = it.next();
                if (o == null)
                    continue;

                i++;
            }

            array = new String[i];
            i = 0;
            for (it = objects.iterator(); it.hasNext();)
            {
                Object o = it.next();
                if (o == null)
                    continue;

                array[i++] = o.toString();
            }

            result = join(array, delim);
        }

        return result;
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings.
     *
     * @param objects  A collection the items to be joined. This collection
     *                 can contain objects of any type; each object's
     *                 <tt>toString()</tt> method is called to produce the
     *                 string to be joined.
     * @param delim    the delimiter character
     *
     * @return the joined string, or "" if the collection is empty.
     *
     * @see #split(String,char,Collection)
     * @see #join(Collection,String)
     */
    public static String join(Collection<? extends Object> objects,
                              char                         delim)
    {
        return join(objects, "" + delim);
    }

    /**
     * Determine whether a given string is empty. A string is empty if it
     * is null, zero-length, or comprised entirely of white space. This method
     * is more efficient than calling <tt>s.trim().length()</tt>, because it
     * does not create a new string just to test its length.
     *
     * @param s  the string to test
     *
     * @return <tt>true</tt> if it's empty, <tt>false</tt> if not.
     */
    public static boolean stringIsEmpty(String s)
    {
        boolean isEmpty = true;

        if (s != null)
        {
            char[] chars = s.toCharArray();

            for (int i = 0; i < chars.length; i++)
            {
                if (! Character.isWhitespace(chars[i]))
                {
                    isEmpty = false;
                    break;
                }
            }
        }

        return isEmpty;
    }

    /**
     * Right justify a string in a fixed-width field, using blanks for
     * padding. If the string is already longer than the field width, it is
     * returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     *
     * @return a right-justified version of the string
     *
     * @see #rightJustifyString(String,int,char)
     * @see #leftJustifyString(String,int)
     * @see #centerString(String,int)
     */
    public static String rightJustifyString(String s, int width)
    {
        return rightJustifyString(s, width, ' ');
    }

    /**
     * Right justify a string in a fixed-width field, using the specified
     * character for padding. If the string is already longer than the
     * field width, it is returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     * @param c      the pad character
     *
     * @return a right-justified version of the string
     *
     * @see #rightJustifyString(String,int)
     * @see #leftJustifyString(String,int,char)
     * @see #centerString(String,int,char)
     */
    public static String rightJustifyString(String s, int width, char c)
    {
        StringBuilder  paddedString = new StringBuilder (width);
        int           paddingNeeded;
        int           len = s.length();

        paddingNeeded = (width < len) ? 0 : (width - len);

        for (int i = 0; i < paddingNeeded; i++)
            paddedString.append(c);

        paddedString.append(s);

        return paddedString.toString();
    }

    /**
     * Left justify a string in a fixed-width field, using blanks for
     * padding. If the string is already longer than the field width, it is
     * returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     *
     * @return a left-justified version of the string
     *
     * @see #leftJustifyString(String,int,char)
     * @see #rightJustifyString(String,int)
     * @see #centerString(String,int)
     */
    public static String leftJustifyString(String s, int width)
    {
        return leftJustifyString(s, width, ' ');
    }

    /**
     * Left justify a string in a fixed-width field, using the specified
     * character for padding. If the string is already longer than the
     * field width, it is returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     * @param c      the pad character
     *
     * @return a left-justified version of the string
     *
     * @see #leftJustifyString(String,int)
     * @see #rightJustifyString(String,int,char)
     * @see #centerString(String,int,char)
     */
    public static String leftJustifyString(String s, int width, char c)
    {
        StringBuilder  paddedString = new StringBuilder (width);
        int           paddingNeeded;
        int           len = s.length();

        paddingNeeded = (width < len) ? 0 : (width - len);
        paddedString.append(s);

        for (int i = 0; i < paddingNeeded; i++)
            paddedString.append(c);

        return paddedString.toString();
    }

    /**
     * Center a string in a fixed-width field, using blanks for padding. If
     * the string is already longer than the field width, it is returned
     * unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     *
     * @return a centered version of the string
     *
     * @see #centerString(String,int,char)
     * @see #rightJustifyString(String,int)
     * @see #leftJustifyString(String,int)
     */
    public static String centerString(String s, int width)
    {
        return centerString(s, width, ' ');
    }

    /**
     * Center a string in a fixed-width field, using the specified
     * character for padding. If the string is already longer than the
     * field width, it is returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     * @param c      the pad character
     *
     * @return a right-justified version of the string
     *
     * @see #centerString(String,int,char)
     * @see #leftJustifyString(String,int,char)
     * @see #rightJustifyString(String,int,char)
     */
    public static String centerString(String s, int width, char c)
    {
        StringBuilder  paddedString = new StringBuilder (width);
        int           paddingNeeded;
        int           len = s.length();
        int           frontPadding;
        int           tailPadding;
        int           i;

        paddingNeeded = (width < len) ? 0 : (width - len);
        i = paddingNeeded / 2;
        frontPadding = i;
        tailPadding  = i + (paddingNeeded % 2);

        for (i = 0; i < frontPadding; i++)
            paddedString.append(c);

        paddedString.append(s);

        for (i = 0; i < tailPadding; i++)
            paddedString.append(c);

        return paddedString.toString();
    }

    /**
     * Encode a byte as a two-digit hexadecimal string, appending the
     * result to a <tt>StringBuilder</tt>. The hexadecimal string uses
     * lower-case digits. Note that the <tt>byte</tt> type
     * in Java is signed. Use the other version of
     * {@link #hexadecimalForByte(int,StringBuilder)} if you want to pass an
     * unsigned byte value.
     *
     * @param b   the byte
     * @param buf the <tt>StringBuilder</tt> to which to append the hex string
     * @return a reference to the <tt>StringBuilder</tt>, for convenience
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     * @see #bytesForHexadecimal(String)
     */
    public static StringBuilder hexadecimalForByte(byte b, StringBuilder buf)
    {
        int nybble;

        // High nybble first

        nybble = (b >>> 4) & 0x0f;
        buf.append(HEXADECIMAL_DIGITS[nybble]);

        // Now the low nybble

        nybble = b & 0x0f;
        buf.append(HEXADECIMAL_DIGITS[nybble]);

        return buf;
    }

    /**
     * Encode a byte as a two-digit hexadecimal string, appending the
     * result to a <tt>StringBuilder</tt>.The hexadecimal string uses
     * lower-case digits. in lower-case. This version of
     * <tt>hexadecimalForByte</tt> is suitable for use with unsigned values.
     *
     * @param b   the byte
     * @param buf the <tt>StringBuilder</tt> to which to append the hex string
     * @return a reference to the <tt>StringBuilder</tt>, for convenience
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     * @see #bytesForHexadecimal(String)
     */
    public static StringBuilder hexadecimalForByte (int b, StringBuilder buf)
    {
        int nybble;

        // High nybble first

        nybble = (b >>> 4) & 0x0f;
        buf.append(HEXADECIMAL_DIGITS[nybble]);

        // Now the low nybble

        nybble = b & 0x0f;
        buf.append(HEXADECIMAL_DIGITS[nybble]);

        return buf;
    }

    /**
     * Encode an array of bytes as a hexadecimal string, appending the
     * result to a <tt>StringBuilder</tt>. Note that the <tt>byte</tt> type
     * in Java is signed. Use the other version of
     * {@link #hexadecimalForBytes(int[],int,int,StringBuilder)}
     * if you want to pass signed byte values.
     *
     * @param bytes  The array of bytes
     * @param start  starting index in byte array
     * @param end    one past ending index in byte array (thus,
     *               bytes.length is a valid value)
     * @param buf    Where to append the result
     * @return a reference to the <tt>StringBuilder</tt>, for convenience
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #bytesForHexadecimal(String)
     */
    public static StringBuilder hexadecimalForBytes(byte[]        bytes,
                                                    int           start,
                                                    int           end,
                                                    StringBuilder buf)
    {
        for (int i = start; i < end; i++)
            hexadecimalForByte(bytes[i], buf);

        return buf;
    }

    /**
     * Encode an array of bytes as a hexadecimal string, returning the
     * string. Calling this method is equivalent to:
     *
     * <blockquote>
     * <pre>
     * hexadecimalForBytes(buf, 0, buf.length, new StringBuilder()).toString()
     * </pre>
     * </blockquote>
     *
     * @param bytes  The array of bytes
     *
     * @return a reference to the <tt>StringBuilder</tt>, for convenience
     *
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     */
    public static String hexadecimalForBytes(byte[] bytes)
    {
        return hexadecimalForBytes(bytes, 0, bytes.length, new StringBuilder())
              .toString();
    }

    /**
     * Encode an array of bytes as a hexadecimal string, appending the
     * result to a <tt>StringBuilder</tt>. Note that this method takes an
     * <tt>int</tt> array; however, it only examines the low-order byte of
     * each integer. It's suitable for use with unsigned values. Use the
     * other version of {@link #hexadecimalForBytes(byte[],int,int,StringBuilder)} if
     * you want to pass signed byte values.
     *
     * @param bytes  The array of bytes
     * @param start  starting index in byte array
     * @param end    one past ending index in byte array (thus,
     *               bytes.length is a valid value)
     * @param buf    Where to append the result
     * @return the <tt>StringBuilder</tt>, for convenience
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #bytesForHexadecimal(String)
     */
    public static StringBuilder hexadecimalForBytes(int[]         bytes,
                                                    int           start,
                                                    int           end,
                                                    StringBuilder buf)
    {
        for (int i = start; i < end; i++)
            hexadecimalForByte(bytes[i], buf);

        return buf;
    }

    /**
     * Encode an integer as a hexadecimal string, appending the result
     * to a <tt>StringBuilder</tt>. This method uses
     * <tt>encodeNumber()</tt> to perform the actual encoding.
     *
     * @param num     The integer to encode
     * @param buf     Where to append the result
     *
     * @return the <tt>StringBuilder</tt>, for convenience
     *
     * @see #encodeNumber(int,int,char[],StringBuilder)
     * @see #hexadecimalForNumber(short,StringBuilder)
     * @see #hexadecimalForNumber(long,StringBuilder)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static StringBuilder hexadecimalForNumber(int           num,
                                                     StringBuilder buf)
    {
        return encodeNumber(num,
                            HEXADECIMAL_DIGITS.length,
                            HEXADECIMAL_DIGITS,
                            buf);
    }

    /**
     * Encode an integer as a hexadecimal string, returning the hex string.
     * This method uses <tt>encodeNumber()</tt> to perform the actual
     * encoding.
     *
     * @param num The integer to encode
     *
     * @return the <tt>StringBuilder</tt>, for convenience
     *
     * @see #encodeNumber(int,int,char[],StringBuilder)
     * @see #hexadecimalForNumber(short)
     * @see #hexadecimalForNumber(int,StringBuilder)
     * @see #hexadecimalForNumber(long)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static String hexadecimalForNumber(int num)
    {
        return encodeNumber(num,
                            HEXADECIMAL_DIGITS.length,
                            HEXADECIMAL_DIGITS,
                            new StringBuilder()).toString();
    }

    /**
     * Encode a long as a hexadecimal string, appending the result
     * to a <tt>StringBuilder</tt>.
     *
     * @param num     The long to encode
     * @param buf     Where to append the result
     *
     * @return the <tt>StringBuilder</tt>, for convenience
     *
     * @see #encodeNumber(long,int,char[],StringBuilder)
     * @see #hexadecimalForNumber(int,StringBuilder)
     * @see #hexadecimalForNumber(short,StringBuilder)
     * @see #hexadecimalForNumber(long)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static StringBuilder hexadecimalForNumber(long          num,
                                                     StringBuilder buf)
    {
        return encodeNumber(num,
                            HEXADECIMAL_DIGITS.length,
                            HEXADECIMAL_DIGITS,
                            buf);
    }

    /**
     * Encode a long as a hexadecimal string, returning the hex string.
     * This method uses <tt>encodeNumber()</tt> to perform the actual
     * encoding.
     *
     * @param num The long integer to encode
     *
     * @return the <tt>StringBuilder</tt>, for convenience
     *
     * @see #encodeNumber(int,int,char[],StringBuilder)
     * @see #hexadecimalForNumber(short)
     * @see #hexadecimalForNumber(long,StringBuilder)
     * @see #hexadecimalForNumber(int)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static String hexadecimalForNumber(long num)
    {
        return encodeNumber(num,
                            HEXADECIMAL_DIGITS.length,
                            HEXADECIMAL_DIGITS,
                            new StringBuilder()).toString();
    }

    /**
     * Encode a short as a hexadecimal string, returning the hex string.
     * This method uses <tt>encodeNumber()</tt> to perform the actual
     * encoding.
     *
     * @param num The short integer to encode
     *
     * @return the <tt>StringBuilder</tt>, for convenience
     *
     * @see #encodeNumber(int,int,char[],StringBuilder)
     * @see #hexadecimalForNumber(long)
     * @see #hexadecimalForNumber(short,StringBuilder)
     * @see #hexadecimalForNumber(int)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static String hexadecimalForNumber(short num)
    {
        return encodeNumber(num,
                            HEXADECIMAL_DIGITS.length,
                            HEXADECIMAL_DIGITS,
                            new StringBuilder()).toString();
    }

    /**
     * Encode a short as a hexadecimal string, appending the result
     * to a <tt>StringBuilder</tt>.
     *
     * @param num     The short integer to encode
     * @param buf     Where to append the result
     *
     * @return the <tt>StringBuilder</tt>, for convenience
     *
     * @see #hexadecimalForNumber(int,StringBuilder)
     * @see #hexadecimalForNumber(long,StringBuilder)
     * @see #hexadecimalForNumber(short)
     * @see #encodeNumber(short,int,char[],StringBuilder)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static StringBuilder hexadecimalForNumber(short num, StringBuilder buf)
    {
        return encodeNumber(num,
                            HEXADECIMAL_DIGITS.length,
                            HEXADECIMAL_DIGITS,
                            buf);
    }

    /**
     * <p>Encode a long integer in an arbitrary base; the caller specifies
     * an array of digit characters to be used for the encoding. For
     * instance, to encode a number in base 36, you might use the following
     * code fragment:</p>
     *
     * <blockquote>
     * <pre>
     * {@code
     * StringBuilder buf = new StringBuilder();
     * long         num = ...
     * char[]       digits = new char[]
     *              {
     *                  '0', '1', '2', '3', '4', '5', '6', '7', '8',
     *                  '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
     *                  'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
     *                  'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
     *              };
     * TextUtils.encodeNumber (num, digits.length, digits, buf);
     * }
     * </pre>
     * </blockquote>
     *
     * <p>For convenience, this class provides a character array of
     * hexadecimal digits. (See {@link #HEXADECIMAL_DIGITS}.) However, it's
     * usually simpler just to use one of the <tt>hexadecimalForNumber()</tt>
     * methods().</p>
     *
     * @param num     The long integer to encode
     * @param base    The base in which to encode the number
     * @param digits  The array of digit characters to use for the encoding.
     *                This array must be at least <tt>base</tt> characters
     *                in length. <tt>digits[0]</tt> represents the numeral to
     *                use for 0, <tt>digits[1]</tt> represents the numeral
     *                to use for 1, etc.
     * @param buf     The <tt>StringBuilder</tt> to which to append the
     *                encoded number string
     *
     * @return the <tt>StringBuilder</tt>, for convenience
     *
     * @see #encodeNumber(short,int,char[],StringBuilder)
     * @see #encodeNumber(int,int,char[],StringBuilder)
     * @see #hexadecimalForNumber(int,StringBuilder)
     * @see #hexadecimalForNumber(short,StringBuilder)
     * @see #hexadecimalForNumber(long,StringBuilder)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static StringBuilder encodeNumber(long          num,
                                             int           base,
                                             char[]        digits,
                                             StringBuilder buf)
    {
        // Encode backwards; it's easier, though undoubtedly much slower.
        // Worst case scenario: Base is 2, and we need 64 digits.

        int      digit;
        char[]   temp = new char[64];
        int      i    = 0;

        while (num != 0)
        {
            digit = (int) (num % base);
            num   = num / base;
            temp[i++] = digits[digit];
        }

        // Special case: num was 0 to begin with

        if (i == 0)
            temp[i++] = digits[0];

        while (--i >= 0)
            buf.append(temp[i]);

        return buf;
    }

    /**
     * <p>Encode a short integer in an arbitrary base; the caller specifies
     * an array of digit characters to be used for the encoding. For
     * instance, to encode a number in base 36, you might use the following
     * code fragment:</p>
     *
     * <blockquote>
     * <pre>
     * {@code
     * StringBuilder buf = new StringBuilder();
     * short        num = ...
     * char[]       digits = new char[]
     *              {
     *                  '0', '1', '2', '3', '4', '5', '6', '7', '8',
     *                  '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
     *                  'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
     *                  'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
     *              };
     * TextUtils.encodeNumber (num, digits.length, digits, buf);
     * }
     * </pre>
     * </blockquote>
     *
     * <p>For convenience, this class provides a character array of
     * hexadecimal digits. (See {@link #HEXADECIMAL_DIGITS}.) However, it's
     * usually simpler just to use one of the <tt>hexadecimalForNumber()</tt>
     * methods().</p>
     *
     * @param num     The short integer to encode
     * @param base    The base in which to encode the number
     * @param digits  The array of digit characters to use for the encoding.
     *                This array must be at least <tt>base</tt> characters
     *                in length. <tt>digits[0]</tt> represents the numeral to
     *                use for 0, <tt>digits[1]</tt> represents the numeral
     *                to use for 1, etc.
     * @param buf     The <tt>StringBuilder</tt> to which to append the
     *                encoded number string
     *
     * @return the <tt>StringBuilder</tt>, for convenience
     *
     * @see #encodeNumber(int,int,char[],StringBuilder)
     * @see #encodeNumber(long,int,char[],StringBuilder)
     * @see #hexadecimalForNumber(int,StringBuilder)
     * @see #hexadecimalForNumber(short,StringBuilder)
     * @see #hexadecimalForNumber(long,StringBuilder)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static StringBuilder encodeNumber(short         num,
                                             int           base,
                                             char[]        digits,
                                             StringBuilder buf)
    {
        // Encode backwards; it's easier, though undoubtedly much slower.
        // Worst case scenario: Base is 2, and we need 16 digits.

        int      digit;
        char[]   temp = new char[16];
        int      i    = 0;

        while (num != 0)
        {
            digit = (num % base);
            num   = (short) (num / base);

            temp[i++] = digits[digit];
        }

        // Special case: num was 0 to begin with

        if (i == 0)
            temp[i++] = digits[0];

        while (--i >= 0)
            buf.append(temp[i]);

        return buf;
    }

    /**
     * <p>Encode an integer in an arbitrary base; the caller specifies an
     * array of digit characters to be used for the encoding. For instance,
     * to encode a number in base 36, you might use the following code
     * fragment:</p>
     *
     * <blockquote>
     * <pre>
     * {@code
     * StringBuilder buf = new StringBuilder();
     * int          num = ...
     * char[]       digits = new char[]
     *              {
     *                  '0', '1', '2', '3', '4', '5', '6', '7', '8',
     *                  '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
     *                  'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
     *                  'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
     *              };
     * TextUtils.encodeNumber (num, digits.length, digits, buf);
     * }
     * </pre>
     * </blockquote>
     *
     * <p>For convenience, this class provides a character array of
     * hexadecimal digits. (See {@link #HEXADECIMAL_DIGITS}.)
     * However, it's usually simpler just to use one of the
     * <tt>hexadecimalForNumber()</tt> methods().</p>
     *
     * @param num     The integer to encode
     * @param base    The base in which to encode the number
     * @param digits  The array of digit characters to use for the encoding.
     *                This array must be at least <tt>base</tt> characters
     *                in length. <tt>digits[0]</tt> represents the numeral to
     *                use for 0, <tt>digits[1]</tt> represents the numeral
     *                to use for 1, etc.
     * @param buf     The <tt>StringBuilder</tt> to which to append the
     *                encoded number string
     *
     * @return the <tt>StringBuilder</tt>, for convenience
     *
     * @see #encodeNumber(short,int,char[],StringBuilder)
     * @see #encodeNumber(long,int,char[],StringBuilder)
     * @see #hexadecimalForNumber(int,StringBuilder)
     * @see #hexadecimalForNumber(short,StringBuilder)
     * @see #hexadecimalForNumber(long,StringBuilder)
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static StringBuilder encodeNumber (int           num,
                                              int           base,
                                              char[]        digits,
                                              StringBuilder buf)
    {
        // Encode backwards; it's easier, though undoubtedly much slower.
        // Worst case scenario: Base is 2, and we need 32 digits.

        int      digit;
        char[]   temp = new char[32];
        int      i    = 0;

        while (num != 0)
        {
            digit = (num % base);
            num   = num / base;
            temp[i++] = digits[digit];
        }

        // Special case: num was 0 to begin with

        if (i == 0)
            temp[i++] = digits[0];

        while (--i >= 0)
            buf.append(temp[i]);

        return buf;
    }

    /**
     * <p>Takes a string of hexadecimal-encoded byte values and converts it
     * back to an array of bytes. The string may or may not contain white
     * space; all white space is stripped before parsing begins. However,
     * white space cannot occur between the digits for a byte; it must only
     * occur between bytes. That is, "6f a1" is legal, while "6 f a 1" is
     * not. Each byte must be encoded as a 2-digits hexadecimal value; as a
     * consequence, the input string must have an even number of
     * characters, once all white space has been stripped. The hexadecimal
     * digits "a" through "f" may be in upper- or lower-case. An illegal
     * hexadecimal string will result in a <tt>NumberFormatException</tt>.
     * The following strings are examples of legal input to this
     * method:</p>
     *
     * <blockquote>
     * <pre>
     * 1F
     * 66756c6c74696c742e636f6d
     * 42 72 69 61 6e 20 43 6c  61 70 70 65 72
     * 49 2D41 63 63656C
     * </pre>
     * </blockquote>
     *
     * <p>The following strings are examples of illegal input:</p>
     *
     * <blockquote>
     * <pre>
     * 1X
     * 6
     * 6f a 1
     * 6675-6c6c-7469-6c742e-636f6d
     * </pre>
     * </blockquote>
     *
     * @param hexString  the hex string to convert
     *
     * @return an array of bytes representing the decoded hex string
     *
     * @throws NumberFormatException if the hex string is invalid
     *
     * @see #hexadecimalForByte(byte,StringBuilder)
     * @see #hexadecimalForByte(int,StringBuilder)
     * @see #hexadecimalForBytes(byte[],int,int,StringBuilder)
     * @see #hexadecimalForBytes(int[],int,int,StringBuilder)
     */
    public static byte[] bytesForHexadecimal(String hexString)
        throws NumberFormatException
    {
        byte[] result = null;

        // Break the string into white-space delimited tokens, and process
        // the tokens. Each token must have an even number of characters.

        StringBuilder    stripped = new StringBuilder();
        StringTokenizer  tok      = new StringTokenizer (hexString);

        while (tok.hasMoreTokens())
            stripped.append(tok.nextToken());

        // Now, stripped contains the original string without white space.
        // Make sure it's the right length.

        int len = stripped.length();

        if ((len % 2) != 0)
        {
            throw new NumberFormatException("Hex string \"" + hexString +
                                            "\" does not have an even number " +
                                            "of hex digits.");
        }

        hexString = stripped.toString();

        // Allocate the resulting byte array.

        result = new byte[len / 2];

        // Now, parse the stripped string.

        int i;
        int j;
        for (i = 0, j = 0; i < len; i+= 2, j++)
        {
            String hexByte = hexString.substring(i, i + 2);

            // Note: We use Integer.parseInt(String,radix) because
            // Byte.parseByte(String,radix) won't properly parse anything
            // larger than "7f". Bytes hold values in the range [-128,
            // 127]. A value larger than "7f" ought to result in the sign
            // bit being set, but the Byte class uses the Integer class to
            // parse the value, and then range-checks the resulting
            // integer. In an integer, "80" doesn't set the sign bit
            // (though it would, in a byte), so it fails. There appears to
            // be no way to encode a negative byte value in hex so that it
            // can be read back in later, at least not using the standard
            // JDK. So we do it ourselves.

            result[j] = (byte) (Integer.parseInt(hexByte, 16) & 0x00ff);
        }

        return result;
    }

    /**
     * Get the (upper-case) Roman numeral string for a number.
     *
     * @param n  the number; must be positive
     *
     * @return the roman number string
     */
    public static String romanNumeralsForNumber(int n)
    {
        assert (n > 0);

        StringBuilder buf = new StringBuilder();

        for (RomanNumberTableEntry tableEntry : ROMAN_NUMBER_TABLE)
        {
            while (n >= tableEntry.quantity)
            {
                buf.append(tableEntry.romanString);
                n -= tableEntry.quantity;
            }
        }

        return buf.toString();
    }

    /**
     * Convert a character to its corresponding Unicode escape sequence.
     *
     * @param c   the character
     * @param buf where to store the result
     *
     * @return the contents of <tt>buf</tt>, as a string
     *
     * @see #charToUnicodeEscape(char)
     */
    public static String charToUnicodeEscape(char c, StringBuilder buf)
    {
        String hex = Integer.toHexString((int) c);

        buf.append("\\u");
        for (int i = hex.length(); i < 4; i++)
            buf.append('0');
        buf.append(hex);

        return buf.toString();
    }

    /**
     * Convert a character to its corresponding Unicode escape sequence.
     *
     * @param c   the character
     *
     * @return the Unicode escape sequence for the character
     *
     * @see #charToUnicodeEscape(char)
     */
    public static String charToUnicodeEscape(char c)
    {
        return charToUnicodeEscape(c, new StringBuilder());
    }

    /**
     * <p>Determine whether a character is printable. This method uses a simple
     * definition of "printable" that doesn't take into account specific
     * locales. A character is assumed to be printable if (a) it's in the Basic
     * Latin, Latin 1 Supplement, or Extended Latin A Unicode block, and
     * (b) its type, as returned by <tt>java.lang.Character.getType()</tt>
     * is one of:</p>
     *
     * <ul>
     *    <li><tt>Character.OTHER_PUNCTUATION</tt>
     *    <li><tt>Character.START_PUNCTUATION</tt>
     *    <li><tt>Character.END_PUNCTUATION</tt>
     *    <li><tt>Character.CONNECTOR_PUNCTUATION</tt>
     *    <li><tt>Character.CURRENCY_SYMBOL</tt>
     *    <li><tt>Character.MATH_SYMBOL</tt>
     *    <li><tt>Character.MODIFIER_SYMBOL</tt>
     *    <li><tt>Character.UPPERCASE_LETTER</tt>
     *    <li><tt>Character.LOWERCASE_LETTER</tt>
     *    <li><tt>Character.DECIMAL_DIGIT_NUMBER</tt>
     *    <li><tt>Character.SPACE_SEPARATOR</tt>
     *    <li><tt>Character.DASH_PUNCTUATION</tt>
     * </ul>
     *
     * <p>All other characters are assumed to be non-printable, even if
     * they could actually be printed in the current locale or on some
     * printer.
     *
     * @param c  the character to test
     *
     * @return whether or not it is non-
     */
    public static boolean isPrintable(char c)
    {
        boolean isPrintable = false;

        Character.UnicodeBlock  ublock;
        StringBuilder           result = new StringBuilder();

        ublock = Character.UnicodeBlock.of (c);
        if ((ublock == Character.UnicodeBlock.BASIC_LATIN) ||
            (ublock == Character.UnicodeBlock.LATIN_1_SUPPLEMENT) ||
            (ublock == Character.UnicodeBlock.LATIN_EXTENDED_A))
        {
            // Might be printable.

            int type = Character.getType (c);

            switch (type)
            {
                case Character.OTHER_PUNCTUATION:
                case Character.START_PUNCTUATION:
                case Character.END_PUNCTUATION:
                case Character.CONNECTOR_PUNCTUATION:
                case Character.CURRENCY_SYMBOL:
                case Character.MATH_SYMBOL:
                case Character.MODIFIER_SYMBOL:
                case Character.UPPERCASE_LETTER:
                case Character.LOWERCASE_LETTER:
                case Character.DECIMAL_DIGIT_NUMBER:
                case Character.DASH_PUNCTUATION:
                case Character.SPACE_SEPARATOR:
                    isPrintable = true;
                    break;
            }
        }

        return isPrintable;
    }
}
