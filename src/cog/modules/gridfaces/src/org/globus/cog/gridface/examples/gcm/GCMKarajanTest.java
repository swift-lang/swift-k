
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.examples.gcm;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;

/**
 * this is a client application for the GridCommandManager. 
 * Tests workflow submission tasks. 
 */
public class GCMKarajanTest implements StatusListener {

    private GridCommandManager gcm;
    static Logger logger = Logger.getLogger(GCMKarajanTest.class.getName());

    public GCMKarajanTest() throws Exception{
        gcm = new GridCommandManagerImpl();
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
    	logger.debug("This application can be used to submit an XML workflow file to Karajan using GCM");
    	logger.debug("  Sorry, this test has not been implemented yet. ");
   }
}