// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.examples.file;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.FileOperationSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Identity;
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
 * This class serves as an example to invoke file operations on 
 * a remote file server. It also forms the basis of the cog-file-operation
 * command. This example  starts a remote fiel resource and then iterates 
 * in a loop. Every iteration prompts the user to submit
 * one file operation command, executes the command, and reports the result 
 * of that cammand to the user. 
 */
public class FileOperation implements StatusListener {
    static Logger logger = Logger.getLogger(FileOperation.class.getName());

    private Task task = null;
    private Identity sessionid = null;

    private String serviceContact = null;
    private TaskHandler handler = null;
    private String provider = null;
    private boolean active = false;
    private boolean exit = false;

    public FileOperation(String serviceContact, String provider)
            throws InvalidProviderException, ProviderMethodException {
        this.serviceContact = serviceContact;
        this.provider = provider;
        this.handler = AbstractionFactory.newFileOperationTaskHandler(provider);
    }

    private void prepareTask(String operation, String[] arguments)
            throws Exception {

        // Create a new file operation task.
        this.task = new TaskImpl("fileOperationTask", Task.FILE_OPERATION);
        this.task.setProvider(this.provider);
        FileOperationSpecification specification = new FileOperationSpecificationImpl();
        specification.setOperation(operation);
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                specification.addArgument(arguments[i]);
            }
        }
        this.task.setSpecification(specification);

        /*
         * If the operation is to start the file resource, then extract the
         * SESSION-ID for this session. This session id will be used to
         * communicate with this resource for all subsequent transactions.
         */
        if (operation.equalsIgnoreCase(FileOperationSpecification.START)) {
            Service service = new ServiceImpl(Service.FILE_OPERATION);
            service.setProvider(this.provider.toLowerCase());

            SecurityContext securityContext = AbstractionFactory
                    .getSecurityContext(provider, new ServiceContactImpl(serviceContact));
            service.setSecurityContext(securityContext);

            ServiceContact sc = new ServiceContactImpl(this.serviceContact);
            service.setServiceContact(sc);

            this.task.addService(service);
        } else {
            this.task.setAttribute("sessionID", this.sessionid);
        }
        this.task.addStatusListener(this);
    }

    private void submitTask() throws Exception {

        try {
            this.active = true;
            handler.submit(this.task);
        } catch (InvalidSecurityContextException ise) {
            System.out.println("Security Exception: " + ise.getMessage());
            logger.debug("Stack trace: ", ise);
            this.active = false;
        } catch (TaskSubmissionException tse) {
            System.out.println("Submission Exception: " + tse.getMessage());
            logger.debug("Stack trace: ", tse);
            this.active = false;
        } catch (IllegalSpecException ispe) {
            System.out.println("Specification Exception: " + ispe.getMessage());
            logger.debug("Stack trace: ", ispe);
            this.active = false;
        } catch (InvalidServiceContactException isce) {
            System.out.println("Service Contact Exception: "
                    + isce.getMessage());
            logger.debug("Stack trace: ", isce);
            this.active = false;
        }
    }

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
        if (status.getStatusCode() == Status.FAILED) {
            if (event.getStatus().getMessage() != null) {
                System.out.println("Operation failed: "
                        + event.getStatus().getMessage());
            } else if (event.getStatus().getException() != null) {
                System.out.println("Operation failed: ");
                event.getStatus().getException().printStackTrace();
            } else {
                System.out.println("Operation failed");
            }
            this.active = false;

            /*
             * If the "start" command has failed, there is no point continuing
             * with the remaining example
             */
            if (((FileOperationSpecification) this.task.getSpecification())
                    .getOperation().equalsIgnoreCase("start")) {
                System.exit(1);
            }
        }
        if (status.getStatusCode() == Status.COMPLETED) {

            /*
             * The output of a file operation task is dependent on command
             * invoked on the file resource. Therefore, the output of every task
             * has to be processed differently.
             */
            evaluateOutput();
            this.active = false;
        }
    }

    private void evaluateOutput() {
        FileOperationSpecification specification = (FileOperationSpecification) this.task
                .getSpecification();
        String operation = specification.getOperation();
        Object output = task.getAttribute("output");
        if (operation.equalsIgnoreCase(FileOperationSpecification.CHMOD)) {
            System.out.println("Output: " + "Changed permissions");
        } else if (operation
                .equalsIgnoreCase(FileOperationSpecification.EXISTS)) {
            System.out.println("Output: " + ((Boolean) output).toString());
        } else if (operation.equalsIgnoreCase(FileOperationSpecification.PWD)) {
            System.out.println("Output: " + (String) output);
        } else if (operation
                .equalsIgnoreCase(FileOperationSpecification.GETDIR)) {
            System.out.println("Output: " + "Got the directory");
        } else if (operation
                .equalsIgnoreCase(FileOperationSpecification.GETFILE)) {
            System.out.println("Output: " + "Got the file");
        } else if (operation
                .equalsIgnoreCase(FileOperationSpecification.ISDIRECTORY)) {
            System.out.println("Output: " + ((Boolean) output).toString());
        } else if (operation.equalsIgnoreCase(FileOperationSpecification.LS)) {
            Collection collection = (Collection) output;
            Iterator iterator = collection.iterator();
            String result = "";
            while (iterator.hasNext()) {
                result += ((GridFile) iterator.next()).getName() + "\n";
            }
            System.out.println("Output:\n" + result);
        } else if (operation.equalsIgnoreCase(FileOperationSpecification.MKDIR)) {
            System.out.println("Output: " + "Directory created");
        } else if (operation
                .equalsIgnoreCase(FileOperationSpecification.PUTDIR)) {
            System.out.println("Output: " + "Put the directory on the server");
        } else if (operation
                .equalsIgnoreCase(FileOperationSpecification.PUTFILE)) {
            System.out.println("Output: " + "Put the file on the server");
        } else if (operation
                .equalsIgnoreCase(FileOperationSpecification.RENAME)) {
            System.out.println("Output: " + "Rename successful");
        } else if (operation.equalsIgnoreCase(FileOperationSpecification.RMDIR)) {
            System.out.println("Output: " + "Removed the directory");
        } else if (operation
                .equalsIgnoreCase(FileOperationSpecification.RMFILE)) {
            System.out.println("Output: " + "Removed the file");
        } else if (operation.equalsIgnoreCase(FileOperationSpecification.CD)) {
            System.out.println("Output: "
                    + "Changed the current working directory");
        } else if (operation.equalsIgnoreCase(FileOperationSpecification.START)) {
            this.sessionid = (Identity) output;
            System.out.println("SessionID: " + this.sessionid.toString());
        } else if (operation.equalsIgnoreCase(FileOperationSpecification.STOP)) {
            System.out.println("Output: " + "File resource stopped");
        }

    }

    public void loop() throws Exception {
        String operation = FileOperationSpecification.START;

        // create a new task object with the given operation
        prepareTask(operation, null);

        // submit the task for this iteration
        submitTask();
        byte readByte[] = new byte[100];
        int count = 0;
        String[] arguments;
        while (!this.exit) {
            // wait for the task to complete or fail
            while (this.active) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            System.out
                    .println("Please Enter your command with its arguments <Enter man for command listing>");
            count = System.in.read(readByte);
            String readString = new String(readByte, 0, count);
            operation = getOperation(readString);
            arguments = getArguments(readString);
            if (operation != null) {
                if (operation.equalsIgnoreCase("man")) {
                    man();
                    continue;
                }
                if (operation.equalsIgnoreCase(FileOperationSpecification.STOP)) {
                    this.exit = true;
                }
                prepareTask(operation, arguments);
                submitTask();
            }
        }
    }

    private String getOperation(String input) {
        String inputString = input;
        StringTokenizer st = new StringTokenizer(inputString);
        if (st.hasMoreTokens()) {
            return st.nextToken();
        }
        return null;
    }

    private String[] getArguments(String input) {
        String inputString = input;
        StringTokenizer st = new StringTokenizer(inputString);
        if (st.hasMoreTokens()) {
            // discard the operation name
            st.nextToken();
        } else {
            return null;
        }
        if (st.hasMoreTokens()) {
            int count = st.countTokens();
            String[] arguments = new String[count];
            for (int i = 0; i < count; i++) {
                arguments[i] = st.nextToken();
            }
            return arguments;
        }
        return null;
    }

    public static void main(String args[]) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("cog-file-operation");
        ap.addOption("service-contact", "Service contact", "hostname",
                ArgumentParser.NORMAL);
        ap.addAlias("service-contact", "s");
        ap.addOption("provider", "Provider; available providers: "
                + AbstractionProperties.getProviders().toString(), "provider",
                ArgumentParser.OPTIONAL);
        ap.addAlias("provider", "p");
        ap.addFlag("verbose",
                "If enabled, display information about what is being done");
        ap.addAlias("verbose", "v");
        ap.addFlag("help", "Display usage");
        ap.addAlias("help", "h");
        try {
            ap.parse(args);
            if (ap.isPresent("help")) {
                ap.usage();
            } else {
                ap.checkMandatory();
                try {
                    // Start the file resource with the contact and provider
                    // information
                    FileOperation fileOperation = new FileOperation(ap
                            .getStringValue("service-contact"), ap
                            .getStringValue("provider", "gridftp"));

                    // start the loop.
                    fileOperation.loop();
                } catch (Exception e) {
                    logger.debug("Exception in main", e);
                    System.err.println("Error: " + e.getMessage());
                    System.exit(1);
                }
            }
        } catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            ap.usage();
            System.exit(1);
        }
    }

    private void man() {
        String man = "Java CoG Kit File Operation commands \n"
                + "------------------------------------ \n"
                + "Please Note: Commands are case sensitive and all arguments are required \n\n"
                + "1. stop \n"
                + "2. ls \n"
                + "3. ls <directory> \n"
                + "4. pwd \n"
                + "5. cd <directory> \n"
                + "6. mkdir <directoryName> \n"
                + "7. rmdir <directoryName> <force (true/false)> \n"
                + "8. rmfile <fileName> \n"
                + "9. isDirectory <directoryName> \n"
                + "10. exists <fileName> \n"
                + "11. getfile <sourceFileName on server> <destinationFileName on client> \n"
                + "12. putfile <sourceFileName on client> <destinationFileName on server> \n"
                + "13. getdir <sourceDirectoryName on server> <destinationDirectoryName on client> \n"
                + "14. putfile <sourceDirectoryName on client> <destinationDirectoryName on server> \n"
                + "15. rename <oldName> <newName> \n"
                + "16. chmod <fileName> <mode> \n";

        System.out.println(man);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getServiceContact() {
        return serviceContact;
    }

    public void setServiceContact(String serviceContact) {
        this.serviceContact = serviceContact;
    }

}