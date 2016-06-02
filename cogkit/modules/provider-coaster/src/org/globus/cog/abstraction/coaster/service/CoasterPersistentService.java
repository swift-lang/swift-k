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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 3, 2010
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.AbstractSettings;
import org.globus.cog.abstraction.coaster.service.job.manager.JobQueue;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.Timer;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;
import org.globus.cog.util.Misc;
import org.globus.cog.util.timer.Timestamp;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class CoasterPersistentService extends CoasterService {
    public static final Logger logger = Logger.getLogger(CoasterPersistentService.class);
    
    private static boolean stats, passive;
    
    private JobQueue sharedQueue;
    private boolean shared;
    private int controlPort = -1;

    public CoasterPersistentService() throws IOException {
        super(false);
    }
    
    private static CPSStatusDisplay statusDisplay;

    public CoasterPersistentService(GSSCredential cred, int port, InetAddress bindTo)
            throws IOException {
        super(cred, port, bindTo);
    }

    public CoasterPersistentService(boolean secure, int port, InetAddress bindTo)
            throws IOException {
        super(secure, port, bindTo);
    }
    
    @Override
    public void start() {
        if (controlPort != -1) {
            try {
                SettingsServer ss = new SettingsServer(sharedQueue.getCoasterQueueProcessor(), controlPort);
                ss.start();
                System.out.println("Settings server URL: " + ss.getURL());
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to start settings server", e);
            }
        }
        super.start();
    }

    public void setShared(boolean shared) {
        this.shared = shared;
        if (shared) {
            sharedQueue = super.createJobQueue(null);
        }
    }
    
    public boolean isPersistent() {
        return true;
    }
    
    public boolean isShared() {
        return shared;
    }
    
    public int getControlPort() {
        return controlPort;
    }

    public void setControlPort(int controlPort) {
        this.controlPort = controlPort;
    }

    public JobQueue getSharedQueue() {
        if (!shared) {
            throw new IllegalStateException("Not in shared mode");
        }
        return sharedQueue;
    }
    
    @Override
    public JobQueue getJobQueue(String id) {
        if (shared) {
            return sharedQueue;
        }
        else {
            return super.getJobQueue(id);
        }
    }

    @Override
    public boolean clientRequestedShutdown(CoasterChannel channel) {
        logger.info("Shutdown requested on channel " + channel + ". Shutting down affected queues.");
        if (shared) {
            sharedQueue.getBroadcaster().removeChannel(channel);
            sharedQueue.cancelTasksForChannel(channel);
        }
        else {
            shutDownQueuesForChannel(channel);
        }
        return false;
    }

    public static void main(String[] args) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("coaster-service");
        ap.addOption("port", "Specifies which port to start the service on", "port",
            ArgumentParser.OPTIONAL);
        ap.addOption("localport", "Specifies which port to start the local service on " +
        		"(workers connect to the local service)",
        		"localport", ArgumentParser.OPTIONAL);
        ap.addAlias("port", "p");
        ap.addOption("portfile", "Specifies a file to write the service port to. " +
        		"If this option is specified, the service port will be chosen automatically. " +
        		"-portfile (-S) and -port (-p) are mutually exclusive.", 
        		"file", ArgumentParser.OPTIONAL);
        ap.addAlias("portfile", "S");
        ap.addOption("localportfile", "Specifies a file to write the local port to. " +
        		"If this option is specified, the local port will be chosen automatically. " +
        		"-localportfile (-W) and -localport are mutually exclusive.", 
        		"file", ArgumentParser.OPTIONAL);
        ap.addAlias("localportfile", "W");
        ap.addFlag("nosec", "Disables GSI security and uses plain TCP sockets instead");
        ap.addOption("proxy",
            "Specifies the location of a proxy credential that will be used for authentication. " +
                    "If not specified, the default proxy will be used.",
            "file", ArgumentParser.OPTIONAL);
        ap.addFlag("local", "Binds the service to the loopback interface");
        ap.addFlag("passive",
            "Initialize the passive worker service and " +
                    "set the passive worker manager to be the default (otherwise the block allocator will be used)." +
                    "The workers will be shared by all connecting clients like in shared mode.");
        ap.addOption("shared", "Enables shared automatically-allocated workers mode in which workers started by " +
        		"a client can be re-used by subsequent clients. The argument specifies a file that contains all " +
        		"the configuration options for the block allocator. Cannot be used with passive workers.", 
        		"config-file", ArgumentParser.OPTIONAL);
        ap.addOption("controlPort", "If specified, starts a simple REST-like service that allows on-the-fly"
        		+ "changes to the settings to be made in shared mode. If zero, the port will be picked "
        		+ "automatically", "port", ArgumentParser.OPTIONAL);
        ap.addOption("logdir", "A directory where the logs should go. If not specified, logs are created in the "
                + "current working directory", "directory", ArgumentParser.OPTIONAL);
        ap.addFlag("stats", "Show a table of various run-time information");
        ap.addFlag("help", "Displays usage information");
        ap.addAlias("help", "h");
        try {
            ap.parse(args);
            if (ap.isPresent("help")) {
                ap.usage();
                System.exit(0);
            }
            InetAddress bindTo = null;
            if (ap.isPresent("local")) {
                bindTo = InetAddress.getLocalHost();
            }
            boolean secure = true;
            if (ap.isPresent("nosec")) {
                secure = false;
            }
            GSSCredential cred = null;
            if (secure) {
                GlobusCredential gc;
                if (ap.hasValue("proxy")) {
                    gc = new GlobusCredential(ap.getStringValue("proxy"));
                }
                else {
                    gc = GlobusCredential.getDefaultCredential();
                }
                cred = new GlobusGSSCredentialImpl(gc, GSSCredential.INITIATE_AND_ACCEPT);
            }

            int port = 1984;
            if (ap.hasValue("port")) {
                if (ap.hasValue("portfile")) {
                    throw new ArgumentParserException("-portfile (-S) and -port are mutually exclusive");
                }
                port = ap.getIntValue("port");
            }
            
            String portFile = null;
            if (ap.hasValue("portfile")) {
                portFile = ap.getStringValue("portfile");
                port = 0;
            }
            
            int localport = 0;
            if (ap.hasValue("localport")) {
                if (ap.hasValue("localportfile")) {
                    throw new ArgumentParserException("-localportfile (-W) and -localport are mutually exclusive");
                }
                localport = ap.getIntValue("localport");
            }
            
            String localPortFile = null;
            if (ap.hasValue("localportfile")) {
                localPortFile = ap.getStringValue("localportfile");
                localport = 0;
            }

            if (ap.hasValue("logdir")) {
                setupLogging(ap.getStringValue("logdir"));
            }
            else {
                setupLogging(null);
            }
            logger.info("Command line arguments: " + Arrays.asList(args));
            final CoasterPersistentService s;
            if (!secure) {
                s = new CoasterPersistentService(false, port, bindTo);
            }
            else {
                s = new CoasterPersistentService(cred, port, bindTo);
            }
            s.setAuthorization(new SelfAuthorization());
            if (localport > 0) {
                s.initializeLocalService(localport);
            }
            else {
                s.initializeLocalService();
            }
            
            writePorts(s, portFile, localPortFile);
                        
            s.setIgnoreIdleTime(true);
            if (ap.isPresent("passive")) {
                if (ap.isPresent("shared")) {
                    System.err.println("You cannot specify both -shared with -passive. " +
                    		"Passive workers are automatically shared by clients.");
                    System.exit(3);
                }
                s.setDefaultQP("passive");
                s.setShared(true);
                passive = true;
            }
            else if (ap.isPresent("shared")) {
                s.setShared(true);
                AbstractSettings settings = loadSharedSettings(ap.getStringValue("shared"), s.getSharedQueue());
            }
            else {
                s.setDefaultQP("block");
            }
            if (ap.isPresent("controlPort")) {
                System.setProperty("tcp.channel.log.io.performance", "true");
            	s.setControlPort(ap.getIntValue("controlPort"));
            }
            s.start();
            addShutdownHook(s);
            System.out.println("Started coaster service: " + s);
            if (passive) {
                System.out.println("Worker connection URL: " + s.getLocalService().getContact());
            }
            if (ap.isPresent("stats")) {
            	disableConsoleLogging();
            	statusDisplay = new CPSStatusDisplay(passive);
            	statusDisplay.initScreen();
                Timer.every(1000, new Runnable() {
                    public void run() {
                    	statusDisplay.printStats(s);
                    }
                });
            }
            s.waitFor();
            System.exit(0);
        }
        catch (ArgumentParserException e) {
            System.err.println(e.getMessage());
            ap.usage();
            System.exit(1);
        }
        catch (GlobusCredentialException e) {
            System.err.println("Error loading credential: " + e.getMessage());
            logger.info("Error loading credential", e);
            System.exit(3);
        }
        catch (GSSException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("Error starting coaster service: " + e.getMessage());
            logger.info("Error starting coaster service", e);
            System.exit(2);
        }
    }
    
    private static AbstractSettings loadSharedSettings(String fileName, JobQueue jobQueue) throws IOException {
        Properties props = new Properties();
        FileReader r = new FileReader(fileName);
        props.load(r);
        r.close();
        
        jobQueue.initQueueProcessor(props.getProperty("workerManager"));
        
        AbstractSettings settings = jobQueue.getQueueProcessor().getSettings();
        for (String name : props.stringPropertyNames()) {
            settings.put(name, props.getProperty(name));
        }
        jobQueue.startQueueProcessor();
        return settings;
    }

    private static void disableConsoleLogging() {
    	ConsoleAppender ca = (ConsoleAppender) getAppender(ConsoleAppender.class);
        if (ca == null) {
            logger.warn("Failed to configure console log level");
        }
        else {
            ca.setThreshold(Level.WARN);
            ca.activateOptions();
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static Appender getAppender(Class cls) {
        Logger root = Logger.getRootLogger();
        Enumeration e = root.getAllAppenders();
        while (e.hasMoreElements()) {
            Appender a = (Appender) e.nextElement();
            if (cls.isAssignableFrom(a.getClass())) {
                return a;
            }
        }
        return null;
    }

    
    private static void addShutdownHook(final CoasterPersistentService s) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutting down service...");
            	if (stats) {
            		if (statusDisplay != null) {
            			statusDisplay.close();
            		}           		
            	}
                try {
                    s.shutdown();
                }
                catch (Exception e) {
                    logger.warn("Shutdown failed", e);
                }
            }
        });
    }

    static void setupLogging(String dir) {
        String timestamp = Timestamp.YMDhms_dash();
        String filename = (dir == null ? "" : dir + "/") + "cps-" + timestamp + ".log";
        logger.warn("Switching log to: " + filename);
        Misc.setFileAppenderOutput(logger, filename);
    }

    private static void writePorts(CoasterPersistentService s, String portFile, String localPortFile) throws IOException {
        writePort(s.getPort(), portFile);
        writePort(s.getLocalService().getPort(), localPortFile);
    }

    private static void writePort(int port, String f) throws IOException {
        if (f != null) {
            FileWriter fw = new FileWriter(f);
            fw.write(String.valueOf(port));
            fw.close();
        }
    }
}
