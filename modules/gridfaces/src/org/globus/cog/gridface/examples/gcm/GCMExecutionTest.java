
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.examples.gcm;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.EXECCommandImpl;
import org.globus.cog.gridface.impl.commands.URLCOPYCommandImpl;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;

/**
 * this is a client application for the GridCommandManager. 
 * Tests job submission, file transfer tasks. 
 */
public class GCMExecutionTest implements StatusListener {

    private GridCommandManager gcm;
    private String provider = null;
    private String executable = null;
    private String serviceContactName = null;
    private String sourceURL = null;
    private String destinationURL = null;
    private boolean background = true;
    static Logger logger = Logger.getLogger(GCMExecutionTest.class.getName());

    public GCMExecutionTest(
        int option,
        String provider,
        boolean background,
        String input1,
        String input2)
        throws Exception {
        gcm = new GridCommandManagerImpl();
        this.provider = provider;
        this.background = background;
        if (option == 1) {
            this.executable = input1;
            this.serviceContactName = input2;
        } else {
            this.sourceURL = input1;
            this.destinationURL = input2;
        }
    }

    /*
     * Exec command. Runs an executable
     */
    public void executeExec() throws Exception {
        logger.debug(">>>EXEC<<<");
        GridCommand command = new EXECCommandImpl();

        ServiceContact serviceContact =
            new ServiceContactImpl(serviceContactName);
        command.setAttribute("servicecontact", serviceContact);

        command.setAttribute("provider", provider);
        command.setAttribute("executable", executable);
        command.setAttribute("redirected", "true");
        command.addStatusListener(this);

        try {
           gcm.execute(command, background);
        } catch (Exception e) {
            logger.debug(">>>Exception in gcmExecutionTest Exec<<<");
            e.printStackTrace();
        }
        logger.debug(">>>Exec Successful<<<");
    }

    /*
     * File Transfer URL copy task
     */
    public void executeURLCOPY() {
        logger.debug(">>>FILE TRANSFER<<<");
        GridCommand command = new URLCOPYCommandImpl();
        command.setAttribute("provider", provider);
        command.setAttribute("source", sourceURL);
        command.setAttribute("destination", destinationURL);
        command.addStatusListener(this);

        try {
            gcm.execute(command, background);
        } catch (Exception e) {
            logger.debug(">>>Exception in gcmExecutionTest File Transfer<<<");
            e.printStackTrace();
        }
        logger.debug(">>>File Transfer Successful<<<");

    }

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
        logger.debug("Command Status Changed to " + status.getStatusCode());
        GridCommand command = (GridCommand) event.getSource();
        if (status.getStatusCode() == Status.COMPLETED) {
            logger.debug(command.getOutput().toString());
			System.exit(0);
        } else if (status.getStatusCode() == Status.FAILED){
			logger.debug(command.getError().toString());
			System.exit(0);
        }
    }

    /*
     * Main method
     */
    public static void main(String[] args) {
        int option = 0;
        String response = null;
        String executable = "/bin/date";
        String provider = "GT2";
        String serviceContact = "arbat.mcs.anl.gov";
        String sourceURL = null;
        String destinationURL = null;
        boolean background = false;

        try {
            BufferedReader stdin =
                new BufferedReader(new InputStreamReader(System.in));
            logger.debug(
                "Which functionality would you like to test? \n" +                "[1. Job Submission 2. File Tranfer ] ");
            option = Integer.parseInt(stdin.readLine());

            if (option == 1) {
                logger.debug("Enter provider: [default: GT2] ");
                response = stdin.readLine();
                if (response != null && !response.equals("")) {
                    provider = response;
                }
				logger.debug("Enter executable: [default: /bin/date ] ");
				response = stdin.readLine();
				if (response != null && !response.equals("")) {
					executable = response;
				}
                logger.debug(
                    "Enter service contact: [default: arbat.mcs.anl.gov] ");
                response = stdin.readLine();
                if ((response != null) && (!response.equals(""))) {
                    serviceContact = response;
                }
                logger.debug(" Run in background? [default: false]");
                response = stdin.readLine();
                if ((response != null) && (!response.equals(""))) {
                    background = Boolean.valueOf(response).booleanValue();
                }

            } else {
                logger.debug("Enter provider: [default: GT2] ");
                response = stdin.readLine();
                if (response != null && !response.equals("")) {
                    provider = response;
                }
                logger.debug("Enter source URL: ");
                response = stdin.readLine();
                if (response != null && !response.equals("")) {
                    sourceURL = response;
                }
                logger.debug("Enter destination URL: ");
                response = stdin.readLine();
                if (response != null && !response.equals("")) {
                    destinationURL = response;
                }
                logger.debug(" Run in background? [default: false]");
                response = stdin.readLine();
                if (response != null && !response.equals("")) {
                    background = Boolean.valueOf(response).booleanValue();
                }
            }
        } catch (Exception exception) {
            logger.fatal("Error in reading inputs. Please try again.",exception);
            System.exit(1);
        }

        try {
            if (option == 1) {
                GCMExecutionTest gcmExecutionTest =
                    new GCMExecutionTest(
                        option,
                        provider,
                        background,
                        executable,
                        serviceContact);
              gcmExecutionTest.executeExec();
            } else {
                GCMExecutionTest gcmExecutionTest =
                    new GCMExecutionTest(
                        option,
                        provider,
                        background,
                        sourceURL,
                        destinationURL);
                gcmExecutionTest.executeURLCOPY();
            }
        } catch (Exception exception) {
            logger.fatal("Exception in executing tasks",exception);            
            System.exit(1);
        }
    }
}