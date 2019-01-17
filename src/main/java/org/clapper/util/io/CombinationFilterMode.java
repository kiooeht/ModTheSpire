package org.clapper.util.io;

/**
 * <p>Used solely to define type-safe mode values for
 * {@link CombinationFilenameFilter} and {@link CombinationFileFilter}.
 *
 * @see CombinationFileFilter
 * @see CombinationFilenameFilter
 */
public enum CombinationFilterMode
{
    /**
     * Mode setting that instructs the filter to <tt>AND</tt> all the
     * contained filters.
     */
    AND_FILTERS,

    /**
     * Mode setting that instructs the filter to <tt>OR</tt> all the
     * contained filters.
     */
    OR_FILTERS
};
