
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

public class TestGRAMExecution extends AbstractGRAMTest{

    public void test(String machine) {
        String msg = globusRunJob(machine, "&(executable=/bin/date)");
        System.out.println("Msg from server in execution=" + msg);
        if ((msg.indexOf("ERROR MESSAGE") > 0)) {
            output.printResult("Execution ", machine, msg, false);
        }
        else {
            output.printResult("Execution", machine, "\nThe OUTPUT for /bin/date :" + msg, true);
        }
    }

    public String getColumnName() {
        return "Execute";
    }

}
