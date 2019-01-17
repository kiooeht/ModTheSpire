package org.clapper.util.regex;

import org.clapper.util.misc.LRUMap;
import org.clapper.util.text.TextUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p>This is a utility class implementing some common regular
 * expression-based operations, using the <tt>java.util.regex</tt> classes.
 * The various operations are briefly described here; see the individual
 * methods for full details.</p>
 *
 * <h3>Substitution</h3>
 *
 * <p>The {@link #substitute} method implements Perl-like regular expression
 * substitution. It takes an edit string representing the substitution,
 * and a string to be edited. It returns the possibly edited string. The
 * substitution syntax is similar to Perl:</p>
 *
 * <blockquote><pre>s/regex/replacement/[g][i][m][o][x]</pre></blockquote>
 *
 * <p>The regular expressions compiled once, and the compiled versions are
 * cached in an internal LRU buffer. The buffer's size is fixed at the time
 * of instantiation.</p>
 *
 * <p>See the documentation for the {@link #substitute} method for full
 * details.</p>
 */
public class RegexUtil
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Default size of the internal LRU buffer that is used to hold
     * compiled regular expressions. The buffer will never contain any more
     * than this many compiled regular expressions. Once the buffer is
     * full, any newly compiled regular expression (e.g., as a result of a
     * {@link #substitute} call made with a new expression) will replace
     * the oldest (least recently used) item in the buffer.
     */
    public static final int DEFAULT_LRU_BUFFER_SIZE = 20;

    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    private class Substitution
    {
        int      flags = 0;          // java.util.regex.Pattern.compile() flags
        boolean  replaceAll = false; // global or once-only
        String   regex = null;
        int      hc    = -1;

        Substitution (String regex)
        {
            this.regex = regex;
        }

        public synchronized int hashCode()
        {
            if (hc == -1)
            {
                StringBuffer buf = new StringBuffer();

                buf.append (regex);
                buf.append (String.valueOf (replaceAll));
                buf.append (String.valueOf (flags));

                hc = buf.toString().hashCode();
            }

            return hc;
        }

        public boolean equals (Object o)
        {
            boolean eq = false;

            if (o instanceof Substitution)
            {
                eq = (regex.equals (((Substitution) o).regex)) &&
                     (flags == ((Substitution) o).flags) &&
                     (replaceAll == ((Substitution) o).replaceAll);
            }

            return eq;
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private LRUMap<Substitution, Pattern> compiledRegexps =
        new LRUMap<Substitution, Pattern> (100);

    /*----------------------------------------------------------------------*\
                               Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>RegexUtil</tt> object. The object's internal
     * LRU buffer will cache up to 100 substitution regular expressions.
     */
    public RegexUtil()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * <p>This method implements Perl-like regular expression substitution. It
     * takes an edit string representing the substitution, and a string to
     * be edited. It returns the possibly edited string. The substitution
     * syntax is similar to Perl:</p>
     *
     * <blockquote><pre>s/regex/replacement/[g][i][m][o][x]</pre></blockquote>
     *
     * <p>The regular expressions compiled once, and the compiled versions are
     * cached in an internal LRU buffer. The buffer's size is fixed at the time
     * of instantiation.</p>
     *
     * <p>Any non-alphabetic, printing character may be used in place of the
     * slashes. The modifiers generally have the same meanings as in Perl,
     * though some of them aren't actually supported (but are present solely
     * for syntactical compatibility).</p>
     *
     * <table>
     *   <caption>Modifiers and Meanings</caption>
     *   <tr>
     *     <th>Modifier</th>
     *     <th>Meaning</th>
     *   </tr>
     *
     *   <tr>
     *     <td>g</td>
     *     <td>Substitute for all occurrences of the regular expression.
     *         not just the first one.</td>
     *   </tr>
     *
     *   <tr>
     *     <td>i</td>
     *     <td>Do case-insensitive pattern matching. This modifier corresponds
     *         to the <tt>java.util.regex.Pattern.CASE_INSENSITIVE</tt> flag.
     *     </td>
     *   </tr>
     *
     *   <tr>
     *     <td>m</td>
     *     <td>Treat the string is consisting of multiple lines. This modifier
     *         corresponds to the <tt>java.util.regex.Pattern.MULTILINE</tt>
     *         flag. It changes the meaning of "^" and "$" so that they
     *         match just after or just before, respectively, a line
     *         terminator or the end of the input sequence. By default
     *         these expressions only match at the beginning and the end of
     *         the entire input sequence.
     *      </td>
     *   </tr>
     *
     *   <tr>
     *     <td>o</td>
     *     <td>Compile once. This modifier is ignored, since regular
     *         expressions are always compiled once and stored in the
     *         internal LRU buffer.</td>
     *   </tr>
     *
     *   <tr>
     *     <td>u</td>
     *     <td>Enables Unicode-aware case folding. This modifier corresponds
     *         to the <tt>java.util.regex.UNICODE_CASE</tt> flag. When this
     *         modifier is specified, case-insensitive matching, when
     *         enabled by the CASE_INSENSITIVE flag, is done in a manner
     *         consistent with the Unicode Standard. By default,
     *         case-insensitive matching assumes that only characters in
     *         the US-ASCII charset are being matched. Specifying this flag
     *         may impose a performance penalty.
     *     </td>
     *   </tr>
     *
     *   <tr>
     *     <td>x</td>
     *     <td>Permits whitespace and comments in a pattern. This modifier
     *         corresponds to the <tt>java.util.regex.Pattern.COMMENTS</tt>
     *         flag. When this mode is active, whitespace is ignored, and
     *         embedded comments starting with # are ignored until the end
     *         of a line.
     *     </td>
     *   </tr>
     * </table>
     *
     * @param substitutionCommand   the "s///" substitution command
     * @param s                     string to edit
     *
     * @return the possibly edited string
     *
     * @throws RegexException bad expression, bad regular expression, etc.
     */
    public String substitute(String substitutionCommand, String s)
        throws RegexException
    {
        // Minimum size: 5 (s/a//)

        if (substitutionCommand.length() < 5)
        {
            throw new RegexException(Package.BUNDLE_NAME,
                                     "RegexUtil.substitutionCommandTooShort",
                                     "Substitution command \"{0}\" is too " +
                                     "short.",
                                     new Object[] {substitutionCommand});
        }

        if (substitutionCommand.charAt(0) != 's')
        {
            throw new RegexException(Package.BUNDLE_NAME,
                                     "RegexUtil.badSubstitutionSyntax",
                                     "\"{0}\" is a syntactically incorrect " +
                                     "substitution command.",
                                     new Object[] {substitutionCommand});
        }

        char delim = substitutionCommand.charAt(1);

        if (Character.isWhitespace(delim) || (Character.isLetter(delim)))
        {
            throw new RegexException(Package.BUNDLE_NAME,
                                     "RegexUtil.badSubstitutionDelim",
                                     "Substitution command \"{0}\" uses " +
                                     "alphabetic or white-space delimiter " +
                                     "\"{1}\".",
                                      new Object[]
                                      {
                                          substitutionCommand,
                                          String.valueOf (delim)
                                      });
        }

        String[] fields = TextUtil.split(substitutionCommand, delim, true);
        if ((fields.length != 3) && (fields.length != 4))
        {
            throw new RegexException(Package.BUNDLE_NAME,
                                     "RegexUtil.badSubstitutionSyntax",
                                     "\"{0}\" is a syntactically incorrect " +
                                     "substitution command.",
                                     new Object[] {substitutionCommand});
        }

        assert ((fields[0].length() == 1) && (fields[0].charAt(0) == 's'));

        String regex = fields[1];
        String replacement = fields[2];
        Substitution sub = new Substitution(regex);

        if (fields.length == 4)
            getSubstitutionFlags(substitutionCommand, fields[3], sub);

        Pattern pattern = (Pattern) compiledRegexps.get(sub);
        if (pattern == null)
        {
            try
            {
                pattern = Pattern.compile(regex, sub.flags);
                compiledRegexps.put(sub, pattern);
            }

            catch (PatternSyntaxException ex)
            {
                throw new RegexException (ex);
            }
        }

        String result = s;
        Matcher matcher = pattern.matcher(s);

        // Note that Matcher.replaceFirst() and Matcher.replaceAll() handle
        // group replacement tokens $1, $2, etc.

        if (sub.replaceAll)
            result = matcher.replaceAll(replacement);
        else
            result = matcher.replaceFirst(replacement);

        return result;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void getSubstitutionFlags (String       substitutionCommand,
                                       String       sFlags,
                                       Substitution sub)
        throws RegexException
    {
        char[] modifiers = sFlags.toCharArray();

        for (int i = 0; i < modifiers.length; i++)
        {
            char mod = modifiers[i];
            switch (mod)
            {
                case 'g':
                    sub.replaceAll = true;
                    break;

                case 'i':
                    sub.flags |= Pattern.CASE_INSENSITIVE;
                    break;

                case 'm':
                    sub.flags |= Pattern.MULTILINE;
                    break;

                case 'o':
                    // N/A
                    break;

                case 'x':
                    sub.flags |= Pattern.COMMENTS;
                    break;

                default:
                    throw new RegexException
                        (Package.BUNDLE_NAME,
                         "RegexUtil.badSubstitutionModifier",
                         "Substitution command \"{0}\" has unknown modifier " +
                         "character\"{1}\".",
                         new Object[]
                             {
                                 substitutionCommand,
                                 String.valueOf (mod)
                             });
            }
        }
    }
}
