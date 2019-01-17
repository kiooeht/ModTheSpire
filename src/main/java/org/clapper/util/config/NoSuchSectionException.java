package org.clapper.util.config;

/**
 * A <tt>NoSuchSectionException</tt> is thrown by the
 * {@link Configuration} class to signify that a requested configuration
 * section does not exist.
 *
 * @see ConfigurationException
 */
public class NoSuchSectionException extends ConfigurationException
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

    private String sectionName = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructs an exception.
     *
     * @param sectionName  the section name to which the exception pertains
     */
    public NoSuchSectionException (String sectionName)
    {
        super (Package.BUNDLE_NAME,
               "noSuchSection",
               "Configuration section \"{0}\" does not exist",
               new Object[] {sectionName});

        this.sectionName = sectionName;
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
}
