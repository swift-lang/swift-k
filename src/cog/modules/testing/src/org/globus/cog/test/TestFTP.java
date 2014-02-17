
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import org.globus.util.GlobusURL;
import org.globus.io.urlcopy.UrlCopy;

import java.io.File;

public class TestFTP extends AbstractFTPTest{

    public void test(String machine) {
        GlobusURL from = null;
        GlobusURL to = null;

        try {
            File localFile = new File(System.getProperty("user.home")
                    + System.getProperty("file.separator")
                    + "testlocal.txt");
            if (!localFile.exists()) {
                //try to create this file now.
                try {
                    localFile.createNewFile();
                }
                        //if you couldnt write to it
                catch (Exception err) {
                    System.out.println("Please create a dummy text file" +
                            " named testlocal.txt in your home "
                            + "directory for testing file transfers");
                    System.exit(-1);
                }
            }

            from = new GlobusURL("file:///" + localFile.getAbsolutePath());
            to = new GlobusURL("gsiftp://" + machine + "/testftp.txt");

            UrlCopy uc = new UrlCopy();
            uc.setSourceUrl(from);
            uc.setDestinationUrl(to);
            uc.setUseThirdPartyCopy(false);
            uc.setAppendMode(true);

            uc.copy();
            output.printResult("FTP", machine, "<h3>From Local Client:\n<br></h3>"
                    + from +
                    "<br><h3>To RemoteServer:\n<br>and vice versa</h3>"
                    + to, true);
        }
        catch (Exception e) {
            output.printResult("FTP", machine, Util.getStackTrace(e), false);
        }
    }

    public String getColumnName() {
        return "FTP";
    }

}
