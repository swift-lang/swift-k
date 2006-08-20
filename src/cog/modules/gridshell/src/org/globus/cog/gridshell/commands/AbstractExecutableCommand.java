/*
 * 
 */
package org.globus.cog.gridshell.commands;

import java.beans.PropertyChangeEvent;

/**
 * There are lots of methods to a command. At times we may only want to implement
 * the execute method. This Class only leaves out the execute method.
 * All methods not in AbstractCommand throw unsupported exception, the implementation
 * may override this, but should not call the superclass
 * 
 */
public abstract class AbstractExecutableCommand extends AbstractCommand {

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#suspend()
	 */
	public Object suspend() throws Exception {
		unsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#resume()
	 */
	public Object resume() throws Exception {
		unsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#kill()
	 */
	public Object kill() throws Exception {
		unsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#destroy()
	 */
	public Object destroy() throws Exception {
		unsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		unsupported();
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent arg0) {
		unsupported();
	}

}