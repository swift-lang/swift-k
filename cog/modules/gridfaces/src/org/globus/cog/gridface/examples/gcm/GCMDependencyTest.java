
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.examples.gcm;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.EXECCommandImpl;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;

/**
 * this is a client application for the GridCommandManager. 
 * Tests dependency maintenance between commands 
 */
public class GCMDependencyTest implements StatusListener {

    private GridCommandManager gcm;
    static Logger logger = Logger.getLogger(GCMDependencyTest.class.getName());
    private String id = null;
    public GCMDependencyTest() throws Exception {
        gcm = new GridCommandManagerImpl();
    }
    
    public Identity executeTask(Enumeration identities) throws Exception{
		GridCommand command = new EXECCommandImpl();
		ServiceContact serviceContact =
			new ServiceContactImpl("arbat.mcs.anl.gov");
		command.setAttribute("servicecontact", serviceContact);
		command.setAttribute("provider", "GT2");
		command.setAttribute("executable", "/bin/date");
		command.setAttribute("redirected", "true");
		command.addStatusListener(this);
		try {
			if (identities != null){
				return gcm.execute(command, true, identities);
			} else{
				return gcm.execute(command, true);
			}
		} catch (Exception e) {
			logger.debug(">>>Exception in gcmExecutionTest Exec<<<");
			e.printStackTrace();
		}
		return null;
    }
    
    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
        logger.debug("Command Status Changed to " + status.getStatusCode());
        GridCommand command = (GridCommand) event.getSource();
        if ((status.getStatusCode() == Status.COMPLETED)) {
            logger.debug(command.getIdentity() + "  "+ command.getOutput().toString());
        }
    }

    /*
     * Main method
     */
    public static void main(String[] args) {
    	Vector identities = new Vector();
    	Identity prevId = null;
    	Identity currId = null;
    	Enumeration idList = null;
        try{
			GCMDependencyTest gcmTest = new GCMDependencyTest();
			currId = gcmTest.executeTask(idList);
			for (int i = 0; i< 3; i++){
				prevId = currId;
				identities.add(0,prevId);
				currId = gcmTest.executeTask(identities.elements());
			}

        }catch(Exception exception){
        	logger.error("Error in GCMDependencyTest",exception);
        }
    }
}