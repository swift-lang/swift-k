/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.FileOperationSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * This class is any sort of file operation
 * 
 * 
 */
public abstract class AbstractFileTask extends AbstractTask {
	private static final Logger logger = Logger.getLogger(AbstractFileTask.class);
	private String[] arguments;

	public abstract String getOperation();
	public abstract Identity getSessionId();
	public abstract int getPort();
	
	public AbstractFileTask(String[] arguments) 
			throws InvalidProviderException, ProviderMethodException {
		this("DEFAULT_TASK_NAME",arguments);
    }
	
	public AbstractFileTask(String taskName,String[] arguments) 
			throws InvalidProviderException, ProviderMethodException {
		super(taskName, Task.FILE_OPERATION);
		setArguments(arguments);
	}
	
	public TaskHandler createTaskHandler() 
			throws InvalidProviderException, ProviderMethodException {
		return AbstractionFactory.newFileOperationTaskHandler(getProvider());
	}
	
    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        logger.debug("arguments.length="
                + ((arguments != null) ? arguments.length : 0));
        this.arguments = arguments;
    }
	public void initTask() throws InvalidProviderException, ProviderMethodException {
	    super.initTask();
	    
		String[] arguments = getArguments();
		String operation = getOperation();
				
		Task task = this;
        FileOperationSpecification specification = new FileOperationSpecificationImpl();
        specification.setOperation(operation);
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
            	logger.debug("adding arg="+arguments[i]);
                specification.addArgument(arguments[i]);
            }
        }
        task.setSpecification(specification);
	}
	public Object getResult() {
	    return this.getAttribute("output");
	}
}
