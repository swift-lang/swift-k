package org.globus.cog.abstraction.examples.etc;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.util.ArgumentParser;

public class CogInfo {

    static Logger logger = Logger.getLogger(CogInfo.class.getName());
    private String os = null;

    public CogInfo() {
        this.os = System.getProperty("os.name");
    }

    public void all() throws Exception {
        user();
        os();
        java();
        ant();
        providers();
        env();
    }

    public void os() {
        System.out.println("OS Name: " + this.os);
        System.out.println("OS Version: " + System.getProperty("os.version")
                + "\n");
    }

    public void user() {
        System.out.println("Username: " + System.getProperty("user.name")
                + "\n");
    }

    public void java() throws Exception {
        System.out.println("Java Path: " + System.getProperty("java.home"));
        System.out.println("Java Version: "
                + System.getProperty("java.version") + "\n");
    }

    public void ant() throws Exception {
        if (this.os.equalsIgnoreCase("linux")) {
            System.out.println("Ant Path: " + runCommand("which ant"));
            System.out.println("Ant Version: \n" + runCommand("ant -version"));
        }
    }

    public void providers() throws Exception {
        System.out.println("Supported Providers: "
                + AbstractionProperties.getProviders().toString() + "\n");
    }

    public void env() throws Exception {
        if (this.os.equalsIgnoreCase("linux")) {
            System.out.println("System Environment: \n" + runCommand("env"));
        }
    }

    private String runCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command, null, null);

        // process output
        InputStreamReader inReader = new InputStreamReader(process
                .getInputStream());
        BufferedReader inBuffer = new BufferedReader(inReader);
        String message = inBuffer.readLine();
        String output = message + "\n";
        while (message != null) {
            message = inBuffer.readLine();
            if (message != null) {
                output += message + "\n";
            }
        }
        return output;
    }

    public static void main(String args[]) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("cog-info");
        ap.addFlag("all", "Display all the client-side information");
        ap.addFlag("os", "Display the operating system on the client machine");
        ap.addFlag("user", "Display the username");
        ap.addFlag("java", "Display all the java-specific information");
        ap.addFlag("ant", "Display all the Apache Ant specific information");
        ap.addFlag("providers", "Display the list of supported providers");
        ap.addAlias("providers", "p");
        ap.addFlag("env", "Prints all the environment variables");
        ap.addFlag("help", "Display usage");
        ap.addAlias("help", "h");
        CogInfo cogInfo = new CogInfo();
        try {
            ap.parse(args);
            if (ap.isPresent("help")) {
                ap.usage();
            } else if (ap.isPresent("all")) {
                cogInfo.all();
            } else {
                if (ap.isPresent("user")) {
                    cogInfo.user();
                }
                if (ap.isPresent("os")) {
                    cogInfo.os();
                }
                if (ap.isPresent("java")) {
                    cogInfo.java();
                }
                if (ap.isPresent("ant")) {
                    cogInfo.ant();
                }
                if (ap.isPresent("providers")) {
                    cogInfo.providers();
                }
                if (ap.isPresent("env")) {
                    cogInfo.env();
                }
            }
        } catch (Exception e) {
            System.err.println("Error while generating information: "
                    + e.getMessage());
        }
    }
}