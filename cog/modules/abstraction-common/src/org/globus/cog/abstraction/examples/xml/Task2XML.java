// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.examples.xml;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.tools.execution.JobSubmission;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

public class Task2XML extends JobSubmission {
    static Logger logger = Logger.getLogger(Task2XML.class.getName());

    public Task2XML() {
        super();
    }

public static void main(String args[]) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("cog-task2xml");
        ap
        .addOption(
                "checkpoint",
                "Checkpoint file name. The task will be checkpointed to this file",
                "fileName", ArgumentParser.NORMAL);
        ap.addAlias("checkpoint", "c");
        ap.addOption("name", "Task name", "taskName", ArgumentParser.OPTIONAL);
        ap.addAlias("name", "n");
        ap.addOption("service-contact", "Service contact", "host",
                ArgumentParser.NORMAL);
        ap.addAlias("service-contact", "s");
        ap.addOption("job-manager", "Execution JobManager (fork, pbs, etc)",
                "jobmanager", ArgumentParser.OPTIONAL);
        ap.addAlias("job-manager", "jm");
        ap.addOption("provider", "Provider; available providers: "
                + AbstractionProperties.getProviders().toString(), "provider",
                ArgumentParser.OPTIONAL);
        ap.addAlias("provider", "p");
        ap.addOption("executable", "Executable", "file", ArgumentParser.NORMAL);
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
                        "attributes",
                        "Additional task specification attributes. Attributes can be specified as \"name=value[,name=value]\"",
                        "string", ArgumentParser.OPTIONAL);
        ap.addAlias("attributes", "a");
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
                            .getStringValue("service-contact"));
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
                    jobSubmission.prepareTask();
                    jobSubmission.marshal();
                } catch (Exception e) {
                    logger.error("Exception in main", e);
                }
            }
        } catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            ap.usage();
        }
    }}