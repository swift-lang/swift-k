//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.ssh;

import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;

public class InteractiveSSHSecurityContextImpl extends SecurityContextImpl {

	private static Logger logger = Logger.getLogger(InteractiveSSHSecurityContextImpl.class.getName());
	
	private String hostName;

	public InteractiveSSHSecurityContextImpl() {
		// this.credentials = new PasswordAuthentication(null, null);
	}

	public InteractiveSSHSecurityContextImpl(Object credentials) {
		setCredentials(credentials);
	}

	public void setCredentials(Object credentials, String alias) {
		setCredentials(credentials);
	}

	public synchronized Object getCredentials() {
	    Object credentials = getCredentials();
		if (credentials == null) {
			boolean forceText = false;
			Object text = getAttribute("nogui");
			if (text != null
					&& (Boolean.TRUE.equals(text) || (text instanceof String && Boolean.valueOf(
							(String) text).booleanValue()))) {
				forceText = true;
			}
			credentials = CredentialsDialog.showCredentialsDialog(hostName,
					(String) getAttribute("username"), (String) getAttribute("privatekey"), forceText);
			if (credentials == null) {
				// Cancel was pressed, so we set it to mock credentials to
				// avoid being asked again
				credentials = new PasswordAuthentication("", new char[0]);
			}
			setCredentials(credentials);
		}
		return credentials;
	}

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
