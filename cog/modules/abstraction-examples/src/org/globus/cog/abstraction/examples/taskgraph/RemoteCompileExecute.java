// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.examples.taskgraph;

import java.io.File;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.examples.transfer.FileTransfer;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphHandlerImpl;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.interfaces.TaskGraphHandler;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

/*
 * This example demonstrates an execution flow where a local java file needs to
 * be compiled and executed remotely. Then it transfers the stdout of the remote
 * execution back to the local machine.
 */
public class RemoteCompileExecute implements StatusListener {
    static Logger logger = Logger.getLogger(RemoteCompileExecute.class
            .getName());
    private String serviceContact = null;
    private String executionProvider = null;
    private String transferProvider = null;
    private String sourceFile = null;
    private String normalizedFileName = null;
    private String javaHome = null;
    private String classpath = null;
    private TaskGraph tg = null;

    public static void main(String args[]) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("cog-compile-execute");
        ap.addOption("src", "Name of the Java source file", "filename",
                ArgumentParser.NORMAL);
        ap.addAlias("src", "s");
        ap.addOption("javaHome",
                "Path of the Java environment on the remote machine", "path",
                ArgumentParser.NORMAL);
        ap.addAlias("javaHome", "j");
        ap.addOption("classpath",
                "Path of the Java classpath on the remote machine", "path",
                ArgumentParser.OPTIONAL);
        ap.addAlias("classpath", "cp");
        ap.addOption("execution-provider",
                "Execution provider; available providers: "
                        + AbstractionProperties.getProviders().toString(),
                "provider", ArgumentParser.NORMAL);
        ap.addAlias("execution-provider", "ep");
        ap.addOption("transfer-provider",
                "Transfer provider; available providers: "
                        + AbstractionProperties.getProviders().toString(),
                "provider", ArgumentParser.NORMAL);
        ap.addAlias("transfer-provider", "tp");
        ap.addOption("service-contact", "Service contact for the remote host",
                "host", ArgumentParser.NORMAL);
        ap.addAlias("service-contact", "sc");
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
                    RemoteCompileExecute rce = new RemoteCompileExecute();
                    rce.setServiceContact(ap.getStringValue("service-contact"));
                    rce.setExecutionProvider(ap
                            .getStringValue("execution-provider"));
                    rce.setTransferProvider(ap
                            .getStringValue("transfer-provider"));
                    rce.setSourceFile(ap.getStringValue("src"));
                    rce.setJavaHome(ap.getStringValue("javaHome"));
                    rce.setClasspath(ap.getStringValue("classpath", null));
                    rce.prepareTaskGraph();
                    rce.submit();
                } catch (Exception e) {
                    logger.error("Exception in main", e);
                }
            }
        } catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            ap.usage();
        }
    }

    private void prepareTaskGraph() throws Exception {
        this.tg = new TaskGraphImpl();
        tg.setName("Workflow");
        Task transferTask = prepareFileTransferTask();
        Task compileTask = prepareCompileTask();
        Task executeTask = prepareExecutionTask();
        tg.add(transferTask);
        tg.add(compileTask);
        tg.add(executeTask);

        tg.addDependency(transferTask, compileTask);
        tg.addDependency(compileTask, executeTask);
        tg.addStatusListener(this);
    }

    private void submit() {
        TaskGraphHandler handler = new TaskGraphHandlerImpl();
        try {
            handler.submit(this.tg);
            logger.debug("TaskGraph submitted");
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

    private Task prepareFileTransferTask() throws Exception {
        FileTransfer fileTransfer = new FileTransfer("Transfer_Task", "file://"
                + this.sourceFile, this.transferProvider + "://"
                + this.serviceContact + "///tmp/" + normalizedFileName
                + ".java");
        fileTransfer.prepareTask();
        Task task = fileTransfer.getFileTransferTask();
        task.removeStatusListener(fileTransfer);
        task.addStatusListener(this);
        return task;
    }

    private Task prepareCompileTask() throws InvalidProviderException,
            ProviderMethodException {
        Task task = new TaskImpl("Compile_Task", Task.JOB_SUBMISSION);
        JobSpecification spec = new JobSpecificationImpl();

        spec.setExecutable(this.javaHome + "/bin/javac");
        if (this.classpath != null) {
            spec.addArgument(" -classpath");
            spec.addArgument(this.classpath);
        }
        spec.addArgument("/tmp/" + this.normalizedFileName + ".java");
        spec.setRedirected(true);
        task.setSpecification(spec);

        ExecutionService service = new ExecutionServiceImpl();
        service.setProvider(this.executionProvider.toLowerCase());

        SecurityContext securityContext = AbstractionFactory
                .newSecurityContext(this.executionProvider.toLowerCase());
        securityContext.setCredentials(null);
        service.setSecurityContext(securityContext);

        ServiceContact sc = new ServiceContactImpl(this.serviceContact);
        service.setServiceContact(sc);
        task.addService(service);

        task.addStatusListener(this);
        return task;
    }

    private Task prepareExecutionTask() throws InvalidProviderException,
            ProviderMethodException {
        Task task = new TaskImpl("Execution_Task", Task.JOB_SUBMISSION);

        JobSpecification spec = new JobSpecificationImpl();

        spec.setExecutable(this.javaHome + "/bin/java");
        if (this.classpath == null) {
            spec.addArgument(" -classpath");
            spec.addArgument("/tmp");
        } else {
            spec.addArgument(" -classpath");
            spec.addArgument("/tmp" + File.pathSeparator + this.classpath);
        }
        spec.addArgument(normalizedFileName);
        spec.setRedirected(true);
        task.setSpecification(spec);

        ExecutionService service = new ExecutionServiceImpl();
        service.setProvider(this.executionProvider.toLowerCase());

        SecurityContext securityContext = AbstractionFactory
                .newSecurityContext(this.executionProvider.toLowerCase());
        securityContext.setCredentials(null);
        service.setSecurityContext(securityContext);

        ServiceContact sc = new ServiceContactImpl(this.serviceContact);
        service.setServiceContact(sc);
        task.addService(service);

        task.addStatusListener(this);
        return task;
    }

    public String getExecutionProvider() {
        return executionProvider;
    }

    public void setExecutionProvider(String provider) {
        this.executionProvider = provider;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
        String filename = this.sourceFile.substring(this.sourceFile
                .lastIndexOf("/"));
        filename = filename.substring(1, filename.lastIndexOf(".java"));
        this.normalizedFileName = filename;
        logger.debug("Normalized file " + this.normalizedFileName);
    }

    public String getServiceContact() {
        return serviceContact;
    }

    public void setServiceContact(String serviceContact) {
        this.serviceContact = serviceContact;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public void statusChanged(StatusEvent event) {
        ExecutableObject eo = event.getSource();
        Status status = event.getStatus();
        if (eo.getObjectType() == ExecutableObject.TASK) {
            logger.debug("Status of " + eo.getName() + " changed to: "
                    + status.getStatusString());
            if (status.getStatusCode() == Status.COMPLETED
                    && ((Task) eo).getStdOutput() != null) {
                System.out.println("Output = " + ((Task) eo).getStdOutput());
            }
        } else {
            if (status.getStatusCode() == Status.COMPLETED
                    || status.getStatusCode() == Status.FAILED) {
                System.exit(0);
            }
        }
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public String getTransferProvider() {
        return transferProvider;
    }

    public void setTransferProvider(String transferProvider) {
        this.transferProvider = transferProvider;
    }
}