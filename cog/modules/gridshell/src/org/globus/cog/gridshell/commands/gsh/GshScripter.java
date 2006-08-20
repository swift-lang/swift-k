/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Command;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.util.TextFileLoader;

/**
 * 
 */
public class GshScripter extends AbstractShellCommand {
    private static final String EOL = "\n";//System.getProperty("line.separator");
    private static final Logger logger = Logger.getLogger(GshScripter.class);
    
    private String[] commands;

    private int commandIndex = 0;
        
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#createGetOpt()
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);		
		result.isAllowDynamicArgs(false);
		result.addArgument(new ArgumentImpl("A file to input",File.class,true));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
	    File inputFileName =(File) getGetOpt().getArgumentAt(1).getValue();
	    TextFileLoader shellScript = new TextFileLoader();
		String content = shellScript.loadFromFile(inputFileName.getAbsolutePath());
		commands = content.split(EOL);
        submitNextCommand();		
		return null;
	}
	
	public void submitNextCommand() {
	    Map args = new HashMap();
	    if(commands!=null && commandIndex<commands.length) {
	        String commandValue = commands[commandIndex++];
	        logger.debug("submitting {{{"+commandValue+"}}}");
	        args.put(Gsh.PARAM_createCommand_commandValue,commandValue);
	        try {
	            getGsh().processCommandState();
		        Command cmd = getGsh().createCommand(args);
		        if(isEchoOn()) {
		            cmd.addPropertyChangeListener(getGsh());
		        }
				cmd.addPropertyChangeListener(this);
				cmd.execute();
	        }catch(Exception e) {
	            throw new RuntimeException("Error",e);
	        }
	    }else {
	        this.setStatusCompleted();
	    }	    
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#destroy()
	 */
	public Object destroy() throws Exception {
		// do nothing method
		return null;
	}


	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pcEvent) {
		if("statusCode".equals(pcEvent.getPropertyName())) {
		    handleStatusChanged(pcEvent);
		}
	}
	
	public void handleStatusChanged(PropertyChangeEvent pcEvent) {
	    int statusCode = ((Integer)pcEvent.getNewValue()).intValue();
	    Command command = (Command)pcEvent.getSource();
	    
	    if(statusCode==Status.COMPLETED) {
	        submitNextCommand();	        
	    }
	}
	public boolean isEchoOn() {
	    String echoValue = String.valueOf(getParent().getScope().getValue("echo"));
	    logger.debug("echoValue="+echoValue);	    
	    return "on".equalsIgnoreCase(echoValue); 
	}
}
