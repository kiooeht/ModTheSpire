package org.clapper.util.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.clapper.util.text.XStringBuilder;

/**
 * Contains the contents of a section.
 */
class Section
{
    /*----------------------------------------------------------------------*\
                               Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * Name of section
     */
    private String name;

    /**
     * Names of variables, in order encountered. Contains strings.
     */
    private List<String> variableNames = new ArrayList<String>();

    /**
     * List of Variable objects, indexed by variable name
     */
    private Map<String, Variable> valueMap = new HashMap<String, Variable>();

    /**
     * The section's unique ID. This ID increases monotonically from 1.
     */
    private int id = 0;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>Section</tt> object.
     *
     * @param name  the section name
     * @param id    the unique numeric ID
     */
    Section (String name, int id)
    {
        this.name = name;
        this.id   = id;
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get this section's unique numeric ID.
     *
     * @return the ID
     */
    int getID()
    {
        return id;
    }

    /**
     * Get this section's name
     *
     * @return the name
     */
    String getName()
    {
        return name;
    }

    /**
     * Get the names of all variables defined in this section, in the order
     * they were encountered in the file.
     *
     * @return an unmodifiable <tt>Collection</tt> of <tt>String</tt> variable
     *         names
     */
    Collection<String> getVariableNames()
    {
        return Collections.unmodifiableList (variableNames);
    }

    /**
     * Get a named variable from this section.
     *
     * @param varName  variable name to retrieve
     *
     * @return the <tt>Variable</tt> object, or null if not found
     *
     * @throws ConfigurationException on error
     */
    Variable getVariable (String varName)
        throws ConfigurationException
    {
        return valueMap.get (varName);
    }

    /**
     * Add a variable to this section, replacing any existing instance
     * of the variable.
     *
     * @param varName the variable name
     * @param value   its (presumably unexpanded) value
     */
    Variable addVariable (String varName, String value)
    {
        Variable variable = new Variable (varName, value, this);
        valueMap.put (varName, variable);
        variableNames.add (varName);
        return variable;
    }

    /**
     * Add a variable to this section, replacing any existing instance
     * of the variable.
     *
     * @param varName      the variable name
     * @param value        its (presumably unexpanded) value
     * @param lineDefined  line number in the file where it was defined
     */
    Variable addVariable (String varName, String value, int lineDefined)
    {
        Variable variable = addVariable (varName, value);
        variable.setLineWhereDefined (lineDefined);
        return variable;
    }

    /**
     * Add all the name/value pairs in a <tt>Map</tt> to this section,
     * overwriting any existing variables with the same names.
     *
     * @param map  the map
     */
    void addVariables (Map<String, String> map)
    {
        for (String varName : map.keySet())
        {
            String value = map.get (varName);
            addVariable (varName, value);
        }
    }

    /*----------------------------------------------------------------------*\
                               Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Double any backslashes found in the values of a map, returning the
     * possibly altered map.
     *
     * @param map  the map
     *
     * @return the <tt>map</tt> parameter
     */
    protected Map<String,String> 
    escapeEmbeddedBackslashes (Map<String,String> map)
    {
        XStringBuilder buf = new XStringBuilder();
        for (Iterator<String> it = map.keySet().iterator(); it.hasNext();)
        {
            String varName = it.next();
            String varValue = map.get (varName);

            if (varValue.indexOf ('\\') != -1)
            {
                // Have to map each backslash to four backslashes due to a 
                // double-parse issue.

                buf.clear();
                buf.append(varValue);
                buf.replaceAll("\\", "\\\\\\\\");
                map.put (varName, buf.toString());
            }
        }
        
        return map;
    }
}
        
