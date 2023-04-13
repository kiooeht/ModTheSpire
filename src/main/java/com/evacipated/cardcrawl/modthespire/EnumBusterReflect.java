package com.evacipated.cardcrawl.modthespire;

import org.clapper.util.classutil.*;
import sun.reflect.ConstructorAccessor;
import sun.reflect.ReflectionFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class EnumBusterReflect {
    private static final Class[] EMPTY_CLASS_ARRAY =
        new Class[0];
    private static final Object[] EMPTY_OBJECT_ARRAY =
        new Object[0];

    private static final String VALUES_FIELD = "$VALUES";
    private static final String ORDINAL_FIELD = "ordinal";

    private final ReflectionFactory reflection =
        ReflectionFactory.getReflectionFactory();

    private final ClassLoader loader;
    private final Class<?> clazz;

    private final Collection<Field> switchFields;

    private final Deque<Memento> undoStack =
        new LinkedList<Memento>();

    /**
     * Construct an EnumBuster for the given enum class and keep
     * the switch statements of the classes specified in
     * switchUsers in sync with the enum values.
     */
    public EnumBusterReflect(ClassLoader loader, Class<?> clazz) throws NoSuchFieldException, ClassNotFoundException
    {
        this.loader = loader;
        this.clazz = clazz;
        switchFields = findRelatedSwitchFields();
    }

    /**
     * Make a new enum instance, without adding it to the values
     * array and using the default ordinal of 0.
     */
    public Enum<?> make(String value) {
        return make(value, 0,
            EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Make a new enum instance with the given ordinal.
     */
    public Enum<?> make(String value, int ordinal) {
        return make(value, ordinal,
            EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Make a new enum instance with the given value, ordinal and
     * additional parameters.  The additionalTypes is used to match
     * the constructor accurately.
     */
    public Enum<?> make(String value, int ordinal,
                  Class[] additionalTypes, Object[] additional) {
        try {
            undoStack.push(new Memento());
            ConstructorAccessor ca = findConstructorAccessor(
                additionalTypes, clazz);
            return constructEnum(clazz, ca, value,
                ordinal, additional);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Could not create enum", e);
        }
    }

    /**
     * This method adds the given enum into the array
     * inside the enum class.  If the enum already
     * contains that particular value, then the value
     * is overwritten with our enum.  Otherwise it is
     * added at the end of the array.
     *
     * In addition, if there is a constant field in the
     * enum class pointing to an enum with our value,
     * then we replace that with our enum instance.
     *
     * The ordinal is either set to the existing position
     * or to the last value.
     *
     * Warning: This should probably never be called,
     * since it can cause permanent changes to the enum
     * values.  Use only in extreme conditions.
     *
     * @param e the enum to add
     */
    public void addByValue(Enum<?> e) {
        try {
            undoStack.push(new Memento());
            Field valuesField = findValuesField();

            // we get the current Enum[]
            Enum<?>[] values = values();
            for (int i = 0; i < values.length; i++) {
                Enum<?> value = values[i];
                if (value.name().equals(e.name())) {
                    setOrdinal(e, value.ordinal());
                    values[i] = e;
                    replaceConstant(e);
                    return;
                }
            }

            // we did not find it in the existing array, thus
            // append it to the array
            Enum<?>[] newValues =
                Arrays.copyOf(values, values.length + 1);
            newValues[newValues.length - 1] = e;
            ReflectionHelper.setStaticFinalField(
                valuesField, newValues);

            int ordinal = newValues.length - 1;
            setOrdinal(e, ordinal);
            addSwitchCase();
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                "Could not set the enum", ex);
        }
    }

    /**
     * We delete the enum from the values array and set the
     * constant pointer to null.
     *
     * @param e the enum to delete from the type.
     * @return true if the enum was found and deleted;
     *         false otherwise
     */
    public boolean deleteByValue(Enum<?> e) {
        if (e == null) throw new NullPointerException();
        try {
            undoStack.push(new Memento());
            // we get the current E[]
            Enum<?>[] values = values();
            for (int i = 0; i < values.length; i++) {
                Enum<?> value = values[i];
                if (value.name().equals(e.name())) {
                    Enum<?>[] newValues =
                        Arrays.copyOf(values, values.length - 1);
                    System.arraycopy(values, i + 1, newValues, i,
                        values.length - i - 1);
                    for (int j = i; j < newValues.length; j++) {
                        setOrdinal(newValues[j], j);
                    }
                    Field valuesField = findValuesField();
                    ReflectionHelper.setStaticFinalField(
                        valuesField, newValues);
                    removeSwitchCase(i);
                    blankOutConstant(e);
                    return true;
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                "Could not set the enum", ex);
        }
        return false;
    }

    /**
     * Undo the state right back to the beginning when the
     * EnumBuster was created.
     */
    public void restore() {
        while (undo()) {
            //
        }
    }

    /**
     * Undo the previous operation.
     */
    public boolean undo() {
        try {
            Memento memento = undoStack.poll();
            if (memento == null) return false;
            memento.undo();
            return true;
        } catch (Exception e) {
            throw new IllegalStateException("Could not undo", e);
        }
    }

    private ConstructorAccessor findConstructorAccessor(
        Class[] additionalParameterTypes,
        Class<?> clazz) throws NoSuchMethodException {
        Class[] parameterTypes =
            new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = int.class;
        System.arraycopy(
            additionalParameterTypes, 0,
            parameterTypes, 2,
            additionalParameterTypes.length);
        Constructor<?> cstr = clazz.getDeclaredConstructor(
            parameterTypes
        );
        return reflection.newConstructorAccessor(cstr);
    }

    private Enum<?> constructEnum(Class<?> clazz,
                            ConstructorAccessor ca,
                            String value, int ordinal,
                            Object[] additional)
        throws Exception {
        Object[] parms = new Object[additional.length + 2];
        parms[0] = value;
        parms[1] = ordinal;
        System.arraycopy(
            additional, 0, parms, 2, additional.length);
        return (Enum<?>)clazz.cast(ca.newInstance(parms));
    }

    /**
     * The only time we ever add a new enum is at the end.
     * Thus all we need to do is expand the switch map arrays
     * by one empty slot.
     */
    private void addSwitchCase() {
        try {
            for (Field switchField : switchFields) {
                int[] switches = (int[]) switchField.get(null);
                switches = Arrays.copyOf(switches, switches.length + 1);
                ReflectionHelper.setStaticFinalField(
                    switchField, switches
                );
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Could not fix switch", e);
        }
    }

    private void replaceConstant(Enum<?> e)
        throws IllegalAccessException, NoSuchFieldException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(e.name())) {
                ReflectionHelper.setStaticFinalField(
                    field, e
                );
            }
        }
    }


    private void blankOutConstant(Enum<?> e)
        throws IllegalAccessException, NoSuchFieldException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(e.name())) {
                ReflectionHelper.setStaticFinalField(
                    field, null
                );
            }
        }
    }

    private void setOrdinal(Enum<?> e, int ordinal)
        throws NoSuchFieldException, IllegalAccessException {
        Field ordinalField = Enum.class.getDeclaredField(
            ORDINAL_FIELD);
        ordinalField.setAccessible(true);
        ordinalField.set(e, ordinal);
    }

    /**
     * Method to find the values field, set it to be accessible,
     * and return it.
     *
     * @return the values array field for the enum.
     * @throws NoSuchFieldException if the field could not be found
     */
    private Field findValuesField()
        throws NoSuchFieldException {
        // first we find the static final array that holds
        // the values in the enum class
        Field valuesField = clazz.getDeclaredField(
            VALUES_FIELD);
        // we mark it to be public
        valuesField.setAccessible(true);
        return valuesField;
    }

    private Collection<Field> findRelatedSwitchFields() throws ClassNotFoundException, NoSuchFieldException
    {
        Collection<Field> result = new ArrayList<Field>();

        ClassFinder finder = new ClassFinder();
        finder.add(new File(ModTheSpire.STS_JAR));

        ClassFilter filter =
            new AndClassFilter(
                new NotClassFilter(new InterfaceOnlyClassFilter()),
                new NotClassFilter(new AbstractClassFilter()),
                new RegexClassFilter("com\\.megacrit\\.cardcrawl\\..+\\$1")
            );
        Collection<ClassInfo> foundClasses = new ArrayList<>();
        finder.findClasses(foundClasses, filter);

        if (ModTheSpire.DEBUG) {
            System.out.println();
            System.out.println(clazz.getName());
        }
        int count = 0;
        for (ClassInfo classInfo : foundClasses) {
            for (FieldInfo field : classInfo.getFields()) {
                String switchMapName = "$SwitchMap$" + clazz.getName().replace('.', '$');
                if (field.getName().equals(switchMapName)) {
                    count++;
                    if (ModTheSpire.DEBUG) System.out.println("  " + classInfo.getClassName());
                    Field realField = loader.loadClass(classInfo.getClassName()).getDeclaredField(field.getName());
                    realField.setAccessible(true);
                    result.add(realField);
                }
            }
        }
        if (ModTheSpire.DEBUG) System.out.println(count + " switch statement(s)");

        return  result;
    }

    private void removeSwitchCase(int ordinal) {
        try {
            for (Field switchField : switchFields) {
                int[] switches = (int[]) switchField.get(null);
                int[] newSwitches = Arrays.copyOf(
                    switches, switches.length - 1);
                System.arraycopy(switches, ordinal + 1, newSwitches,
                    ordinal, switches.length - ordinal - 1);
                ReflectionHelper.setStaticFinalField(
                    switchField, newSwitches
                );
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Could not fix switch", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Enum<?>[] values()
        throws NoSuchFieldException, IllegalAccessException {
        Field valuesField = findValuesField();
        return (Enum<?>[]) valuesField.get(null);
    }

    private class Memento {
        private final Enum<?>[] values;
        private final Map<Field, int[]> savedSwitchFieldValues =
            new HashMap<Field, int[]>();

        private Memento() throws IllegalAccessException {
            try {
                values = values().clone();
                for (Field switchField : switchFields) {
                    int[] switchArray = (int[]) switchField.get(null);
                    savedSwitchFieldValues.put(switchField,
                        switchArray.clone());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Could not create the class", e);
            }
        }

        private void undo() throws
            NoSuchFieldException, IllegalAccessException {
            Field valuesField = findValuesField();
            ReflectionHelper.setStaticFinalField(valuesField, values);

            for (int i = 0; i < values.length; i++) {
                setOrdinal(values[i], i);
            }

            // reset all of the constants defined inside the enum
            Map<String, Enum<?>> valuesMap =
                new HashMap<String, Enum<?>>();
            for (Enum<?> e : values) {
                valuesMap.put(e.name(), e);
            }
            Field[] constantEnumFields = clazz.getDeclaredFields();
            for (Field constantEnumField : constantEnumFields) {
                Enum<?> en = valuesMap.get(constantEnumField.getName());
                if (en != null) {
                    ReflectionHelper.setStaticFinalField(
                        constantEnumField, en
                    );
                }
            }

            for (Map.Entry<Field, int[]> entry :
                savedSwitchFieldValues.entrySet()) {
                Field field = entry.getKey();
                int[] mappings = entry.getValue();
                ReflectionHelper.setStaticFinalField(field, mappings);
            }
        }
    }
}
