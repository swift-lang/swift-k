
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import javax.naming.directory.SearchControls;

import org.globus.cog.beans.GridSearch;

public class TestGRIS extends AbstractTest {

    public void test(String machine) {
        String msg = runGRIS(machine, null, "(objectclass=*)");
        if ((msg.indexOf("ERROR MESSAGE") > 0)) {
            output.printResult("GRIS ", machine, msg, false);
        }
        else {
            output.printResult("GRIS", machine, "\nDefault Output :" + msg, true);
        }
    }

    public String getServiceName() {
        return "mds";
    }

    public String getColumnName() {
        return "GRIS";
    }

    private String runGRIS(String machine, String attrib, String filter) {
        int port = MachineListParser.getPortAsInt(machine);
        String host = MachineListParser.getHost(machine);
        String msg = "";
        GridSearch gridInfoSearch = new GridSearch();
        gridInfoSearch.setSaslMech(null);
        if (attrib != null) {
            gridInfoSearch.setScope(SearchControls.ONELEVEL_SCOPE);
        }
        gridInfoSearch.setHostname(host);
        if (port != 0) {
    	    gridInfoSearch.setPort(port);
	}
        String[] attribs = null;
        if (filter != null) {
            attribs = new String[1];
            attribs[0] = attrib;
        }

        gridInfoSearch.search(filter, attribs);
        msg = gridInfoSearch.getMessage();
        return msg;
    }

}
