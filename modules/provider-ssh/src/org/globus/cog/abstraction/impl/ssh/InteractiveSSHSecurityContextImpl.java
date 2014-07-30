/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
