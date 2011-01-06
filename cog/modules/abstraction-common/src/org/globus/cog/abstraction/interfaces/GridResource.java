// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

public interface GridResource {
    public static final int FILE = 1;
    public static final int EXECUTION = 2;
    public static final int INFORMATION = 3;

    public void setName(String name);
    public String getName();

    public void setIdentity(Identity id);
    public Identity getIdentity();

    public int getType();
}
