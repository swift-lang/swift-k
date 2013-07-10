
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import org.globus.cog.beans.GridRun;


public abstract class AbstractGRAMTest extends AbstractTest{

    public String getServiceName() {
        return "gram";
    }

    public String globusRunJob(String machine, String executable) {
        GridRun gridRun = new GridRun();
        gridRun.setSystem(false);
        gridRun.setRemoteContact(machine);
        gridRun.setRsl(executable);
        gridRun.executeJob();
        String msg = gridRun.getMessage();
        return msg;
    }
}
