/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;

import javax.swing.Action;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.gridshell.commands.AbstractCommand;
import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.commands.AbstractShellProgram;
import org.globus.cog.gridshell.connectionmanager.ConnectionManager;
import org.globus.cog.gridshell.connectionmanager.ConnectionManagerImpl;
import org.globus.cog.gridshell.ctrl.AbstractActionBinder;
import org.globus.cog.gridshell.ctrl.GridShellImpl;
import org.globus.cog.gridshell.ctrl.ShellBinder;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.app.StorableImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.getopt.interfaces.Validator;
import org.globus.cog.gridshell.interfaces.Command;
import org.globus.cog.gridshell.interfaces.GridShell;
import org.globus.cog.gridshell.interfaces.GridShellApp;
import org.globus.cog.gridshell.interfaces.GridShellSwingGUI;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.GridShellAppImpl;
import org.globus.cog.gridshell.tasks.PwdTask;

/**
 * This is a controller portion of the shell in a MVC scheme
 * 
 * TODO: commands should be in a thread pool, not a new thread each time
 * 
 * 
 */
public class Gsh extends AbstractShellProgram implements GridShell {
	private static Logger logger = Logger.getLogger(Gsh.class);
	
	public final static String PARAM_createCommand_commandValue = "commandValue";
	
	private GridShellSwingGUI gui;
	private GridShellApp application;
	
	private GshEngine shEngine;
	
	private ConnectionManager connectionManager = new ConnectionManagerImpl();
	
	/**
	 * Provides our bindings
	 */
	private AbstractActionBinder actionBinder;
	
	private Action closeAction = GridShellImpl.DEFAULT_CLOSE;
	
    public final Validator FILE_VALIDATOR = new Validator() {
        public Object validate(String value) throws Exception {
            logger.debug("value="+value);
            if(value==null) {
                return null;
            }
            
            if(!value.startsWith("/")) {
                PwdTask p = new PwdTask(getConnectionManager().getDefaultConnection());
                p.initTask();
                String pwd = (String) p.submitAndWait();
                return new File(pwd,value);
            }
            return new File(value);
        }        
    };
    
	public Gsh() {
	    // add custom validators
	    StorableImpl.setValidator(File.class,FILE_VALIDATOR);
	}
	
	public ConnectionManager getConnectionManager() {
		return connectionManager;		
	}
	
	public Action getCloseAction() {
	    return closeAction;
	}
	
	/**
	 * Init the super class, then init ourself
	 * Requires AbstractActionBinder.init's arguments and
	 * gridShellSwingGUI->an instance of gridShellSwingGUI
	 */
	public Object init(Map args) throws Exception {
		// init the super class
		super.init(args);		
		// init ourself
		if(args.containsKey("closeAction")) {
		    closeAction = (Action)args.get("closeAction");
		}
		// get the close command
		
		// if there was a parent it was a program, must remove its bindings
		if(this.getParent() != null) {
			// this should be removing the bindings
			getParent().suspend();
		}		
		
		// the gui is where the user interacts (the view)
		String argName = "gridShellSwingGUI";
		if(args.containsKey(argName)) {
			this.gui = (GridShellSwingGUI)args.get(argName);
		}else if(this.gui == null){
			Object parent = getParent();
			if(parent != null && parent instanceof GridShell) {
				this.gui = ((GridShell)parent).getGUI();
			}else {
				throw new IllegalArgumentException("args must contain "+argName);
			}
		}
		
		// the application is the computer's view (application)
		if(application == null) {
			this.application = new GridShellAppImpl();
		}
						
		// init shEngine
		shEngine = new GshEngine();
		shEngine.init(this);
		
		// the action binder binds key events to this shell
		if(actionBinder == null) {
			actionBinder = new ShellBinder(this);
		}
   		application.addPropertyChangeListener(gui);
   		application.setPrompt(GridShell.DEFAULT_PROMPT);
		actionBinder.createBindings();
		
		return null;		
	}
	

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Program#createCommand(java.util.Map)
	 */
	public Command createCommand(Map args) throws Exception {		
		// return the result
		return shEngine.createCommand(args);
	}
	
	public void initCommand(AbstractShellCommand command, String commandEntered) throws Exception {
		shEngine.initCommand(command,commandEntered);
	}
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Program#runCommand(org.globus.cog.gridface.impl.gridshell.interfaces.Command)
	 */
	public void executeCommand(Command command) {
		AbstractShellCommand ashCommand = (AbstractShellCommand) command;
		if(ashCommand.getGetOpt().isOptionSet("help")) {
			this.getGUI().appendHistoryValue(Help.getOptToString(ashCommand.getGetOpt()));
			// add status (property change) listener
			command.addPropertyChangeListener(this);
			ashCommand.setStatusCompleted();
		}else {
			// check the required
			ashCommand.getGetOpt().checkRequired();
			
			// add to the list of commands we have ran
			this.addCommand(command);
			
			// add status (property change) listener
			command.addPropertyChangeListener(this);
			
			Thread cmdThread = new Thread(command);
			cmdThread.setPriority(Thread.NORM_PRIORITY);
			// run the command in new thread
			cmdThread.start();
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.interfaces.Program#createAndExecuteCommand(java.util.Map)
	 */
	public void createAndExecuteCommand(Map args) {
	    try {
		    String commandValue = (String)args.get("commandValue");
		    
		    // change the state
			processCommandState();
			
			// used to reset prompt and prepend to the output history value
			final String prompt = (getApplication().getPrompt() == null) ? "" : getApplication().getPrompt();
							
			// update the application
			getApplication().getShellHistory().appendHistory(commandValue);		
			// update the gui
			getGUI().appendHistoryValue(prompt+commandValue+"\n");
			getGUI().setCommandValue(prompt);
			
		    executeCommand(shEngine.createCommand(args));
	    }catch(Exception exception) {			
			logger.debug("Error",exception);
			appendErrorToHistory("Error",exception);
			acceptCommandState();
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		// this method executes gsh
		
		// TODO:
		// for now this is a do nothing method
		// later it will be in charge of executing each command
		// in a shell script
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#destroy()
	 */
	public Object destroy() throws Exception {
		// restore bindings
		actionBinder.restoreBindings();
		application.removePropertyChangeListener(gui);
		// TODO: save the shell history
		
		// TODO: save scope
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#suspend()
	 */
	public Object suspend() throws Exception {
		actionBinder.restoreBindings();
		application.removePropertyChangeListener(gui);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#resume()
	 */
	public Object resume() throws Exception {
		actionBinder.createBindings();
		application.addPropertyChangeListener(gui);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#kill()
	 */
	public Object kill() throws Exception {
		destroy();
		return null;
	}
	
	//////// utility methods ////////
	
	public void propertyChange(PropertyChangeEvent pcEvent) {
		logger.debug("propertyChange="+String.valueOf(pcEvent));
		if(AbstractCommand.PROPERTY_STATUS_CODE.equals(pcEvent.getPropertyName())) {
			handlePropertyStatusCode(pcEvent);
		}
	}
	
	private void handlePropertyStatusCode(PropertyChangeEvent pcEvent) {
		logger.debug("Status="+pcEvent);
		Command command = (Command)pcEvent.getSource();
		
		if(pcEvent == null) {
			return;
		}
		int STATUS_CODE = ((Integer)pcEvent.getNewValue()).intValue();
		if(STATUS_CODE == Status.FAILED) {
			String message = "Command "+command+" failed.";
			appendErrorToHistory(message,command.getStatus().getException());
			acceptCommandState();			
		}else if(STATUS_CODE == Status.COMPLETED) {
			Object result = command.getResult();
			if(result != null) {
				getGUI().appendHistoryValue(String.valueOf(result)+"\n");
			}			
			acceptCommandState();
		}
	}
	
	private void appendErrorToHistory(String message, Throwable thrown) {		
		logger.error(message,thrown);
		message += "\n";
		if(thrown != null) {
			message += "Cause: "+thrown.getMessage()+"\n";
		}
		message += getExceptionString(thrown)+"\n";
		getGUI().appendHistoryValue(message);
	}
	
	public static String getExceptionString(Throwable thrown) {
		if(thrown == null) { return null; }
		StringWriter writer = new StringWriter();
	    thrown.printStackTrace(new PrintWriter(writer));

		return writer.toString();
	}


	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShell#processCommandState()
	 */
	public void processCommandState() {
		JTextComponent command = getGUI().getCommandField();
		JTextComponent history = getGUI().getHistoryField();

		command.setEditable(false);
		command.setFocusable(false);

		final Cursor newCursor = GridShellSwingGUI.PROCESS_CURSOR;

		history.setCursor(newCursor);
		command.setCursor(newCursor);
		getGUI().getJComponent().setCursor(newCursor);
	}
	/*  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShell#acceptCommandState()
	 */
	public void acceptCommandState() {
		JTextComponent command = getGUI().getCommandField();
		JTextComponent history = getGUI().getHistoryField();

		command.setEditable(true);
		command.setFocusable(true);
		command.requestFocus(true);
		final Cursor newCursor = GridShellSwingGUI.ACCEPT_CURSOR;
		history.setCursor(newCursor);
		command.setCursor(newCursor);
		getGUI().getJComponent().setCursor(newCursor);
	}
	/* (non-Javadoc) 
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShell#getGUI()
	 */
	public GridShellSwingGUI getGUI() {
		return gui;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShell#getApplication()
	 */
	public GridShellApp getApplication() {		
		return application;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShell#setApplication(org.globus.cog.gridface.impl.gridshell.interfaces.GridShellApp)
	 */
	public void setApplication(GridShellApp nApp) {
		this.application = nApp;
	}
	
	
	/**
	 * A helper method so that errors are very carefully described. Only creats the command, still needs initialized.
	 * @param commandName
	 * @param commandEntered
	 * @return
	 * @throws Exception
	 */
	public AbstractShellCommand createCommand(String commandName) throws Exception {
		return shEngine.createCommand(commandName);
	}
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		return result;
	}
	
	/**
	 * Creates a getopt for given command, adds the help option, and adds the background argument
	 * @param command
	 * @param scope
	 * @return
	 */
	public static GetOpt createGetOpt(AbstractShellCommand command, Scope scope) {		
		return GshEngine.createGetOpt(command,scope);
	}
	/**
	 * Just a helper method to break things down
	 * @param commandEntered
	 * @return
	 */
	protected String getCommandName(String commandEntered) {		
		return shEngine.getCommandName(commandEntered);
	}
	
	public Collection getAvaialbeCommandNames() {
		return shEngine.getAvaialbeCommandNames();
	}
	public Scope getManPageMapping() {
		return shEngine.getManPageMapping();
	}
	public GshEngine getGshEngine() {
	    return shEngine;
	}
}
