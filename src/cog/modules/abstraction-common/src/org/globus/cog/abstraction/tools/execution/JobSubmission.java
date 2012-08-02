// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.tools.execution;

import java.io.File;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.abstraction.xml.TaskMarshaller;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

/*
 * This clas serves as an example demonstrating the pattern to do job
 * submissions with the abstractions framework. It also forms the basis of the
 * cog-job-submit command.
 */
public class JobSubmission implements StatusListener {
    static Logger logger = Logger.getLogger(JobSubmission.class.getName());

    private String name = null;

    private String checkpointFile = null;

    private Task task = null;

    private String serviceContact = null;

    private String provider = null;

    private boolean batch = false;

    private boolean redirected;
    

    private String executable = null;

    private String arguments = null;

    private String environment = null;

    private String attributes = null;

    private String directory = null;

    private String stderr, stdout, stdin;

    private boolean commandLine = false;

    private String jobmanager = null;

    private String specification = null;

    public void prepareTask() throws Exception {
        /*
         * Create a new job submission task with the given task name.
         */
        this.task = new TaskImpl(this.name, Task.JOB_SUBMISSION);
        logger.debug("Task Identity: " + this.task.getIdentity().toString());

        /*
         * Generate a new JobSpecification with all the given attributes.
         */
        JobSpecification spec = new JobSpecificationImpl();

        if (this.specification != null) {
            spec.setSpecification(this.specification);
        } else {
            spec.setExecutable(executable);

            if (arguments != null) {
                spec.setArguments(arguments);
            }

            if (environment != null) {
                setEnvironment(spec);
            }

            if (directory != null) {
                spec.setDirectory(directory);
            }

            /*
             * if the task is a batch task, then this example returns
             * immediately after submission. Otherwise it will return only after
             * the task is completed or failed.
             */
            if (batch) {
                spec.setBatchJob(true);
            }

            /*
             * If the redirected flag is set to true, the stdout and stderr of
             * the task will be redirected to the local machine. Otherwise these
             * will be piped on the remote machine.
             */
            if (redirected) {
                spec.setStdOutputLocation(FileLocation.MEMORY);
                spec.setStdErrorLocation(FileLocation.MEMORY);
            }

            /*
             * The file name where the stdout of the task should be piped. If
             * null, the stdout is piped to the console. Hence, if stdout =
             * "output.txt" and redirected = true, then the output file
             * output.txt is available on the local machine.
             */
            if (stdout != null) {
                spec.setStdOutput(stdout);
            }

            /*
             * The file name where the stderr of the task should be piped. If
             * null, the stderr is piped to the console. Hence, if stderr =
             * "error.txt" and redirected = true, then the error file error.txt
             * is available on the local machine.
             */
            if (stderr != null) {
                spec.setStdError(stderr);
            }
            
            if (stdin != null) {
            	spec.setStdInput(stdin);
            }

            /*
             * All additional attributes that are not available as an API for
             * the JobSpecification interface can be provided as a task
             * attribute. For example the "count" paramter in a GT rsl is
             * important for MPI jobs. Since it is not a part of the
             * JobSpecification intercace, it can be provided as a task
             * attribute.
             */
            if (this.attributes != null) {
                setAttributes(spec);
            }
        }
        this.task.setSpecification(spec);

        /*
         * Create an execution service for this task.
         */
        ExecutionService service = new ExecutionServiceImpl();
        service.setProvider(this.provider.toLowerCase());

        ServiceContact sc = new ServiceContactImpl(this.serviceContact);
        service.setServiceContact(sc);
        
        SecurityContext securityContext = AbstractionFactory.getSecurityContext(provider, sc);
        service.setSecurityContext(securityContext);

        /*
         * This is an abstraction for the jobmanager. For example, the
         * servicecontact can be hot.anl.gov. One can specify different
         * jobmanagers for the same service contact, i.e if jobmanager = PBS
         * then the service is equivalent to hot.anl.gov/jobmanager-pbs
         */
        service.setJobManager(jobmanager);

        this.task.addService(service);

        /*
         * Add a task listerner for this task. This allows the task to be
         * executed asynchronously. The client can continue with other
         * activities and gets asynchronously notified every time the status of
         * the task changes.
         */
        this.task.addStatusListener(this);
    }

    public Task getExecutionTask() {
        return this.task;
    }

    private void submitTask() throws Exception {
        TaskHandler handler = AbstractionFactory
                .newExecutionTaskHandler(provider);
        try {
            handler.submit(this.task);
        } catch (InvalidSecurityContextException ise) {
            System.out.println("Security Exception: " + getMessages(ise));
            logger.debug("Stack trace: ", ise);
            System.exit(1);
        } catch (TaskSubmissionException tse) {
            System.out.println("Submission Exception: " + getMessages(tse));
            logger.debug("Stack trace: ", tse);
            System.exit(1);
        } catch (IllegalSpecException ispe) {
            System.out.println("Specification Exception: " + getMessages(ispe));
            logger.debug("Stack trace: ", ispe);
            System.exit(1);
        } catch (InvalidServiceContactException isce) {
            System.out.println("Service Contact Exception: " + getMessages(isce));
            logger.debug("Stack trace: ", isce);
            System.exit(1);
        }
        //wait
        while (true) {
            Thread.sleep(1000);
        }
    }
    
    private String getMessages(Throwable e) {
    	StringBuffer sb = new StringBuffer();
    	Throwable last = null;
    	while (e != null) {
    		if (last == null || last.getMessage() == null || last.getMessage().indexOf(e.getMessage()) == -1) {
    			sb.append("\n\t");
    			sb.append(e.getMessage());
    		}
    		last = e;
    		e = e.getCause();
    	}
    	return sb.toString();
    }

    public void marshal() {
        try {
            // Translate the task object into an XML file
            File xmlFile = new File(this.checkpointFile);
            xmlFile.createNewFile();
            TaskMarshaller.marshal(this.task, xmlFile);
        } catch (Exception e) {
            logger.error("Cannot marshal the task", e);
        }
    }

    private void setEnvironment(JobSpecification spec) {
        String env = getEnvironment();
        StringTokenizer st = new StringTokenizer(env, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() > 0) {
                StringTokenizer st2 = new StringTokenizer(token, "=");
                while (st2.hasMoreTokens()) {
                    String name = st2.nextToken().trim();
                    String value = st2.nextToken().trim();
                    spec.addEnvironmentVariable(name, value);
                }
            }
        }
    }

    private void setAttributes(JobSpecification spec) {
        String att = getAttributes();
        StringTokenizer st = new StringTokenizer(att, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() > 0) {
                StringTokenizer st2 = new StringTokenizer(token, "=");
                while (st2.hasMoreTokens()) {
                    String name = st2.nextToken().trim();
                    String value = st2.nextToken().trim();
                    spec.setAttribute(name, value);
                }
            }
        }
    }

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
        logger.debug("Status changed to " + status.getStatusString());
        if (status.getStatusCode() == Status.SUBMITTED) {
            if (this.checkpointFile != null) {
                marshal();
                System.out.println("Task checkpointed to file: "
                        + this.checkpointFile);
            }
        }
        if (status.getStatusCode() == Status.FAILED) {
            if (event.getStatus().getException() != null) {
                System.out.println("Job failed: ");
                event.getStatus().getException().printStackTrace();
            }
            else if (event.getStatus().getMessage() != null) {
                System.out.println("Job failed: "
                        + event.getStatus().getMessage());
            } else {
                System.out.println("Job failed");
            }
            if (this.commandLine) {
                System.exit(1);
            }
        }
        if (status.getStatusCode() == Status.COMPLETED) {
            if (isBatch()) {
                System.out.println("Job Submitted");
            } else {
                System.out.println("Job completed");
                if (this.task.getStdOutput() != null) {
                    System.out.println(this.task.getStdOutput());
                }
                if (this.task.getStdError() != null) {
                    System.err.println(this.task.getStdError());
                }
            }
            if (this.commandLine) {
                System.exit(0);
            }
        }
    }

    public static void main(String args[]) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("cog-job-submission");
        ap.addOption("name", "Task name", "taskName", ArgumentParser.OPTIONAL);
        ap.addAlias("name", "n");
        ap.addOption("service-contact", "Service contact", "host",
                ArgumentParser.OPTIONAL);
        ap.addAlias("service-contact", "s");
        ap.addOption("job-manager", "Execution JobManager (fork, pbs, etc)",
                "jobmanager", ArgumentParser.OPTIONAL);
        ap.addAlias("job-manager", "jm");
        ap.addOption("provider", "Provider; available providers: "
                + AbstractionProperties.getProviders().toString(), "provider",
                ArgumentParser.OPTIONAL);
        ap.addAlias("provider", "p");
        ap
                .addOption(
                        "specification",
                        "Provider-specific format of the specification. If this option is used, "
                                + "then individual specification-related parameters such as "
                                + "executable, environment, etc are ignored. ",
                        "spec", ArgumentParser.OPTIONAL);
        ap.addAlias("specification", "spec");
        ap.addOption("executable", "Executable", "file",
                ArgumentParser.OPTIONAL);
        ap.addAlias("executable", "e");
        ap.addOption("arguments", "Arguments. If more than one, use quotes",
                "string", ArgumentParser.OPTIONAL);
        ap.addAlias("arguments", "args");
        ap
                .addOption(
                        "environment",
                        "Environment variables for the remote execution environment, specified as \"name=value[,name=value]\"",
                        "string", ArgumentParser.OPTIONAL);
        ap.addAlias("environment", "env");
        ap.addOption("directory", "Target directory", "string",
                ArgumentParser.OPTIONAL);
        ap.addAlias("directory", "d");
        ap.addFlag("batch", "If present, the job is run in batch mode");
        ap.addAlias("batch", "b");
        ap
                .addFlag("redirected",
                        "If present, the arguments to -stdout and -stderr refer to local files");
        ap.addAlias("redirected", "r");
        ap
                .addOption(
                        "stdout",
                        "Indicates a file where the standard output of the job should be redirected",
                        "file", ArgumentParser.OPTIONAL);
        ap
                .addOption(
                        "stderr",
                        "Indicates a file where the standard error of the job should be redirected",
                        "file", ArgumentParser.OPTIONAL);
        ap
                .addOption(
                        "stdin",
                        "Indicates a file from which the standard input of the job should be read",
                        "file", ArgumentParser.OPTIONAL);
        ap
                .addOption(
                        "attributes",
                        "Additional task specification attributes. Attributes can be specified as \"name=value[,name=value]\"",
                        "string", ArgumentParser.OPTIONAL);
        ap.addAlias("attributes", "a");
        ap
                .addOption(
                        "checkpoint",
                        "Checkpoint file name. The task will be checkpointed to this file once submitted",
                        "fileName", ArgumentParser.OPTIONAL);
        ap.addAlias("checkpoint", "c");
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
                    JobSubmission jobSubmission = new JobSubmission();
                    jobSubmission.setServiceContact(ap
                            .getStringValue("service-contact",""));
                    jobSubmission.setProvider(ap.getStringValue("provider",
                            "GT2"));
                    jobSubmission.setJobManager(ap
                            .getStringValue("job-manager"));
                    jobSubmission.setName(ap.getStringValue("name", "myTask"));
                    jobSubmission.setCheckpointFile(ap.getStringValue(
                            "checkpoint", null));
                    jobSubmission.setCommandline(true);
                    jobSubmission.setBatch(ap.isPresent("batch"));
                    jobSubmission.setRedirected(ap.isPresent("redirected"));
                    jobSubmission.setSpecification(ap
                            .getStringValue("specification"));
                    jobSubmission
                            .setExecutable(ap.getStringValue("executable"));
                    jobSubmission.setArguments(ap.getStringValue("arguments",
                            null));
                    jobSubmission.setEnvironment(ap.getStringValue(
                            "environment", null));
                    jobSubmission.setAttributes(ap.getStringValue("attributes",
                            null));
                    jobSubmission.setDirectory(ap.getStringValue("directory",
                            null));
                    jobSubmission.setStdout(ap.getStringValue("stdout", null));
                    jobSubmission.setStderr(ap.getStringValue("stderr", null));
                    jobSubmission.setStdin(ap.getStringValue("stdin", null));
                    jobSubmission.prepareTask();
                    jobSubmission.submitTask();
                } catch (Exception e) {
                    logger.error("Exception in main", e);
                }
            }
        } catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            ap.usage();
        }
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public boolean isBatch() {
        return batch;
    }

    public void setBatch(boolean batch) {
        this.batch = batch;
    }

    public boolean isRedirected() {
        return redirected;
    }

    public void setRedirected(boolean redirected) {
        this.redirected = redirected;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }
    
    public String getStdin() {
        return stdin;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }


    public void setCommandline(boolean bool) {
        this.commandLine = bool;
    }

    public boolean isCommandline() {
        return this.commandLine;
    }

    public String getCheckpointFile() {
        return checkpointFile;
    }

    public void setCheckpointFile(String file) {
        this.checkpointFile = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceContact() {
        return serviceContact;
    }

    public void setServiceContact(String serviceContact) {
        this.serviceContact = serviceContact;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getJobManager() {
        return jobmanager;
    }

    public void setJobManager(String jobmanager) {
        this.jobmanager = jobmanager;
		
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }
}