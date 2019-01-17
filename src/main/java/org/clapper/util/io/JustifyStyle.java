package org.clapper.util.io;

/**
 * <p>The <tt>JustifyStyle</tt> enumeration spells out the legal field
 * justification values for classes such as {@link JustifyTextWriter}. It
 * resides in a separate class for readability.</p>
 *
 * @see JustifyTextWriter
 * @see org.clapper.util.text.TextUtil#rightJustifyString(String,int)
 * @see org.clapper.util.text.TextUtil#leftJustifyString(String,int)
 * @see org.clapper.util.text.TextUtil#centerString(String,int)
 */
public enum JustifyStyle
{
    RIGHT_JUSTIFY, LEFT_JUSTIFY, CENTER
}
