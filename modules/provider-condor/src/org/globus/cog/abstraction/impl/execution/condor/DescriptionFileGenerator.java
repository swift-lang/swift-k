package org.globus.cog.abstraction.impl.execution.condor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class DescriptionFileGenerator {

    public static File generate(Task task) throws IOException {
        JobSpecification specification = (JobSpecification) task
                .getSpecification();
        File descriptionFile = null;
        String descriptionFileName = (String) specification
                .getAttribute("descriptionFile");
        if (descriptionFileName != null
                && descriptionFileName.trim().length() > 0) {
            descriptionFile = new File(descriptionFileName);
            return descriptionFile;
        } else {
            descriptionFile = File.createTempFile(task.getIdentity().getValue(), ".desc");

            constructDescriptionFile(descriptionFile, task);
            return descriptionFile;
        }

    }

    private static void constructDescriptionFile(File descriptionFile, Task task)
            throws IOException {
        JobSpecification specification = (JobSpecification) task
                .getSpecification();
        FileWriter fileWriter = new FileWriter(descriptionFile);
        fileWriter.write("#####################################\n");
        fileWriter.write("# Task ID: " + task.getIdentity().toString() + "\n");
        fileWriter.write("#####################################\n\n");

        String executable = specification.getExecutable();
        if (executable != null) {
            fileWriter.write("Executable = " + executable + "\n");
        }

        String argumentString = specification.getArgumentsAsString();
        argumentString = argumentString.replaceAll("\\\"", "\\\\\"");
        if (argumentString != null) {
            fileWriter.write("Arguments = " + argumentString + "\n");
        }

        // set the default universe (if not specified)
        String universe = (String) specification.getAttribute("universe");
        if (universe == null) {
            specification.setAttribute("Universe", "vanilla");
        }

        String stdinput = specification.getStdInput();
        if (stdinput != null) {
            fileWriter.write("Input = " + stdinput + "\n");
        }

        String stdout = specification.getStdOutput();
        if (stdout != null) {
            fileWriter.write("Output = " + stdout + "\n");
        }

        String stderror = specification.getStdError();
        if (stderror != null) {
            fileWriter.write("Error = " + stderror + "\n");
        }

        String directory = specification.getDirectory();
        if (directory != null) {
            fileWriter.write("Initialdir = " + directory + "\n");
        }

        // set the default log (if not specified)
        String log = (String) specification.getAttribute("log");
        if (log == null) {
            File logFile = File.createTempFile(task.getIdentity().getValue(), ".log");
            log = logFile.getAbsolutePath();
            specification.setAttribute("log", log);
        }

        // write all condor-specific attributes to the description file
        Enumeration enumeration = specification.getAllAttributes();
        String attribute = null;
        while (enumeration.hasMoreElements()) {
            attribute = (String) enumeration.nextElement();
            fileWriter.write(attribute + " = "
                    + (String) specification.getAttribute(attribute) + "\n");
        }

        fileWriter.write("Queue");
        fileWriter.flush();
        fileWriter.close();
    }
}
