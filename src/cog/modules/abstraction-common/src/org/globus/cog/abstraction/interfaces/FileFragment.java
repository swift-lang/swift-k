//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 9, 2011
 */
package org.globus.cog.abstraction.interfaces;

/**
 * 
 * Represents a fragment of a file (or an entire file).
 */
public interface FileFragment {
    public static final long MAX_LENGTH = Long.MAX_VALUE;
    public static final long FILE_START = 0;

    /**
     * Returns the full file name (including path). The path
     * can be relative or absolute and uses the UNIX convention
     * (i.e. "/" is the path separator).
     */
    String getFile();

    /**
     * Returns the offset in the file of the fragment.
     */
    long getOffset();
    
    /**
     * Returns the length of the fragment or {@link #MAX_LENGTH} 
     * for up to the end of the file.
     */
    long getLength();
    
    /**
     * Returns <tt>true</tt> if this fragment does not represent an
     * entire file
     */
    boolean isFragment();
}
