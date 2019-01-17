package org.clapper.util.classutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.clapper.util.logging.Logger;

import org.clapper.util.io.AndFileFilter;
import org.clapper.util.io.FileOnlyFilter;
import org.clapper.util.io.FileFilterMatchType;
import org.clapper.util.io.RegexFileFilter;
import org.clapper.util.io.RecursiveFileFinder;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassReader;

/**
 * <p>A <tt>ClassFinder</tt> object is used to find classes. By default, an
 * instantiated <tt>ClassFinder</tt> won't find any classes; you have to
 * add the classpath (via a call to {@link #addClassPath}), add jar files,
 * add zip files, and/or add directories to the <tt>ClassFinder</tt> so it
 * knows where to look. Adding a jar file to a <tt>ClassFinder</tt> causes
 * the <tt>ClassFinder</tt> to look at the jar's manifest for a
 * "Class-Path" entry; if the <tt>ClassFinder</tt> finds such an entry, it
 * adds the contents to the search path, as well. After the
 * <tt>ClassFinder</tt> has been "primed" with things to search, you call
 * its {@link #findClasses findClasses()} method to have it search for
 * the classes, optionally passing a {@link ClassFilter} that can be used
 * to filter out classes you're not interested in.</p>
 *
 * <p>This package also contains a rich set of {@link ClassFilter}
 * implementations, including:</p>
 *
 * <ul>
 *  <li>A {@link RegexClassFilter} for filtering class names on a regular
 *      expression
 *  <li>Filters for testing various class attributes (such as whether a
 *      class is an interface, or a subclass of a known class, etc.
 *  <li>Filters that can combine other filters in logical operations
 * </ul>
 *
 * <p>The following example illustrates how you might use a
 * <tt>ClassFinder</tt> to locate all non-abstract classes that implement
 * the <tt>ClassFilter</tt> interface, searching the classpath as well
 * as anything specified on the command line.</p>
 *
 * <blockquote><pre>
 * import org.clapper.util.classutil.*;
 *
 * public class Test
 * {
 *     public static void main (String[] args) throws Throwable
 *     {
 *         ClassFinder finder = new ClassFinder();
 *         for (String arg : args)
 *             finder.add(new File(arg));
 *
 *         ClassFilter filter =
 *             new AndClassFilter
 *                 // Must not be an interface
 *                 (new NotClassFilter (new InterfaceOnlyClassFilter()),
 *
 *                 // Must implement the ClassFilter interface
 *                 new SubclassClassFilter (ClassFilter.class),
 *
 *                 // Must not be abstract
 *                 new NotClassFilter (new AbstractClassFilter()));
 *
 *         Collection&lt;ClassInfo&gt; foundClasses = new ArrayList&lt;ClassInfo&gt;();
 *         finder.findClasses (foundClasses, filter);
 *
 *         for (ClassInfo classInfo : foundClasses)
 *             System.out.println ("Found " + classInfo.getClassName());
 *     }
 * }
 * </pre></blockquote>
 *
 * <p>This class, and the {@link ClassInfo} class, rely on the ASM
 * byte-code manipulation library. If that library is not available, this
 * package will not work. See
 * <a href="http://asm.objectweb.org"><i>asm.objectweb.org</i></a>
 * for details on ASM.</p>
 *
 * <p><b>WARNING: This class is not thread-safe.</b></p>
 */
public class ClassFinder
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Places to search.
     */
    private LinkedHashMap<String,File> placesToSearch =
        new LinkedHashMap<String,File>();

    /**
     * Found classes. Cleared after every call to findClasses()
     */
    private Map<String,ClassInfo> foundClasses =
        new LinkedHashMap<String,ClassInfo>();

    /**
     * For logging
     */
    private static final Logger log = new Logger (ClassFinder.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>ClassFinder</tt> that will search for classes
     * using the default class loader.
     */
    public ClassFinder()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Add the contents of the system classpath for classes.
     */
    public void addClassPath()
    {
        String path = null;

        try
        {
            path = System.getProperty ("java.class.path");
        }

        catch (Exception ex)
        {
            path= "";
            log.error ("Unable to get class path", ex);
        }

        StringTokenizer tok = new StringTokenizer (path, File.pathSeparator);

        while (tok.hasMoreTokens())
            add (new File (tok.nextToken()));
    }

    /**
     * Add a jar file, zip file or directory to the list of places to search
     * for classes.
     *
     * @param file  the jar file, zip file or directory
     *
     * @return <tt>true</tt> if the file was suitable for adding;
     *         <tt>false</tt> if it was not a jar file, zip file, or
     *         directory.
     */
    public boolean add (File file)
    {
        boolean added = false;

        if (ClassUtil.fileCanContainClasses (file))
        {
            String absPath = file.getAbsolutePath();
            if (placesToSearch.get (absPath) == null)
            {
                placesToSearch.put (absPath, file);
                if (isJar (absPath))
                    loadJarClassPathEntries (file);
            }

            added = true;
        }

        return added;
    }

    /**
     * Add an array jar files, zip files and/or directories to the list of
     * places to search for classes.
     *
     * @param files  the array of jar files, zip files and/or directories.
     *               The array can contain a mixture of all of the above.
     *
     * @return the total number of items from the array that were actually
     *         added. (Elements that aren't jars, zip files or directories
     *         are skipped.)
     */
    public int add (File[] files)
    {
        int totalAdded = 0;

        for (File file : files)
        {
            if (add (file))
                totalAdded++;
        }

        return totalAdded;
    }

    /**
     * Add a <tt>Collection</tt> of jar files, zip files and/or directories
     * to the list of places to search for classes.
     *
     * @param files  the collection of jar files, zip files and/or directories.
     *
     * @return the total number of items from the collection that were actually
     *         added. (Elements that aren't jars, zip files or directories
     *         are skipped.)
     */
    public int add (Collection<File> files)
    {
        int totalAdded = 0;

        for (File file : files)
        {
            if (add (file))
                totalAdded++;
        }

        return totalAdded;
    }

    /**
     * Clear the finder's notion of where to search.
     */
    public void clear()
    {
        placesToSearch.clear();
        foundClasses.clear();
    }

    /**
     * Find all classes in the search areas, implicitly accepting all of
     * them.
     *
     * @param classes where to store the resulting matches
     *
     * @return the number of matched classes added to the collection
     */
    public int findClasses (Collection<ClassInfo> classes)
    {
        return findClasses (classes, null);
    }

    /**
     * Search all classes in the search areas, keeping only those that
     * pass the specified filter.
     *
     * @param classes where to store the resulting matches
     * @param filter  the filter, or null for no filter
     *
     * @return the number of matched classes added to the collection
     */
    public int findClasses (Collection<ClassInfo> classes,
                            ClassFilter           filter)
    {
        int total = 0;

        foundClasses.clear();

        // Load all the classes first.

        for (File file : placesToSearch.values())
        {
            String name = file.getPath();

            log.info ("Finding classes in " + name);
            if (isJar (name))
                processJar (name, foundClasses);
            else if (isZip (name))
                processZip (name, foundClasses);
            else
                processDirectory (file, foundClasses);
        }

        log.info ("Loaded " + foundClasses.size() + " classes.");

        // Next, weed out the ones we don't want.

        for (ClassInfo classInfo : foundClasses.values())
        {
            String className = classInfo.getClassName();
            String locationName = classInfo.getClassLocation().getPath();
            log.debug ("Looking at " + locationName + " (" + className + ")");

            if ((filter == null) || (filter.accept (classInfo, this)))
            {
                log.debug ("Filter accepted " + className);
                total++;
                classes.add (classInfo);
            }

            else
            {
                log.debug ("Filter rejected " + className);
            }
        }

        log.info ("Returning " + total + " total classes");
        foundClasses.clear();
        return total;
    }

    /**
     * Intended to be called only from a {@link ClassFilter} object's
     * {@link ClassFilter#accept accept()} method, this method attempts to
     * find all the superclasses (except <tt>java.lang.Object</tt>for a
     * given class, by checking all the currently-loaded class data.
     *
     * @param classInfo     the {@link ClassInfo} objects for the class
     * @param superClasses  where to store the {@link ClassInfo} objects
     *                      for the superclasses. The map is indexed by
     *                      class name.
     *
     * @return the number of superclasses found
     */
    public int findAllSuperClasses (ClassInfo             classInfo,
                                    Map<String,ClassInfo> superClasses)
    {
        int total = 0;

        String superClassName = classInfo.getSuperClassName();
        if (superClassName != null)
        {
            ClassInfo superClassInfo = foundClasses.get (superClassName);
            if (superClassInfo != null)
            {
                superClasses.put (superClassName, superClassInfo);
                total++;
                total += findAllSuperClasses (superClassInfo, superClasses);
            }
        }

        return total;
    }

    /**
     * Intended to be called only from a {@link ClassFilter} object's
     * {@link ClassFilter#accept accept()} method, this method attempts to
     * find all the interfaces implemented by given class (directly and
     * indirectly), by checking all the currently-loaded class data.
     *
     * @param classInfo     the {@link ClassInfo} objects for the class
     * @param interfaces    where to store the {@link ClassInfo} objects
     *                      for the interfaces. The map is indexed by
     *                      class name
     *
     * @return the number of interfaces found
     */
    public int findAllInterfaces (ClassInfo             classInfo,
                                  Map<String,ClassInfo> interfaces)
    {
        int total = 0;
        String superClassName = classInfo.getSuperClassName();
        if (superClassName != null)
        {
            ClassInfo superClassInfo = foundClasses.get (superClassName);
            if (superClassInfo != null)
            {
                total += findAllInterfaces (superClassInfo, interfaces);
            }
        }

        String[] interfaceNames = classInfo.getInterfaces();
        if (interfaces != null)
        {
            for (String interfaceName : interfaceNames)
            {
                ClassInfo intfClassInfo = foundClasses.get (interfaceName);
                if (intfClassInfo != null)
                {
                    interfaces.put (interfaceName, intfClassInfo);
                    total++;
                    total += findAllInterfaces (intfClassInfo, interfaces);
                }
            }
        }

        return total;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void processJar (String                jarName,
                             Map<String,ClassInfo> foundClasses)
    {
        JarFile jar = null;
        try
        {
            jar = new JarFile (jarName);
            File jarFile = new File (jarName);
            processOpenZip (jar, jarFile,
                            new ClassInfoClassVisitor (foundClasses, jarFile));
        }

        catch (IOException ex)
        {
            log.error ("Can't open jar file \"" + jarName + "\"", ex);
        }

        finally
        {
            try
            {
                if (jar != null) jar.close();
            }

            catch (IOException ex)
            {
                log.error ("Can't close " + jarName, ex);
            }

            jar = null;
        }
    }

    private void processZip (String                zipName,
                             Map<String,ClassInfo> foundClasses)
    {
        ZipFile zip = null;

        try
        {
            zip = new ZipFile (zipName);
            File zipFile = new File (zipName);
            processOpenZip (zip, zipFile,
                            new ClassInfoClassVisitor (foundClasses, zipFile));
        }

        catch (IOException ex)
        {
            log.error ("Can't open jar file \"" + zipName + "\"", ex);
        }

        finally
        {
            try
            {
                if (zip != null) zip.close();
            }

            catch (IOException ex)
            {
                log.error ("Can't close " + zipName, ex);
            }

            zip = null;
        }
    }

    private void processOpenZip (ZipFile      zip,
                                 File         zipFile,
                                 ClassVisitor classVisitor)
    {
        String zipName = zipFile.getPath();
        for (Enumeration<? extends ZipEntry> e = zip.entries();
             e.hasMoreElements(); )
        {
            ZipEntry entry = e.nextElement();

            if ((! entry.isDirectory()) &&
                (entry.getName().toLowerCase().endsWith (".class")))
            {
                try
                {
                    log.debug ("Loading " + zipName + "(" + entry.getName() +
                               ")");
                    loadClassData (zip.getInputStream (entry), classVisitor);
                }

                catch (IOException ex)
                {
                    log.error ("Can't open \"" + entry.getName() +
                               "\" in zip file \"" + zipName + "\": ",
                               ex);
                }

                catch (ClassUtilException ex)
                {
                    log.error ("Can't open \"" + entry.getName() +
                               "\" in zip file \"" + zipName + "\": ",
                               ex);
                }
            }
        }
    }

    private void processDirectory (File                  dir,
                                   Map<String,ClassInfo> foundClasses)
    {
        RecursiveFileFinder finder = new RecursiveFileFinder();
        RegexFileFilter nameFilter =
            new RegexFileFilter ("\\.class$", FileFilterMatchType.FILENAME);
        AndFileFilter fileFilter = new AndFileFilter (nameFilter,
                                                      new FileOnlyFilter());
        Collection<File> files = new ArrayList<File>();
        finder.findFiles (dir, fileFilter, files);

        ClassVisitor classVisitor = new ClassInfoClassVisitor (foundClasses,
                                                               dir);

        for (File f : files)
        {
            String path = f.getPath();
            log.debug ("Loading " + f.getPath());
            InputStream is = null;
            try
            {
                is = new FileInputStream(f);
                loadClassData (is, classVisitor);
            }

            catch (IOException ex)
            {
                log.error ("Can't open \"" + path + "\": ", ex);
            }

            catch (ClassUtilException ex)
            {
                log.error ("Can't open \"" + path + "\": ", ex);
            }

            finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }

                    catch (IOException ex)
                    {
                        log.error("Can't close InputStream for \"" +
                                  path + "\"",
                                  ex);
                    }
                }
            }
        }
    }

    private void loadJarClassPathEntries (File jarFile)
    {
        try
        {
            JarFile jar = new JarFile (jarFile);
            Manifest manifest = jar.getManifest();
            if (manifest == null)
                return;

            Map map = manifest.getEntries();
            Attributes attrs = manifest.getMainAttributes();
            Set<Object> keys = attrs.keySet();

            for (Object key : keys)
            {
                String value = (String) attrs.get (key);

                if (key.toString().equals ("Class-Path"))
                {
                    String jarName = jar.getName();
                    log.debug ("Adding Class-Path from jar " + jarName);

                    StringBuilder buf = new StringBuilder();
                    StringTokenizer tok = new StringTokenizer (value);
                    while (tok.hasMoreTokens())
                    {
                        buf.setLength (0);
                        String element = tok.nextToken();
                        String parent = jarFile.getParent();
                        if (parent != null)
                        {
                            buf.append (parent);
                            buf.append (File.separator);
                        }

                        buf.append (element);
                    }

                    String element = buf.toString();
                    log.debug ("From " + jarName + ": " + element);

                    add (new File (element));
                }
            }
        }

        catch (IOException ex)
        {
            log.error ("I/O error processing jar file \"" +
                       jarFile.getPath() + "\"",
                       ex);
        }
    }

    private void loadClassData (InputStream is, ClassVisitor classVisitor)
        throws ClassUtilException
    {
        try
        {
            ClassReader cr = new ClassReader (is);
            cr.accept(classVisitor, ClassInfo.ASM_CR_ACCEPT_CRITERIA);
        }

        catch (Exception ex)
        {
            throw new ClassUtilException (ClassUtil.BUNDLE_NAME,
                                          "ClassFinder.cantReadClassStream",
                                          "Unable to load class from open " +
                                          "input stream",
                                          ex);
        }
    }

    private boolean isJar (String fileName)
    {
        return fileName.toLowerCase().endsWith (".jar");
    }

    private boolean isZip (String fileName)
    {
        return fileName.toLowerCase().endsWith (".zip");
    }
}
