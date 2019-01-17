package org.clapper.util.classutil;

import java.io.File;

/**
 * Miscellaneous class-related utility methods.
 */
public class ClassUtil
{
    /*----------------------------------------------------------------------*\
                         Package-visible Constants
    \*----------------------------------------------------------------------*/

    static final String BUNDLE_NAME = "org.clapper.util.classutil.Bundle";

    /*----------------------------------------------------------------------*\
                                Static Data
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                               Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Can't be instantiated
     */
    private ClassUtil()
    {
        // Can't be instantiated
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Determine whether a file is a jar file, zip file or directory (i.e.,
     * represents places that can be searched for classes).
     *
     * @param file  the file to check
     *
     * @return <tt>true</tt> if the file represents a place that can be
     *         searched for classes, <tt>false</tt> if not
     */
    public static boolean fileCanContainClasses (File file)
    {
        boolean can      = false;
        String  fileName = file.getPath();

        if (file.exists())
        {
            can = ((fileName.toLowerCase().endsWith (".jar")) ||
                   (fileName.toLowerCase().endsWith (".zip")) ||
                   (file.isDirectory()));
        }

        return can;
    }

    /**
     * Strip the package name from a fully-qualified class name and return
     * just the short class name.
     *
     * @param fullClassName  the full class name
     *
     * @return the short name
     */
    public static String getShortClassName (String fullClassName)
    {
        String shortClassName = fullClassName;
        int i = shortClassName.lastIndexOf ('.');

        if ( (i != -1) && (++i < shortClassName.length()) )
            shortClassName = shortClassName.substring (i);

        return shortClassName;
    }

    /**
     * Strip the package name from a fully-qualified class name and return
     * just the short class name.
     *
     * @param cls  the <tt>Class</tt> object whose name is to be trimmed
     *
     * @return the short name
     */
    public static String getShortClassName (Class cls)
    {
        return getShortClassName (cls.getName());
    }

    /**
     * Convenience method that loads a class and attempts to instantiate it
     * via its default constructor. This method catches all the explicit,
     * checked exceptions that can occur and wraps them in a
     * {@link ClassUtilException}, reducing the number of lines of code
     * necessary to instantiate a class given its name. Note that this method
     * <i>only</i> catches and wraps checked exceptions. Unchecked exceptions,
     * such as <tt>ExceptionInInitializerError</tt>, are propagated directly
     * to the caller.
     *
     * @param className the fully-qualified class name
     *
     * @return the instantiated object
     *
     * @throws ClassUtilException on error
     */
    public static Object instantiateClass(String className)
        throws ClassUtilException
    {
        try
        {
            Class cls = Class.forName(className);
            return cls.newInstance();
        }

        catch (ClassNotFoundException ex)
        {
            throw new ClassUtilException("Can't load class " + className,
                                         ex);
        }

        catch (ClassCastException ex)
        {
            throw new ClassUtilException("Can't load class " + className,
                                         ex);
        }

        catch (IllegalAccessException ex)
        {
            throw new ClassUtilException("Can't load class " + className,
                                         ex);
        }

        catch (InstantiationException ex)
        {
            throw new ClassUtilException("Can't load class " + className,
                                         ex);
        }
    }
}
