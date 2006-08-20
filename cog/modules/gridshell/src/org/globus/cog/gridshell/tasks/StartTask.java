/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * A task to start, open, a connection
 * 
 * 
 */
public class StartTask extends AbstractFileTask implements StatusListener {
	private static final Logger logger = Logger.getLogger(StartTask.class);
	
	private Object credentials;
	private String provider;
	private String serviceContact;
	private TaskHandler handler;
	private int port;
	
	private Identity sessionId;
	/**
	 * Creates a task that provides a connection
	 * @param credentials
	 * @param provider
	 * @param serviceContact
	 * @param port
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public StartTask(Object credentials,String provider,String serviceContact, int port) 
			throws InvalidProviderException, ProviderMethodException {
		super(null);
		logger.debug("Start("+credentials+","+provider+","+serviceContact+","+port+")");
		this.credentials = credentials;
		this.provider = provider;
		this.serviceContact = serviceContact;
		this.port = port;
		this.handler = createTaskHandler();
	}
	
	public void initTask() 
			throws InvalidProviderException, ProviderMethodException {
		super.initTask();
		
		// Init for specifically start
		
		Task task = this;		
        Service service = new ServiceImpl(Service.FILE_OPERATION);
        service.setProvider(getProvider().toLowerCase());

        SecurityContext securityContext = AbstractionFactory
                .newSecurityContext(getProvider());
        securityContext.setCredentials(credentials);
        service.setSecurityContext(securityContext);

        ServiceContact sc = new ServiceContactImpl(serviceContact);
        sc.setPort(port);
        service.setServiceContact(sc);

        task.addService(service);
        
		this.addStatusListener(this);
	}
			
    public void statusChanged(StatusEvent event) {
    	logger.debug("statusChanged( "+event+" )");
        Status status = event.getStatus();
        if(status.getStatusCode() == Status.COMPLETED) {        	
        	sessionId = (Identity)this.getAttribute("output");
        	logger.debug("sessionId="+sessionId);
        }
    }
    
	/*  (non-Javadoc)
	 * @see org.globus.cog.abstraction.interfaces.Task#getProvider()
	 */
	public String getProvider() {
		return provider;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {		
		return FileOperationSpecification.START;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getServiceContact()
	 */
	public String getServiceContact() {
		return this.serviceContact;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getSessionId()
	 */
	public Identity getSessionId() {
		return this.sessionId;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getTaskHandler()
	 */
	public TaskHandler getTaskHandler() {
		return handler;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getPort()
	 */
	public int getPort() {
		return this.port;
	}
}