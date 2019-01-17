package org.clapper.util.classutil;

import java.util.Map;
import java.util.HashMap;

/**
 * <p><tt>SubclassClassFilter</tt> is a {@link ClassFilter} that matches
 * class names that (a) can be loaded and (b) extend a given subclass or
 * implement a specified interface, directly or indirectly. It uses the
 * <tt>java.lang.Class.isAssignableFrom()</tt> method, so it actually has to
 * load each class it tests. For maximum flexibility, a
 * <tt>SubclassClassFilter</tt> can be configured to use a specific class
 * loader.</p>
 */
public class SubclassClassFilter implements ClassFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private Class baseClass;

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>SubclassClassFilter</tt> that will accept
     * only classes that extend the specified class or implement the
     * specified interface.
     *
     * @param baseClassOrInterface  the base class or interface
     */
    public SubclassClassFilter (Class baseClassOrInterface)
    {
        this.baseClass = baseClassOrInterface;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Perform the acceptance test on the loaded <tt>Class</tt> object.
     *
     * @param classInfo   the {@link ClassInfo} object to test
     * @param classFinder the invoking {@link ClassFinder} object
     *
     * @return <tt>true</tt> if the class name matches,
     *         <tt>false</tt> if it doesn't
     */
    public boolean accept (ClassInfo classInfo, ClassFinder classFinder)
    {
        Map<String,ClassInfo> superClasses = new HashMap<String,ClassInfo>();

        if (baseClass.isInterface())
            classFinder.findAllInterfaces (classInfo, superClasses);
        else
            classFinder.findAllSuperClasses (classInfo, superClasses);

        return superClasses.keySet().contains (baseClass.getName());
    }
}
