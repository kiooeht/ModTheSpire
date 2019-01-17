package org.clapper.util.config;

import org.clapper.util.text.XStringBufBase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Contents of a variable. Mostly exists to make replacing a variable
 * value easier while looping over a section.
 */
class Variable
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    private static final char LITERAL_QUOTE = '\'';
    private static final char SUBST_QUOTE   = '"';

    /*----------------------------------------------------------------------*\
                               Instance Data
    \*----------------------------------------------------------------------*/

    private String         name;
    private String         cookedValue;
    private String[]       cookedTokens = null;
    private String         rawValue;
    private int            lineWhereDefined = 0; // 0 means unknown
    private ValueSegment[] rawSegments    = null;
    private ValueSegment[] cookedSegments = null;
    private Section        parentSection;

    /*----------------------------------------------------------------------*\
                               Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>Variable</tt> object.
     *
     * @param name             the variable name
     * @param value            the raw value
     * @param parentSection    the containing <tt>Section</tt> object
     * @param lineWhereDefined the line number where defined, or 0 for unknown
     */
    Variable (String  name,
              String  value,
              Section parentSection,
              int     lineWhereDefined)
    {
        this.name = name;
        this.parentSection = parentSection;
        this.lineWhereDefined = lineWhereDefined;
        setValue (value);
    }

    /**
     * Allocate a new <tt>Variable</tt> object.
     *
     * @param name             the variable name
     * @param value            the raw value
     * @param parentSection    the containing <tt>Section</tt> object
     */
    Variable (String name, String value, Section parentSection)
    {
        this (name, value, parentSection, 0);
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the variable name
     *
     * @return the name
     */
    String getName()
    {
        return name;
    }

    /**
     * Get the parent section that contains this variable.
     *
     * @return the variable's section
     */
    Section getSection()
    {
        return parentSection;
    }

    /**
     * Get the raw value for the variable. The raw value is the value
     * before any metacharacter expansion or variable substitution.
     *
     * @return the raw value
     *
     * @see #getCookedValue
     * @see #setCookedValue
     * @see #setValue
     */
    String getRawValue()
    {
        return rawValue;
    }

    /**
     * Get the cooked value for the variable. The cooked value is the value
     * after any metacharacter expansion and variable substitution.
     *
     * @return the cooked value
     *
     * @see #getRawValue
     * @see #getCookedTokens
     * @see #setCookedValue
     * @see #setValue
     */
    String getCookedValue()
    {
        return cookedValue;
    }

    /**
     * Get the cooked tokens for the variable. The cooked tokens are the
     * individual tokens, assembled from <tt>ValueSegment</tt> objects,
     * that result from parsing and from honoring quotes, with
     * metacharacters and variables expanded. NOTE: Do NOT assume that
     * these are white space-delimited tokens! Tokens are broken up based
     * on quoted fields. A value without any quoted strings is treated as a
     * single token.
     *
     * @return the cooked tokens
     *
     * @see #getRawValue
     * @see #getCookedValue
     * @see #setCookedValue
     * @see #setValue
     */
    String[] getCookedTokens()
    {
        return cookedTokens;
    }

    /**
     * Set the cooked value to the specified string, leaving the raw value
     * unmodified.
     *
     * @param value  the value to set
     *
     * @see #getRawValue
     * @see #getCookedValue
     * @see #getCookedSegments
     * @see #setValue
     */
    void setCookedValue (String value)
    {
        this.cookedValue = value;
    }

    /**
     * Get the cooked value, broken into separate segments. The segments
     * are stored internally. The cooked value is not updated until a call
     * to {@link #reassembleCookedValueFromSegments}. Calling this method
     * multiple times will not hamper efficiency. If the internal list of
     * segments is null (as it will be after construction or after a call
     * to <tt>reassembleCookedValueFromSegments()</tt>), then this method
     * creates the segments; otherwise, it just returns the existing ones.
     *
     * @return the segments
     *
     * @throws ConfigurationException on parsing error
     *
     * @see #segmentValue
     * @see #reassembleCookedValueFromSegments
     */
    ValueSegment[] getCookedSegments()
        throws ConfigurationException
    {
        segmentValue();
        return cookedSegments;
    }

    /**
     * Get the raw value, broken into separate segments. The segments
     * are stored internally. The cooked value is not updated until a call
     * to {@link #reassembleCookedValueFromSegments}. Calling this method
     * multiple times will not hamper efficiency. If the internal list of
     * segments is null (as it will be after construction or after a call
     * to <tt>reassembleCookedValueFromSegments()</tt>), then this method
     * creates the segments; otherwise, it just returns the existing ones.
     *
     * @return the segments
     *
     * @throws ConfigurationException on parsing error
     *
     * @see #segmentValue
     * @see #reassembleCookedValueFromSegments
     */
    ValueSegment[] getRawSegments()
        throws ConfigurationException
    {
        segmentValue();
        return rawSegments;
    }

    /**
     * Segment the raw value into literal and non-literal pieces, storing the
     * resulting <tt>ValueSegment</tt> objects internally.
     *
     * @throws ConfigurationException on parsing error
     *
     * @see #getCookedSegments
     * @see #reassembleCookedValueFromSegments
     */
    void segmentValue()
        throws ConfigurationException
    {
        if (rawSegments == null)
        {
            Collection<ValueSegment> segments = new ArrayList<ValueSegment>();
            char                     ch;
            char                     last;
            char[]                   chars;
            ValueSegment             currentSegment = new ValueSegment();
            int                      i;

            chars = rawValue.toCharArray();
            last  = '\0';
            currentSegment.isLiteral = false;
            currentSegment.isWhiteSpaceEscaped = false;

            for (i = 0; i < chars.length; i++)
            {
                ch = chars[i];
                switch (ch) // NOPMD
                {
                    case LITERAL_QUOTE:
                        if ((last == XStringBufBase.METACHAR_SEQUENCE_START) ||
                            (currentSegment.isWhiteSpaceEscaped))
                        {
                            // Escaped quote. Pass as literal.
                            currentSegment.append (ch);
                        }

                        else if (currentSegment.isLiteral)
                        {
                            // End of literal sequence. Thus, end of segment.

                            if (currentSegment.length() > 0)
                            {
                                segments.add (currentSegment);
                                currentSegment = new ValueSegment();
                            }

                            currentSegment.isLiteral = false;
                        }

                        else
                        {
                            // Start of literal sequence. Any previously
                            // buffered characters are part of the previous
                            // sequence and must be saved.

                            if (currentSegment.length() > 0)
                            {
                                segments.add (currentSegment);
                                currentSegment = new ValueSegment();
                            }

                            currentSegment.isLiteral = true;
                        }
                        break;

                    case SUBST_QUOTE:
                        if ((last == XStringBufBase.METACHAR_SEQUENCE_START) ||
                            (currentSegment.isLiteral))
                        {
                            // Escaped quote. Pass as literal.
                            currentSegment.append (ch);
                        }

                        else if (currentSegment.isWhiteSpaceEscaped)
                        {
                            // End of white-space escaped sequence. Thus,
                            // end of segment.

                            if (currentSegment.length() > 0)
                            {
                                segments.add (currentSegment);
                                currentSegment = new ValueSegment();
                            }

                            currentSegment.isWhiteSpaceEscaped = false;
                        }

                        else
                        {
                            // Start of literal sequence. Any previously
                            // buffered characters are part of the previous
                            // sequence and must be saved.

                            if (currentSegment.length() > 0)
                            {
                                segments.add (currentSegment);
                                currentSegment = new ValueSegment();
                            }

                            currentSegment.isWhiteSpaceEscaped = true;
                        }
                        break;

                    default:
                        currentSegment.append (ch);
                }
                last = ch;
            }

            if (currentSegment.isLiteral)
            {
                throw new ConfigurationException ("Unmatched " +
                                                  LITERAL_QUOTE +
                                                  " in variable \"" +
                                                  this.name +
                                                  "\"");
            }

            else if (currentSegment.isWhiteSpaceEscaped)
            {
                throw new ConfigurationException ("Unmatched " +
                                                  SUBST_QUOTE +
                                                  " in variable \"" +
                                                  this.name +
                                                  "\"");
            }

            if (currentSegment.length() > 0)
                segments.add (currentSegment);

            if (segments.size() > 0)
            {
                // Initially, the raw and cooked segments are identical.
                // The main parser will "cook" the cooked segments.

                rawSegments = new ValueSegment[segments.size()];
                cookedSegments = new ValueSegment[segments.size()];
                i = 0;
                for (ValueSegment vs : segments)
                {
                    rawSegments[i] = vs;
                    cookedSegments[i] = vs.makeCopy();
                    i++;
                }
            }
        }
    }

    /**
     * Reassemble the cooked value from the stored segments, zeroing out the
     * segments.
     *
     * @see #getCookedSegments
     * @see #getCookedValue
     * @see #setCookedValue
     */
    void reassembleCookedValueFromSegments()
    {
        if (cookedSegments != null)
        {
            StringBuilder buf = new StringBuilder();
            cookedTokens = new String[cookedSegments.length];

            int i = 0;
            for (ValueSegment segment : cookedSegments)
            {
                String s = segment.segmentBuf.toString();
                buf.append (s);
                cookedTokens[i++] = s;
//                valueSegments[i] = null;
            }

            cookedValue = buf.toString();
//            valueSegments = null;
        }
    }

    /**
     * Set both the raw and cooked values to the specified string.
     *
     * @param value  the value to set
     *
     * @see #getRawValue
     * @see #getCookedValue
     * @see #setCookedValue
     */
    final void setValue (String value)
    {
        this.rawValue = value;
        this.cookedValue = value;
    }

    /**
     * Retrieve the line number where the variable was defined.
     *
     * @return the line number, or 0 for unknown
     *
     * @see #setLineWhereDefined
     */
    int getLineWhereDefined()
    {
        return this.lineWhereDefined;
    }

    /**
     * Set the line number where the variable was defined.
     *
     * @param lineNumber the line number, or 0 for unknown
     *
     * @see #getLineWhereDefined
     */
    void setLineWhereDefined (int lineNumber)
    {
        this.lineWhereDefined = lineNumber;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/
}

