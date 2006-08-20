
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import java.net.URI;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.CHDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.GETDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.GETFILECommandImpl;
import org.globus.cog.gridface.impl.commands.ISDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.LSCommandImpl;
import org.globus.cog.gridface.impl.commands.MKDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.PUTDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.PUTFILECommandImpl;
import org.globus.cog.gridface.impl.commands.PWDCommandImpl;
import org.globus.cog.gridface.impl.commands.RMDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.RMFILECommandImpl;
import org.globus.cog.gridface.impl.commands.URLCOPYCommandImpl;
import org.globus.cog.gridface.impl.gcm.CommandRunner;
import org.globus.cog.gridface.interfaces.ConnectionObject;
import org.globus.cog.gridface.interfaces.FileTransferObject;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;


public class FileTransferObjectImpl implements FileTransferObject {
	private ConnectionObject connObject;
	private GridCommandManager gcm;
	private GridCommand command; 
	private Identity sessionId;
	private StatusListener statusListener;
	private String provider;
	
	static Logger logger = Logger.getLogger(FileTransferObjectImpl.class);
	
	public FileTransferObjectImpl(StatusListener statusListener, GridCommandManager gcm) {
		this.statusListener = statusListener;
		this.gcm = gcm;
		this.connObject = new ConnectionObjectImpl(statusListener, gcm);
	}
	
	
	private String removeFirstCharacter(String string) {
//		String returnString;
//		try {
//			returnString = string.substring(1);
//		} catch (StringIndexOutOfBoundsException e) {
//			returnString = "";
//		}
//		return returnString;
		return string;
	}
	
	public void execute(GridCommand command, boolean background) throws Exception {
		gcm.execute(command, background);
	}
	
	public void setSessionId(Identity sessionId) {
		this.sessionId = sessionId;
		connObject.setSessionId(sessionId);
	}
	
	public Identity getSessionId() {
		return this.sessionId;
	}
	
	public GridCommand getProperties(URI uri) {
		return null;
	}
	
	
	public GridCommand setProperties(URI uri) {
		return null;
	}
	
	
	public GridCommand thirdPartyCopyFile(URI uriSource, URI uriDestination) {
		System.out.println("thirdparty copy: " + uriSource + " " + uriDestination);
		GridCommand command = new URLCOPYCommandImpl();
		command.setAttribute("provider", "GT2");
		command.setAttribute("source", uriSource.toASCIIString());
		command.setAttribute("destination", uriDestination.toASCIIString());
		command.addStatusListener(statusListener);
		return command;
	}
	
	/**
	 * 
	 */
	public GridCommand getFile(URI uriRemote, URI uriLocal) {
		GridCommand command = new GETFILECommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addArgument(uriRemote.getPath());
		command.addArgument(uriLocal.getPath());
		command.addStatusListener(statusListener);
		return command;
	}
	
	
	/**
	 * 
	 */
	public GridCommand putFile(URI uriLocal, URI uriRemote){
		GridCommand command = new PUTFILECommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addArgument(uriLocal.getPath());
		command.addArgument(uriRemote.getPath());
		command.addStatusListener(statusListener);
		return command;
	}
	
	
	public GridCommand getDir(URI uriRemote, URI uriLocal) {
		GridCommand command = new GETDIRCommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addArgument(uriRemote.getPath());
		command.addArgument(uriLocal.getPath());
		command.addStatusListener(statusListener);
		return command;
	}
	
	public GridCommand putDir(URI uriLocal, URI uriRemote) {
		GridCommand command = new PUTDIRCommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addArgument(uriLocal.getPath());
		command.addArgument(uriRemote.getPath());
		command.addStatusListener(statusListener);
		return command;
	}
	
	/**
	 * 
	 */
	public GridCommand rmfile(URI uri) {
		GridCommand command = new RMFILECommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addArgument(removeFirstCharacter(uri.getPath()));
		command.addStatusListener(statusListener);
		return command;
	}
	
	/**
	 * 
	 */
	public GridCommand rmdir(URI uri) {
		GridCommand command = new RMDIRCommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addArgument(removeFirstCharacter(uri.getPath()));
		command.addArgument("true");
		command.addStatusListener(statusListener);
		return command;
	}
	
	public GridCommand setCurrentDirectory(URI uri) {
	    logger.debug("setCurrentDirectory( "+uri);
		GridCommand command = new CHDIRCommandImpl();
		command.setAttribute("sessionid",sessionId);
		command.setAttribute("provider", provider);
		
		//remove the first character which should be a slash.
		//this requires people to put another slash in if the server
		//they have logged them into hasn't put them in the root and they want to get at the root
		//this happens in gsiftp
		String path =  removeFirstCharacter(uri.getPath());
		if(path.equals(""))
			path = ".";
		command.addArgument(path);
		command.addStatusListener(statusListener);
		return command; 
	}
	
	/**
	 * Gets the directory we're currently working in.
	 * @return URI The URI of the current directory.
	 */
	public GridCommand getCurrentDirectory() {
		GridCommand command = new PWDCommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addStatusListener(statusListener);
		return command;
	}
	
	/**
	 * Creates the directory specified by the URI
	 * @param uri The URI of the directory we want to create.
	 */
	public GridCommand makeDirectory(URI uri) {
		GridCommand command = new MKDIRCommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addArgument(removeFirstCharacter(uri.getPath()));
		command.addStatusListener(statusListener);
		return command;
	}
	
	/** Returns an <code>Collection</code> of the current directory.
	 * @return Collection An <code>Collection</code> of type <code>FileInfo</code>
	 */
	public GridCommand ls() {
		logger.debug("Calling ls");
		command = new LSCommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addStatusListener(statusListener);
		return command;
	}
	
	
	/**
	 * Returns and <code>Collection</code> of the directory specified by the URI. 
	 * @param uri The URI whose directory listing we want.
	 * @return Collection An <code>Collection</code> of type <code>FileInfo</code>
	 */
	public GridCommand ls(URI uri) {
		command = new LSCommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addArgument(uri.getPath());
		command.addStatusListener(statusListener);
		return command;
	}
	
	public GridCommand exists(URI uri) {
		return null;
	}
	
	public GridCommand size(URI uri) {
		return null;
	}
	
	
	/**
	 * 
	 */
	public GridCommand isDirectory(URI uri) {
		command = new ISDIRCommandImpl();
		command.setAttribute("sessionid", sessionId);
		command.setAttribute("provider", provider);
		command.addArgument(removeFirstCharacter(uri.getPath()));
		command.addStatusListener(statusListener);
		return command;
	}
	
	///* Connection Object methods taken care of by connObject */
	public String getHost() {
		return connObject.getHost();
	}
	
	public void setHost(String host) {
		connObject.setHost(host);
	}
	
	public String getPort() {
		return connObject.getPort();
	}
	
	public void setPort(String port) {
		connObject.setPort(port);
	}
	
	public String getProtocol() {
		return connObject.getProtocol();
	}
	
	public void setProtocol(String protocol) {
		connObject.setProtocol(protocol);
	}
	
	public String getSecurityContext() {
		return connObject.getSecurityContext();
	}
	
	public void setSecurityContext(String port) {
		connObject.setSecurityContext(port);
	}
	
	public GridCommand connect(String protocol, String host, String port) throws Exception {
	    logger.info("connect");
		this.provider = protocol;
		return connObject.connect(protocol, host, port);
	}
	
	public GridCommand disConnect() {
		return connObject.disConnect();
	}
	
	public GridCommand reConnect() throws Exception {
		return connObject.reConnect();
	}
	
	public GridCommand close() {
		return connObject.close();
	}
	
	public void getProxy() {
		connObject.getProxy();
	}
	
	public void setProxy() {
		connObject.setProxy();
	}
	
	public void setUsername(String username) {
		connObject.setUsername(username);
	}
	
	public void setPassword(String password) {
		connObject.setPassword(password);
	}
}
