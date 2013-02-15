
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;


public interface TestInterface {
    public void test(String machine);

    public String getServiceName();

    public String getColumnName();

    void setOutputWriter(OutputWriter output);

    void setMachines(MachineListParser machines);
}
