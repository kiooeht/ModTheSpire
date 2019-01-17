package org.clapper.util.text;

/**
 * <p>The <tt>UnixShellVariableSubstituter</tt> class implements the
 * <tt>VariableSubstituter</tt> interface and provides an inline
 * variable substitution capability using a simplified Unix Bourne (or GNU
 * bash) shell variable syntax. This syntax recognizes the
 * "<tt>$var</tt>", "<tt>${var}</tt>" and "<tt>${var?default}</tt> sequences
 * as variable references. For example, given the string (variables in
 * <b>bold</b>):</p>
 *
 * <blockquote>
 * <pre>
 * file:<b>$user.home</b>/profiles/<b>$PLATFORM</b>/<b>${cfg?config}</b>.txt
 * </pre>
 * </blockquote>
 *
 * <p>and the variable values:</p>
 *
 * <blockquote><pre>
 * user.home=/home/bmc
 * PLATFORM=freebsd
 * </pre></blockquote>
 *
 * <p>a <tt>UnixShellVariableSubstituter</tt> will produce the result
 * string (substitutions noted in <b>bold</b>):
 *
 * <blockquote><pre>
 * file:<b>/home/bmc</b>/profiles/<b>freebsd</b>/<b>config</b>.txt
 * </pre></blockquote>
 *
 * <b><u>Notes and Caveats</u></b>
 *
 * <ol>
 *    <li>To include a literal "$" character in a string, precede it with
 *        a backslash.
 *
 *    <li>If a variable doesn't have a value, its reference is replaced
 *        by an empty string, unless it is of the form <tt>${var?default}</tt>.
 *        For instance, if variable <tt>foo</tt> does not have a value, the
 *        string <tt>$foo</tt> will substitute "", <tt>${foo}</tt> will
 *        substitute "", and <tt>${foo?foo value}</tt> will substitute
 *        "foo value".
 *
 *    <li>As with all <tt>VariableSubstituter</tt> classes, an instance
 *        of the <tt>UnixShellVariableSubstituter</tt> class enforces
 *        its own variable syntax (Unix Bourne shell-style, in this case),
 *        but defers actual variable value resolution to a separate
 *        <tt>VariableDereferencer</tt> object. One consequence of that
 *        approach is that variable names may be case-sensitive or
 *        case-insensitive, depending on how the supplied
 *        <tt>VariableDereferencer</tt> object interprets variable
 *        names.
 * </ol>
 *
 * <p>It's also possible to configure a <tt>UnixShellVariableSubstituter</tt>
 * to throw a {@link UndefinedVariableException}, rather than substituting
 * a blank, if a variable is undefined and a default value is not specified.
 * See the {@link #setAbortOnUndefinedVariable} method.</p>
 *
 * @see WindowsCmdVariableSubstituter
 * @see VariableDereferencer
 * @see VariableSubstituter
 * @see java.lang.String
 */
public class UnixShellVariableSubstituter
    extends AbstractVariableSubstituter
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final char VAR_START        = '$';
    private static final char VAR_OPEN_BRACE   = '{';
    private static final char VAR_CLOSE_BRACE  = '}';
    private static final char VAR_IF_EXISTS_OP = '?';

    private enum ParseState {NOT_IN_VAR, IN_VAR, IN_DEFAULT_VALUE};

    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * List of metacharacters that are used to introduce a variable reference.
     * These characters cannot be permitted in a variable name.
     */
    public static final String VARIABLE_METACHARACTERS = new String
                                   (new char[]
                                    {
                                        VAR_START,
                                        VAR_OPEN_BRACE,
                                        VAR_CLOSE_BRACE,
                                        VAR_IF_EXISTS_OP
                                    });

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private boolean honorEscapes = false;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor.
     */
    public UnixShellVariableSubstituter()
    {
        super();
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the value of the flag that controls whether or not the
     * <tt>substitute()</tt> method will honor backslash escapes within
     * variable references.
     *
     * @return <tt>true</tt> if backslash escapes are honored,
     *         <tt>false</tt> if not.
     *
     * @see #setHonorEscapes
     */
    public boolean getHonorEscapes()
    {
        return honorEscapes;
    }

    /**
     * Set the value of the flag that controls whether or not the
     * <tt>substitute()</tt> method will honor backslash escapes within
     * variable references. The flag defaults to <tt>false</tt>.
     *
     * @param enable  <tt>true</tt> if backslash escapes should be honored,
     *                <tt>false</tt> if not.
     *
     * @see #getHonorEscapes
     */
    public void setHonorEscapes(boolean enable)
    {
        this.honorEscapes = enable;
    }

    /**
     * <p>Substitute all variable references in the supplied string, using
     * a Unix Bourne Shell-style variable syntax. This method uses a
     * supplied <tt>VariableDereferencer</tt> object to resolve variable
     * values. Note that this method throws no exceptions. Syntax errors in
     * the variable references are silently ignored. Variables that have no
     * value are substituted as the empty string. If the
     * <tt>nameChecker</tt> parameter is not null, this method calls its
     * {@link VariableNameChecker#legalVariableCharacter(char)} method to
     * determine whether a given character is a legal part of a variable
     * name. If <tt>nameChecker</tt> is null, then this method assumes that
     * variable names may consist solely of alphanumeric characters,
     * underscores and periods. This syntax is sufficient to substitute
     * variables from <tt>System.properties</tt>, for instance.</p>
     *
     * @param s            the string containing possible variable references
     * @param deref        the <tt>VariableDereferencer</tt> object
     *                     to use to resolve the variables' values.
     * @param nameChecker  the <tt>VariableNameChecker</tt> object to be
     *                     used to check for legal variable name characters,
     *                     or null
     * @param context      an optional context object, passed through
     *                     unmodified to the <tt>deref</tt> object's
     *                     {@link VariableDereferencer#getVariableValue}
     *                     method. This object can be anything at all (and,
     *                     in fact, may be null if you don't care.) It's
     *                     primarily useful for passing context information
     *                     from the caller to the
     *                     <tt>VariableDereferencer</tt>.
     * @param allowEscapes whehther or not escape sequences are honored
     *
     * @return The (possibly) expanded string.
     *
     * @throws UndefinedVariableException     undefined variable, and
     *                                        {@link #getAbortOnUndefinedVariable}
     *                                        returns true
     * @throws VariableSyntaxException        syntax error, and
     *                                        {@link #getAbortOnSyntaxError}
     *                                        returns true
     * @throws VariableSubstitutionException  substitution error
     *
     * @see #substitute(String,VariableDereferencer,Object)
     * @see VariableDereferencer#getVariableValue(String,Object)
     *
     * @deprecated As of version 2.3; please use {@link #setHonorEscapes}
     */
    public String substitute(String               s,
                             VariableDereferencer deref,
                             VariableNameChecker  nameChecker,
                             Object               context,
                             boolean              allowEscapes)
        throws VariableSyntaxException,
               UndefinedVariableException,
               VariableSubstitutionException
    {
        boolean save = getHonorEscapes();
        setHonorEscapes(allowEscapes);
        try
        {
            return substitute(s, deref, nameChecker, context);
        }

        finally
        {
            setHonorEscapes(save);
        }
    }

    /**
     * <p>Substitute all variable references in the supplied string, using
     * a Unix Bourne Shell-style variable syntax. This method uses a
     * supplied <tt>VariableDereferencer</tt> object to resolve variable
     * values. Note that this method throws no exceptions. Syntax errors in
     * the variable references are silently ignored. Variables that have no
     * value are substituted as the empty string. If the
     * <tt>nameChecker</tt> parameter is not null, this method calls its
     * {@link VariableNameChecker#legalVariableCharacter(char)} method to
     * determine whether a given character is a legal part of a variable
     * name. If <tt>nameChecker</tt> is null, then this method assumes that
     * variable names may consist solely of alphanumeric characters,
     * underscores and periods. This syntax is sufficient to substitute
     * variables from <tt>System.properties</tt>, for instance.</p>
     *
     * @param s            the string containing possible variable references
     * @param deref        the <tt>VariableDereferencer</tt> object
     *                     to use to resolve the variables' values.
     * @param nameChecker  the <tt>VariableNameChecker</tt> object to be
     *                     used to check for legal variable name characters,
     *                     or null
     * @param context      an optional context object, passed through
     *                     unmodified to the <tt>deref</tt> object's
     *                     {@link VariableDereferencer#getVariableValue}
     *                     method. This object can be anything at all (and,
     *                     in fact, may be null if you don't care.) It's
     *                     primarily useful for passing context information
     *                     from the caller to the
     *                     <tt>VariableDereferencer</tt>.
     *
     * @return The (possibly) expanded string.
     *
     * @throws UndefinedVariableException     undefined variable, and
     *                                        {@link #getAbortOnUndefinedVariable}
     *                                        returns true
     * @throws VariableSyntaxException        syntax error, and
     *                                        {@link #getAbortOnSyntaxError}
     *                                        returns true
     * @throws VariableSubstitutionException  substitution error
     *
     * @see #substitute(String,VariableDereferencer,Object)
     * @see VariableDereferencer#getVariableValue(String,Object)
     */
    public String substitute(String               s,
                             VariableDereferencer deref,
                             VariableNameChecker  nameChecker,
                             Object               context)
        throws VariableSyntaxException,
               UndefinedVariableException,
               VariableSubstitutionException
    {
        StringBuilder result        = new StringBuilder();
        int           len           = s.length();
        StringBuilder var           = new StringBuilder();
        StringBuilder defaultValue  = new StringBuilder();
        ParseState    state         = ParseState.NOT_IN_VAR;
        boolean       braces        = false;
        boolean       nextIsLiteral = false;
        boolean       syntaxError   = false;
        int           i;
        char          ch[];

        if (nameChecker == null)
            nameChecker = this;

        ch = s.toCharArray();
        i = 0;
        while ((i < len) && (! syntaxError))
        {
            char c = ch[i++];

            if (nextIsLiteral)
            {
                // Literal

                result.append(c);
                nextIsLiteral = false;
            }

            else if (state == ParseState.NOT_IN_VAR)
            {
                // Not in a variable.

                if (c == VAR_START)          // Possible start of new variable.
                    state = ParseState.IN_VAR;
                else if (honorEscapes && (c == '\\')) // next char is literal
                    nextIsLiteral = true;
                else                         // Just a regular old character.
                    result.append(c);
            }

            else if (state == ParseState.IN_DEFAULT_VALUE)
            {
                if (c == VAR_CLOSE_BRACE)
                {
                    state = ParseState.IN_VAR;
                    i--;
                }

                else
                {
                    defaultValue.append(c);
                }
            }

            // If we get here, we're currently assembling a variable name.

            else if ( (var.length() == 0) && (c == VAR_OPEN_BRACE) )
            {
                // start of ${...} sequence
                braces = true;
            }

            else if (braces && (c == VAR_IF_EXISTS_OP))
            {
                // Specification of default value.

                state = ParseState.IN_DEFAULT_VALUE;
            }

            else if (nameChecker.legalVariableCharacter(c))
            {
                var.append (c);
            }

            else
            {
                // Not a legal variable character, so we're done assembling
                // this variable name.

                String varName = var.toString();
                state = ParseState.NOT_IN_VAR;

                if (braces)
                {
                    if (c == VAR_CLOSE_BRACE)    // final brace; substitute
                    {
                        result.append(dereference(varName,
                                                  defaultValue.toString(),
                                                  context,
                                                  deref));
                    }

                    else   // Missing trailing '}'. No substitution.
                    {
                        result.append(VAR_START);
                        result.append(VAR_OPEN_BRACE);
                        result.append(var.toString());
                        i--;             // push 'c' back on the stack
                        syntaxError = true;
                    }

                    braces = false;
                }

                else if (var.length() == 0)
                {
                    // '$' followed by something illegal. Syntax error.

                    result.append (VAR_START);
                    i--;                 // push 'c' back on the stack
                    syntaxError = true;
                }

                else
                {
                    // Legal, non-bracketed variable. Substitute.

                    result.append(dereference(varName,
                                              defaultValue.toString(),
                                              context,
                                              deref));
                    i--;             // push 'c' back on the stack
                }

                var.setLength (0);
            }
        }           // end while

        if (state == ParseState.IN_VAR)
        {
            // One last variable to handle.

            if (braces) // No trailing '}'. Syntax error.
            {
                result.append(VAR_START);
                result.append(VAR_OPEN_BRACE);
                result.append(var.toString());
                syntaxError = true;
            }

            else if (var.length() == 0)      // just a trailing "$"
            {
                result.append(VAR_START);
            }

            else
            {
                result.append(dereference(var.toString(),
                                          defaultValue.toString(),
                                          context,
                                          deref));
            }
        }

        if (syntaxError && (getAbortOnSyntaxError() == true))
        {
            throw new VariableSyntaxException
                ("Syntax error in reference to variable \"" +
                 var.toString() + "\"");
        }

        return result.toString();
    }

    /**
     * Determine whether a character is one of the variable metacharacters
     * (i.e., the characters that identify a variable reference). Such
     * characters cannot be part of a variable name.
     *
     * @param c  the character to test
     *
     * @return <tt>true</tt> if the character is one of the variable
     *         metacharacters, <tt>false</tt> if not
     */
    public static boolean isVariableMetacharacter (char c)
    {
        boolean isMeta = false;
        char[]  meta  = VARIABLE_METACHARACTERS.toCharArray();

        for (int i = 0; i < meta.length; i++)
        {
            if (c == meta[i])
            {
                isMeta = true;
                break;
            }
        }
        return isMeta;
    }

    /*----------------------------------------------------------------------*\
                             Private Methods
    \*----------------------------------------------------------------------*/

    private String dereference(String varName,
                               String defaultValue,
                               Object context,
                               VariableDereferencer deref)
        throws VariableSubstitutionException
    {
        String result = deref.getVariableValue(varName, context);

        if ((result == null) || (result.length() == 0))
        {
            if ((defaultValue.length() == 0) &&
                (getAbortOnUndefinedVariable() == true))
            {
                throw new UndefinedVariableException
                    ("Variable \"" + varName + "\" is undefined.");
            }

            result = defaultValue;
        }

        return result;
    }
}
