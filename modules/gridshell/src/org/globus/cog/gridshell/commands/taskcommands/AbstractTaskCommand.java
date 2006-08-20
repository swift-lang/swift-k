/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import java.beans.PropertyChangeEvent;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphHandlerImpl;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.interfaces.TaskGraphHandler;
import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.connectionmanager.ConnectionManager;
import org.globus.cog.gridshell.tasks.AbstractTask;
import org.globus.cog.gridshell.tasks.StartTask;

/**
 * An abstract class that is used for GridShell commands that use tasks from abstractions
 * 
 */
public abstract class AbstractTaskCommand extends AbstractShellCommand implements StatusListener {
    private class GraphOutputStatusListener implements StatusListener {
	    public void statusChanged(StatusEvent event) {
			int statusCode = event.getStatus().getStatusCode();
			logger.debug("outputListener->source="+event.getSource()+" statusString="+event.getStatus().getStatusString());
			AbstractTask t = (AbstractTask) event.getSource();
			String provider = t.getProvider();
			String host = t.getServiceContact();
			if(statusCode == Status.COMPLETED) {
			    synchronized(bufferedResult) {
				    appendResult(provider);
				    appendResult("://");
				    appendResult(host);
				    appendResult(EOL);
				    appendResult(t.getResult());				    
			    }
			}
		}
	}
	
	private StatusListener graphOutputStatusListener = new GraphOutputStatusListener();
	private static Logger logger = Logger.getLogger(AbstractTaskCommand.class);
	private static final String EOL = System.getProperty("line.separator");
	
	private TaskGraph taskGraph = new TaskGraphImpl();
	private TaskGraphHandler handler = new TaskGraphHandlerImpl();
	
	private AbstractTask task;
	
	protected StringBuffer bufferedResult = new StringBuffer();
	
	/**
	 * Gets the output for this command, allows the command to format
	 * the output (ie the ls command)
	 * @return
	 */
	abstract public Object getTaskOutput();
	
	public void appendResult(Object value) {
	    synchronized(bufferedResult) {
	        bufferedResult.append(value);
	    }
	}
	
	public void setTask(AbstractTask task) {
	    this.task = task;    
	}	
	public void addTask(AbstractTask task) {
		try {
		    task.addStatusListener(graphOutputStatusListener);		    
		    task.initTask();
            this.taskGraph.add(task);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't add task",e);
        }
	}
	public StartTask getConnection() {
	    return getConnectionManager().getCurrentConnection();
	}
	
	public ConnectionManager getConnectionManager() {
		return getGsh().getConnectionManager();
	}
	public AbstractTask getTask() {
	    return task;
	}
	
	public TaskGraph getTaskGraph() {
		return taskGraph;
	}	
	public Object execute() throws Exception {
	    if(getTask()==null) {
	        logger.debug("doing taskgraph");
	        getTaskGraph().addStatusListener(this);
	        handler.submit(taskGraph);
	    }else {
	        logger.debug("doing task");
	        getTask().addStatusListener(this);
	        getTask().initTask();	        
	        getTask().submitTask();
	    }
		return null;
	}
	public void statusChanged(StatusEvent event) {
		int statusCode = event.getStatus().getStatusCode();
		logger.debug("source="+event.getSource()+" statusString="+event.getStatus().getStatusString());
		
		if(statusCode == Status.COMPLETED) {
		    setResult(getTaskOutput());
			this.setStatusCompleted();
		}else if(statusCode == Status.CANCELED) {
			this.setStatusFailed("Task was canceld");
		}else if(statusCode == Status.FAILED) {			
			Exception thrown = event.getStatus().getException();			
			String message = event.getStatus().getMessage();
			// fire a status failed event
			this.setStatusFailed("Task failed: "+message,thrown);
		}
	}
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent arg0) {
		// do nothing		
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.interfaces.Command#destroy()
	 */
	public Object destroy() throws Exception {
		// TODO this cleans up the command, nothing really to do
		return null;
	}
}
