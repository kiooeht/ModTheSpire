package org.clapper.util.misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.TimeZone;

import java.net.InetAddress;

/**
 * <p>Contains constants for defining and accessing build info. Also
 * acts as a base class for specific packages' classes that retrieve
 * package build data.</p>
 */
public class BuildInfo
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * The key to retrieve the operating system where the build occurred.
     */
    public static final String BUILD_OS_KEY = "build.os";

    /**
     * The key to retrieve the VM used for the build.
     */
    public static final String BUILD_VM_KEY = "build.vm";

    /**
     * The key to retrieve the compiler used for the build.
     */
    public static final String BUILD_COMPILER_KEY = "build.compiler";

    /**
     * The key to retrieve the version of Ant used for the build.
     */
    public static final String BUILD_ANT_VERSION_KEY = "build.ant.version";

    /**
     * The build date, in "raw" (internal, numeric) form.
     */
    public static final String BUILD_DATE_KEY = "build.date";

    /**
     * The user and host where the build occurred.
     */
    public static final String BUILT_BY_KEY = "built.by";

    /**
     * The build ID, really just the time in a compressed format.
     */
    public static final String BUILD_ID_KEY = "build.id";

    /**
     * The date format, used with <tt>java.text.SimpleDateFormat</tt>,
     * used to write the date string to the build file. This format
     * can also be used to parse the date string, if necessary.
     */
    public static final String DATE_FORMAT_STRING = "yyyy/MM/dd HH:mm:ss z";

    /**
     * The date format, used with <tt>java.text.SimpleDateFormat</tt>,
     * used to create the build ID.
     */
    public static final String BUILD_ID_DATE_FORMAT_STRING
        = "yyyyMMdd.HHmmss.SSS";

    /*----------------------------------------------------------------------*\
                                Data Items
    \*----------------------------------------------------------------------*/

    private static ResourceBundle buildInfoBundle = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructor.
     *
     * @param bundleName  the resource bundle containing the build info
     */
    public BuildInfo (String bundleName)
    {
        getBuildInfoBundle (bundleName);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the build date, as a string, from the resource bundle.
     *
     * @return the build date, as a string
     */
    public String getBuildDate()
    {
        return getBundleString (BUILD_DATE_KEY);
    }

    /**
     * Get the string that identifies the user who built the software.
     * Typically, this string contains both the user ID and the host
     * where the build occurred.
     *
     * @return the user identification string
     */
    public String getBuildUserID()
    {
        return getBundleString (BUILT_BY_KEY);        
    }

    /**
     * Get the string that identifies the operating system where the build
     * occurred.
     *
     * @return the operating system string
     */
    public String getBuildOperatingSystem()
    {
        return getBundleString (BUILD_OS_KEY);
    }

    /**
     * Get the build ID string.
     *
     * @return the build ID string
     */
    public String getBuildID()
    {
        return getBundleString (BUILD_ID_KEY);
    }

    /**
     * Get the string that identifies the Java virtual machine that was
     * used during the build.
     *
     * @return the Java VM string
     */
    public String getBuildJavaVM()
    {
        return getBundleString (BUILD_VM_KEY);
    }

    /**
     * Get the Java compiler used during the build.
     *
     * @return the Java compiler string
     */
    public String getBuildJavaCompiler()
    {
        return getBundleString (BUILD_COMPILER_KEY);
    }

    /**
     * Get the version of Ant used during the build process.
     *
     * @return the Ant version string
     */
    public String getBuildAntVersion()
    {
        return getBundleString (BUILD_ANT_VERSION_KEY);
    }

    /**
     * Update the build bundle file.
     *
     * @param bundleFile   the path to the properties file
     * @param javaCompiler Java compiler name, or null if not known
     * @param antVersion   Ant version, or null if not known
     *
     * @throws IOException  Can't recreate file.
     */
    public static void makeBuildInfoBundle (File   bundleFile,
                                            String javaCompiler,
                                            String antVersion)
        throws IOException
    {
        // Fill an in-memory Properties object, which will be written to disk.

        Properties  props   = new Properties();

        // BUILD_VM_KEY

        String      vmInfo  = System.getProperty ("java.vm.name")
                            + " "
                            + System.getProperty ("java.vm.version")
                            + " ("
                            + System.getProperty ("java.vm.vendor")
                            + ")";
        props.setProperty (BUILD_VM_KEY, vmInfo);

        // BUILD_OS_KEY

        String      osInfo  = System.getProperty ("os.name")
                            + " "
                            + System.getProperty ("os.version")
                            + " ("
                            + System.getProperty ("os.arch")
                            + ")";
        props.setProperty (BUILD_OS_KEY, osInfo);

        // BUILD_COMPILER_KEY

        if (javaCompiler != null)
            props.setProperty (BUILD_COMPILER_KEY, javaCompiler);

        // BUILD_ANT_VERSION_KEY

        if (antVersion != null)
            props.setProperty (BUILD_ANT_VERSION_KEY, antVersion);

        // BUILD_DATE_KEY

        XDate now = new XDate();
        DateFormat dateFmt = new SimpleDateFormat (DATE_FORMAT_STRING);
        props.setProperty (BUILD_DATE_KEY, dateFmt.format (now));

        // BUILD_ID_KEY

        props.setProperty
            (BUILD_ID_KEY,
             now.formatInTimeZone (BUILD_ID_DATE_FORMAT_STRING,
                                   TimeZone.getTimeZone ("UTC")));

        // BUILT_BY_KEY

        String user = System.getProperty ("user.name");
        String host = "localhost";

        try
        {
            InetAddress localhost = InetAddress.getLocalHost();
            host = localhost.getHostName();
        }

        catch (Exception ex)
        {
        }

        props.setProperty (BUILT_BY_KEY, user + " on " + host);

        // Save it.

        String header = "Build information. "
                      + "AUTOMATICALLY GENERATED. DO NOT EDIT!";
        System.out.println ("Updating " + bundleFile);
        FileOutputStream  out  = new FileOutputStream (bundleFile);
        props.store (out, header);
    }

    /**
     * Display build information to the specified <tt>PrintStream</tt>.
     *
     * @param out  where to write the build information
     *
     * @see #showBuildInfo(PrintWriter)
     */
    public void showBuildInfo(PrintStream out)
    {
        showBuildInfo(new PrintWriter(out));
    }

    /**
     * Display build information to the specified <tt>PrintWriter</tt>.
     *
     * @param out  where to write the build information
     *
     * @see #showBuildInfo(PrintStream)
     */
    public void showBuildInfo(PrintWriter out)
    {
        out.println("Build date:     " + getBuildDate());
        out.println("Built by:       " + getBuildUserID());
        out.println("Built on:       " + getBuildOperatingSystem());
        out.println("Build Java VM:  " + getBuildJavaVM());
        out.println("Build compiler: " + getBuildJavaCompiler());
        out.println("Ant version:    " + getBuildAntVersion());
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Attempts to get the resource bundle, if an attempt hasn't already
     * been made.
     *
     * @param bundleName  name of resource bundle
     *
     * @return the bundle, or null if it could not be found.
     */
    private synchronized ResourceBundle getBuildInfoBundle (String bundleName)
    {
        if (buildInfoBundle == null)
        {
            try
            {
                buildInfoBundle = ResourceBundle.getBundle (bundleName);
            }

            catch (MissingResourceException ex)
            {
            }
        }

        return buildInfoBundle;
    }

    /**
     * Get a string from the bundle.
     *
     * @param key   The key for the string to be retrieved.
     *
     * @return  The string, or null if unavailable
     */
    private static String getBundleString (String key)
    {
        String result = "";

        try
        {
            if (buildInfoBundle != null)
                result = buildInfoBundle.getString (key);
        }

        catch (MissingResourceException ex)
        {
        }

        return result;
    }
}
