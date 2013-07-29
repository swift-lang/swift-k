// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.tools.transfer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferTask;
import org.globus.cog.abstraction.impl.common.task.GenericTaskHandler;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.RemoteFile;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

/*
 * This class serves as an example to transfer files between two Grid resources.
 * It also forms the basis of the cog-file-transfer command. 
 */
public class FileTransfer implements StatusListener {
    static Logger logger = Logger.getLogger(FileTransfer.class);

    private Task task = null;
    private RemoteFile sourceURI = null;
    private RemoteFile destinationURI = null;
    private boolean commandLine = false;
    private boolean thirdparty = false;

    public FileTransfer(String name, String sourceURI, String destinationURI)
            throws Exception {
        this.sourceURI = new RemoteFile(sourceURI);
        this.destinationURI = new RemoteFile(destinationURI);
        this.task = new FileTransferTask(name);
        logger.debug("Task Identity: " + this.task.getIdentity().toString());
    }

    public void setSourceURI(String sourceURI) throws Exception {
        this.sourceURI = new RemoteFile(sourceURI);
    }

    public void setDestinationURI(String destinationURI) throws Exception {
        this.destinationURI = new RemoteFile(destinationURI);
    }

    public String getSourceURI() {
        return this.sourceURI.toString();
    }

    public String getDestinationString() {
        return this.destinationURI.toString();
    }

    public void setCommandline(boolean bool) {
        this.commandLine = bool;
    }

    public boolean isCommandline() {
        return this.commandLine;
    }

    public void setThirdparty(boolean bool) {
        this.thirdparty = bool;
    }

    public boolean isThirdparty() {
        return this.thirdparty;
    }

    public void prepareTask() throws Exception {
        FileTransferSpecification spec = new FileTransferSpecificationImpl();
        spec.setSource(sourceURI.getPath());
        spec.setDestination(destinationURI.getPath());
        spec.setThirdParty(this.thirdparty);
        this.task.setSpecification(spec);

        // create te source service
        ServiceContact sourceServiceContact = new ServiceContactImpl();
        sourceServiceContact.setHost(sourceURI.getHost());
        sourceServiceContact.setPort(sourceURI.getPort());
        
        SecurityContext sourceSecurityContext = 
            AbstractionFactory.getSecurityContext(sourceURI.getProtocol(), sourceServiceContact);

        Service sourceService = new ServiceImpl(sourceURI.getProtocol(),
                Service.FILE_TRANSFER, sourceServiceContact,
                sourceSecurityContext);

        ServiceContact destinationServiceContact = new ServiceContactImpl();
        destinationServiceContact.setHost(destinationURI.getHost());
        destinationServiceContact.setPort(destinationURI.getPort());
        
        SecurityContext destinationSecurityContext = 
            AbstractionFactory.getSecurityContext(destinationURI.getProtocol(), destinationServiceContact);

        Service destinationService = new ServiceImpl(
                destinationURI.getProtocol(), Service.FILE_TRANSFER,
                destinationServiceContact, destinationSecurityContext);

        // add the source service at index 0
        this.task.setService(Service.FILE_TRANSFER_SOURCE_SERVICE,
                sourceService);

        // add the destination service at index 1
        this.task.setService(Service.FILE_TRANSFER_DESTINATION_SERVICE,
                destinationService);

        this.task.addStatusListener(this);
    }

    private void submitTask() throws Exception {
        logger.debug("Submit task");
        TaskHandler handler = new GenericTaskHandler();
        try {
            handler.submit(this.task);
        } catch (InvalidSecurityContextException ise) {
            logger.error("Security Exception");
            ise.printStackTrace();
            System.exit(1);
        } catch (TaskSubmissionException tse) {
            logger.error("TaskSubmission Exception");
            tse.printStackTrace();
            System.exit(1);
        } catch (IllegalSpecException ispe) {
            logger.error("Specification Exception");
            ispe.printStackTrace();
            System.exit(1);
        } catch (InvalidServiceContactException isce) {
            logger.error("Service Contact Exception");
            isce.printStackTrace();
            System.exit(1);
        }
    }

    public Task getFileTransferTask() {
        return this.task;
    }

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
        logger.debug("Status changed to " + status.getStatusString());
        if (status.getStatusCode() == Status.FAILED) {
            logger.error("File transfer failed",status.getException());
            if (this.commandLine) {
                System.exit(1);
            }
        }
        if (status.getStatusCode() == Status.COMPLETED) {
            logger.debug("File transfer completed");
            if (this.commandLine) {
                System.exit(0);
            }
        }

        if (status.getStatusCode() == Status.COMPLETED
                || status.getStatusCode() == Status.FAILED) {
            //   System.exit(1);
        }
    }

    public static void main(String args[]) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("cog-file-transfer");
        ap
                .addOption(
                        "source-uri",
                        "Source URI: <provider>://<hostname>[:port]/<directory>/<file>",
                        "URI", ArgumentParser.NORMAL);
        ap.addAlias("source-uri", "s");
        ap
                .addOption(
                        "destination-uri",
                        "Destination URI: <provider>://<hostname>[:port]/<directory>/<file>",
                        "URI", ArgumentParser.NORMAL);
        ap.addAlias("destination-uri", "d");
        ap
                .addFlag(
                        "thirdparty",
                        "If present, performs a third party file transfer. Valid only between two GridFTP resources");
        ap.addAlias("thirdparty", "t");
        ap.addFlag("help", "Display usage");
        ap.addAlias("help", "h");
        try {
            ap.parse(args);
            if (ap.isPresent("help")) {
                ap.usage();
            } else {
                ap.checkMandatory();
                try {
                    FileTransfer fileTransfer = new FileTransfer("myTestTask",
                            ap.getStringValue("source-uri"), ap
                                    .getStringValue("destination-uri"));
                    fileTransfer.setCommandline(true);
                    fileTransfer.setThirdparty(ap.isPresent("thirdparty"));
                    fileTransfer.prepareTask();
                    fileTransfer.submitTask();
                } catch (Exception e) {
                    logger.error("Exception in main", e);
                }
            }
        } catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            ap.usage();
        }
    }
}