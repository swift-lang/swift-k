
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import org.globus.util.GlobusURL;
import org.globus.io.urlcopy.UrlCopy;

public class TestFTP3Party extends AbstractFTPTest{

    public void test(String machine) {
        System.out.println("\n\n\nTesting third party =\n\n\n" + machine);
        GlobusURL from = null;
        GlobusURL to = null;

        try {
            from = new GlobusURL("gsiftp://" + machine + "/testftp.txt");
            to = new GlobusURL("gsiftp://" + machine + "/test3Party.txt");

            UrlCopy uc = new UrlCopy();
            uc.setSourceUrl(from);
            uc.setDestinationUrl(to);
            uc.setUseThirdPartyCopy(false);
            uc.setAppendMode(true);
            uc.copy();
            System.out.println("\n\n\nTesting third party =\n\n\n" + machine);
            output.printResult("3Party", machine, "<h3>From : \n<br></h3>" + from +
                    "<br><h3>To : \n<br></h3>" + to, true);

        }
        catch (Exception e) {
            output.printResult("3Party", machine, Util.getStackTrace(e), false);
        }
    }

    public String getColumnName() {
        return "3rd Party";
    }

}
