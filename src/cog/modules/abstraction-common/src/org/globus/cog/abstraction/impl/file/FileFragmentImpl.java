//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 9, 2011
 */
package org.globus.cog.abstraction.impl.file;

import org.globus.cog.abstraction.interfaces.FileFragment;

public class FileFragmentImpl implements FileFragment {
    private final String file;
    private final long offset, length;
    
    public FileFragmentImpl(String file, long offset, long length) {
        this.file = file;
        this.offset = checkPositive(offset, "offset");
        this.length = checkPositive(length, "length");
    }
    
    private long checkPositive(long n, String param) {
        if (n < 0) {
            throw new IllegalArgumentException(param + " < 0 (" + n + ")");
        }
        return n;
    }

    /**
     * Creates a new FileFragment object for the entire specified file
     */
    public FileFragmentImpl(String file) {
        this(file, FILE_START, MAX_LENGTH);
    }

    public String getFile() {
        return file;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }

    public boolean isFragment() {
        return (offset != FILE_START) || (length != MAX_LENGTH);
    }
}
