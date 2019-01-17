package org.clapper.util.misc;

/**
 * <p>Contains the software version for the <i>org.clapper.util</i>
 * library. Also contains a main program which, invoked, displays the name
 * of the API, the version, and detailed build information on standard
 * output.</p>
 */
public final class Version extends VersionBase
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * The name of the resource bundle containing the build info.
     */
    public static final String BUILD_INFO_BUNDLE_NAME
        = "org.clapper.util.misc.BuildInfoBundle";
    

    /*----------------------------------------------------------------------*\
                             Private Data Items
    \*----------------------------------------------------------------------*/
    
    private static Version instance = new Version();

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private Version()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get an instance of this class.
     *
     * @return a singleton instance of this class.
     */
    public static Version getInstance()
    {
        return new Version();
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/


    /**
     * Get the class name of the version resource bundle, which contains
     * values for the product version, copyright, etc.
     *
     * @return the name of the version resource bundle
     */
    protected String getVersionBundleName()
    {
        return "org.clapper.util.misc.Bundle";
    }

    /**
     * Get the class name of the build info resource bundle, which contains
     * data about when the product was built, generated (presumably)
     * during the build by
     * {@link BuildInfo#makeBuildInfoBundle BuildInfo.makeBuildInfoBundle()}.
     *
     * @return the name of the build info resource bundle
     */
    protected String getBuildInfoBundleName()
    {
        return BUILD_INFO_BUNDLE_NAME;
    }

    /**
     * Get the key for the version string. This key is presumed to be
     * in the version resource bundle.
     *
     * @return the version string key
     */
    protected String getVersionKey()
    {
        return "api.version";
    }

    /**
     * Get the key for the copyright string. This key is presumed to be
     * in the version resource bundle.
     *
     * @return the copyright string key
     */
    protected String getCopyrightKey()
    {
        return "api.copyright";
    }

    /**
     * Get the key for the name of the utility or application.
     *
     * @return the key
     */
    protected String getApplicationNameKey()
    {
        return "api.name";
    }

    /*----------------------------------------------------------------------*\
                               Main Program
    \*----------------------------------------------------------------------*/

    /**
     * Display the build information
     *
     * @param args  command-line parameters (ignored)
     */
    public static void main (String[] args)
    {
        System.out.println(getInstance().getVersionDisplay());
        System.exit (0);
    }
}
