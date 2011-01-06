
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.examples.gcm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.CHDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.LSCommandImpl;
import org.globus.cog.gridface.impl.commands.PWDCommandImpl;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;

/**
 * this is a client application for the GridCommandManager. 
 * Tests file operation tasks. 
 */
public class GCMFileTest implements StatusListener {

    private GridCommandManager gcm;
    private String provider = null;
    private Identity sessionId = null;
    private String host = null;
    private String port = null;
    private String file = null;
    private String username = null;
    private String password = null;
    private boolean background = false;

    static Logger logger = Logger.getLogger(GCMFileTest.class.getName());

    public GCMFileTest(
        String provider,
        String host,
        String port,
        String file,
        String username,
        String password)
        throws Exception {
        gcm = new GridCommandManagerImpl();
        this.provider = provider;
        this.host = host;
        this.port = port;
        this.file = file;
        this.username = username;
        this.password = password;
    }

    public void executeOPEN() throws Exception {
//    	TODO put on hold for CoG 4 - GridFace integration, 
//        logger.debug(">>>Open connection<<<");
//        GridCommand command = new OPENCommandImpl();
//        command.setAttribute("provider", provider);
//
//        //Set up Service Contact
//        ServiceContact serviceContact = new ServiceContactImpl();
//        serviceContact.setIP(host);
//        serviceContact.setPort(port);
//        serviceContact.setProtocol(provider);
//        serviceContact.setURL(
//            provider + "://" + host + ":" + port + "/" + file);
//        logger.debug(serviceContact.getURL());
//        command.setAttribute("ServiceContact", serviceContact);
//
//        //Set up Security Context
//        SecurityContext securityContext =
//            SecurityContextFactory.newSecurityContext(provider);
//        securityContext.setCredentials(null);
//        securityContext.setAttribute("username", username);
//        securityContext.setAttribute("password", password);
//        command.setAttribute("SecurityContext", securityContext);
//
//        command.addStatusListener(this);
//        try {
//            gcm.execute(command, false);
//        } catch (Exception e) {
//            logger.debug(">>>Exception in gcmClient open<<<");
//            e.printStackTrace();
//        }
    }

    public void executeLS() {
        logger.debug(">>>LS<<<");
        if (sessionId != null) {
            GridCommand command = new LSCommandImpl();
            command.setAttribute("provider", provider);
            command.setAttribute("sessionid", sessionId);
            command.addStatusListener(this);
            try {
                gcm.execute(command, false);
            } catch (Exception e) {
                logger.debug(">>>Exception in gcmClient LS<<<");
                e.printStackTrace();
            }
            logger.debug(">>>LS completed<<<");
        } else {
            logger.debug(">>> No SessionId<<<");
        }
    }

    public void executeCHDIR() {
        logger.debug(">>CHDIR to Root directory<<<");
        if (sessionId != null) {
            GridCommand command = new CHDIRCommandImpl();
            command.setAttribute("provider", provider);
            command.setAttribute("sessionid", sessionId);
            command.addArgument("/");
            command.addStatusListener(this);
            try {
                gcm.execute(command, false);
            } catch (Exception e) {
                logger.debug(">>>Exception in gcmClient CHDIR<<<");
                e.printStackTrace();
            }
            logger.debug(">>>CHDIR completed<<<");
        } else {
            logger.debug(">>> No SessionId<<<");
        }
    }

    public void executePWD() {
        logger.debug(">>PWD<<<");
        if (sessionId != null) {
            GridCommand command = new PWDCommandImpl();
            command.setAttribute("provider", provider);
            command.setAttribute("sessionid", sessionId);
            command.addStatusListener(this);
            try {
                gcm.execute(command, false);
            } catch (Exception e) {
                logger.debug(">>>Exception in gcmClient PWD<<<");
                e.printStackTrace();
            }
            logger.debug(">>>PWD completed<<<");
        } else {
            logger.debug(">>> No SessionId<<<");
        }
    }

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
		GridCommand command = (GridCommand) event.getSource();
        logger.debug(command.getCommand() + "command status changed to " + status.getStatusCode());

        if ((status.getStatusCode() == Status.COMPLETED)) {
			logger.debug(command.getCommand() + "output");
            if (command.getCommand().equals("open")) {
                sessionId = (Identity) command.getOutput();
                logger.debug(sessionId);
            } else if (command.getCommand().equals("list")) {
                Enumeration e = (Enumeration) command.getOutput();
                while (e.hasMoreElements()) {
                    logger.debug(e.nextElement().toString());
                }
            } else {
            	if (command.getOutput() != null)
                logger.debug(command.getOutput().toString());
            }
        } else if ((status.getStatusCode() == Status.FAILED)) {
			if (command.getError() != null)
            logger.error(command.getError());
        }
    }

    public static void main(String[] args) {
        int option = 0;
        String response = null;
        String provider = "gridftp";
        String host = null;
        String port = null;
        String file = null;
        String username = null;
        String password = null;

        BufferedReader stdin =
            new BufferedReader(new InputStreamReader(System.in));

        logger.debug(
            "This application tests open, ls, pwd and chdir commands of a file resource using gcm. \n" +
            "Please press ^c to quit application when done" );
        //Get provider
        try {
            logger.debug(
                "Enter provider: [gridftp / ftp / webdav / local ] \n"
                    + "[ default: gridftp ] ");
            response = stdin.readLine();
            if (response != null && !response.equals("")) {
                provider = response;
            }
        } catch (Exception exception) {
            logger.fatal("Error in reading inputs. Please try again.",exception);
            System.exit(1);
        }

        //Set defaults        
        if (provider.equalsIgnoreCase("gridftp")) {
            host = "arbat.mcs.anl.gov";
            port = "6224";
        } else if (provider.equalsIgnoreCase("ftp")) {
            host = "ftp.mcs.anl.gov";
            port = "21";
            username = "anonymous";
            password = "";
        } else if (provider.equalsIgnoreCase("webdav")) {
        	provider = "http";
            host = "localhost";
            port = "8080";
            file = "slide";
            username = "root";
            password = "root";
        } else if (provider.equalsIgnoreCase("local")) {
        	provider = "file";
            host = "N/A";
            port = "0";
        } else {
            logger.debug("Invalid provider");
            System.exit(1);
        }

        try {
            // Obtain host
            logger.debug("Enter host: [default: " + host + "]");
            response = stdin.readLine();
            if ((response != null) && (!response.equals(""))) {
                host = response;
            }

            //Obtain port
            logger.debug("Enter port: [default: " + port + "]");
            response = stdin.readLine();
            if ((response != null) && (!response.equals(""))) {
                port = response;
            }

            if (provider.equalsIgnoreCase("http")) {
                //Obtain default location
                logger.debug("Enter file: [default: " + file + "]");
                response = stdin.readLine();
                if ((response != null) && (!response.equals(""))) {
                    file = response;
                }
            }

            if (provider.equalsIgnoreCase("ftp")
                || provider.equalsIgnoreCase("http")) {
                //Obtain username
                logger.debug("Enter username: [default: " + username + "]");
                response = stdin.readLine();
                if ((response != null) && (!response.equals(""))) {
                    username = response;
                }
                //Obtain password
                logger.debug("Enter password: [default: " + password + "]");
                response = stdin.readLine();
                if ((response != null) && (!response.equals(""))) {
                    password = response;
                }
            }
        } catch (Exception exception) {
            logger.fatal("Error in reading inputs. Please try again.",exception);
            System.exit(0);
        }
        
        try{
        	GCMFileTest gcmFileTest = new GCMFileTest(provider, host, port, file, username, password);
        	gcmFileTest.executeOPEN();
        	gcmFileTest.executeLS();
        	gcmFileTest.executePWD();
        	if (!provider.equalsIgnoreCase("http")){
				gcmFileTest.executeCHDIR();
				gcmFileTest.executePWD();
				gcmFileTest.executeLS();
        	}
        }catch(Exception exception){
			logger.fatal("Error in executing tasks",exception);
			System.exit(0);
        }
    }
}
