package org.clapper.util.text;

/**
 * Symbolic names for certain Unicode constants. The constant
 * names are adapted from the Unicode standard. See
 * <a href="http://charts.unicode.org/"><i>charts.unicode.org</i></a>.
 */
public final class Unicode
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /**
     * Copyright symbol.
     */
    public static final char COPYRIGHT_SIGN = '\u00a9';

    /**
     * The "registered" symbol ("R" in a circle).
     */
    public static final char REGISTERED_SIGN = '\u00ae';

    /**
     * "One-quarter" (1/4) symbol.
     */
    public static final char ONE_QUARTER_FRACTION = '\u00bc';

    /**
     * "One-half" (1/2) symbol.
     */
    public static final char ONE_HALF_FRACTION = '\u00bd';

    /**
     * "Three-quarters" (3/4) symbol.
     */
    public static final char THREE_QUARTERS_FRACTION = '\u00be';

    /**
     * Classic mathematical multiplication ("times") symbol.
     */
    public static final char MULTIPLICATION_SIGN = '\u00d7';

    /**
     * Classic mathematical division symbol.
     */
    public static final char DIVISION_SIGN = '\u00f7';

    /**
     * Symbol for degrees
     */
    public static final char DEGREE_SIGN = '\u00b0';

    /**
     * Non-breaking space.
     */
    public static final char NBSP = '\u00a0';

    /**
     * En space
     */
    public static final char EN_SPACE = '\u2002';

    /**
     * Em space
     */
    public static final char EM_SPACE = '\u2003';

    /**
     * Thin space (defined as a fifth of an em)
     */
    public static final char THIN_SPACE = '\u2009';

    /**
     * Hair space. Thinner than a <tt>THIN_SPACE</tt>, in traditional
     * typography a hair space is the thinnest space available.
     */
    public static final char HAIR_SPACE = '\u200a';

    /**
     * Zero-width non-joiner.
     */
    public static final char ZERO_WIDTH_NON_JOINER = '\u200c';

    /**
     * Synonym for {@link #ZERO_WIDTH_NON_JOINER}
     */
    public static final char ZWNJ = ZERO_WIDTH_NON_JOINER;

    /**
     * Zero-width joiner.
     */
    public static final char ZERO_WIDTH_JOINER = '\u200d';

    /**
     * Synonym for {@link #ZERO_WIDTH_JOINER}
     */
    public static final char ZWJ = ZERO_WIDTH_JOINER;

    /**
     * Hyphen (normal encoding, i.e., hyphen-minus)
     */
    public static final char HYPHEN = '\u002d';

    /**
     * Synonym for {@link #HYPHEN}
     */
    public static final char HYPHEN_MINUS = HYPHEN;

    /**
     * Non-breaking hyphen
     */
    public static final char NON_BREAKING_HYPHEN = '\u2011';

    /**
     * En dash
     */
    public static final char EN_DASH = '\u2013';

    /**
     * Em dash
     */
    public static final char EM_DASH = '\u2014';

    /**
     * Left single quotation mark
     */
    public static final char LEFT_SINGLE_QUOTE = '\u2018';

    /**
     * Right single quotation mark
     */
    public static final char RIGHT_SINGLE_QUOTE = '\u2019';

    /**
     * Single low-9 quotation mark, used as an opening single quote in
     * some languages.
     */
    public static final char SINGLE_LOW_9_QUOTE = '\u201a';

    /**
     * Left double quotation mark
     */
    public static final char LEFT_DOUBLE_QUOTE = '\u201c';

    /**
     * Right double quotation mark
     */
    public static final char RIGHT_DOUBLE_QUOTE = '\u201d';

    /**
     * Trademark symbol
     */
    public static final char TRADEMARK = '\u2122';

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private Unicode()
    {
        // Cannot be instantiated
    }
}
