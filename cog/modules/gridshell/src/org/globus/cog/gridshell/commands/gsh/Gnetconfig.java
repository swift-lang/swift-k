/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.beans.PropertyChangeEvent;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.IPGetSetter;
import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.app.OptionImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.common.CoGProperties;

/**
 * 
 */
public class Gnetconfig extends AbstractShellCommand {
	private static final Logger logger = Logger.getLogger(Gnetconfig.class);
	private static final String EOL = System.getProperty("line.separator");
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		logger.info("execute");
		StringBuffer result = new StringBuffer();
		
		
		Object devName = getGetOpt().getArgumentAt(1).getValue();
		Object ipaddress = getGetOpt().getOption("ipaddress").getValue();
		
		if(devName != null) {
		    IPGetSetter.setRedirectIPAddressDev(devName.toString());
		}
		if(ipaddress != null) {
		    IPGetSetter.setRedirectIPAddress(ipaddress.toString());
		}
		if(getGetOpt().isOptionSet("value")) {
		    result.append("ipaddress: ");
		    result.append(CoGProperties.getDefault().getIPAddress());
		    result.append(EOL);
		}
		if(getGetOpt().isOptionSet("deviceinfo")) {
		    result.append("Device info: ");
		    result.append(IPGetSetter.getAllIPAddresses());
		    result.append(EOL);
		}
		setResult(result);
		this.setStatusCompleted();
		return null;
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
	public void propertyChange(PropertyChangeEvent arg0) {
		// do nothing method		
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#getGetOpt()
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		result.addOption(OptionImpl.createFlag("display current cog.properties ip value","v","value"));
		result.addOption(OptionImpl.createFlag("prints the detected devices and their values","d","deviceinfo"));
		result.addArgument(new ArgumentImpl("the device name to use for setting the ipaddress",String.class,false));
		result.addOption(new OptionImpl("set cog.properties to this ipaddress",String.class,false,"i","ipaddress",false));
		return result;
	}

}
