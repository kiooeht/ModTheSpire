package org.clapper.util.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**

/**
 * <p>The <tt>MultipleMapVariableDereferencer</tt> class implements the
 * <tt>VariableDereferencer</tt> interface and resolves variable
 * references by looking them up in one or more supplied <tt>Map</tt>
 * objects.</p>
 *
 * <p>The keys and values in the supplied <tt>Map</tt> objects
 * <b>must</b> be <tt>String</tt> objects.</p>
 *
 * <p>If more than one contained map has a value for a specific key, the
 * first map's value will be used. ("First", in this case, means the first
 * map added to the <tt>MultipleMapVariableDereference</tt> object.)</p>
 *
 * @see VariableDereferencer
 * @see VariableSubstituter
 * @see MapVariableDereferencer
 */
public class MultipleMapVariableDereferencer implements VariableDereferencer
{
    /*----------------------------------------------------------------------*\
                               Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                             Private Instance Data
    \*----------------------------------------------------------------------*/

    private Collection<Map<String,String>> mapList =
        new ArrayList<Map<String,String>>();

    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new instance of <tt>MultipleMapVariableDereferencer</tt>
     * that has no associated <tt>Map</tt> objects. <tt>Map</tt> objects
     * can be added with the {@link #addMap} method.
     */
    public MultipleMapVariableDereferencer()
    {
    }

    /**
     * Creates a new instance of <tt>MultipleMapVariableDereferencer</tt>
     * that is initialized with a specified set of maps.
     *
     * @param maps  the maps to use
     */
    public MultipleMapVariableDereferencer(Map<String,String>... maps)
    {
        for (Map<String,String> map : maps)
            addMap(map);
    }

    /*----------------------------------------------------------------------*\
                                Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Add a map to the list of maps to use when dereferencing variable values.
     * The map is added to the end of the list.
     *
     * @param map  The map to add
     */
    public final void addMap(Map<String,String> map)
    {
        mapList.add(map);
    }

    /**
     * Get the value associated with a given variable.
     *
     * @param varName  The name of the variable for which the value is
     *                 desired.
     * @param context  a context object, passed through from the caller
     *                 to the dereferencer, or null if there isn't one.
     *                 Ignored here.
     *
     * @return The variable's value. If the variable has no value, this
     *         method must return null.
     */
    public String getVariableValue (String varName, Object context)
    {
        String result = null;

        for (Map<String,String> map : mapList)
        {
            result = map.get(varName);
            if (result != null)
                break;
        }

        return result;
    }
}
