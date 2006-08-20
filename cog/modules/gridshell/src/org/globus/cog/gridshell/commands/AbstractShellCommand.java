/*
 * 
 */
package org.globus.cog.gridshell.commands;

import java.awt.Frame;
import java.net.PasswordAuthentication;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.PublicKeyAuthentication;
import org.globus.cog.gridshell.commands.gsh.Gsh;
import org.globus.cog.gridshell.ctrl.GridShellImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Command;
import org.globus.cog.gridshell.interfaces.GridShell;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.util.CredSupport;

/**
 * Simply extends the requirement of a command to provide a GetOpt that parses
 * for a particular command
 * 
 */
public abstract class AbstractShellCommand extends AbstractCommand {
	private static final Logger logger = Logger.getLogger(AbstractShellCommand.class);
	
	protected GetOpt getopt;
	
	public Object init(Map args) throws Exception {
		Object result = super.init(args);
		if(args.containsKey("getopt")) {
			getopt = (GetOpt)args.get("getopt");
		}else {
			logger.debug("getopt not set for "+this);
		}
		
		return result;
	}
	public GetOpt getGetOpt() {
		return getopt;
	}
	/**
	 * Returns the GetOpt object used to parse and validate this command
	 * @param scope
	 * @return
	 */
	public abstract GetOpt createGetOpt(Scope scope);
	
    public Object getCredentials() {
        GetOpt getopt = getGetOpt();
        Frame frame = GridShellImpl.getFrame(getGridShell().getGUI().getJComponent());
        Object result = null;
        String certificate = (String) getopt.getOption("certificate").getValue();
        String username = (String) getopt.getOption("username").getValue();
        boolean isPassword = getopt.isOptionSet("password");
        char[] password =  null;
        
        if(isPassword) {            
            password = new CredSupport(frame).getPassword();
            if(certificate!=null) {
                result = new PublicKeyAuthentication(username,certificate,password);
            }else {
                result = new PasswordAuthentication(username,password);
            }
        } 
        logger.debug("result="+result);
        return result;
    }
		
	/**
	 * Used to get the gridshell if it is the parent
	 * @return
	 */
	protected GridShell getGridShell() {
		if(getParent() instanceof GridShell) {
			logger.debug("returning parent="+getParent());
			return (GridShell)getParent();
		}else {
			logger.info("returning null");
			return null;
		}
	}
	
	protected Gsh getGsh() {
		if(getParent() instanceof Gsh) {
			logger.debug("returning parent"+getParent());
			return (Gsh) getParent();
		}else {
			logger.info("returning null");
			return null;
		}
	}
	
	protected AbstractShellCommand createAbstractShellCommand(String commandName) throws Exception {
		AbstractShellCommand result = null;

		Command parent = getParent();
		if(parent instanceof Gsh) {
			Gsh prgm = (Gsh)parent;
			Command command = null;
			try {
				command = prgm.createCommand(commandName);
			}catch(CommandNotFoundException e) {
				throw new CommandNotFoundException("Cannot create command '"+commandName+"' it is not defined.",e);
			}				
			if(command instanceof AbstractShellCommand) {
				return (AbstractShellCommand)command;
			}else {
				throw new Exception("Cannot display help for non AbstractShellCommand");
			}			
		}else {
			throw new Exception("Cannot diplay help if parent is not a gsh instance");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#suspend()
	 */
	public Object suspend() throws Exception {
		this.unsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#resume()
	 */
	public Object resume() throws Exception {
		this.unsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#kill()
	 */
	public Object kill() throws Exception {
		this.unsupported();
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public final void run() {
		try {
			execute();
		}catch(Exception e) {
			this.setStatusFailed("Failed to run",e);
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// each command's string value is the commandline value entered in the shell
		if(getGetOpt() != null) {
			return getGetOpt().getCommandLineValue();
		}else {
			return super.toString();
		}
	}
	
}
