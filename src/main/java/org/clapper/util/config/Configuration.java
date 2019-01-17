package org.clapper.util.config;

import java.io.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import java.net.MalformedURLException;
import java.net.URL;

import org.clapper.util.logging.Logger;

import org.clapper.util.text.TextUtil;
import org.clapper.util.text.UnixShellVariableSubstituter;
import org.clapper.util.text.VariableDereferencer;
import org.clapper.util.text.VariableNameChecker;
import org.clapper.util.text.VariableSubstitutionException;
import org.clapper.util.text.VariableSubstituter;
import org.clapper.util.text.XStringBuffer;

import org.clapper.util.io.FileUtil;

/**
 * <p><tt>Configuration</tt> implements a parser, generator and in-memory
 * store for a configuration file whose syntax is reminiscent of classic
 * Windows .INI files, though with many extensions.</p>
 *
 * <h3>Syntax</h3>
 *
 * <p>A configuration file is broken into sections, and each section is
 * introduced by a section name in brackets. For example:</p>
 *
 * <blockquote><pre>
 * [main]
 * installation.directory=/usr/local/foo
 * program.directory: /usr/local/foo/programs
 *
 * [search]
 * searchCommand: find /usr/local/foo -type f -name '*.class'
 *
 * [display]
 * searchFailedMessage=Search failed, sorry.
 * </pre></blockquote>
 *
 * <p>Notes and caveats:</p>
 *
 * <ul>
 *   <li> At least one section is required.
 *   <li> Sections may be empty.
 *   <li> It is an error to have any variable definitions before the first
 *        section header.
 *   <li> The section name "system" is reserved. It doesn't really exist, but
 *        it's used during variable substitution (see below) to substitute from
 *        <tt>System.properties</tt>.
 *   <li> The section name "program" is reserved. It doesn't really exist, but
 *        it's used during variable substitution to substitute certain canned
 *        values, such as the running process's working directory.
 * </ul>
 *
 * <h4>Section Name Syntax</h4>
 *
 * <p>There can be any amount of whitespace before and after the brackets
 * in a section name; the whitespace is ignored. Section names may consist
 * of alphanumeric characters and periods. Anything else is not
 * permitted.</p>
 *
 * <h4>Variable Syntax</h4>
 *
 * <p>Each section contains zero or more variable settings. Similar to a
 * <tt>Properties</tt> file, the variables are specified as name/value
 * pairs, separated by an equal sign ("=") or a colon (":"). Variable names
 * are case-sensitive and may contain any printable character (including
 * white space), other than '$', '{', and '}' Variable values may contain
 * anything at all. The parser ignores whitespace on either side of the "="
 * or ":"; that is, leading whitespace in the value is skipped. The way to
 * include leading whitespace in a value is escape the whitespace
 * characters with backslashes. (See below).</p>
 *
 * <h4>Continuation Lines</h4>
 *
 * <p>Variable definitions may span multiple lines; each line to be
 * continued must end with a backslash ("\") character, which escapes the
 * meaning of the newline, causing it to be treated like a space character.
 * The following line is treated as a logical continuation of the first
 * line; however, any leading whitespace is removed from continued lines.
 * For example, the following four variable assignments all have the
 * same value:
 *
 * <blockquote><pre>
 * [test]
 * a: one two three
 * b:            one two three
 * c: one two \
 * three
 * d:        one \
 *                         two \
 *    three
 * </pre></blockquote>
 *
 * <p>Because leading whitespace is skipped, all four variables have the
 * value "one two three".</p>
 *
 * <p>Only variable definition lines may be continued. Section header
 * lines, comment lines (see below) and include directives (see below)
 * cannot span multiple lines.</p>
 *
 * <h4>Expansions of Variable Values</h4>
 *
 * <p>The configuration parser preprocesses each variable's value,
 * replacing embedded metacharacter sequences and substituting variable
 * references. You can use backslashes to escape the special characters
 * that the parser uses to recognize metacharacter and variable sequences;
 * you can also use single quotes. See <a href="#RawValues">Suppressing
 * Metacharacter Expansion and Variable Substitution</a>, below, for more
 * details.</p>
 *
 * <h5>Metacharacters</h5>
 *
 * <p>The parser recognizes Java-style ASCII escape sequences <tt>\t</tt>,
 * <tt>\n</tt>, <tt>\r</tt>, <tt>\\</tt>, <tt>\"</tt>, <tt>\'</tt>,
 * <tt>\&nbsp;</tt> (a backslash and a space), and
 * <tt>&#92;u</tt><i>xxxx</i> are recognized and converted to single
 * characters. Note that metacharacter expansion is performed <i>before</i>
 * variable substitution.</p>
 *
 * <h5>Variable Substitution</h5>
 *
 * <p>A variable value can interpolate the values of other variables, using
 * a variable substitution syntax. The general form of a variable reference
 * is <tt>${sectionName:varName?default}</tt>.</p>
 *
 * <ul>
 *   <li><tt>sectionName</tt> is the name of the section containing the
 *       variable to substitute; if omitted, it defaults to the current
 *       section.
 *   <li><tt>varName</tt> is the name of the variable to substitute.
 *   <li><tt>default</tt> is the default value for the variable, if the
 *       variable is undefined. If omitted, a reference to an undefined
 *       variable (or undefined section) will either result in an exception
 *       or will be replaced with  an empty string, depending on the setting
 *       of the "abort on undefined value" flag. See
 *       {@link #setAbortOnUndefinedVariable}.
 * </ul>
 *
 * <p>If a variable reference specifies a section name, the referenced section
 * must precede the current section. It is not possible to substitute the value
 * of a variable in a section that occurs later in the file.</p>
 *
 * <p>The section names "system", "env", and "program" are reserved for
 * special "pseudosections."</p>
 *
 * <p>The "system" pseudosection is used to interpolate values from Java's
 * <tt>System.properties</tt> class. For instance,
 * <tt>${system:user.home}</tt> substitutes the value of the
 * <tt>user.home</tt> system property (typically, the home directory of the
 * user running <i>curn</i>). Similarly, <tt>${system:user.name}</tt>
 * substitutes the user's name.</p>
 *
 * <p>For example:</p>
 *
 * <blockquote><pre>
 * [main]
 * installation.directory=${system:user.home?/tmp}/this_package
 * program.directory: ${installation.directory}/foo/programs
 *
 * [search]
 * searchCommand: find ${main:installation.directory} -type f -name '*.class'
 *
 * [display]
 * searchFailedMessage=Search failed, sorry.
 * </pre></blockquote>
 *
 * <p>The "env" pseudosection is used to interpolate values from the
 * environment. On UNIX systems, for instance, <tt>${env:HOME}</tt>
 * substitutes user's home directory (and is, therefore, a synonym for
 * <tt>${system:user.home}</tt>. On some versions of Windows,
 * <tt>${env:USERNAME}</tt> will substitute the name of the user running
 * <i>curn</i>. Note: On UNIX systems, environment variable names are
 * typically case-sensitive; for instance, <tt>${env:USER}</tt> and
 * <tt>${env:user}</tt> refer to different environment variables. On
 * Windows systems, environment variable names are typically
 * case-insensitive; <tt>${env:USERNAME}</tt> and <tt>${env:username}</tt>
 * are equivalent.</p>
 *
 * <p>The "program" pseudosection is a placeholder for various special
 * variables provided by the <tt>Configuration</tt> class. Those variables
 * are:</p>
 *
 * <table border="1" class="nested-table">
 *   <caption>Variables</caption>
 *   <tr>
 *     <th>Variable</th>
 *     <th>Description</th>
 *     <th>Examples</th>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>cwd</tt></td>
 *     <td>
 *        the program's current working directory. Thus,
 *        <tt>${program:cwd}</tt> will substitute the working directory,
 *        with the appropriate system-specific file separator. On a Windows
 *        system, the file separator character (a backslash) will be doubled,
 *        to ensure that it is properly interpreted by the configuration file
 *        parsing logic.
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>cwd.url</tt></td>
 *     <td>
 *        the program's current working directory as a <tt>file</tt> URL,
 *        without the trailing "/". Useful when you need to create a URL
 *        reference to something relative to the current directory. This is
 *        especially useful on Windows, where
 *        <blockquote><pre>file://${program:cwd}/something.txt</pre></blockquote>
 *         produces an invalid URL, with a mixture of backslashes and
 *         forward slashes.  By contrast,
 *         <blockquote><pre>${program:cwd.url}/something.txt</pre></blockquote>
 *         always produces a valid URL, regardless of the underlying host
 *         operating system.
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>now</tt></td>
 *     <td>
 *        the current time, formatted by calling
 *        <tt>java.util.Date.toString()</tt> with the default locale.
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *
 *   <tr>
 *     <td>
 *       <tt>now</tt> <i>delim</i> <i>fmt</i> [<i>delim</i> <i>lang delim country</i>]]
 *     </td>
 *     <td>
 *        <p>The current date/time, formatted with the specified
 *        <tt>java.text.SimpleDateFormat</tt> format string. If specified,
 *        the given locale and country code will be used; otherwise, the
 *        default system locale will be used. <i>lang</i> is a Java language
 *        code, such as "en", "fr", etc. <i>country</i> is a 2-letter country
 *        code, e.g., "UK", "US", "CA", etc. <i>delim</i> is a user-chosen
 *        delimiter that separates the variable name ("<tt>now</tt>") from the
 *        format and the optional locale fields. The delimiter can be anything
 *        that doesn't appear in the format string, the variable name, or
 *        the locale.</p>
 *
 *        <p>Note: <tt>SimpleDateFormat</tt> requires that literal strings
 *        (i.e., strings that should not be processed as part of the format)
 *        be enclosed in quotes. For instance:</p>
 *
 *        <blockquote><pre>yyyy.MM.dd 'at' hh:mm:ss z</pre></blockquote>
 *
 *        <p>Because single quotes are special characters in configuration
 *        files, it's important to escape them if you use them inside date
 *        formats. So, to include the above string in a configuration
 *        file's <tt>${program:now}</tt> reference, use the following:</p>
 *
 *        <blockquote><pre>${program:now/yyyy.MM.dd \'at\' hh:mm:ss z}</pre></blockquote>
 *
 *        <p>See <a href="#RawValues">Suppressing Metacharacter Expansion
 *        and Variable Substitution</a>, below, for more details.</p>
 *     </td>
 *     <td>
 * <pre> ${program:now|yyyy.MM.dd 'at' hh:mm:ss z}
 * ${program:now|yyyy/MM/dd 'at' HH:mm:ss z|en|US}
 * ${program:now|dd MMM, yyyy hh:mm:ss z|fr|FR}</pre>
 *     </td>
 *   </tr>
 * </table>
 *
 * <p>Notes and caveats:</p>
 *
 * <ul>
 *   <li> <tt>Configuration</tt> uses the
 *        {@link UnixShellVariableSubstituter} class to do variable
 *        substitution, so it honors all the syntax conventions supported
 *        by that class.
 *
 *   <li> A variable that directly or indirectly references itself via
 *        variable substitution will cause the parser to throw an exception.
 *
 *   <li> Variable substitutions are only permitted within variable
 *        values and include targets (see below). They are ignored in variable
 *        names, section names, and comments.
 *
 *   <li> Variable substitution is performed <i>after</i> metacharacter
 *        expansion (so don't include metacharacter sequences in your variable
 *        names).
 *
 *   <li> To include a literal "$" character in a variable value, escape
 *        it with a backslash, e.g., "<tt>var=value with \$ dollar sign</tt>"
 * </ul>
 *
 * <h5><a name="RawValues">Suppressing Metacharacter Expansion and Variable
 * Substitution</a></h5>
 *
 * <p>To prevent the parser from interpreting metacharacter sequences,
 * variable substitutions and other special characters, enclose part or
 * all of the value in single quotes.
 * <p>For example, suppose you want to set variable "prompt" to the
 * literal value "Enter value. To specify a newline, use \n." The following
 * configuration file line will do the trick:</p>
 *
 * <blockquote><pre>prompt: 'Enter value. To specify a newline, use \n'
 * </pre></blockquote>
 *
 * <p>Similarly, to set variable "abc" to the literal string "${foo}"
 * suppressing the parser's attempts to expand "${foo}" as a variable
 * reference, you could use:</p>
 *
 * <blockquote><pre>abc: '${foo}'</pre></blockquote>
 *
 * <p>To include a literal single quote, you must escape it with a
 * backslash.</p>
 *
 * <p>Note: It's also possible, though hairy, to escape the special meaning
 * of special characters via the backslash character. For instance, you can
 * escape the variable substitution lead-in character, '$', with a
 * backslash. e.g., "\$". This technique is not recommended, however,
 * because you have to double-escape any backslash characters that you want
 * to be preserved literally. For instance, to get "\t", you must specify
 * "\\\\t". To get a literal backslash, specify "\\\\". (Yes, that's four
 * backslashes, just to get a single unescaped one.) This double-escaping
 * is a regrettable side effect of how the configuration file parses
 * variable values: It makes two separate passes over the value (one for
 * metacharacter expansion and another for variable expansion). Each of
 * those passes honors and processes backslash escapes. This problem would
 * go away if the configuration file parser parsed both metacharacter
 * sequences and variable substitutions itself, in one pass. It doesn't
 * currently do that, because I wanted to make use of the existing
 * {@link XStringBuffer#decodeMetacharacters()} method and the
 * {@link UnixShellVariableSubstituter} class. In general, you're better off
 * just sticking with single quotes.</p>
 *
 * <h5>Double Quotes</h5>
 *
 * <p>Double quotes can be used to escape the special meaning of white
 * space, while still permitting metacharacters and variable references to
 * be expanded. (Metacharacter and variable references are not expanded
 * between single quotes.) When retrieving a variable's value via
 * {@link #getConfigurationValue}, a program will not be able to tell whether
 * double quotes were used or not, since {@link #getConfigurationValue}
 * returns the "cooked" value as a single string. However, callers can use
 * the {@link #getConfigurationTokens} method to retrieve the parsed tokens
 * that comprise a configuration value. Double- and single-quoted strings are
 * returned as individual tokens.</p>
 *
 * <h4>Includes</h4>
 *
 * <p>A special include directive permits inline inclusion of another
 * configuration file. The include directive takes two forms:
 *
 * <blockquote><pre>
 * %include "path"
 * %include "URL"
 * </pre></blockquote>
 *
 * <p>For example:</p>
 *
 * <blockquote><pre>
 * %include "/home/bmc/mytools/common.cfg"
 * %include "http://configs.example.com/mytools/common.cfg"
 * </pre></blockquote>
 *
 * <p>The included file may contain any content that is valid for this
 * parser. It may contain just variable definitions (i.e., the contents of
 * a section, without the section header), or it may contain a complete
 * configuration file, with individual sections. Since
 * <tt>Configuration</tt> recognizes a variable syntax that is
 * essentially identical to Java's properties file syntax, it's also legal
 * to include a properties file, provided it's included within a valid
 * section.</p>
 *
 * <p>Note: Attempting to include a file from itself, either directly or
 * indirectly, will cause the parser to throw an exception.</p>
 *
 * <h4>Comments and Blank Lines</h4>
 *
 * <p>A comment line is a one whose first non-whitespace character is a "#"
 * or a "!". This comment syntax is identical to the one supported by a
 * Java properties file. A blank line is a line containing no content, or
 * one containing only whitespace. Blank lines and comments are ignored.</p>
 */
public class Configuration
    implements VariableDereferencer, VariableNameChecker
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    private static final String COMMENT_CHARS              = "#!";
    private static final char   SECTION_START              = '[';
    private static final char   SECTION_END                = ']';
    private static final String INCLUDE                    = "%include";
    private static final int    MAX_INCLUDE_NESTING_LEVEL  = 50;
    private static final String SYSTEM_SECTION_NAME        = "system";
    private static final String PROGRAM_SECTION_NAME       = "program";
    private static final String ENV_SECTION_NAME           = "env";
    private static final int    SYSTEM_SECTION_ID          = 0;
    private static final int    PROGRAM_SECTION_ID         = 1;
    private static final int    ENV_SECTION_ID             = 2;
    private static final int    FIRST_CONFIG_SECTION_ID    = 3;

    /*----------------------------------------------------------------------*\
                                  Classes
    \*----------------------------------------------------------------------*/

    /**
     * Line types
     */
    private static enum LineType
    {
        COMMENT,
        INCLUDE,
        SECTION,
        VARIABLE,
        BLANK
    }

    /**
     * Contains one logical input line.
     */
    private static class Line
    {
        int            number = 0;
        LineType       type   = LineType.COMMENT;
        XStringBuffer  buffer = new XStringBuffer();

        Line()
        {
            // Nothing to do
        }

        void newLine()
        {
            buffer.setLength (0);
        }
    }

    /**
     * Context for variable substitution
     */
    private class SubstitutionContext
    {
        Variable currentVariable;
        int totalSubstitutions = 0;

        SubstitutionContext (Variable v)
        {
            currentVariable = v;
        }
    }

    /**
     * Container for data used only during parsing.
     */
    private class ParseContext
    {
        /**
         * Current section. Only set during parsing.
         */
        Section currentSection = null;

        /**
         * Current variable name being processed. Used during the variable
         * substitution parsing phase.
         */
        Variable currentVariable = null;

        /**
         * Current include file nesting level. Used as a fail-safe during
         * parsing.
         */
        int includeFileNestingLevel = 0;

        /**
         * Table of files/URLs currently open. Used during include
         * processing.
         */
        Set<String> openURLs = new HashSet<String>();

        ParseContext()
        {
            // Nothing to do
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The URL of the configuration file, if available
     */
    private URL configURL = null;

    /**
     * List of sections, in order encountered. Each element is a reference to
     * a Section object.
     */
    private List<Section> sectionsInOrder = new ArrayList<Section>();

    /**
     * Sections by name. Each index is a string. Each value is a reference to
     * a Section object.
     */
    private Map<String, Section> sectionsByName =
                                             new HashMap<String, Section>();

    /**
     * Special section for System.properties
     */
    private static Section systemSection;

    /**
     * Special section for program properties
     */
    private Section programSection;

    /**
     * Special section for env properties
     */
    private Section envSection;

    /**
     * Section ID values.
     */
    private int nextSectionIDValue = FIRST_CONFIG_SECTION_ID;

    /**
     * For logging
     */
    private static final Logger log = new Logger (Configuration.class);

    /**
     * Whether or not to abort if a variable is undefined
     */
    private boolean abortOnUndefinedVariable = true;

    /**
     * The substituter
     */
    private UnixShellVariableSubstituter varSubstituter =
        new UnixShellVariableSubstituter();

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct an empty <tt>Configuration</tt> object. The object may
     * later be filled with configuration data via one of the <tt>load()</tt>
     * methods, or by calls to {@link #addSection addSection()} and
     * {@link #setVariable setVariable()}.
     */
    public Configuration()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Add a new section to this configuration data.
     *
     * @param sectionName  the name of the new section
     *
     * @throws SectionExistsException a section by that name already exists
     *
     * @see #containsSection
     * @see #getSectionNames
     * @see #setVariable
     */
    public void addSection (String sectionName)
        throws SectionExistsException
    {
        if (sectionsByName.get (sectionName) != null)
            throw new SectionExistsException (sectionName);

        makeNewSection (sectionName);
    }

    /**
     * Clear this object of all configuration data.
     */
    public void clear()
    {
        sectionsInOrder.clear();
        sectionsByName.clear();
        configURL = null;
    }

    /**
     * Determine whether this object contains a specified section.
     *
     * @param sectionName  the section name
     *
     * @return <tt>true</tt> if the section exists in this configuration,
     *         <tt>false</tt> if not.
     *
     * @see #getSectionNames
     * @see #addSection
     */
    public final boolean containsSection (String sectionName)
    {
        return (sectionsByName.get (sectionName) != null);
    }

    /**
     * Get the URL of the configuration file, if available.
     *
     * @return the URL of the configuration file, or null if the file
     *         was parsed from an <tt>InputStream</tt>
     */
    public URL getConfigurationFileURL()
    {
        return configURL;
    }

    /**
     * Get the names of the sections in this object, in the order they were
     * parsed and/or added.
     *
     * @param collection  the <tt>Collection</tt> to which to add the section
     *                    names. The names are added in the order they were
     *                    parsed and/or added to this object; of course, the
     *                    <tt>Collection</tt> may reorder them.
     *
     * @return the <tt>collection</tt> parameter, for convenience
     *
     * @see #getVariableNames
     */
    public Collection<String> getSectionNames (Collection<String> collection)
    {
        for (Section section : sectionsInOrder)
            collection.add (section.getName());

        return collection;
    }

    /**
     * Get the names of the sections in this object, in the order they were
     * parsed and/or added.
     *
     * @return a new <tt>Collection</tt> of section names
     *
     * @see #getVariableNames
     */
    public Collection<String> getSectionNames()
    {
        return getSectionNames (new ArrayList<String>());
    }

    /**
     * Get the names of the all the variables in a section, in the order
     * they were parsed and/or added.
     *
     * @param sectionName the name of the section to access
     * @param collection  the <tt>Collection</tt> to which to add the variable
     *                    names. The names are added in the order they were
     *                    parsed and/or added to this object; of course, the
     *                    <tt>Collection</tt> may reorder them.
     *
     * @return the <tt>collection</tt> parameter, for convenience
     *
     * @throws NoSuchSectionException  no such section
     *
     * @see #getSectionNames
     * @see #containsSection
     * @see #getVariableValue
     */
    public Collection<String> getVariableNames (String             sectionName,
                                                Collection<String> collection)
        throws NoSuchSectionException
    {
        Section section = sectionsByName.get (sectionName);
        if (section == null)
            throw new NoSuchSectionException (sectionName);

        collection.addAll (section.getVariableNames());

        return collection;
    }

    /**
     * Get the names of the all the variables in a section, in the order
     * they were parsed and/or added.
     *
     * @param sectionName the name of the section to access
     *
     * @return a new <tt>Collection</tt> of variable names
     *
     * @throws NoSuchSectionException  no such section
     *
     * @see #getSectionNames
     * @see #containsSection
     * @see #getVariableValue
     */
    public Collection<String> getVariableNames (String sectionName)
        throws NoSuchSectionException
    {
        return getVariableNames (sectionName, new ArrayList<String>());
    }

    /**
     * Get the value for a variable. This method returns a "collapsed"
     * value, with any quotes stripped. It's impossible to tell where
     * quoted substrings appeared. If you need to know where individual
     * tokens begin and end, use the {@link #getConfigurationTokens}
     * method. For example, if the configuration line looks like this:
     *
     * <blockquote><pre>foo: abc "def ghi" jkl</pre></blockquote>
     *
     * this method will return the string
     *
     * <blockquote><pre>abc def ghi jkl</pre></blockquote>
     *
     * whereas {@link #getConfigurationTokens} will return the following
     * individual tokens:
     *
     * <blockquote><pre>
     * abc
     * def ghi
     * jkl
     * </pre></blockquote>
     *
     * Getting the tokens preserves the white space-escaping properties of
     * the double quotes.
     *
     * @param sectionName   the name of the section containing the variable
     * @param variableName  the variable name
     *
     * @return the value for the variable (which may be the empty string)
     *
     * @throws NoSuchSectionException  the named section does not exist
     * @throws NoSuchVariableException the section has no such variable
     *
     * @see #getConfigurationTokens
     */
    public String getConfigurationValue (String sectionName,
                                         String variableName)
        throws NoSuchSectionException,
               NoSuchVariableException
    {
        Section section = sectionsByName.get (sectionName);
        if (section == null)
            throw new NoSuchSectionException (sectionName);

        Variable variable = null;

        try
        {
            variable = section.getVariable (variableName);
        }

        catch (ConfigurationException ex)
        {
        }

        if (variable == null)
            throw new NoSuchVariableException (sectionName, variableName);

        return variable.getCookedValue();
    }

    /**
     * Get the raw value (i.e., without any substitutions) for a variable.
     *
     * @param sectionName   the name of the section containing the variable
     * @param variableName  the variable name
     *
     * @return the value for the variable (which may be the empty string)
     *
     * @throws NoSuchSectionException  the named section does not exist
     * @throws NoSuchVariableException the section has no such variable
     *
     * @see #getConfigurationValue
     */
    public String getRawValue (String sectionName, String variableName)
        throws NoSuchSectionException,
               NoSuchVariableException
    {
        Section section = sectionsByName.get (sectionName);
        if (section == null)
            throw new NoSuchSectionException (sectionName);

        Variable variable = null;

        try
        {
            variable = section.getVariable (variableName);
        }

        catch (ConfigurationException ex)
        {
        }

        if (variable == null)
            throw new NoSuchVariableException (sectionName, variableName);

        return variable.getRawValue();
    }

    /**
     * Get the value for a variable as a series of tokens. This is the
     * method to use if you need to retain any white space between quotes.
     * With the {@link #getConfigurationValue} method, it's impossible to
     * tell where quoted substrings appeared. For example, if the
     * configuration line looks like this:
     *
     * <blockquote><pre>foo: abc "def ghi" jkl mno</pre></blockquote>
     *
     * {@link #getConfigurationValue} will return the string
     *
     * <blockquote><pre>abc def ghi jkl mno</pre></blockquote>
     *
     * whereas this method will return the following individual tokens:
     *
     * <blockquote><pre>
     * abc
     * def ghi
     * jkl
     * mno
     * </pre></blockquote>
     *
     * <p>Getting the tokens preserves the white space-escaping properties of
     * the double quotes.</p>
     *
     * @param sectionName   the name of the section containing the variable
     * @param variableName  the variable name
     *
     * @return the value tokens for the variable, or null if the value
     *         exists but was empty
     *
     * @throws ConfigurationException  some configuration error
     *
     * @see #getConfigurationValue
     *
     * @since Version 2.1.3
     */
    public String[] getConfigurationTokens (String sectionName,
                                            String variableName)
        throws ConfigurationException
    {
        Section section = sectionsByName.get (sectionName);
        if (section == null)
            throw new NoSuchSectionException (sectionName);

        Variable variable = null;

        try
        {
            variable = section.getVariable (variableName);
        }

        catch (ConfigurationException ex)
        {
            log.error ("Can't get value for variable \"" +
                       variableName +
                       "\" in section \"" +
                       sectionName +
                       "\"",
                       ex);
        }

        if (variable == null)
            throw new NoSuchVariableException (sectionName, variableName);

        // The "cooked" tokens are split into literal (i.e., quoted) and
        // non-quoted pieces. This is almost never what a caller wants.
        // Instead, the caller wants individual tokens, with quoted parts
        // represented as a single token. For instance, given:
        //
        //      abc def ghi
        //
        // the caller wants:
        //
        //      abc
        //      def
        //      ghi
        //
        // Similarly, given:
        //
        //      abc "def ghi" jkl
        //
        // the caller wants:
        //
        //      abc
        //      def ghi
        //      jkl
        //
        // Fortunately, the parser keeps track of whether a segment was
        // literal or not, so this is easy to manufacture.

        ValueSegment[] cookedSegments = variable.getCookedSegments();
        ArrayList<String> result = new ArrayList<String>();

        if (cookedSegments != null)
        {
            for (ValueSegment segment : cookedSegments)
            {
                String cookedToken = segment.toString();
                if (segment.isLiteral || segment.isWhiteSpaceEscaped)
                {
                    // Was quoted. Use it as is.

                    result.add(cookedToken);
                }

                else
                {
                    // Break it into white space-delimited tokens.

                    String[] tokens = TextUtil.split(cookedToken);
                    for (String token : tokens)
                        result.add(token);
                }
            }
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Convenience method to get and convert an optional integer parameter.
     * The default value applies if the variable is missing or is there but
     * has an empty value.
     *
     * @param sectionName   section name
     * @param variableName  variable name
     * @param defaultValue  default value if not found
     *
     * @return the value, or the default value if not found
     *
     * @throws ConfigurationException some configuration error
     *
     * @see #getOptionalCardinalValue
     * @see #getRequiredIntegerValue
     */
    public int getOptionalIntegerValue (String sectionName,
                                        String variableName,
                                        int    defaultValue)
        throws ConfigurationException
    {
        int result = defaultValue;

        try
        {
            result = getRequiredIntegerValue(sectionName, variableName);
        }

        catch (NoSuchVariableException ex)
        {
            // Use default
        }

        return result;
    }

    /**
     * Convenience method to get and convert a required integer parameter.
     *
     * @param sectionName   section name
     * @param variableName  variable name
     *
     * @return the value
     *
     * @throws ConfigurationException  some configuration error, including bad
     *                                 numeric value
     *
     * @see #getRequiredCardinalValue
     * @see #getOptionalIntegerValue
     */
    public int getRequiredIntegerValue (String sectionName,
                                        String variableName)
        throws ConfigurationException
    {
        String sNum = getConfigurationValue (sectionName, variableName);

        try
        {
            return Integer.parseInt (sNum);
        }

        catch (NumberFormatException ex)
        {
            throw new ConfigurationException
                (Package.BUNDLE_NAME,
                 "Configuration.badNumericValue",
                 "Bad numeric value \"{0}\" for variable \"{1}\" in section " +
                 "\"{2}\"",
                 new Object[]
                 {
                     sNum,
                     variableName,
                     sectionName
                 });
        }
    }

    /**
     * Convenience method to get and convert an optional non-negative integer
     * parameter. The default value applies if the variable is missing or
     * is there but has an empty value. (The term "cardinal" is borrowed from
     * the Modula-3 language.)
     *
     * @param sectionName   section name
     * @param variableName  variable name
     * @param defaultValue  default value if not found. Must be non-negative.
     *
     * @return the value, or the default value if not found
     *
     * @throws ConfigurationException bad numeric value, or other config error
     *
     * @see #getOptionalIntegerValue
     * @see #getRequiredCardinalValue
     */
    public int getOptionalCardinalValue (String sectionName,
                                         String variableName,
                                         int    defaultValue)
        throws ConfigurationException
    {
        assert (defaultValue >= 0);
        int result = defaultValue;

        try
        {
            result = getRequiredCardinalValue(sectionName, variableName);
        }

        catch (NoSuchVariableException ex)
        {
            // Accept default
        }

        return result;
    }

    /**
     * Convenience method to get and convert a required integer parameter.
     *
     * @param sectionName   section name
     * @param variableName  variable name
     *
     * @return the value
     *
     * @throws ConfigurationException  bad numeric value, or other config error
     *
     * @see #getOptionalCardinalValue
     * @see #getRequiredIntegerValue
     */
    public int getRequiredCardinalValue (String sectionName,
                                         String variableName)
        throws ConfigurationException
    {
        String sNum = getConfigurationValue (sectionName, variableName);
        int i = getRequiredIntegerValue (sectionName, variableName);
        if (i < 0)
        {
            throw new ConfigurationException
                               (Package.BUNDLE_NAME,
                                "Configuration.negativeCardinalValue",
                                "Bad negative numeric value \"{0}\" " +
                                "for variable \"{1}\" in section \"{2}\"",
                                new Object[]
                                {
                                    sNum,
                                    variableName,
                                    sectionName
                                });
        }

        return i;
    }

    /**
     * Convenience method to get and convert an optional floating point
     * numeric parameter. The default value applies if the variable is
     * missing or is there but has an empty value.
     *
     * @param sectionName   section name
     * @param variableName  variable name
     * @param defaultValue  default value if not found
     *
     * @return the value, or the default value if not found
     *
     * @throws ConfigurationException bad numeric value or other config error
     */
    public double getOptionalDoubleValue (String sectionName,
                                          String variableName,
                                          double defaultValue)
        throws ConfigurationException
    {
        double result = defaultValue;

        try
        {
            result = getRequiredDoubleValue(sectionName, variableName);
        }

        catch (NoSuchVariableException ex)
        {
            // Accept default
        }

        return result;
    }

    /**
     * Convenience method to get and convert a required floating point
     * numeric parameter.
     *
     * @param sectionName   section name
     * @param variableName  variable name
     *
     * @return the value
     *
     * @throws ConfigurationException  bad numeric value or other config error
     */
    public double getRequiredDoubleValue (String sectionName,
                                          String variableName)
        throws ConfigurationException
    {
        String sNum = getConfigurationValue (sectionName, variableName);

        try
        {
            return Double.parseDouble (sNum);
        }

        catch (NumberFormatException ex)
        {
            throw new ConfigurationException (Package.BUNDLE_NAME,
                                              "Configuration.badFloatValue",
                                              "Bad floating point value " +
                                              "\"{0}\" for variable \"{1}\" " +
                                              "in section \"{2}\"",
                                              new Object[]
                                              {
                                                  sNum,
                                                  variableName,
                                                  sectionName
                                              });
        }
    }

    /**
     * Convenience method to get and convert an optional boolean parameter.
     * The default value applies if the variable is missing or is there
     * but has an empty value.
     *
     * @param sectionName   section name
     * @param variableName  variable name
     * @param defaultValue  default value if not found
     *
     * @return the value, or the default value if not found
     *
     * @throws ConfigurationException bad numeric value, or other config error
     */
    public boolean getOptionalBooleanValue (String  sectionName,
                                            String  variableName,
                                            boolean defaultValue)
        throws ConfigurationException
    {
        boolean result;

        try
        {
            String s = getConfigurationValue (sectionName, variableName);

            if (TextUtil.stringIsEmpty (s))
                result = defaultValue;
            else
                result = TextUtil.booleanFromString (s);
        }

        catch (NoSuchVariableException ex)
        {
            result = defaultValue;
        }

        catch (IllegalArgumentException ex)
        {
            throw new ConfigurationException (ex.getMessage());
        }

        return result;
    }

    /**
     * Convenience method to get and convert a required boolean parameter.
     *
     * @param sectionName   section name
     * @param variableName  variable name
     *
     * @return the value
     *
     * @throws ConfigurationException  bad numeric value, or other config error
     */
    public boolean getRequiredBooleanValue (String sectionName,
                                            String variableName)
        throws ConfigurationException

    {
        String val = getConfigurationValue(sectionName, variableName);
        try
        {
            return TextUtil.booleanFromString(val);
        }

        catch (IllegalArgumentException ex)
        {
            throw new ConfigurationException
                (Package.BUNDLE_NAME,
                 "Configuration.badBooleanValue",
                 "Bad boolean value \"{0}\" for variable \"{1}\" in " +
                 "section \"{2}\".\n",
                 new Object[] {val, variableName, sectionName});
        }

    }

    /**
     * Convenience method to get an optional string value. The default
     * value applies if the variable is missing or is there but has an
     * empty value.
     *
     * @param sectionName   section name
     * @param variableName  variable name
     * @param defaultValue  default value if not found
     *
     * @return the value, or the default value if not found
     *
     * @throws ConfigurationException bad numeric value or other config error
     */
    public String getOptionalStringValue (String sectionName,
                                          String variableName,
                                          String defaultValue)
        throws ConfigurationException
    {
        String result;

        try
        {
            result = getConfigurationValue (sectionName, variableName);
            if (TextUtil.stringIsEmpty (result))
                result = defaultValue;
        }

        catch (NoSuchVariableException ex)
        {
            result = defaultValue;
        }

        return result;
    }

    /**
     * Get the value associated with a given variable. Required by the
     * {@link VariableDereferencer} interface, this method is used during
     * parsing to handle variable substitutions (but also potentially
     * useful by other applications). See this class's documentation for
     * details on variable references.
     *
     * @param varName  The name of the variable for which the value is
     *                 desired.
     * @param context  a context object, passed through from the caller
     *                 to the dereferencer, or null if there isn't one.
     *                 For this class, the context object is a
     *                 SubstitutionContext variable.
     *
     * @return The variable's value. If the variable has no value, this
     *         method must return null.
     *
     * @throws VariableSubstitutionException  variable references itself
     */
    public String getVariableValue (String varName, Object context)
        throws VariableSubstitutionException
    {
        int                  i;
        Section              section;
        String               sectionName;
        String               value = null;
        SubstitutionContext  substContext = (SubstitutionContext) context;
        Section              variableParentSection;
        Variable             currentVariable;

        try
        {
            checkVariableName (varName);
        }

        catch (ConfigurationException ex)
        {
            throw new VariableSubstitutionException (ex);
        }

        currentVariable = substContext.currentVariable;
        if (currentVariable.getName().equals (varName))
        {
            throw new VariableSubstitutionException
                                 (Package.BUNDLE_NAME,
                                  "Configuration.recursiveSubst",
                                  "Attempt to substitute value for variable " +
                                  "\"{0}\" within itself.",
                                  new Object[] {varName});
        }

        variableParentSection = substContext.currentVariable.getSection();
        i = varName.indexOf (':');
        if (i == -1)
        {
            // No section in the variable reference. Use the variable's
            // context.

            section = variableParentSection;
            sectionName = section.getName();
        }

        else
        {
            sectionName = varName.substring (0, i);
            varName = varName.substring (i + 1);

            if (sectionName.equals (SYSTEM_SECTION_NAME))
                section = systemSection;

            else if (sectionName.equals (PROGRAM_SECTION_NAME))
                section = programSection;

            else if (sectionName.equals (ENV_SECTION_NAME))
                section = envSection;

            else
                section = sectionsByName.get (sectionName);
        }

        if (section == null)
        {
            if (abortOnUndefinedVariable)
            {
                throw new VariableSubstitutionException
                    (Package.BUNDLE_NAME,
                     "Configuration.nonExistentSection",
                     "Reference to variable \"{0}\" in nonexistent section " +
                     "\"{1}\".",
                     new Object[] {varName, sectionName});
            }
        }

        else
        {
            if (variableParentSection.getID() < section.getID())
            {
                String parentSectionName = variableParentSection.getName();
                String thisSectionName = section.getName();

                throw new VariableSubstitutionException
                    (Package.BUNDLE_NAME,
                     "Configuration.badSectionRef",
                     "Variable \"{0}\" in section \"{1}\" cannot substitute " +
                     "the value of variable \"{2}\" from section \"{3}\", " +
                     "because section \"{3}\" appears after section \"{1}\" " +
                     "in the configuration file.",
                     new Object[]
                     {
                         substContext.currentVariable.getName(),
                         parentSectionName,
                         varName,
                         thisSectionName,
                         thisSectionName,
                         parentSectionName
                     });
            }

            Variable varToSubst;

            try
            {
                varToSubst = section.getVariable (varName);
            }

            catch (ConfigurationException ex)
            {
                throw new VariableSubstitutionException (ex.getMessage());
            }

            if (varToSubst != null)
             {
                value = varToSubst.getCookedValue();
            }
        }

        substContext.totalSubstitutions++;

        return value;
    }

    /**
     * Required by the {@link VariableNameChecker} interface, this method
     * determines whether a character may legally be used in a variable name
     * or not.
     *
     * @param c   The character to test
     *
     * @return <tt>true</tt> if the character may be part of a variable name,
     *         <tt>false</tt> otherwise
     *
     * @see VariableSubstituter#substitute
     */
    public boolean legalVariableCharacter (char c)
    {
        return ! (UnixShellVariableSubstituter.isVariableMetacharacter (c));
    }

    /**
     * Load configuration from a <tt>File</tt>. Any existing data is
     * discarded.
     *
     * @param file  the file
     *
     * @throws IOException            read error
     * @throws ConfigurationException parse error
     */
    public void load(File file)
        throws IOException,
               ConfigurationException
    {
        load(file, null);
    }

    /**
     * Load configuration from a <tt>File</tt>. Any existing data is
     * discarded.
     *
     * @param file     the file
     * @param encoding the encoding to use, or null for the default
     *
     * @throws IOException                  read error
     * @throws ConfigurationException       parse error
     */
    public void load(File file, String encoding)
        throws IOException,
               ConfigurationException
    {
        clear();
        URL url = file.toURI().toURL();
        parse (new FileInputStream(file), encoding, url);
        this.configURL = url;
    }

    /**
     * Load configuration from a file specified as a pathname. Any existing
     * data is discarded.
     *
     * @param path  the path
     *
     * @throws FileNotFoundException   specified file doesn't exist
     * @throws IOException             can't open or read file
     * @throws ConfigurationException  error in configuration data
     */
    public void load(String path)
        throws IOException,
               ConfigurationException
    {
        load(path, null);
    }

    /**
     * Load configuration from a file specified as a pathname. Any existing data
     * is discarded.
     *
     * @param path     the path
     * @param encoding the encoding to use, or null for the default
     *
     * @throws FileNotFoundException        specified file doesn't exist
     * @throws IOException                  can't open or read file
     * @throws ConfigurationException       error in configuration data
     * @throws UnsupportedEncodingException bad encoding
     */
    public void load(String path, String encoding)
        throws IOException,
               ConfigurationException
    {
        clear();
        URL url = new File(path).toURI().toURL();
        parse(new FileInputStream(path), encoding, url);
        this.configURL = url;
    }

    /**
     * Load the configuration from a URL.
     *
     * @param url      the URL
     *
     * @throws IOException            on I/O error
     * @throws ConfigurationException on configuration error
     */
    public void load(URL url)
        throws IOException,
               ConfigurationException
    {
            load(url, null);
    }

    /**
     * Load the configuration from a URL.
     *
     * @param url      the URL
     * @param encoding the encoding, if known, or null
     *
     * @throws IOException            on I/O error
     * @throws ConfigurationException on configuration error
     */
    public void load(URL url, String encoding)
        throws IOException,
               ConfigurationException
    {
        clear();
        parse(url.openStream(), encoding, url);
        this.configURL = url;
    }

    /**
     * Load configuration from an <tt>InputStream</tt>. Any existing data
     * is discarded.
     *
     * @param iStream  the <tt>InputStream</tt>
     *
     * @throws IOException             can't open or read URL
     * @throws ConfigurationException  error in configuration data
     */
    public void load(InputStream iStream)
        throws IOException,
               ConfigurationException
    {
        load(iStream, null);
    }

    /**
     * Load configuration from an <tt>InputStream</tt>. Any existing data
     * is discarded.
     *
     * @param iStream  the <tt>InputStream</tt>
     * @param encoding the encoding to use, or null for the default
     *
     * @throws IOException                  can't open or read URL
     * @throws ConfigurationException       error in configuration data
     * @throws UnsupportedEncodingException bad encoding
     */
    public void load(InputStream iStream, String encoding)
        throws IOException,
               ConfigurationException
    {
        clear();
        parse(iStream, encoding, null);
    }

    /**
     * Set a variable's value. If the variable does not exist, it is created.
     * If it does exist, its current value is overwritten with the new one.
     * Metacharacters and variable references are not expanded unless the
     * <tt>expand</tt> parameter is <tt>true</tt>. An <tt>expand</tt> value
     * of <tt>false</tt> is useful when creating new configuration data to
     * be written later.
     *
     * @param sectionName  name of existing section to contain the variable
     * @param variableName name of variable to set
     * @param value        variable's value
     * @param expand       <tt>true</tt> to expand metacharacters and variable
     *                     references in the value, <tt>false</tt> to leave
     *                     the value untouched.
     *
     * @throws NoSuchSectionException        section does not exist
     * @throws VariableSubstitutionException variable substitution error
     */
    public void setVariable (String  sectionName,
                             String  variableName,
                             String  value,
                             boolean expand)
        throws NoSuchSectionException,
               VariableSubstitutionException
    {
        Section section = sectionsByName.get (sectionName);
        if (section == null)
            throw new NoSuchSectionException (sectionName);

        Variable variable;

        try
        {
            variable = section.getVariable (variableName);
            if (variable != null)
                variable.setValue(value);
            else
                variable = section.addVariable(variableName, value);
        }

        catch (ConfigurationException ex)
        {
            throw new VariableSubstitutionException (ex.getMessage());
        }


        if (expand)
        {
            try
            {
                substituteVariables(variable, varSubstituter, true);
            }

            catch (ConfigurationException ex)
            {
                throw new VariableSubstitutionException (ex.getMessage());
            }
        }
    }

    /**
     * Get the value of the flag that controls whether the
     * <tt>Configuration</tt> object will abort when it encounters an
     * undefined variable. If this flag is clear, then an undefined variable
     * is expanded to an empty string. If this flag is set, then an undefined
     * value results in a {@link ConfigurationException}.
     *
     * @return <tt>true</tt> if the "abort on undefined variable" capability
     *         is enabled, <tt>false</tt> if it is disabled.
     *
     * @see #setAbortOnUndefinedVariable
     */
    public boolean getAbortOnUndefinedVariable()
    {
        return abortOnUndefinedVariable;
    }

    /**
     * Set or clear the flag that controls whether the <tt>Configuration</tt>
     * object will abort when it encounters an undefined variable. If this
     * flag is clear, then an undefined variable is expanded to an empty
     * string. If this flag is set, then an undefined value results in a
     * {@link ConfigurationException}. The flag defaults to <tt>true</tt>.
     *
     * @param enable  <tt>true</tt> to enable the "abort on undefined variable"
     *                flag, <tt>false</tt> to disable it.
     *
     * @see #getAbortOnUndefinedVariable
     */
    public void setAbortOnUndefinedVariable(boolean enable)
    {
        abortOnUndefinedVariable = enable;
        varSubstituter.setAbortOnUndefinedVariable(enable);
    }

    /**
     * Writes the configuration data to a <tt>PrintWriter</tt>. The sections
     * and variables within the sections are written in the order they were
     * originally read from the file. Non-printable characters (and a few
     * others) are encoded into metacharacter sequences. Comments are not
     * propagated, since they are not retained when the data is parsed.
     *
     * @param out  where to write the configuration data
     *
     * @throws ConfigurationException on error
     *
     * @see XStringBuffer#encodeMetacharacters()
     */
    public void write (PrintWriter out)
        throws ConfigurationException
    {
        XStringBuffer  value = new XStringBuffer();
        boolean        firstSection = true;

        out.print (COMMENT_CHARS.charAt (0));
        out.print (" Written by ");
        out.println (this.getClass().getName());
        out.print (COMMENT_CHARS.charAt (0));
        out.print (" on ");
        out.println (new Date().toString());
        out.println();

        for (Section section : sectionsInOrder)
        {
            if (! firstSection)
                out.println();

            out.println (SECTION_START + section.getName() + SECTION_END);
            firstSection = false;

            for (String varName : section.getVariableNames())
            {
                Variable var = section.getVariable (varName);
                value.setLength (0);
                value.append (var.getRawValue());
                //value.encodeMetacharacters();

                out.println (varName + ": " + value.toString());
            }
        }
    }

    /**
     * Writes the configuration data to a <tt>PrintStream</tt>. The sections
     * and variables within the sections are written in the order they were
     * originally read from the file. Non-printable characters (and a few
     * others) are encoded into metacharacter sequences. Comments and
     * variable references are not propagated, since they are not retained
     * when the data is parsed.
     *
     * @param out  where to write the configuration data
     *
     * @throws ConfigurationException on error
     *
     * @see XStringBuffer#encodeMetacharacters()
     */
    public void write (PrintStream out)
        throws ConfigurationException
    {
        PrintWriter w = new PrintWriter (out);
        write (w);
        w.flush();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse configuration data from the specified stream.
     *
     * @param in       the input stream
     * @param encoding the encoding to use, or null for the default
     * @param url      the URL associated with the stream, or null if not known
     *
     * @throws ConfigurationException       parse error
     * @throws UnsupportedEncodingException bad encoding
     */
    private synchronized void parse (InputStream in, String encoding, URL url)
        throws ConfigurationException,
               UnsupportedEncodingException
    {
        loadConfiguration (in, encoding, url, new ParseContext());
    }

    /**
     * Load the configuration data into memory, without processing
     * metacharacters or variable substitution. Includes are processed,
     * though.
     *
     * @param in           input stream
     * @param encoding     the encoding to use, or null for the default
     * @param url          URL associated with the stream, or null if not known
     * @param parseContext current parsing context
     *
     * @throws ConfigurationException       parse error
     * @throws UnsupportedEncodingException bad encoding
     */
    private void loadConfiguration(InputStream  in,
                                   String       encoding,
                                   URL          url,
                                   ParseContext parseContext)
        throws ConfigurationException,
               UnsupportedEncodingException
    {
        BufferedReader r;
        Line           line = new Line();
        String         sURL = url.toExternalForm();

        // Now, create the phantom program, env and system sections. These
        // MUST be created first, or other sections won't be able to
        // substitute from them. (i.e., They must have the lowest IDs.)

        programSection = new ProgramSection (PROGRAM_SECTION_NAME,
                                             PROGRAM_SECTION_ID);
        systemSection  = new SystemSection (SYSTEM_SECTION_NAME,
                                            SYSTEM_SECTION_ID);
        envSection  = new EnvSection (ENV_SECTION_NAME, ENV_SECTION_ID);

        if (parseContext.openURLs.contains (sURL))
        {
            throw new ConfigurationException
                              (Package.BUNDLE_NAME,
                               "Configuration.recursiveInclude",
                               "{0}, line {1}: Attempt to include \"{2}\" " +
                               "from itself, either directly or indirectly.",
                               new Object[]
                               {
                                   url.toExternalForm(),
                                   String.valueOf (line.number),
                                   sURL
                               });
        }

        parseContext.openURLs.add (sURL);

        // Parse the entire file into memory before doing variable
        // substitution and metacharacter expansion.

        InputStreamReader ir;
        if (encoding == null)
            ir = new InputStreamReader(in);
        else
            ir = new InputStreamReader(in, encoding);
        r = new BufferedReader(ir);

        while (readLogicalLine (r, line))
        {
            try
            {
                switch (line.type)
                {
                    case COMMENT:
                    case BLANK:
                        break;

                    case INCLUDE:
                        handleInclude (line, url, encoding, parseContext);
                        break;

                    case SECTION:
                        parseContext.currentSection = handleNewSection (line,
                                                                        url);
                        break;

                    case VARIABLE:
                        if (parseContext.currentSection == null)
                        {
                            throw new ConfigurationException
                                     (Package.BUNDLE_NAME,
                                      "Configuration.varBeforeSection",
                                      "{0}, line {1}: Variable assignment " +
                                      "before first section.",
                                      new Object[]
                                      {
                                          url.toExternalForm(),
                                          String.valueOf (line.number)
                                      });
                        }

                        handleVariable (line, url, parseContext);
                        break;

                    default:
                        throw new IllegalStateException
                                          ("Bug: line.type=" + line.type);
                }
            }

            catch (IOException ex)
            {
                throw new ConfigurationException
                                     (getExceptionPrefix (line, url) +
                                      ex.toString());
            }
        }

        parseContext.openURLs.remove (sURL);
    }

    /**
     * Handle a new section.
     *
     * @param line  line buffer
     * @param url   URL currently being processed, or null if unknown
     *
     * @return a new Section object, which has been stored in the appropriate
     *         places
     *
     * @throws ConfigurationException  configuration error
     */
    private Section handleNewSection (Line line, URL url)
        throws ConfigurationException
    {
        String s = line.buffer.toString().trim();

        if (s.charAt (0) != SECTION_START)
        {
            throw new ConfigurationException
                        (Package.BUNDLE_NAME,
                         "Configuration.badSectionBegin",
                         "{0}, line {1}: Section does not begin with \"{2}\"",
                         new Object[]
                         {
                             url.toExternalForm(),
                             String.valueOf (line.number),
                             String.valueOf (SECTION_START)
                         });
        }

        else if (s.charAt (s.length() - 1) != SECTION_END)
        {
            throw new ConfigurationException
                        (Package.BUNDLE_NAME,
                         "Configuration.badSectionEnd",
                         "{0}, line {1}: Section does not end with \"{2}\"",
                         new Object[]
                         {
                             url.toExternalForm(),
                             String.valueOf (line.number),
                             String.valueOf (SECTION_END)
                         });
        }

        return makeNewSection (s.substring (1, s.length() - 1));
    }

    /**
     * Handle a new variable during parsing.
     *
     * @param line         line buffer
     * @param url          URL currently being processed, or null if unknown
     * @param parseContext current parsing context
     *
     * @throws ConfigurationException  configuration error
     */
    private void handleVariable (Line         line,
                                 URL          url,
                                 ParseContext parseContext)
        throws ConfigurationException
    {
        char[] s = line.buffer.toString().toCharArray();
        int    iSep;

        for (iSep = 0; iSep < s.length; iSep++)
        {
            if ((s[iSep] == ':') || (s[iSep] == '='))
                break;
        }

        if (iSep == s.length)
        {
            throw new ConfigurationException (Package.BUNDLE_NAME,
                                              "Configuration.missingAssignOp",
                                              "{0}, line {1}: Missing \"=\" " +
                                              "or \":\" for variable " +
                                              "definition.",
                                              new Object[]
                                              {
                                                  url.toExternalForm(),
                                                  String.valueOf (line.number)
                                              });
        }

        if (iSep == 0)
        {
            throw new ConfigurationException (Package.BUNDLE_NAME,
                                              "Configuration.noVariablName",
                                              "{0}, line {1}: Missing " +
                                              "variable name for variable " +
                                              "definition.",
                                              new Object[]
                                              {
                                                  url.toExternalForm(),
                                                  String.valueOf (line.number)
                                              });
        }

        int i = 0;
        int j = iSep - 1;
        while (Character.isWhitespace (s[i]))
            i++;

        while (Character.isWhitespace (s[j]))
            j--;

        if (i > j)
        {
            throw new ConfigurationException (Package.BUNDLE_NAME,
                                              "Configuration.noVariablName",
                                              "{0}, line {1}: Missing " +
                                              "variable name for variable " +
                                              "definition.",
                                              new Object[]
                                              {
                                                  url.toExternalForm(),
                                                  String.valueOf (line.number)
                                              });
        }

        String varName = new String (s, i, j - i + 1);
        if (varName.length() == 0)
        {
            throw new ConfigurationException (Package.BUNDLE_NAME,
                                              "Configuration.noVariablName",
                                              "{0}, line {1}: Missing " +
                                              "variable name for variable " +
                                              "definition.",
                                              new Object[]
                                              {
                                                  url.toExternalForm(),
                                                  String.valueOf (line.number)
                                              });
        }

        checkVariableName (varName);

        i = skipWhitespace (s, iSep + 1);
        j = s.length - i;

        Section currentSection = parseContext.currentSection;
        String value = new String (s, i, j);
        Variable existing = currentSection.getVariable (varName);
        if (existing != null)
        {
            throw new ConfigurationException
                              (Package.BUNDLE_NAME,
                               "Configuration.duplicateVar",
                               "{0}, line {1}: Section \"{2}\" has a " +
                               "duplicate definition for variable " +
                               "\"{3}\". The first instance was defined " +
                               "on line {4}.",
                               new Object[]
                               {
                                   url.toExternalForm(),
                                   String.valueOf (line.number),
                                   currentSection.getName(),
                                   varName,
                                   String.valueOf (existing.getLineWhereDefined())
                               });
        }

        Variable newVar = currentSection.addVariable (varName,
                                                      value,
                                                      line.number);

        // Expand the metacharacters and variable references in the variable.

        try
        {
            newVar.segmentValue();
            decodeMetacharacters (newVar);
            substituteVariables (newVar, varSubstituter, false);
            newVar.reassembleCookedValueFromSegments();
        }

        catch (VariableSubstitutionException ex)
        {
            throw new ConfigurationException (ex.getMessage());
        }
    }

    /**
     * Determine whether a variable name is legal, throwing an exception if
     * it isn't.
     *
     * @param varName the name to check
     *
     * @throws ConfigurationException bad variable name
     */
    private void checkVariableName (String varName)
        throws ConfigurationException
    {
        for (char c : varName.toCharArray())
        {
            if (! legalVariableCharacter (c))
            {
                throw new ConfigurationException
                                      (Package.BUNDLE_NAME,
                                       "Configuration.badVariableName",
                                       "\"{0}\" is an illegal variable name",
                                       new Object[] {varName});
            }
        }
    }

    /**
     * Handle an include directive.
     *
     * @param line         line buffer
     * @param encoding     the encoding to use, or null for the default
     * @param url          URL currently being processed, or null if unknown
     * @param parseContext current parsing context
     *
     * @throws IOException                  I/O error opening or reading include
     * @throws ConfigurationException       configuration error
     */
    private void handleInclude (Line         line,
                                URL          url,
                                String       encoding,
                                ParseContext parseContext)
        throws IOException,
               ConfigurationException
    {
        if (parseContext.includeFileNestingLevel >= MAX_INCLUDE_NESTING_LEVEL)
        {
            throw new ConfigurationException
                                (Package.BUNDLE_NAME,
                                 "Configuration.maxNestedIncludeExceeded",
                                 "{0}, line {1}: Exceeded maximum nested " +
                                 "include level of {2}.",
                                 new Object[]
                                 {
                                     url.toExternalForm(),
                                     String.valueOf (line.number),
                                     String.valueOf (MAX_INCLUDE_NESTING_LEVEL)
                                 });
        }

        parseContext.includeFileNestingLevel++;

        String s = line.buffer.toString();

        // Parse the file name.

        String includeTarget = s.substring (INCLUDE.length() + 1).trim();
        int len = includeTarget.length();

        // Make sure double quotes surround the file or URL.

        if ((len < 2) ||
            (! includeTarget.startsWith ("\"")) ||
            (! includeTarget.endsWith ("\"")))
        {
            throw new ConfigurationException
                                 (Package.BUNDLE_NAME,
                                  "Configuration.malformedDirective",
                                  "{0}, line {1}: Malformed \"{2}\" directive",
                                  new Object[]
                                  {
                                      url.toExternalForm(),
                                      String.valueOf (line.number),
                                      INCLUDE
                                  });
        }

        // Extract the file.

        includeTarget = includeTarget.substring (1, len - 1);
        if (includeTarget.length() == 0)
        {
            throw new ConfigurationException
                                 (Package.BUNDLE_NAME,
                                  "Configuration.includeMissingFile",
                                  "{0}, line {1}: Missing file name or URL " +
                                  "in \"{2}\" directive",
                                  new Object[]
                                  {
                                      url.toExternalForm(),
                                      String.valueOf (line.number),
                                      INCLUDE
                                  });
        }

        // Process the include

        try
        {
            loadInclude (new URL (includeTarget), encoding, parseContext);
        }

        catch (MalformedURLException ex)
        {
            // Not obviously a URL. First, determine whether it has
            // directory information or not. If not, try to use the
            // parent's directory information.

            if (FileUtil.isAbsolutePath (includeTarget))
            {
                loadInclude (new URL (url.getProtocol(),
                                      url.getHost(),
                                      url.getPort(),
                                      includeTarget),
                             encoding,
                             parseContext);
            }

            else
            {
                // It's relative to the parent. If the parent URL is not
                // specified, then we can't do anything except try to load
                // the include as is. It'll probably fail...

                if (url == null)
                {
                    loadInclude (new File (includeTarget).toURI().toURL(),
                                 encoding,
                                 parseContext);
                }

                else
                {
                    String parent = new File (url.getFile()).getParent();

                    if (parent == null)
                        parent = "";

                    loadInclude (new URL (url.getProtocol(),
                                          url.getHost(),
                                          url.getPort(),
                                          parent + "/" + includeTarget),
                                 encoding,
                                 parseContext);
                }
            }
        }

        parseContext.includeFileNestingLevel--;
    }

    /**
     * Actually attempts to load an include reference. This is basically just
     * a simplified front-end to loadConfiguration().
     *
     * @param url          the URL to be included
     * @param encoding     the encoding to use, or null for the default
     * @param parseContext current parsing context
     *
     * @throws IOException  I/O error
     * @throws ConfigurationException configuration error
     */
    private void loadInclude (URL url,
                              String encoding,
                              ParseContext parseContext)
        throws IOException,
               ConfigurationException
    {
        loadConfiguration (url.openStream(), encoding, url, parseContext);
    }

    /**
     * Read the next logical line of input from a config file.
     *
     * @param r    the reader
     * @param line where to store the line. The line number in this
     *             object is incremented, the "buffer" field is updated,
     *             and the "type" field is set appropriately.
     *
     * @return <tt>true</tt> if a line was read, <tt>false</tt> for EOF.
     *
     * @throws ConfigurationException read error
     */
    private boolean readLogicalLine (BufferedReader r, Line line)
        throws ConfigurationException
    {
        boolean  continued    = false;
        boolean  gotSomething = false;

        line.newLine();
        for (;;)
        {
            String s;

            try
            {
                s = r.readLine();
            }

            catch (IOException ex)
            {
                throw new ConfigurationException (ex.toString());
            }

            if (s == null)
                break;

            gotSomething = true;
            line.number++;

            // Strip leading white space on all lines.

            int i;
            char[] chars = s.toCharArray();

            i = skipWhitespace (chars, 0);
            if (i < chars.length)
                s = s.substring (i);
            else
                s = "";

            if (! continued)
            {
                // First line. Determine what it is.

                if (s.length() == 0)
                    line.type = LineType.BLANK;

                else if (COMMENT_CHARS.indexOf (s.charAt (0)) != -1)
                    line.type = LineType.COMMENT;

                else if (s.charAt (0) == SECTION_START)
                    line.type = LineType.SECTION;

                else if (new StringTokenizer (s).nextToken().equals (INCLUDE))
                    line.type = LineType.INCLUDE;

                else
                    line.type = LineType.VARIABLE;
            }

            if ((line.type == LineType.VARIABLE) && (hasContinuationMark (s)))
            {
                continued = true;
                line.buffer.append (s.substring (0, s.length() - 1));
            }

            else
            {
                line.buffer.append (s);
                break;
            }
        }

        return gotSomething;
    }

    /**
     * Determine whether a line has a continuation mark or not.
     *
     * @param s  the line
     *
     * @return true if there's a continuation mark, false if not
     */
    private boolean hasContinuationMark (String s)
    {
        boolean has = false;

        if (s.length() > 0)
        {
            char[] chars = s.toCharArray();

            if (chars[chars.length-1] == '\\')
            {
                // Possibly. See if there are an odd number of them.

                int total = 0;
                for (int i = chars.length - 1; i >= 0; i--)
                {
                    if (chars[i] != '\\')
                        break;

                    total++;
                }

                has = ((total % 2) == 1);
            }
        }

        return has;
    }

    /**
     * Get an appropriate exception prefix (e.g., line number, etc.)
     *
     * @param line  line buffer
     * @param url   URL currently being processed, or null if unknown
     *
     * @return a suitable string
     */
    private String getExceptionPrefix (Line line, URL url)
    {
        StringBuffer buf = new StringBuffer();

        if (url != null)
        {
            buf.append (url.toExternalForm());
            buf.append (", line ");
        }

        else
        {
            buf.append ("Line ");
        }

        buf.append (line.number);
        buf.append (": ");

        return buf.toString();
    }

    /**
     * Handle metacharacter substitution for a variable value.
     *
     * @param var The current variable being processed
     *
     * @throws VariableSubstitutionException variable substitution error
     * @throws ConfigurationException        some other configuration error
     */
    private void decodeMetacharacters (Variable var)
        throws VariableSubstitutionException,
               ConfigurationException
    {
        ValueSegment[] segments = var.getCookedSegments();

        for (ValueSegment segment : segments)
        {
            if (segment.isLiteral)
                continue;

            segment.segmentBuf.decodeMetacharacters();
        }
    }

    /**
     * Handle variable substitution for a variable value.
     *
     * @param var            The current variable being processed
     * @param substituter    VariableSubstituter to use
     * @param concatSegments Re-concatenate the segments
     *
     * @throws VariableSubstitutionException variable substitution error
     * @throws ConfigurationException        some other configuration error
     */
    private void substituteVariables (Variable            var,
                                      VariableSubstituter substituter,
                                      boolean             concatSegments)
        throws VariableSubstitutionException,
               ConfigurationException
    {
        ValueSegment[] segments = var.getCookedSegments();
        SubstitutionContext context = new SubstitutionContext (var);

        for (ValueSegment segment : segments)
        {
            // Keep substituting the current variable's value until there
            // no more substitutions are performed. This handles the case
            // where a dereferenced variable value contains its own
            // variable references.

            if (segment.isLiteral)
                continue;

            String s = segment.segmentBuf.toString();
            do
            {
                context.totalSubstitutions = 0;
                s = substituter.substitute (s, this, this, context);
            }
            while (context.totalSubstitutions > 0);

            segment.segmentBuf.setLength (0);
            segment.segmentBuf.append (s);
        }

        if (concatSegments)
            var.reassembleCookedValueFromSegments();
    }

    /**
     * Get index of first non-whitespace character.
     *
     * @param chars character array to check
     * @param start starting point
     *
     * @return index of first non-whitespace character past "start", or -1
     */
    private int skipWhitespace (char[] chars, int start)
    {
        while (start < chars.length)
        {
            if (! Character.isWhitespace (chars[start]))
                break;

            start++;
        }

        return start;
    }

    /**
     * Create and save a new Section.
     *
     * @param sectionName the name
     *
     * @return the Section object, which has been saved.
     */
    private Section makeNewSection (String sectionName)
    {
        int id = nextSectionID();

        Section section = new Section (sectionName, id);
        sectionsInOrder.add (section);
        sectionsByName.put (sectionName, section);

        return section;
    }

    /**
     * Get the next section ID
     *
     * @return the ID
     */
    private synchronized int nextSectionID()
    {
        return ++nextSectionIDValue;
    }
}
