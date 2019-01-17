package org.clapper.util.text;

/**
 * <p>The <tt>WindowsCmdVariableSubstituter</tt> class implements the
 * <tt>VariableSubstituter</tt> interface and provides an inline
 * variable substitution capability using a syntax that's reminiscent of
 * the Microsoft Windows <tt>cmd.exe</tt> command interpreter.
 * This syntax assumes that variable references are surrounded by "%"
 * characters. For example, given the string (variables in
 * <b>bold</b>):</p>
 *
 * <blockquote>
 * <pre>
 * file:<b>%user.home%</b>/profiles/<b>%PLATFORM%</b>/config.txt
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
 * <p>a <tt>WindowsCmdVariableSubstituter</tt> will produce the result
 * string (substitutions noted in <b>bold</b>):
 *
 * <blockquote><pre>
 * file:<b>/home/bmc</b>/profiles/<b>freebsd</b>/config.txt
 * </pre></blockquote>
 *
 * <b><u>Notes and Caveats</u></b>
 *
 * <ol>
 *    <li>To include a literal "%" character in a string, double it.
 *
 *    <li>If a variable doesn't have a value, its reference is replaced
 *        by an empty string.
 *
 *    <li>As with all <tt>VariableSubstituter</tt> classes, an instance
 *        of the <tt>WindowsCmdVariableSubstituter</tt> class enforces
 *        its own variable syntax but defers actual variable value
 *        resolution to a separate <tt>VariableDereferencer</tt> object.
 *        One consequence of that approach is that variable names may be
 *        case-sensitive or case-insensitive, depending on how the supplied
 *        <tt>VariableDereferencer</tt> object interprets variable names.
 * </ol>
 *
 * <p>It's also possible to configure a <tt>WindowsCmdVariableSubstituter</tt>
 * to throw a {@link UndefinedVariableException}, rather than substituting
 * a blank, if a variable is undefined and a default value is not specified.
 * See the {@link #setAbortOnUndefinedVariable} method.</p>
 *
 * @see UnixShellVariableSubstituter
 * @see VariableDereferencer
 * @see VariableSubstituter
 * @see java.lang.String
 */
public class WindowsCmdVariableSubstituter
    extends AbstractVariableSubstituter
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor.
     */
    public WindowsCmdVariableSubstituter()
    {
        super();
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

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
     * name. If <tt>nameChecker</tt> is null, then this method assumes
     * that variable names may consist solely of alphanumeric characters and
     * underscores.</p>
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
     * @throws VariableSubstitutionException  substitution error
     *
     * @see #substitute(String,VariableDereferencer,Object)
     * @see VariableDereferencer#getVariableValue(String,Object)
     */
    public String substitute(      String               s,
                             final VariableDereferencer deref,
                             final VariableNameChecker  nameChecker,
                             final Object               context)
        throws VariableSyntaxException,
               UndefinedVariableException,
               VariableSubstitutionException
    {
        if (s != null)
            s = doSubstitution(s, context, nameChecker, deref);

        return s;
    }

    /*----------------------------------------------------------------------*\
                             Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Worker routine called by substitute() to perform the actual
     * substitution on a non-null string.
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
     * @throws VariableSubstitutionException  substitution error
     */
    private String doSubstitution(final String s,
                                  final Object context,
                                        VariableNameChecker nameChecker,
                                  final VariableDereferencer deref)
        throws VariableSubstitutionException
    {

        StringBuffer  result      = new StringBuffer();
        int           len         = s.length();
        char          prev        = '\0';
        StringBuffer  var         = new StringBuffer();
        boolean       inVar       = false;
        boolean       syntaxError = false;
        char          ch[];

        if (nameChecker == null)
            nameChecker = this;

        ch = s.toCharArray();
        for (int i = 0; i < len; i++)
        {
            char c = ch[i];

            if (c == '%')
            {
                if (inVar)
                {
                    if (prev == '%')
                    {
                        // Doubled "%". Insert one literal "%".

                        inVar = false;
                        result.append('%');
                    }

                    else
                    {
                        // End of variable reference. If the variable name
                        // is syntactically incorrect, just store the
                        // entire original sequence in the result string.

                        String varName = var.toString();
                        if (syntaxError)
                        {
                            result.append('%' + varName + '%');
                        }

                        else
                        {
                            String value = deref.getVariableValue(varName,
                                                                  context);
                            if (((value == null) || (value.length() == 0))
                                                 &&
                                (getAbortOnUndefinedVariable() == true))
                            {
                                throw new UndefinedVariableException
                                    ("Variable \"" + varName +
                                     "\" is not defined.");
                            }

                            result.append(value == null ? "" : value);
                        }

                        var.setLength(0);
                        inVar       = false;
                        syntaxError = false;
                        prev        = '\0';  // prevent match on trailing "%"
                    }
                }

                else
                {
                    // Possible start of a new variable.

                    inVar = true;
                    prev = c;
                }
            }

            else
            {
                // Not a '%'

                if (inVar)
                {
                    var.append(c);
                    if (! nameChecker.legalVariableCharacter(c))
                        syntaxError = true;
                }

                else
                {
                    result.append(c);
                }
                prev = c;
            }
        }

        if (inVar)
        {
            // Never saw the trailing "%" for the last variable reference.
            // Transfer the characters buffered in 'var' into the result,
            // without modification.

            result.append('%');
            result.append(var.toString());
            syntaxError = true;
        }

        if (syntaxError && (getAbortOnSyntaxError() == true))
        {
            throw new VariableSyntaxException
                ("Syntactically incorrect reference to variable \"" +
                 var.toString() + "\"");
        }

        return result.toString();
    }
}
