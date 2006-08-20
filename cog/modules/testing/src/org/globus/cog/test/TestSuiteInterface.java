
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;


public interface TestSuiteInterface {
    public void tests();

    public void setName(String name);

    public String getName();
    
    public String getServiceName();
}
