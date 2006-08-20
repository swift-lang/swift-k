
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import org.globus.util.GlobusURL;
import org.globus.io.urlcopy.UrlCopy;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;

public class TestFTPTransfer extends AbstractFTPTest {

    public void test(String machine) {
        System.out.println("\n\n\nTesting second party =\n\n\n" + machine);
        GlobusURL from = null,from1 = null;
        GlobusURL to = null,to1 = null;
        String basedir = System.getProperty("user.home") + System.getProperty("file.separator");

        try {
            File localFile = new File(basedir + "testlocal.txt");
            if (!localFile.exists()) {

                //try to create this file now.
                try {
                    localFile.createNewFile();
                    PrintWriter fout = new PrintWriter(new FileWriter(localFile));
                    fout.println("This is a test program which test.\n" + "The gridftp java client against the" + " running gridftp servers" + "First it tranfers this file to remote " + "place and then gets it back.");
                    fout.println("------------------------");
                    fout.close();

                }
                        //if you couldnt write to it
                catch (Exception err) {
                    System.out.println("Please create the dummy text" + " file named testlocal.txt in your" + " home directory for testing file " + "transfers");
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
            from1 = new GlobusURL("gsiftp://" + machine + "/testftp.txt");
            to1 = new GlobusURL("file:///" + basedir + "testToLocal.txt");
            uc.setSourceUrl(from1);
            uc.setDestinationUrl(to1);
            uc.setUseThirdPartyCopy(false);
            uc.setAppendMode(true);

            uc.copy();
            output.printResult("2Party", machine, "<h3>From Local Client : \n<br></h3>" + from + "<br><h3>To Remote Server: \n<br></h3>" + to + "<br><h3>From Remote Server : \n<br></h3>" + from1 + "<br><h3>To Local Client: \n<br></h3>" + to1, true);
        }
        catch (Exception e) {
            output.printResult("2Party", machine, Util.getStackTrace(e), false);
        }
    }

    public String getColumnName() {
        return "Transfer";
    }

}
