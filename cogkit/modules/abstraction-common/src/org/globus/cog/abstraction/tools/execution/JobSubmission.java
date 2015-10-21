/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.tools.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.impl.common.CleanUpSetImpl;
import org.globus.cog.abstraction.impl.common.StagingSetEntryImpl;
import org.globus.cog.abstraction.impl.common.StagingSetImpl;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.CleanUpSet;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.StagingSet;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

/*
 * This clas serves as an example demonstrating the pattern to do job
 * submissions with the abstractions framework. It also forms the basis of the
 * cog-job-submit command.
 */
public class JobSubmission implements StatusListener {
    static Logger logger = Logger.getLogger(JobSubmission.class.getName());

    private String name;
    private Task task;

    private String serviceContact;
    private String provider;
    
    private boolean batch;
    private boolean redirected;
    
    private String executable;
    private final List<String> arguments;

    private final Map<String, String> environment;

    private final Map<String, Object> attributes;

    private String directory;

    private String stderr, stdout, stdin;
    
    private StagingSet stagein, stageout;
    
    private CleanUpSet cleanup;

    private boolean commandLine;

    private String jobmanager;

    private String specification;
    
    private boolean verbose;
    
    public JobSubmission() {
        arguments = new ArrayList<String>();
        environment = new HashMap<String, String>();
        attributes = new HashMap<String, Object>();
    }

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
        } 
        else {
            spec.setExecutable(executable);

            if (arguments != null) {
                spec.setArguments(arguments);
            }

            if (!environment.isEmpty()) {
                spec.setEnvironmentVariables(environment);
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
            for (Map.Entry<String, Object> e : attributes.entrySet()) {
                spec.setAttribute(e.getKey(), e.getValue());
            }
            
            if (stagein != null) {
                spec.setStageIn(stagein);
            }
            if (stageout != null) {
                spec.setStageOut(stageout);
            }
            if (cleanup != null) {
                spec.setCleanUpSet(cleanup);
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
        TaskHandler handler = AbstractionFactory.newExecutionTaskHandler(provider);
        try {
            handler.submit(this.task);
        } 
        catch (InvalidSecurityContextException ise) {
            System.err.println("Security Exception: " + getMessages(ise));
            logger.debug("Stack trace: ", ise);
            System.exit(1);
        } 
        catch (TaskSubmissionException tse) {
            System.err.println("Submission Exception: " + getMessages(tse));
            logger.debug("Stack trace: ", tse);
            System.exit(1);
        } 
        catch (IllegalSpecException ispe) {
            System.err.println("Specification Exception: " + getMessages(ispe));
            logger.debug("Stack trace: ", ispe);
            System.exit(1);
        } 
        catch (InvalidServiceContactException isce) {
            System.err.println("Service Contact Exception: " + getMessages(isce));
            logger.debug("Stack trace: ", isce);
            System.exit(1);
        }
        //wait
        while (true) {
            Thread.sleep(500);
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

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
        if (verbose) {
            System.out.println(status.getTime() + " " + status.getStatusString());
        }
        logger.debug("Status changed to " + status.getStatusString());
        if (status.getStatusCode() == Status.FAILED) {
            if (event.getStatus().getException() != null) {
                System.err.println("Job failed: ");
                event.getStatus().getException().printStackTrace();
            }
            else if (event.getStatus().getMessage() != null) {
                System.err.println("Job failed: "
                        + event.getStatus().getMessage());
            } 
            else {
                System.err.println("Job failed");
            }
            if (this.task.getStdOutput() != null) {
                System.out.println(this.task.getStdOutput());
            }
            if (this.task.getStdError() != null) {
                System.err.println(this.task.getStdError());
            }
            if (this.commandLine) {
                System.exit(1);
            }
        }
        if (status.getStatusCode() == Status.COMPLETED) {
            if (!isBatch()) {
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
        ap.addOption("specification",
                "Provider-specific format of the specification. If this option is used, "
                 + "then individual specification-related parameters such as "
                 + "executable, environment, etc are ignored. ",
                 "spec", ArgumentParser.OPTIONAL);
        ap.addAlias("specification", "spec");
        ap.addOption(ArgumentParser.DEFAULT, "Executable", "executable",
                ArgumentParser.NORMAL);
        ap.setArguments(true);
        ap.addOption("environment", "Environment variables for the remote execution environment, "
                + "specified as \"name=value[,name=value]\"",
                "string", ArgumentParser.OPTIONAL);
        ap.addAlias("environment", "env");
        ap.addOption("directory", "Target directory", "string",
                ArgumentParser.OPTIONAL);
        ap.addAlias("directory", "d");
        ap.addFlag("batch", "If present, the job is run in batch mode");
        ap.addAlias("batch", "b");
        ap.addFlag("redirected",
                "If present, the arguments to -stdout and -stderr refer to local files");
        ap.addAlias("redirected", "r");
        ap.addOption("stdout", "Indicates a file where the standard output of the job should be "
                + "redirected",
                "file", ArgumentParser.OPTIONAL);
        ap.addOption("stderr", "Indicates a file where the standard error of the job should be "
                + "redirected",
                "file", ArgumentParser.OPTIONAL);
        ap.addOption("stdin", "Indicates a file from which the standard input of the job should "
                + "be read",
                "file", ArgumentParser.OPTIONAL);
        ap.addOption("attributes", "Additional task specification attributes. Attributes can be "
                + "specified as \"name=value[,name=value]\"",
                "string", ArgumentParser.OPTIONAL);
        ap.addAlias("attributes", "a");
        ap.addOption("stagein", "A colon-separated list of files to stage-in in the form source -> destination. "
                + "Leading and trailing spaces in the file names are ignored. Relative files on the remote "
                + "site are interpreted as relative to the job directory.",
                "fileList", ArgumentParser.OPTIONAL);
        ap.addOption("stageout", "A colon-separated list of files to stage-out in the form destination <- source. "
                + "Leading and trailing spaces in the file names are ignored. Relative files on the remote "
                + "site are interpreted as relative to the job directory.",
                "fileList", ArgumentParser.OPTIONAL);
        ap.addOption("cleanup", "A colon-separated list of files and/or directories to clean up."
                + "The files/directories can be relative (to the job directory), but they cannot point to"
                + " anything outside of the job directory.", 
                "list", ArgumentParser.OPTIONAL);
        ap.addFlag("verbose", "If enabled, display information about what is being done");
        ap.addAlias("verbose", "v");
        ap.addFlag("help", "Display usage");
        ap.addAlias("help", "h");
        try {
            ap.parse(args);
            if (ap.isPresent("help")) {
                ap.usage();
            } 
            else {
                ap.checkMandatory();
                try {
                    JobSubmission jobSubmission = new JobSubmission();
                    jobSubmission.setServiceContact(ap.getStringValue("service-contact",""));
                    jobSubmission.setProvider(ap.getStringValue("provider", "GT2"));
                    jobSubmission.setJobManager(ap.getStringValue("job-manager"));
                    jobSubmission.setName(ap.getStringValue("name", "myTask"));
                    jobSubmission.setCommandline(true);
                    jobSubmission.setBatch(ap.isPresent("batch"));
                    jobSubmission.setRedirected(ap.isPresent("redirected"));
                    jobSubmission.setSpecification(ap.getStringValue("specification"));
                    jobSubmission.setExecutable(ap.getStringValue(ArgumentParser.DEFAULT));
                    jobSubmission.setArguments(ap.getArguments());
                    jobSubmission.setEnvironment(ap.getStringValue("environment", null));
                    jobSubmission.setAttributes(ap.getStringValue("attributes", null));
                    jobSubmission.setDirectory(ap.getStringValue("directory", null));
                    jobSubmission.setStdout(ap.getStringValue("stdout", null));
                    jobSubmission.setStderr(ap.getStringValue("stderr", null));
                    jobSubmission.setStdin(ap.getStringValue("stdin", null));
                    jobSubmission.setStagein(ap.getStringValue("stagein", null));
                    jobSubmission.setStageout(ap.getStringValue("stageout", null));
                    jobSubmission.setCleanup(ap.getStringValue("cleanup", null));
                    jobSubmission.setVerbose(ap.isPresent("verbose"));
                    jobSubmission.prepareTask();
                    jobSubmission.submitTask();
                } 
                catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    logger.error("Exception in main", e);
                }
            }
        } 
        catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            ap.usage();
        }
    }

    public List<String> getArguments() {
        return arguments;
    }
    
    public void setArguments(String arguments) {
        if (arguments == null) {
            return;
        }
        String[] l = arguments.split("\\s+");
        for (String s : l) {
            this.arguments.add(s);
        }
    }

    public void setArguments(List<String> arguments) {
        this.arguments.addAll(arguments);
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

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        if (environment == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(environment, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() > 0) {
                StringTokenizer st2 = new StringTokenizer(token, "=");
                while (st2.hasMoreTokens()) {
                    String name = st2.nextToken().trim();
                    String value = st2.nextToken().trim();
                    this.environment.put(name, value);
                }
            }
        }
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        if (attributes == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(attributes, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() > 0) {
                StringTokenizer st2 = new StringTokenizer(token, "=");
                while (st2.hasMoreTokens()) {
                    String name = st2.nextToken().trim();
                    String value = st2.nextToken().trim();
                    this.attributes.put(name, value);
                }
            }
        }
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
    
    public void setStagein(String stagein) {
        if (stagein == null) {
            return;
        }
        this.stagein = getStagingSet(stagein, "->", true);
    }
    
    public void setStageout(String stageout) {
        if (stageout == null) {
            return;
        }
        this.stageout = getStagingSet(stageout, "<-", false);
    }

    private StagingSet getStagingSet(String s, String sep, boolean srcIsFirst) {
        StagingSet ss = new StagingSetImpl();
        StringTokenizer st = new StringTokenizer(s, ":");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            String[] st2 = tok.split(sep);
            String src;
            String dst;
            if (srcIsFirst) {
                src = "file://localhost/" + st2[0].trim();
                dst = st2[1].trim();
            }
            else {
                dst = "file://localhost/" + st2[0].trim();
                src = st2[1].trim();
            }
            ss.add(new StagingSetEntryImpl(src, dst));
        }
        return ss;
    }
    
    public void setCleanup(String cleanup) {
        if (cleanup == null) {
            return;
        }
        
        this.cleanup = new CleanUpSetImpl();
        StringTokenizer st = new StringTokenizer(cleanup, ":");
        while (st.hasMoreTokens()) {
            this.cleanup.add(st.nextToken());
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }    
}