/*
 * 
 */
package org.globus.cog.gridshell.commands;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.gridshell.interfaces.Command;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeImpl;

/**
 * This class is an abstract implementation of a command
 * 
 */
public abstract  class AbstractCommand implements Command, PropertyChangeListener {
	private static final Logger logger = Logger.getLogger(AbstractCommand.class);
	
	/* a property that can be listened to */
	public static final String PROPERTY_STATUS_CODE = "statusCode";	
	/* the variable name to store the result in for this scope */
	private String resultVariableName = null;
	/* the result of this command upon command completion */
	private Object result = null;
	/* property change listeners */
	private PropertyChangeSupport pcListeners = new PropertyChangeSupport(this);
	/* the status of this command */
	private Status status = new StatusImpl();
	/* the scope of this command */
	private Scope scope = new ScopeImpl();
	/* the identity of this command */
	private Identity identity = new IdentityImpl();
	/* the parent of this command */
	private Command parent = null;	
	
	/**
	 * Accepts the arguments:
	 * Scope scope - the scope of this command
	 * Command parent - the parent of this command
	 * String resultVariableName - the variable in this scope to store a result by default
	 */
	public Object init(Map args) throws Exception {
		if(args != null) {
			String varName = "scope";
			if(args.containsKey(varName)) {
				scope = (Scope) args.get(varName);
				args.remove(varName);
			}
			varName = "parent";
			if(args.containsKey(varName)) {
				parent = (Command) args.get(varName);
				args.remove(varName);
			}
			
			// TODO: must go in different class
			varName = "resultVariableName";
			if(args.containsKey(varName)) {
				resultVariableName = (String) args.get(varName);
				args.remove(varName);
			}
		}
		return null;
	}
	
	protected void setResult(Object result) {
		logger.debug("result="+result);
		this.result = result;
	}
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#result()
	 */
	public Object getResult() {
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#getScope()
	 */
	public Scope getScope() {
		synchronized (scope) {
			return scope;
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#getIdentity()
	 */
	public Identity getIdentity() {
		synchronized(identity) {
			return identity;
		}
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#getParent()
	 */
	public Command getParent() {
		if(parent != null) {
			synchronized(parent) {
				return parent;				
			}
		}else {
			return parent;
		}		
	}
	
	/**
	 * A helper method that calls this.setStatusCode(Status.COMPLETED);
	 */
	public final void setStatusCompleted() {
		setStatusCode(Status.COMPLETED);
	}
	
	/*  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#setStatus(org.globus.cog.abstraction.interfaces.Status)
	 */
	public final void setStatusCode(int value) {
		logger.debug("setStatusCode( "+value+" )");
		synchronized(status) {
			// TODO: at times get status failed twice (from a task)
			// for now just set old value to null and it will fire everytime
			// if old value and new value are the same, it will not fire!!
			Integer oldValue = null; // new Integer(status.getStatusCode());
			status.setStatusCode(value);
			logger.debug("pcListener.size="+pcListeners.getPropertyChangeListeners().length);
			pcListeners.firePropertyChange(PROPERTY_STATUS_CODE,oldValue,new Integer(value));
		}		
	}
	
	public final void setStatusFailed(String message) {
		setStatusFailed(message,new CommandException(message,getStatus()));
	}
	
	public final void setStatusFailed(String message, Exception thrown) {
		logger.debug("FAILED: "+message,thrown);
		status.setException(thrown);
		status.setMessage(message);
		setStatusCode(Status.FAILED);
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#getStatus()
	 */
	public final Status getStatus() {
		synchronized (status) {
			return immutableStatusCode(status);
		}
	}
	/**
	 * Create a Status that the status code cannot be modified, 
	 * so that property change events are correct.
	 * 
	 * @param status
	 * @return
	 */
	private Status immutableStatusCode(final Status status) {
		Status result = new Status() {
			private void unmodifiable() {
				throw new CommandException("Can't modify status through getStatus()",this);
			}			
			public void setStatusCode(int status) {
				unmodifiable();
			}

			public int getStatusCode() {				
				return status.getStatusCode();
			}

			public String getStatusString() {
				return status.getStatusString();
			}

			public void setPrevStatusCode(int status) {
				unmodifiable();
			}
			public int getPrevStatusCode() {				
				return status.getPrevStatusCode();
			}
			public String getPrevStatusString() {				
				return status.getPrevStatusString();
			}
			public void setException(Exception exception) {
				status.setException(exception);
			}
			public Exception getException() {
				return status.getException();
			}
			public void setMessage(String message) {
				status.setMessage(message);
			}
			public String getMessage() {
				return status.getMessage();
			}
			public void setTime(Date time) {
				status.setTime(time);
			}
			public Date getTime() {
				return status.getTime();
			}
			public boolean isTerminal() {
				return status.isTerminal();
			}			
		};
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.PropertyChangeNotifier#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public final void addPropertyChangeListener(PropertyChangeListener pcListener) {
		logger.debug("addPropertyChangeListener( "+pcListener+" )");
		synchronized(pcListeners) {
			pcListeners.addPropertyChangeListener(pcListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.PropertyChangeNotifier#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public final void addPropertyChangeListener(String propertyName, PropertyChangeListener pcListener) {
		synchronized(pcListeners) {
			pcListeners.addPropertyChangeListener(propertyName,pcListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.PropertyChangeNotifier#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public final void removePropertyChangeListener(PropertyChangeListener pcListener) {
		logger.debug("removePropertyChangeListener( "+pcListener+" )");
		synchronized(pcListeners) {
			pcListeners.removePropertyChangeListener(pcListener);
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object thatObj) {
		Command that = (Command)thatObj;
		return getIdentity().toString().compareTo(that.getIdentity().toString());
	}
	
	public static Map arrayToMap(Object[] array) {
		if(array == null) {
			return null;
		}else if(array.length % 2 != 0) {
			throw new IllegalArgumentException("array must contain an even number of entries: {key_1,value_1...key_n,value_n}");
		}
		Map result = new HashMap();
		Object key = null;
		Object value = null;
		for(int i=0;i<array.length;i++) {
			if(i%2==0) {
				key = array[i];
			}else {
				value = array[i];
				result.put(key,value);
				key = null;
				value = null;
			}
		}
		return result;
	}
	protected void unsupported() {
		throw new CommandException("Unsupported method",getStatus());
	}
}
