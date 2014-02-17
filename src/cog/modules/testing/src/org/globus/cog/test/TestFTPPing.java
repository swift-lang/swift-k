
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import org.globus.ftp.GridFTPClient;

public class TestFTPPing extends AbstractFTPTest {

	public void test(String machine) {
		String host = MachineListParser.getHost(machine);
		int port = MachineListParser.getPortAsInt(machine);
		GridFTPClient client = null;

		try {
			client = new GridFTPClient(host, port);
			client.authenticate(null);//previously proxy
			client.close();
			System.out.println("Test passed .");
			output.printResult("Ping", machine,
				"Successfully connected to GridFTP server at port 2811", true);
		}
		catch (Exception se) {
			output.printResult("Ping", machine, Util.getStackTrace(se), false);
		}
	}

	public String getColumnName() {
		return "Ping";
	}

}
