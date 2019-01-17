package org.clapper.util.config;

import org.clapper.util.text.TextUtil;

import java.util.Date;
import java.util.Locale;

import java.text.SimpleDateFormat;

import java.io.File;
import java.io.IOException;

/**
 * Implements the special "program" section.
 *
 * @see Section
 */
class ProgramSection extends Section
{
    /*----------------------------------------------------------------------*\
                         Package-visible Constants
    \*----------------------------------------------------------------------*/

    static final String PROGRAM_NOW_VAR     = "now";
    static final String PROGRAM_CWD_VAR     = "cwd";
    static final String PROGRAM_CWD_URL_VAR = "cwd.url";

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>ProgramSection</tt> object, loading its values from
     * the system properties list.
     *
     * @param name  the section name
     * @param id    the unique numeric ID
     *
     * @throws ConfigurationException  failed to initialize object
     */
    ProgramSection (String name, int id)
        throws ConfigurationException
    {
        super (name, id);
        loadProgramSectionStaticValues();
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

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
        Variable result;

        // Handle the dynamic ones explicitly.

        if (varName.startsWith (PROGRAM_NOW_VAR))
        {
            result = new Variable (varName, substituteDatetime (varName),
                                   this, 0);
        }
        else
        {
            result = super.getVariable (varName);
        }

        return result;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void loadProgramSectionStaticValues() throws ConfigurationException
    {
        try
        {
            File dir = new File (".");
            String value = dir.getCanonicalPath();
            addVariable (PROGRAM_CWD_VAR, value);

            value = dir.getCanonicalFile().toURI().toURL().toString();
            if (value.charAt (value.length() - 1) == '/')
                value = value.substring (0, value.length() - 1);
            addVariable (PROGRAM_CWD_URL_VAR, value);
        }

        catch (IOException ex)
        {
            throw new ConfigurationException (ex);
        }
    }

    /**
     * Handle substitution of a date variable in the [program] section.
     *
     * @param varName  the variable name
     *
     * @return the formatted date/time value
     *
     * @throws ConfigurationException bad date format, or something
     */
    private String substituteDatetime (String varName)
         throws ConfigurationException
    {
        String value = "";
        Date   now   = new Date();

        if (varName.equals (PROGRAM_NOW_VAR))
        {
            value = now.toString();
        }

        else
        {
            char delim = varName.charAt (PROGRAM_NOW_VAR.length());
            String[] tokens = TextUtil.split (varName, delim);
            if ((tokens.length != 2) && (tokens.length != 4))
            {
                throw new ConfigurationException
                    (Package.BUNDLE_NAME, "ProgramSection.badNowFieldCount",
                     "Section \"{0}\", variable reference \"{1}\": " +
                     "Incorrect number of fields in extended version of " +
                     "\"{2}\" variable. Found {3} fields, expected either " +
                     "{4} or {5}.",
                     new Object[]
                     {
                         this.getName(),
                         varName,
                         PROGRAM_NOW_VAR,
                         String.valueOf (tokens.length),
                         "2",
                         "4"
                     });
            }

            Locale locale = null;

            if (tokens.length == 2)
                locale = Locale.getDefault();
            else
                locale = new Locale (tokens[2], tokens[3]);

            try
            {
                SimpleDateFormat fmt = new SimpleDateFormat (tokens[1],
                                                             locale);
                value = fmt.format (now);
            }

            catch (IllegalArgumentException ex)
            {
                throw new ConfigurationException (ex.toString());
            }
        }

        return value;
    }
}
