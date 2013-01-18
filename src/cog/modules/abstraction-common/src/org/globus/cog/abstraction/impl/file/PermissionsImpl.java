// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file;

import org.globus.cog.abstraction.interfaces.Permissions;

/**
 * Class to set and get Permissions for Grid File objects
 */
public class PermissionsImpl implements Permissions {

    private boolean readable = false;
    private boolean writable = false;
    private boolean executable = false;

    public PermissionsImpl() {
    }

    public PermissionsImpl(int digit) {
        readable = (digit & 4) != 0;
        writable = (digit & 2) != 0;
        executable = (digit & 1) != 0;
    }

    /** set/unset readable */
    public void setRead(boolean canRead) {
        this.readable = canRead;
    }

    /** return true if readable */
    public boolean getRead() {
        return this.readable;
    }

    /** set/unset writable */
    public void setWrite(boolean canWrite) {
        this.writable = canWrite;
    }

    /** return true if writable */
    public boolean getWrite() {
        return this.writable;
    }

    /** set/unset executable */
    public void setExecute(boolean canExecute) {
        this.executable = canExecute;
    }

    /** return true if executable */
    public boolean getExecute() {
        return this.executable;
    }

    /**
     * Returns a string representing the octal digit of this permission
     */
    public String toString() {
        return String.valueOf(toDigit());
    }

    public int toDigit() {
        return (readable ? 4 : 0) + (writable ? 2 : 0) + (executable ? 1 : 0);
    }
}
