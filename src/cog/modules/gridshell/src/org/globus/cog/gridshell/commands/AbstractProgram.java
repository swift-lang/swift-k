/*
 * 
 */
package org.globus.cog.gridshell.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.globus.cog.gridshell.interfaces.Command;
import org.globus.cog.gridshell.interfaces.Program;

/**
 * 
 */
public abstract class AbstractProgram extends AbstractCommand implements Program {
	
	private Collection submittedCommands = new LinkedList();

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Program#getCommands()
	 */
	public Collection getCommands() {
		synchronized(submittedCommands) {
			return Collections.unmodifiableCollection(submittedCommands);
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Program#addCommand(org.globus.cog.gridface.impl.gridshell.interfaces.Command)
	 */
	public void addCommand(Command command) {
		synchronized(submittedCommands) {
			submittedCommands.add(command);
		}				
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Program#removeCommand(org.globus.cog.gridface.impl.gridshell.interfaces.Command)
	 */
	public void removeCommand(Command command) {
		synchronized(command) {
			submittedCommands.remove(command);
		}				
	}
}
