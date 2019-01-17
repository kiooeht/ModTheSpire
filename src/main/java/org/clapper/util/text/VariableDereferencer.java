package org.clapper.util.text;

/**
 * <p>The <code>VariableDereferencer</code> interface defines the behavior
 * of classes that can look up variables by name, returning their values.
 * It is used primarily to mark classes that can work hand-in-hand with
 * <code>VariableSubstituter</code> objects to resolve variable references
 * in strings.</p>
 *
 * <p>The values for referenced variables can come from anywhere (in a
 * <code>Properties</code> object, via direct method calls, from a symbol
 * table, etc.), provided the values can be located using only the
 * variable's name.</p>
 *
 * @see MapVariableDereferencer
 * @see VariableSubstituter
 */
public interface VariableDereferencer
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the value associated with a given variable.
     *
     * @param varName  The name of the variable for which the value is
     *                 desired.
     * @param context  a context object, passed through from the caller
     *                 to the dereferencer, or null if there isn't one.
     *
     * @return The variable's value. If the variable has no value, this
     *         method must return null.
     *
     * @throws VariableSubstitutionException  substitution error
     */
    public String getVariableValue (String varName, Object context)
        throws VariableSubstitutionException;
}
