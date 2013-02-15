
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import org.globus.gram.Gram;

public class TestGRAMPing extends AbstractGRAMTest{

    public void test(String machine) {
        try {
            Gram.ping(machine);
            output.printResult("Ping", machine, "Test sucessful: Can submit to host.", true);
        }
        catch (Exception se) {
            output.printResult("Ping", machine, Util.getStackTrace(se), false);
        }
    }

    public String getColumnName() {
        return "Ping";
    }

}
