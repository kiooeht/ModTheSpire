package org.clapper.util.config;

import org.clapper.util.text.XStringBuilder;

/**
 * A variable value segment. This is just a substring, with a flag that
 * indicates whether the segment is literal, white space-escaped or not.
 */
class ValueSegment
{
    XStringBuilder segmentBuf          = new XStringBuilder();
    boolean        isLiteral           = false;
    boolean        isWhiteSpaceEscaped = false;

    ValueSegment()
    {
        // Nothing to do
    }

    void append (char ch)
    {
        segmentBuf.append (ch);
    }

    int length()
    {
        return segmentBuf.length();
    }

    public String toString()
    {
        return segmentBuf.toString();
    }

    public ValueSegment makeCopy()
    {
        ValueSegment copy        = new ValueSegment();
        copy.segmentBuf          = new XStringBuilder(this.segmentBuf.toString());
        copy.isLiteral           = this.isLiteral;
        copy.isWhiteSpaceEscaped = this.isWhiteSpaceEscaped;
        return copy;
    }
}
