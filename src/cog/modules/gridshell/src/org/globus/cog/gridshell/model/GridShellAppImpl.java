/*
 * An implementation of the application of GridShell
 */
package org.globus.cog.gridshell.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.interfaces.GridShellApp;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.interfaces.ShellHistory;

public class GridShellAppImpl implements GridShellApp {
    // the default history size
	public static final int DEFAULT_HISTORY_SIZE = 50;
	// The logger for this class
	private static Logger logger = Logger.getLogger(GridShellAppImpl.class);
	// property change listeners
	private PropertyChangeSupport pcListeners;
	// the prompt
	private String prompt;
	// the history for the shell
	private ShellHistory shellHistory;

	public GridShellAppImpl() {
		init();
	}
	private void init() {
		setShellHistory(new ShellHistoryImpl());
		pcListeners = new PropertyChangeSupport(this);
	}
		
	
	private void writeObject(ObjectOutputStream s) throws IOException {
	    Map app = new HashMap();
	    app.put("prompt",getPrompt());
	    app.put("shellHistory",getShellHistory());
	    s.writeObject(app);
	    
		s.flush();
	}
	
	private void readObject(ObjectInputStream s) throws IOException {
		init();
		try {
			Map app = (Map)s.readObject();
			String newPrompt = (String)app.get("prompt");
			ShellHistory newHistory = (ShellHistory)app.get("shellHistory");
			
			this.setPrompt(newPrompt);
			this.setShellHistory(newHistory);
		}catch(Exception exception) {
			logger.error("Failed to load object",exception);
		}
	}
		
	/**
	 * Sets the shell history
	 * @param nShellHistory - the new value of shell history
	 */
	private void setShellHistory(ShellHistory nShellHistory) {
		this.shellHistory = nShellHistory;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellApp#getScope()
	 */
	public Scope getScope() {
		// TODO: have actual scope
		Scope result = new ScopeImpl();
		try {
			result.setVariableTo("test","hello world");
		} catch (ScopeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellApp#getShellHistory()
	 */
	public synchronized ShellHistory getShellHistory() {
		return shellHistory;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellApp#getPrompt()
	 */
	public synchronized String getPrompt() {
		return this.prompt;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellApp#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener pListener) {
		if(pcListeners == null) {
			pcListeners = new PropertyChangeSupport(this);
		}
		pcListeners.addPropertyChangeListener(pListener);
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellApp#setPrompt(java.lang.String)
	 */
	public synchronized void setPrompt(String value) {
		String oldValue = this.getPrompt();
		this.prompt = value;		
		pcListeners.firePropertyChange(new PropertyChangeEvent(this,"prompt",oldValue,getPrompt()));
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellApp#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener pListener) {
		if(pcListeners == null) {
			return;
		}
		pcListeners.removePropertyChangeListener(pListener);
	}
}