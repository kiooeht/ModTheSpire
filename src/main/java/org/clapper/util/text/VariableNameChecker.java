package org.clapper.util.text;

/**
 * <p>This interface defines the methods for a class that checks characters
 * to determine whether they're legal for a variable name that's to be
 * substituted by a {@link VariableSubstituter} object. It has a single
 * method, {@link #legalVariableCharacter}, which determines whether a
 * specified character is a legal part of a variable name or not. This
 * capability provides additional flexibility by allowing callers to define
 * precisely what characters constitute legal variable names.</p>
 *
 * @see VariableSubstituter
 * @see VariableDereferencer
 */
public interface VariableNameChecker
{
    /*----------------------------------------------------------------------*\
                             Required Methods
    \*----------------------------------------------------------------------*/

    /**
     * <p>Determine whether a character may legally be used in a variable
     * name or not.</p>
     *
     * @param c   The character to test
     *
     * @return <tt>true</tt> if the character may be part of a variable name,
     *         <tt>false</tt> otherwise
     *
     * @see VariableSubstituter#substitute
     */
    public boolean legalVariableCharacter (char c);
}
