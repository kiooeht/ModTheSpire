package org.clapper.util.config;

import java.util.Map;
import org.clapper.util.misc.PropertiesMap;

/**
 * Implements the special "system" section
 *
 * @see Section
 */
class SystemSection extends Section
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>SystemSection</tt> object, loading its values from
     * the system properties list.
     *
     * @param name  the section name
     * @param id    the ID
     */
    SystemSection (String name, int id)
    {
        super (name, id);

        // Escape any embedded backslashes in variable values.

        Map<String,String> propMap = new PropertiesMap (System.getProperties());
        super.addVariables (escapeEmbeddedBackslashes (propMap));
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

}
