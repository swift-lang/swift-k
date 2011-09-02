//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 3, 2010
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.Logger;
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

    public CoasterPersistentService() throws IOException {
        super(false);
    }

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
        ap.addOption("localport", "Specifies which port to start the local service on",
            "localport", ArgumentParser.OPTIONAL);
        ap.addAlias("port", "p");
        ap.addFlag("nosec", "Disables GSI security and uses plain TCP sockets instead");
        ap.addOption("proxy",
            "Specifies the location of a proxy credential that will be used for authentication. " +
                    "If not specified, the default proxy will be used.",
            "file", ArgumentParser.OPTIONAL);
        ap.addFlag("local", "Binds the service to the loopback interface");
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
                port = ap.getIntValue("port");
            }
            int localport = 0;
            if (ap.hasValue("localport")) {
                localport = ap.getIntValue("localport");
            }

            setupLogging();
            
            CoasterPersistentService s;
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
            s.setIgnoreIdleTime(true);
            s.start();
            System.out.println("Started coaster service: " + s);
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
    
    static void setupLogging() {
        String timestamp = Timestamp.YMDhms_dash();
        String filename =  "cps-"+timestamp+".log";        
        logger.warn("Switching log to: " + filename);
        Misc.setFileAppenderOutput(logger, filename);
    }
}
