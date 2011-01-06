	
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.examples.gcm;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.EXECCommandImpl;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;

/**
 * this is a client application for the GridCommandManager. 
 * Tests cancelation of a submitted task 
 */
public class GCMCancelTest implements StatusListener {

    private GridCommandManager gcm;
    static Logger logger = Logger.getLogger(GCMCancelTest.class.getName());
    private String id = null;
    public GCMCancelTest() throws Exception {
        gcm = new GridCommandManagerImpl();
    }
    
    public void executeTask() throws Exception{
		GridCommand command = new EXECCommandImpl();
		ServiceContact serviceContact =
			new ServiceContactImpl("arbat.mcs.anl.gov");
		command.setAttribute("servicecontact", serviceContact);
		command.setAttribute("provider", "GT2");
		command.setAttribute("executable", "/bin/date");
		command.setAttribute("taskArguments","-i 5 -s 3");
		command.setAttribute("redirected", "true");
		command.addStatusListener(this);
		try {
			id = gcm.execute(command, true).toString();
		   logger.debug(id);
		} catch (Exception e) {
			logger.debug(">>>Exception in gcmExecutionTest Exec<<<");
			e.printStackTrace();
		}
    }
    
    public void cancel() throws Exception{

    	// Job has to be put into the task graph
    	while(gcm.getStatus(id) == null){
    		Thread.sleep(1000);
    	}
    	//Status should be atleast submitted
    	while (gcm.getStatus(id).getStatusCode() < 2){
    		Thread.sleep(1000);
    	}
    	logger.debug("Cancel now");
    	boolean canceled = gcm.cancel(id.toString());
    	logger.debug("Cancel "+ canceled);
    }

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
        logger.debug("Command Status Changed to " + status.getStatusCode());
        GridCommand command = (GridCommand) event.getSource();
        if ((status.getStatusCode() == Status.COMPLETED)) {
            logger.debug(command.getOutput().toString());
        }
    }

    /*
     * Main method
     */
    public static void main(String[] args) {
        try{
			GCMCancelTest gcmTest = new GCMCancelTest();
			gcmTest.executeTask();
			gcmTest.cancel();
        }catch(Exception exception){
        	logger.error("Error in GCMCancelTest",exception);
        }
    }
}