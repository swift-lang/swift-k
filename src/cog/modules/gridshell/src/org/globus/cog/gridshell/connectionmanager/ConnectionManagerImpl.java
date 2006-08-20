/*
 * 
 */
package org.globus.cog.gridshell.connectionmanager;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.tasks.StartTask;

/**
 * 
 */
public class ConnectionManagerImpl implements ConnectionManager {
	private static final Logger logger = Logger.getLogger(ConnectionManagerImpl.class);
	
	private LinkedList connections = new LinkedList();
	
	private StartTask DEFAULT_CONNECTION;
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.connectionmanager.ConnectionManager#getDefaultConnection()
	 */
	public StartTask getDefaultConnection() {		
		return getDefault();
	}
	
	public StartTask getDefault() {
	    if(DEFAULT_CONNECTION == null) {
			try {
				DEFAULT_CONNECTION = new StartTask(null,"file",null,-1);
				DEFAULT_CONNECTION.initTask();
				DEFAULT_CONNECTION.submitAndWait();
			} catch (Exception e) {
				logger.debug("Couldn't init DEFAULT_CONNECTION",e);
			}
		}
		return DEFAULT_CONNECTION;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.connectionmanager.ConnectionManager#push(org.globus.cog.gridshell.commands.taskcommand.tasks.StartTask)
	 */
	public void push(StartTask connection) {
		connections.addLast(connection);
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.connectionmanager.ConnectionManager#pop()
	 */
	public StartTask pop() {
		return (StartTask)connections.removeLast();
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.connectionmanager.ConnectionManager#getCurrentConnection()
	 */
	public StartTask getCurrentConnection() {
		if(!isCurrentDefaultConnection()) {
			return (StartTask)connections.getLast();
		}else {
			return getDefaultConnection();
		}
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.connectionmanager.ConnectionManager#isCurrentDefaultConnection()
	 */
	public boolean isCurrentDefaultConnection() {
	    return connections.isEmpty();
	}
	
	public Collection getConnections() {		
		return Collections.unmodifiableCollection(connections);
	}
}
