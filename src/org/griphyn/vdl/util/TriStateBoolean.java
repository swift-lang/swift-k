/*
 * Created on Feb 7, 2007
 */
package org.griphyn.vdl.util;

public final class TriStateBoolean {
    public static final TriStateBoolean FALSE = new TriStateBoolean(0);
    public static final TriStateBoolean TRUE = new TriStateBoolean(1);
    public static final TriStateBoolean MAYBE = new TriStateBoolean(2);
    
    private int value;
    
    private TriStateBoolean(int value) {
        this.value = value;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TriStateBoolean) {
            return value == ((TriStateBoolean) obj).value;
        }
        else {
            return false;   
        }
    }

    public int hashCode() {
        return value;
    }

    public String toString() {
        switch(value) {
            case 0: return "false";
            case 1: return "true";
            default: return "maybe";
        }
    }
    
    public static TriStateBoolean valueOf(String value) {
        if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
            return FALSE;
        }
        else if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
            return TRUE;
        }
        else {
            return MAYBE;
        }
    }
}
