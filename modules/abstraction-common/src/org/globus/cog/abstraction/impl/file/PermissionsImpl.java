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

    private final boolean readable;
    private final boolean writable;
    private final boolean executable;

    public PermissionsImpl() {
        this(0);
    }

    public PermissionsImpl(int digit) {
        readable = (digit & 4) != 0;
        writable = (digit & 2) != 0;
        executable = (digit & 1) != 0;
    }
    
    public PermissionsImpl(boolean read, boolean write, boolean execute) {
        this.readable = read;
        this.writable = write;
        this.executable = execute;
    }

    /** return true if readable */
    public boolean getRead() {
        return this.readable;
    }

    /** return true if writable */
    public boolean getWrite() {
        return this.writable;
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
    
    public static final Permissions NONE = new PermissionsImpl(false, false, false);
    public static final Permissions R = new PermissionsImpl(true, false, false);
    public static final Permissions W = new PermissionsImpl(false, true, false);
    public static final Permissions X = new PermissionsImpl(false, false, true);
    public static final Permissions RW = new PermissionsImpl(true, true, false);
    public static final Permissions RX = new PermissionsImpl(true, false, true);
    public static final Permissions WX = new PermissionsImpl(false, true, true);
    public static final Permissions RWX = new PermissionsImpl(true, true, true);
    
    public static Permissions instance(boolean r, boolean w, boolean x) {
        if (r) {
            if (w) {
                if (x) {
                    return RWX;
                }
                else {
                    return RW;
                }
            }
            else {
                if (x) {
                    return RX;
                }
                else {
                    return R;
                }
            }
        }
        else {
            if (w) {
                if (x) {
                    return WX;
                }
                else {
                    return W;
                }
            }
            else {
                if (x) {
                    return X;
                }
                else {
                    return NONE;
                }
            }
        }
    }
    
    public static Permissions instance(int c) {
        switch (c) {
            case 0:
                return NONE;
            case 1:
                return X;
            case 2:
                return W;
            case 3:
                return WX;
            case 4:
                return R;
            case 5:
                return RX;
            case 6:
                return RW;
            case 7:
                return RWX;
            default:
                throw new IllegalArgumentException();
        }
    }
}
