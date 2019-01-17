package org.clapper.util.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the special "env" section
 *
 * @see Section
 */
class EnvSection extends Section
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>EnvSection</tt> object, loading its values from
     * the environment.
     *
     * @param name  the section name
     * @param id    the ID
     */
    EnvSection (String name, int id)
    {
        super (name, id);

        // Need a modifiable copy of the environment map, so we can
        // escape any embedded backslashes. (That's necessary because the
        // values from the environment will be subsituted pre-parse.)

        Map<String,String> env = new HashMap<String,String> (System.getenv());
        super.addVariables (escapeEmbeddedBackslashes (env));
    }
}
