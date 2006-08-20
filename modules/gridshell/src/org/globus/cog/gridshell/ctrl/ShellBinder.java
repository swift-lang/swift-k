/*
 * 
 */
package org.globus.cog.gridshell.ctrl;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.commands.AbstractCommand;
import org.globus.cog.gridshell.interfaces.GridShell;
import org.globus.cog.gridshell.interfaces.GridShellApp;
import org.globus.cog.gridshell.interfaces.GridShellSwingGUI;

/**
 * 
 */
public class ShellBinder extends AbstractActionBinder {
	private static Logger logger = Logger.getLogger(ShellBinder.class);
	
	protected GridShell gridShell;
	
	protected GridShellSwingGUI gui;
	protected GridShellApp application;
	
	// String keys for the key bindings
	protected static final String commandEntered = "command-entered";
	protected static final String getDecHistory = "up-pressed";
	protected static final String getIncHistory = "down-pressed";
		
	/* actions for the key bindings */
	// used to signal a command has been entered
	protected Action commandEnteredAction = new AbstractAction() {
  	  	public void actionPerformed(ActionEvent e) {
  	  	  String prompt = getApplication().getPrompt();
		  String commandFieldValue = getGUI().getCommandValue();
		  String commandValue = commandFieldValue;
		  if(prompt != null) {
		  	commandValue = commandFieldValue.replaceFirst(prompt,"");
		  }
		  enterPressed(commandValue);
   	  	}
   	}; 
   	// used to map getting the previous history
   	protected Action getDecHistoryAction = new AbstractAction() {
  	  	public void actionPerformed(ActionEvent e) {
  		  String prompt = getApplication().getPrompt();
		  boolean isNewValue = getApplication().getShellHistory().decPeekIndex();
		  Object peek = getApplication().getShellHistory().peek();
		  
		  if(isNewValue) {
		    String peekValue;
		    if(peek != null) {
		      peekValue = prompt+peek.toString();
		    }else {
		  	  peekValue = prompt;
		    }
		    getGUI().setCommandValue(peekValue);
		  }else {
		  	Toolkit.getDefaultToolkit().beep();
		  }
   	  	}
   	};
   	// used to map when incrementing the history
   	protected Action getIncHistoryAction = new AbstractAction() {
  	  	public void actionPerformed(ActionEvent e) {
  		  String prompt = getApplication().getPrompt();
		  boolean isNewValue = getApplication().getShellHistory().incPeekIndex();
		  Object peek = getApplication().getShellHistory().peek();

		  if(isNewValue) {
		    String peekValue;
		    if(peek != null) {
		      peekValue = prompt+peek.toString();
		    }else {
		  	  peekValue = prompt;
		    }
		    getGUI().setCommandValue(peekValue);
		  }else {
		  	Toolkit.getDefaultToolkit().beep();
		  }
   	  	}
   	};
	
    
   	public ShellBinder(GridShell gridShell) {
   		this.gridShell = gridShell;
   		
   		gui = gridShell.getGUI();
   		application = gridShell.getApplication();
   	}
	
	public void createBindings() {
		JComponent mappedComponent = getGUI().getCommandField();
		
		// add bindings
		addMapping(mappedComponent,KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),commandEntered,this.commandEnteredAction);
		addMapping(mappedComponent,KeyStroke.getKeyStroke(KeyEvent.VK_UP,0),getDecHistory,this.getDecHistoryAction);
		addMapping(mappedComponent,KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0),getIncHistory,this.getIncHistoryAction);
	}
	
	/** 
	 * @param commandValue - commandLine minus the prompt value
	 * @return
	 */
	public Object enterPressed(String commandValue) {
		logger.info("enterPressed");
		
		gridShell.createAndExecuteCommand(
				AbstractCommand.arrayToMap(
						new Object[] {
								"commandValue",commandValue
				})
		);
		return null;
	}
	
	public Object upKeyPressed() {
		return null;
	}
	

	public GridShellSwingGUI getGUI() {
		return gui;
	}
    public GridShellApp getApplication() {
    	return application;
    }
}
