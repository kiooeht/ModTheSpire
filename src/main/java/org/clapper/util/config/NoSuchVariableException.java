package org.clapper.util.config;

/**
 * A <tt>NoSuchVariableException</tt> is thrown by the
 * {@link Configuration} class to signify that a requested configuration
 * variable does not exist.
 *
 * @see ConfigurationException
 */
public class NoSuchVariableException extends ConfigurationException
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /**
     * See JDK 1.5 version of java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private String variableName = null;
    private String sectionName  = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructs an exception.
     *
     * @param sectionName   the section that doesn't have the variable
     * @param variableName  the variable name to which the exception pertains
     */
    public NoSuchVariableException (String sectionName, String variableName)
    {
        super (Package.BUNDLE_NAME,
               "noSuchVariable",
               "Variable \"{0}\" does not exist in configuration section " +
               "\"{1}\"",
               new Object[] {variableName, sectionName});

        this.sectionName  = sectionName;
        this.variableName = variableName;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Gets the section name associated with this exception.
     *
     * @return the section name
     */
    public String getSectionName()
    {
        return sectionName;
    }

    /**
     * Gets the variable name associated with this exception.
     *
     * @return the variable name
     */
    public String getVariableName()
    {
        return variableName;
    }
}
