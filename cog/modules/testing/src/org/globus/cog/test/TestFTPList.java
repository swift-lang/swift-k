
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import org.globus.ftp.GridFTPClient;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.Session;
import org.globus.ftp.FileInfo;

import java.util.Vector;

public class TestFTPList extends AbstractFTPTest {

	public void test(String machine) {
		int port = MachineListParser.getPortAsInt(machine);
		String host = MachineListParser.getHost(machine);

		System.out.println("\nTesting list GridFTP on " + machine + " at port no  "
			+ port);

		GridFTPClient client = null;

		try {
			client = new GridFTPClient(host, port);
			client.authenticate(null);
			client.setDataChannelAuthentication(DataChannelAuthentication.NONE);
			String root = client.getCurrentDir();
			System.out.println("the root dir =" + root);
			client.setType(Session.TYPE_ASCII);
			client.changeDir(root);
			Vector v = client.list();
			String s = new String("");
			System.out.println("list received");
			while (!v.isEmpty()) {
				FileInfo f = (FileInfo) v.remove(0);
				s += f.toString() + "\n";
			}

			client.close();
			System.out.println("Test passed .");
			output.printResult("List", machine,
				"<h3>User's Home Directory Files:\n<br></h3>" + s, true);
		}
		catch (Exception se) {
			output.printResult("List", machine, Util.getStackTrace(se), false);
		}
	}

	public String getColumnName() {
		return "List";
	}

}
