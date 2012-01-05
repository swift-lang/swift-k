
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

public class GridFTPTestSuite extends AbstractTestSuite implements TestSuiteInterface {

    public GridFTPTestSuite(String dir, String prefix, String machinelist, int timeout) {
        super(dir, prefix, machinelist, timeout);
        setName("ftp");
	setServiceName("gsiftp");
        addTest(new DisplayTime());
        addTest(new DisplayHost());
        addTest(new DisplayOS());
        addTest(new DisplayFTPVersion());
        addTest(new TestFTPPing());
	addTest(new TestFTPList());
	addTest(new TestFTPTransfer());
	addTest(new TestFTP3Party());
    }
}
