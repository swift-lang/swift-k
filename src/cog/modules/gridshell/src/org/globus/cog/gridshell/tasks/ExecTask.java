/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * 
 */
public class ExecTask extends AbstractTask {
    
    private Object credentials;
    private String provider;
	private String serviceContact;
	private TaskHandler handler;
	private int port;
	
	private String executable;
	private String arguments;
	private boolean isBatch = false;
	private boolean isRedirected = true;
	private String stdOut = null;
	private String stdErr = null;
    
	public ExecTask(String provider,String serviceContact,int port,String executable,String arguments) 
			throws InvalidProviderException, ProviderMethodException {
	    this("DEFAULT_TASK_NAME",null,provider,serviceContact,port,executable,arguments);
	}
    public ExecTask(String taskName, Object credentials,String provider,String serviceContact,int port,String executable,String arguments) 
    		throws InvalidProviderException, ProviderMethodException {
        super(taskName,Task.JOB_SUBMISSION);
        this.credentials = credentials;
        this.provider = provider;
        this.serviceContact = serviceContact;
        this.port = port;
        this.executable = executable;
        this.arguments = arguments;
        this.handler = AbstractionFactory.newExecutionTaskHandler(getProvider());
    }
    
    public void isBatch(boolean value) {
        this.isBatch = value;
    }
    public void isRedirected(boolean value) {
        this.isRedirected = value;
    }
    public void setStdOut(String value) {
        this.stdOut = value;
    }
    public void setStdErr(String value) {
        this.stdErr = value;
    }
    
    public void initTask() throws InvalidProviderException, ProviderMethodException {
        super.initTask();      
		
		Task task = this;
		JobSpecification spec = new JobSpecificationImpl();
		spec.setExecutable(executable);
		if(arguments!=null) {
		    spec.setArguments(arguments);
		}
		spec.setRedirected(isRedirected);
		spec.setStdOutput(stdOut);
		spec.setStdError(stdErr);
		task.setSpecification(spec);

        Service service = new ServiceImpl(Service.JOB_SUBMISSION);
        service.setProvider(getProvider().toLowerCase());

        SecurityContext securityContext = AbstractionFactory
                .newSecurityContext(getProvider());
        securityContext.setCredentials(credentials);
        service.setSecurityContext(securityContext);

        ServiceContact sc = new ServiceContactImpl(serviceContact);
        sc.setPort(port);
        service.setServiceContact(sc);

        task.addService(service);
        
    }
    public Object getResult() {
        return this.getStdOutput();
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.commands.taskcommand.tasks.AbstractTask#getTaskHandler()
     */
    public TaskHandler getTaskHandler() {
        return handler;
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.commands.taskcommand.tasks.AbstractTask#getProvider()
     */
    public String getProvider() {
        return provider;
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.commands.taskcommand.tasks.AbstractTask#getServiceContact()
     */
    public String getServiceContact() {
        return serviceContact;
    }

}
