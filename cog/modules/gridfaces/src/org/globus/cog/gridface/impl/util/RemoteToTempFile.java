/*
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.gridface.impl.util;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Level;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.CLOSECommandImpl;
import org.globus.cog.gridface.impl.commands.GETFILECommandImpl;
import org.globus.cog.gridface.impl.commands.OPENCommandImpl;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;

/**
 * This class is a hack and demostrates that GridCommandImpl is not very reusable. In addition
 * this class needs some optimization to allow for if the user already has a connection.
 */
public class RemoteToTempFile {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RemoteToTempFile.class);
	
	public static class FinishListener implements StatusListener {
		private GridCommand task;
		private Action completed;
		private Action error;
		
		public final Action DEFAULT_ERROR = new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				RuntimeException exception = new RuntimeException(task.getStatus().getException());
				logger.error("DEFAULT_ERROR",exception);
				throw exception;
			}
		};
		
		public FinishListener (GridCommand task, Action completed) {
			this(task,completed,null);
		}
		
		public FinishListener (GridCommand task, Action completed, Action error) {
			if(error == null) {
				this.error = DEFAULT_ERROR;
			}else {
				this.error = error;
			}
			this.task = task;
			this.completed = completed;
			logger.debug("created finishlistener for "+task);
		}

		/* (non-Javadoc)
		 * @see org.globus.cog.abstraction.interfaces.StatusListener#statusChanged(org.globus.cog.abstraction.impl.common.StatusEvent)
		 */
		public void statusChanged(StatusEvent event) {
			logger.debug("status changed: " + event.getStatus().getStatusString());
			if(event.getStatus().getStatusCode() == Status.COMPLETED){
				completed.actionPerformed(null);
			}else if(event.getStatus().getStatusCode() == Status.FAILED) {
//				if(error == null) {
//					error = DEFAULT_ERROR;
//				}
				System.out.println(task);
				logger.debug("stderr"+task);
				logger.error("status excption",task.getStatus().getException());
				//error.actionPerformed(null);
			}else {
				logger.debug(event);
			}
		}		
	}
	

	
	/**
	 * Consists of open->get->close->completedAction
	 * 
	 * @param gcm
	 * @param fromURIString
	 * @param toURIString
	 * @param completedAction
	 * @param userName
	 * @param password
	 */
	public static void remoteToTemp(final GridCommandManager gcm,final String fromURIString, final String toURIString,
			final Action completedAction, String userName, String password) {
		logger.setLevel(Level.ALL);
		logger.debug("remoteToTemp "+fromURIString+","+toURIString);
		try {
			final URI fromURI = new URI(fromURIString);
			final URI toURI = new URI(toURIString);
			final String provider = fromURI.getScheme();
	        final String host = fromURI.getHost();
	        final String port = String.valueOf(fromURI.getPort());	        
	        final String path = fromURI.getPath();
	        
	        //////////////////////
	        // open
			final OPENCommandImpl connection = new OPENCommandImpl();	
			logger.debug("open");
			
			// set the provider for this gridcommand
	        connection.setAttribute("provider", provider);
	        
	        // set the service contact
	        ServiceContact serviceContact = new ServiceContactImpl();
	        logger.debug("serviceContact.host="+host);
			serviceContact.setHost(host);
			serviceContact.setPort(Integer.parseInt(port));
	        connection.setAttribute("ServiceContact", serviceContact);
	        
	        logger.debug("sevice conact done");
	        
            // If there is a username and password set them
            if( userName != null && password != null ) {
            	SecurityContext securityContext;
            	try {
            		logger.debug("creating securityContext");
    		  	
            		securityContext =
            			AbstractionFactory.newSecurityContext(provider);
    		  	
            		logger.debug("securityContext created");
    		  	
            		securityContext.setAttribute("username", userName);
            		securityContext.setAttribute("password", password);
            		connection.setAttribute("SecurityContext", securityContext);
            	} catch (Exception ex){
    		  			logger.error("error", ex);
            	}    		     		  
            }	
            logger.debug("securityContext done");
  		  	
            connection.setAttribute("machine", provider+"://"+host+":"+port+"/");
            connection.setAttribute("redirect","true");
	  		try {
				logger.debug("prepare fileoperationtask");
				connection.prepareFileOperationTask();
				// once open is done we want to get the file
				connection.addStatusListener(new FinishListener(connection,new AbstractAction() {
					public void actionPerformed(ActionEvent aEvent) {
						final Identity sessionId = (Identity) connection.getOutput();
						///////////////////////
						// get
						GETFILECommandImpl get = new GETFILECommandImpl();
						try {
							get.setAttribute("SessionId",sessionId);
							get.setAttribute("provider",provider);
							get.setAttribute("redirect","true");
							get.setAttribute("source",fromURIString);
							get.setAttribute("destination",toURIString);
							get.setArguments(new String[] {fromURI.getPath(),toURI.getPath()} );
						    
							get.prepareFileTransferTask();
							// once the file is gotten we want to close
							get.addStatusListener(new FinishListener(get,
									new AbstractAction() {
										public void actionPerformed(ActionEvent aEvent) {
											try {
												///////////////////////
												// close
												CLOSECommandImpl close = new CLOSECommandImpl();
												close.setAttribute("provider",provider);
												close.setAttribute("SessionId",sessionId);
												// once we are done call our completedAction we passed in
												close.addStatusListener(new FinishListener(close,completedAction));
												gcm.execute(close,true);
											}catch(Exception exception) {
												logger.error("error",exception);
											}
										}
									}
							));
							gcm.execute(get,true);
						} catch (Exception exception) {
							logger.error("error",exception);
						}	
					}
				}));
				logger.debug("running connection");
				gcm.execute(connection,false);
  		  } catch (Exception exception) {
				logger.error("error",exception);
  		  } 
		} catch (URISyntaxException exception){
			logger.error("Invalid url",exception);
		}		
	}
	
	public static void main(String[] args) {
		logger.setLevel(Level.ALL);
		
		class MyBool {
			Boolean bool = new Boolean(false);			
			
			public void set(boolean value) {
				if(value) {
					bool = Boolean.TRUE;
				}else {
					bool = Boolean.FALSE;
				}
			}			
			public boolean get() {
				return bool.booleanValue();
			}
		}
		
		final MyBool done = new MyBool();
		
		try {
			File tempFile = File.createTempFile("tempFile",".tmp");
			System.out.println(tempFile);
			remoteToTemp(new GridCommandManagerImpl(),"gsiftp://wiggum.mcs.anl.gov:2811//home/rwinch/indexer/a.txt",tempFile.toURI().toString(), new AbstractAction() {

				public void actionPerformed(ActionEvent aEvent) {
					System.out.println("done");
					done.set(true);
				}				
			}, null,null);
			
		}catch(Exception exception) {
			logger.error("error", exception);
		}
		
		while(!done.get()) {
			try {
				Thread.sleep(5000);
			}catch(Exception e) {
				
			}
		}
	}
}
