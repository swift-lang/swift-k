
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

public class TestGRAMVersion extends AbstractGRAMTest {

    public void test(String machine) {
        String msg = globusRunJob(machine, "&(executable=$(GLOBUS_LOCATION)/bin/globusrun)" + "(arguments=-versions)" + "(environment=(LD_LIBRARY_PATH $(GLOBUS_LOCATION)/lib))");
        System.out.println("\n   msg=\n" + msg);
        if ((msg.indexOf("ERROR MESSAGE") > 0)) {
            output.printResult("GRAM Version", machine, msg, false);
        }
        else {
            /*This is where version info is retrieved.*/
            int index = msg.indexOf("globusrun:");
            //The length of globusrun: is 10 +1 for space .
            String s = msg.substring(index + 11, msg.indexOf("(", index + 11));
            // ?!?
            output.printResult("GRAM Version", machine, msg, s);
        }
    }

    public String getColumnName() {
        return "Detected Version";
    }

}
