package org.clapper.util.text;

/**
 * <p>This interface defines the methods for a class that does inline
 * variable substitution, converting strings containing variable references
 * into strings where the variables are replaced with their corresponding
 * values. It defines the semantics of variable substitution, but it does
 * not define the syntax. Each subclass defines its own syntax (and logic)
 * for recognizing and replacing a variable reference within a string.</p>
 *
 * <p>The <tt>VariableSubstituter</tt> interface defines the minimum
 * functionality and behavior of classes that provide inline string
 * variable substitution. A <tt>VariableSubstituter</tt> object
 * replaces variable references in a string with the variables' values.
 * Examples of variable references include (but are not limited to) some of
 * the following:</p>
 *
 * <blockquote>
 * <pre>
 * The value of HOME is %HOME%            # Windows-style variable reference
 * The value of HOME is $HOME             # Unix shell-style variable reference
 * The value of HOME is %(HOME)s          # Python ConfigParser syntax
 * </pre>
 * </blockquote>
 *
 * <p>A <tt>VariableSubstituter</tt> object parses a string passed to
 * it. It copies literal text from the source string to the result string;
 * when it encounters a variable reference, it resolves the variable's
 * value through a separate {@link VariableDereferencer} object, and
 * substitutes the resulting value in place of the variable reference in
 * the result string. Each individual <tt>VariableSubstituter</tt>
 * class uses its own variable reference syntax. For example, the
 * {@link UnixShellVariableSubstituter} class recognizes variable
 * references using the traditional UNIX Bourne Shell variable syntax. The
 * {@link WindowsCmdVariableSubstituter} class recognizes %-delimited
 * variable references, the way the Windows <tt>cmd.exe</tt> program
 * does.</p>
 *
 * <p>A <tt>VariableSubstituter</tt> object dereferences a variable's
 * value by calling the <tt>getVariableValue()</tt> method in a
 * <tt>VariableDereferencer</tt> object. One such object might choose
 * to dereference variables by looking them up in a <tt>Properties</tt>
 * object. Another might resolve variables via hard-coded method calls. Yet
 * another might use a symbol table built by a parser. The variable
 * substitution logic is separated from the variable dereferencing logic;
 * the same variable substitution object (with its specific variable
 * syntax) can resolve variables from a number of different sources.</p>
 *
 * @see VariableDereferencer
 */

public interface VariableSubstituter
{
    /*----------------------------------------------------------------------*\
                             Required Methods
    \*----------------------------------------------------------------------*/

    /**
     * <p>Substitute all variable references in the supplied string,
     * returning the new String, according to the variable syntax defined
     * by the implementing class. This method uses a supplied
     * <tt>VariableDereferencer</tt> object to resolve variable values.
     * Note that this method throws no exceptions. Syntax errors in the
     * variable references are silently ignored. Variables that have no
     * value are substituted as the empty string. The
     * <tt>VariableSubstituter</tt> class enforces its own notion of what
     * characters are legal in a variable name. If you want more control
     * over the legal characters, use the second <tt>substitute</tt>
     * method.</p>
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
    public String substitute (String               s,
                              VariableDereferencer deref,
                              Object               context)
        throws VariableSubstitutionException;

    /**
     * <p>Substitute all variable references in the supplied string,
     * returning the new String, according to the variable syntax defined
     * by the implementing class. This method uses a supplied
     * <tt>VariableDereferencer</tt> object to resolve variable values.
     * Note that this method throws no exceptions. Syntax errors in the
     * variable references are silently ignored. Variables that have no
     * value are substituted as the empty string. If the
     * <tt>nameChecker</tt> parameter is not null, this method calls its
     * {@link VariableNameChecker#legalVariableCharacter(char)} method to
     * determine whether a given character is a legal part of a variable
     * name. If <tt>nameChecker</tt> is null, then this method uses its
     * own notion of legal variable name characters.</p>
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
     * @throws UndefinedVariableException     undefined variable, and
     *                                        {@link #getAbortOnUndefinedVariable}
     *                                        returns true
     * @throws VariableSyntaxException        syntax error, and
     *                                        {@link #getAbortOnSyntaxError}
     *                                        returns true
     *
     * @see #substitute(String,VariableDereferencer,Object)
     * @see VariableDereferencer#getVariableValue(String,Object)
     */
    public String substitute (String               s,
                              VariableDereferencer deref,
                              VariableNameChecker  nameChecker,
                              Object               context)
        throws VariableSubstitutionException;

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
    public boolean getAbortOnUndefinedVariable();

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
    public void setAbortOnUndefinedVariable(boolean enable);

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
    public boolean getAbortOnSyntaxError();

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
    public void setAbortOnSyntaxError(boolean enable);
}
