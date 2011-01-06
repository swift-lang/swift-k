
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

public class Launcher {
    public static void main(String args[]) {
        String dir = args[2] + System.getProperty("file.separator");
        String prefix = args[3];
        String machinelist = args[1];
        int timeout;
        try {
            timeout = Integer.parseInt(args[4]);
        }
        catch (Exception e){
            timeout = 120;
        }
        TestSuiteInterface t = null;

        if (args[0].equals("general")){
            t = new GridTestSuite(dir, prefix, machinelist, timeout);
        }
        else if (args[0].equals("gram")){
            t = new GRAMTestSuite(dir, prefix, machinelist, timeout);
        }
        else if (args[0].equals("gridftp")){
            t = new GridFTPTestSuite(dir, prefix, machinelist, timeout);
        }
        else{
            System.out.println("Unrecognized test: "+args[0]+". Available tests are: general, gram, gridftp");
            System.exit(101);
        }

        t.tests();
	//force exit. there might be hanging threads
	System.exit(0);
    }
}
