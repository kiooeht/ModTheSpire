package org.clapper.util.text;

/**
 * Abstract base class for {@link VariableSubstituter} classes, containing
 * various useful utility methods.
 *
 * @version <tt>$Revision$</tt>
 */
public abstract class AbstractVariableSubstituter
    implements VariableSubstituter, VariableNameChecker
{
    /*----------------------------------------------------------------------*\
                               Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                             Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * Whether or not to abort if a variable is undefined
     */
    private boolean abortOnUndefinedVariable = false;

    /**
     * Whether or not to abort on syntax error.
     */
    private boolean abortOnSyntaxError = false;

    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new instance of AbstractVariableSubstituter
     */
    protected AbstractVariableSubstituter()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Determine whether a character is a legal variable identifier character.
     * This default implementation permits alphanumerics, underscores and
     * periods ("."). Subclasses can override this method.
     *
     * @param c  The character
     *
     * @return <tt>true</tt> if the character is legal, <tt>false</tt>
     *         otherwise.
     */
    public boolean legalVariableCharacter (char c)
    {
        // Must be a letter, digit or underscore.

        return (Character.isLetterOrDigit (c) || (c == '_') || (c == '.'));
    }
    /**
     * <p>Substitute all variable references in the supplied string, using
     * a Unix Bourne Shell-style variable syntax. This method uses a
     * supplied <tt>VariableDereferencer</tt> object to resolve variable
     * values. Note that this method throws no exceptions. Syntax errors in
     * the variable references are silently ignored. Variables that have no
     * value are substituted as the empty string. This method assumes that
     * variable names may consist solely of alphanumeric characters,
     * underscores and periods. This syntax is sufficient to substitute
     * variables from <tt>System.properties</tt>, for instance. If you want
     * more control over the legal characters, use the one of the other
     * <tt>substitute</tt> methods.</p>
     *
     * <p>Calling this method is equivalent to:</p>
     *
     * <blockquote><pre>substitute(s, deref, null)</pre></blockquote>
     *
     * @param s       the string containing possible variable references
     * @param deref   the <tt>VariableDereferencer</tt> object
     *                to use to resolve the variables' values.
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
     * @see #substitute(String,VariableDereferencer,VariableNameChecker,Object)
     * @see VariableDereferencer#getVariableValue(String,Object)
     */
    public String substitute(String s, VariableDereferencer deref)
        throws VariableSyntaxException,
               UndefinedVariableException,
               VariableSubstitutionException
    {
        return substitute(s, deref, null);
    }


    /**
     * <p>Substitute all variable references in the supplied string, using
     * a Unix Bourne Shell-style variable syntax. This method uses a
     * supplied <tt>VariableDereferencer</tt> object to resolve variable
     * values. Note that this method throws no exceptions. Syntax errors in
     * the variable references are silently ignored. Variables that have no
     * value are substituted as the empty string. This method assumes that
     * variable names may consist solely of alphanumeric characters,
     * underscores and periods. This syntax is sufficient to substitute
     * variables from <tt>System.properties</tt>, for instance. If you want
     * more control over the legal characters, use the one of the other
     * <tt>substitute</tt> methods.</p>
     *
     * @param s       the string containing possible variable references
     * @param deref   the <tt>VariableDereferencer</tt> object
     *                to use to resolve the variables' values.
     * @param context an optional context object, passed through unmodified
     *                to the <tt>deref</tt> object's
     *                {@link VariableDereferencer#getVariableValue} method.
     *                This object can be anything at all (and, in fact, may
     *                be null if you don't care.) It's primarily useful
     *                for passing context information from the caller to
     *                the (custom) <tt>VariableDereferencer</tt>.
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
     * @see #substitute(String,VariableDereferencer,VariableNameChecker,Object)
     * @see VariableDereferencer#getVariableValue(String,Object)
     */
    public String substitute(String s,
                             VariableDereferencer deref,
                             Object context)
        throws VariableSyntaxException,
               UndefinedVariableException,
               VariableSubstitutionException
    {
        return substitute(s, deref, null, context);
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
    public abstract String substitute(String s,
                                      VariableDereferencer deref,
                                      VariableNameChecker nameChecker,
                                      Object context)
        throws VariableSyntaxException,
               UndefinedVariableException,
               VariableSubstitutionException;

    /**
     * Get the value of the flag that controls whether the
     * <tt>substitute()</tt> methods will abort when they encounter a
     * syntax error. If this flag is clear, then this object will recover
     * from a syntax error (usually by leaving the syntactically bad
     * variable reference untouched in the resulting string). If this flag
     * is set, then a syntax error results in a
     * {@link VariableSyntaxException}.
     *
     * @return <tt>true</tt> if the "abort on syntax error" capability
     *         is enabled, <tt>false</tt> if it is disabled.
     *
     * @see #setAbortOnUndefinedVariable
     */
    public boolean getAbortOnSyntaxError()
    {
        return abortOnSyntaxError;
    }

    /**
     * Get the value of the flag that controls whether the
     * <tt>substitute()</tt> methods will abort when they encounter an
     * undefined variable. If this flag is clear, then an undefined variable
     * is expanded to an empty string. If this flag is set, then an undefined
     * value results in an {@link UndefinedVariableException}.
     *
     * @return <tt>true</tt> if the "abort on undefined variable" capability
     *         is enabled, <tt>false</tt> if it is disabled.
     * @see #setAbortOnUndefinedVariable
     */
    public boolean getAbortOnUndefinedVariable()
    {
        return abortOnUndefinedVariable;
    }

    /**
     * Set or clear the flag that controls whether the
     * <tt>substitute()</tt> methods will abort when they encounter a
     * syntax error. If this flag is clear, then this object will recover
     * from a syntax error (usually by leaving the syntactically bad
     * variable reference untouched in the resulting string). If this flag
     * is set, then a syntax error results in a
     * {@link VariableSyntaxException}. The flag defaults to <tt>false</tt>.
     *
     * @param enable  <tt>true</tt> to enable the "abort on syntax error"
     *                flag, <tt>false</tt> to disable it.
     *
     * @see #getAbortOnUndefinedVariable
     */
    public void setAbortOnSyntaxError(boolean enable)
    {
        abortOnSyntaxError = enable;
    }

    /**
     * Set or clear the flag that controls whether the <tt>substitute()</tt>
     * methods will abort when they encounter an undefined variable. If this
     * flag is clear, then an undefined variable is expanded to an empty
     * string. If this flag is set, then an undefined value results in an
     * {@link UndefinedVariableException}. The flag defaults to <tt>false</tt>.
     *
     * @param enable  <tt>true</tt> to enable the "abort on undefined variable"
     *                flag, <tt>false</tt> to disable it.
     * @see #getAbortOnUndefinedVariable
     */
    public void setAbortOnUndefinedVariable(boolean enable)
    {
        abortOnUndefinedVariable = enable;
    }
}
