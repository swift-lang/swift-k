
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

public class GRAMTestSuite extends AbstractTestSuite implements TestSuiteInterface {
    public GRAMTestSuite(String dir, String prefix, String machinelist, int timeout) {
        super(dir, prefix, machinelist, timeout);
        setName("gram");
		setServiceName("gram");
        addTest(new DisplayTime());
        addTest(new DisplayHost());
        addTest(new DisplayOS());
        addTest(new DisplayGRAMVersion());
        addTest(new TestGRAMPing());
        addTest(new TestGRAMVersion());
        addTest(new TestGRAMExecution());
    }
}
