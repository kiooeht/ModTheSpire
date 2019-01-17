package org.clapper.util.classutil;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.util.Map;

/**
 * <p>An ASM <tt>ClassVisitor</tt> that records the appropriate class
 * information for a {@link ClassFinder} object.</p>
 *
 * <p>This class relies on the ASM byte-code manipulation library. If that
 * library is not available, this package will not work. See
 * <a href="http://asm.objectweb.org"><i>asm.objectweb.org</i></a>
 * for details on ASM.</p>
 *
 * @see ClassFinder
 */
class ClassInfoClassVisitor extends ClassVisitor
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private Map<String,ClassInfo> foundClasses;
    private File                  location;
    private ClassInfo             currentClass = null;

    /*----------------------------------------------------------------------*\
                               Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructor
     *
     * @param foundClasses  where to store the class information. The
     *                      {@link ClassInfo} records are stored in the map,
     *                      indexed by class name.
     * @param location      file (jar, zip) or directory containing classes
     *                      being processed by this visitor
     *
     */
    ClassInfoClassVisitor(Map<String,ClassInfo> foundClasses, File location)
    {
        super(Opcodes.ASM5);
        this.foundClasses = foundClasses;
        this.location = location;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * "Visit" a class. Required by ASM <tt>ClassVisitor</tt> interface.
     *
     * @param version     class version
     * @param access      class access modifiers, etc.
     * @param name        internal class name
     * @param signature   class signature (not used here)
     * @param superName   internal super class name
     * @param interfaces  internal names of all directly implemented
     *                    interfaces
     */
    @Override
    public void visit(int      version,
                      int      access,
                      String   name,
                      String   signature,
                      String   superName,
                      String[] interfaces)
    {
        ClassInfo classInfo = new ClassInfo(name,
                                            superName,
                                            interfaces,
                                            access,
                                            location);
        // Be sure to use the converted name from classInfo.getName(), not
        // the internal value in "name".
        foundClasses.put(classInfo.getClassName(), classInfo);
        currentClass = classInfo;
    }

    /**
     * "Visit" a field.
     *
     * @param access      field access modifiers, etc.
     * @param name        field name
     * @param description field description
     * @param signature   field signature
     * @param value       field value, if any
     *
     * @return null.
     */
    @Override
    public FieldVisitor visitField(int access,
                                   String name,
                                   String description,
                                   String signature,
                                   Object value)
    {
        assert (currentClass != null);
        if (signature == null)
            signature = description + " " + name;
        return currentClass.visitField(access, name, description,
                                       signature, value);
    }

    /**
     * "Visit" a method.
     *
     * @param access      field access modifiers, etc.
     * @param name        field name
     * @param description field description
     * @param signature   field signature
     * @param exceptions  list of exception names the method throws
     *
     * @return null.
     */
    @Override
    public MethodVisitor visitMethod(int access,
                                     String name,
                                     String description,
                                     String signature,
                                     String[] exceptions)
    {
        assert (currentClass != null);
        if (signature == null)
            signature = name + description;
        return currentClass.visitMethod(access, name, description,
                                        signature, exceptions);
    }

    /**
     * Get the location (the jar file, zip file or directory) containing
     * the classes processed by this visitor.
     *
     * @return where the class was found
     */
    public File getClassLocation()
    {
        return location;
    }
}
