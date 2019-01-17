package org.clapper.util.classutil;

/**
 * Instances of classes that implement this interface are used, with a
 * {@link ClassFinder} object, to filter class names. This interface is
 * deliberately reminiscent of the <tt>java.io.FilenameFilter</tt>
 * interface.
 *
 * @see ClassFinder
 */
public interface ClassFilter
{
    /**
     * Tests whether a class name should be included in a class name
     * list.
     *
     * @param classInfo   the loaded information about the class
     * @param classFinder the {@link ClassFinder} that called this filter
     *                    (mostly for access to <tt>ClassFinder</tt>
     *                    utility methods)
     *
     * @return <tt>true</tt> if and only if the name should be included
     *         in the list; <tt>false</tt> otherwise
     */
    public boolean accept (ClassInfo classInfo, ClassFinder classFinder);
}
