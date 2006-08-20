/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import java.net.PasswordAuthentication;
import java.net.URI;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.app.OptionImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.tasks.StartTask;
import org.globus.cog.gridshell.util.CredSupport;

/**
 * 
 */
public class Open extends AbstractTaskCommand {
	private static final Logger logger = Logger.getLogger(Open.class);
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridface.impl.gridshell.interfaces.Scope)
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		result.addArgument(new ArgumentImpl("the uri",URI.class,true));
		result.addOption(new OptionImpl("a username",String.class,false,null,"u","username",false));
		result.addOption(OptionImpl.createFlag("set to true for password prompt","p","password"));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		// extract from commandline
		URI uri = (URI)getGetOpt().getArgumentAt(1).getValue();
		Object obj = getGetOpt().getOption("username").getValue();
		logger.debug("obj="+obj+" class="+obj);
		String userName = (String)obj;
		Boolean isPassword = (Boolean)getGetOpt().getOption("password").getValue();
		
		Object credential = null;
		
		// if we have info to create a credential do so
		if(userName != null || isPassword.booleanValue()) {
		    char[] password = new CredSupport(null).getPassword();
		    if(password != null) {
		        credential = new PasswordAuthentication(userName,password);
		    }
		}
		
		// extract values from the uri
		String provider = uri.getScheme();
		String serviceContact = uri.getHost();
		int port = uri.getPort();		
		
		// create the connection
		final StartTask connection = new StartTask(credential,provider,serviceContact,port);		
		setTask(connection);
		connection.addStatusListener(new StatusListener() {
            public void statusChanged(StatusEvent event) {
                if(Status.COMPLETED == event.getStatus().getStatusCode()) {
                    getConnectionManager().push(connection);
                }
            }
		});
		return super.execute();
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.taskcommands.AbstractTaskCommand#getTaskOutput()
	 */
	public Object getTaskOutput() {		
		return getTask().getAttribute("output");
	}
}
