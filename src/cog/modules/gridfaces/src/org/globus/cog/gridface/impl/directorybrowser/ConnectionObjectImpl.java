
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.CLOSECommandImpl;
import org.globus.cog.gridface.impl.commands.OPENCommandImpl;
import org.globus.cog.gridface.interfaces.ConnectionObject;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;

public class ConnectionObjectImpl implements ConnectionObject {
	private static final Logger logger = Logger.getLogger(ConnectionObjectImpl.class);
	private GridCommandManager gcm;
	private int clientId;
	private String host;
	private String port;
	private String protocol;
	private String username;
	// TODO: password should be a char array, not a string
	// this is for security reasons since strings are immutable objects, a char[]
	// can be cleared out	
	private String password;
	private StatusListener statusListener;
	private Identity sessionId;
	
	public ConnectionObjectImpl(StatusListener statusListener, GridCommandManager gcm) {
		this.gcm = gcm;
		this.statusListener = statusListener;
	}
	
	/**
	 * A method that gets called internally if we have an invalid port.  Perhaps this should be 
	 * implemented further up the line at some point?
	 * @param protocol
	 * @return The default port for the provided protocol.
	 */
	private String getDefaultPort(String protocol) {
		if(protocol.equalsIgnoreCase("gsiftp")) {
			return "2811";
		} else if(protocol.equalsIgnoreCase("gridftp")) {
			return "2811";
		} else if(protocol.equalsIgnoreCase("ftp")) {
			return "21";
		} else {
			return "-1";
		}
	}
	
	public GridCommandManager getGcm() {
		return gcm;
	}

	public void setSessionId(Identity sessionId) {
		this.sessionId = sessionId;
	}
	
	public Identity getSessionId() {
		return this.sessionId;
	}
	
	public String getHost() {
		if(this.host==null)
			return "";
		return this.host;
	}


	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return this.port;
	}

	public void setPort(String port) {
		if(port.equals("-1"))
			port = this.getDefaultPort(this.getProtocol());
		this.port = port;
	}
	
	public String getProtocol() {
		return this.protocol;
	}
	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getSecurityContext() {
		return null;
	}

	public void setSecurityContext(String port) {
	}
	
/**
 * Connect to the specified server and port.
 * @param host The server to connect to.
 * @param port The port to connect to.
 * @throws Exception
 */
	public GridCommand connect(String protocol, String host, String port) throws Exception {	    
		logger.debug("protocol="+protocol+" host="+host+" port="+port);
		this.setProtocol(protocol);
		this.setHost(host);
		this.setPort(port);
		
		GridCommand command = new OPENCommandImpl();
		command.setAttribute("provider", this.getProtocol());

		//create and setup the servicecontact
		ServiceContact serviceContact = new ServiceContactImpl();
		
		//Cog 4 changes
		serviceContact.setHost(this.getHost());
		serviceContact.setPort(new Integer(this.getPort()).intValue());
//		serviceContact.setIP(this.getHost());
//        serviceContact.setPort(this.getPort());
		
		
        //serviceContact.setURL(this.getProtocol() + "://" + this.getHost() + ":" + this.getPort() + "/");
        //serviceContact.setURL("http://wiggum.mcs.anl.gov:9999/slide");
        command.setAttribute("ServiceContact", serviceContact);
        
        //Changed for CoG 4,  10/28/04
        //create and setup the securityconetext
//        SecurityContext securityContext =
//            SecurityContextFactory.newSecurityContext(this.getProtocol());
        SecurityContext securityContext = AbstractionFactory.newSecurityContext(this.getProtocol());     
        
        if(username != null && password != null) {
        	logger.info("using password authentication "+username+" password "+password);
        	securityContext.setCredentials(new PasswordAuthentication(username,password.toCharArray()));
        }else {
        	logger.info("null credentials");
        	securityContext.setCredentials(null);
        }
       
        
        command.setAttribute("SecurityContext", securityContext);
        
        command.addStatusListener(statusListener);
        logger.info("executing");
        gcm.execute(command, false);
        logger.info("done");
        return command;
	}

	public GridCommand disConnect() {
		GridCommand command = new CLOSECommandImpl();
		command.setAttribute("sessionId", this.getSessionId());
		command.setAttribute("provider", this.getProtocol());
		command.addStatusListener(statusListener);
		try {
			gcm.execute(command, true);
		} catch (Exception e) {
		}
		return command;
	}


	public GridCommand reConnect() throws Exception {
		return this.connect(this.getProtocol(), this.getHost(), this.getPort());
	}

	
	public GridCommand close() {
		return this.disConnect();
	}

	public void getProxy() {

	}

	public void setProxy() {

	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	
	public void setPassword(String password) {
		this.password =  password;
	}
}
