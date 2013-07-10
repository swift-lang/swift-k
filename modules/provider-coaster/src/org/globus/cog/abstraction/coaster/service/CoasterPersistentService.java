//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 3, 2010
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
                    "set the passive worker manager to be the default (otherwise the block allocator will be used)");
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

            setupLogging();
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
                s.setDefaultQP("passive");
                passive = true;
            }
            else {
                s.setDefaultQP("block");
            }
            s.start();
            addShutdownHook(s);
            System.out.println("Started coaster service: " + s);
            System.out.println("Worker connection URL: " + s.getLocalService().getContact());
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
            	if (stats) {
            		if (statusDisplay != null) {
            			statusDisplay.close();
            		}           		
            	}
                System.out.println("Shutting down service...");
                s.shutdown();
            }
        });
    }

    static void setupLogging() {
        String timestamp = Timestamp.YMDhms_dash();
        String filename =  "cps-"+timestamp+".log";        
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
