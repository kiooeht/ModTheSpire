package com.evacipated.cardcrawl.modthespire;

import sun.reflect.*;
import java.lang.reflect.*;

public class ReflectionHelper {
    private static final String MODIFIERS_FIELD = "modifiers";

    private static final ReflectionFactory reflection =
        ReflectionFactory.getReflectionFactory();

    public static void setStaticFinalField(
        Field field, Object value)
        throws NoSuchFieldException, IllegalAccessException {
        // we mark the field to be public
        field.setAccessible(true);
        // next we change the modifier in the Field instance to
        // not be final anymore, thus tricking reflection into
        // letting us modify the static final field
        Field modifiersField =
            Field.class.getDeclaredField(MODIFIERS_FIELD);
        modifiersField.setAccessible(true);
        int modifiers = modifiersField.getInt(field);
        // blank out the final bit in the modifiers int
        modifiers &= ~Modifier.FINAL;
        modifiersField.setInt(field, modifiers);
        FieldAccessor fa = reflection.newFieldAccessor(
            field, false
        );
        fa.set(null, value);
    }
}
