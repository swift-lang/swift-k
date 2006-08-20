
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;


public class TestGRAM extends AbstractGRAMTest{
    public void test(String machine) {
        String msg = globusRunJob(machine, "&(executable=/bin/date)");
        if ((msg.indexOf("ERROR MESSAGE") > 0)) {
            output.printResult("GRAM", machine, msg, false);
        }
        else {
            output.printResult("GRAM", machine, msg, true);
        }
    }

    public String getColumnName() {
        return "GRAM";
    }
}
