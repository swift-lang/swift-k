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

package org.globus.cog.abstraction.impl.common;

import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.CredentialsDialog;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;

public class InteractivePasswordSecurityContextImpl extends SecurityContextImpl {

    private static Logger logger = Logger
            .getLogger(InteractivePasswordSecurityContextImpl.class.getName());
    private String username, hostName;
    
    public InteractivePasswordSecurityContextImpl(String username) {
        this.username = username;
    }

    public InteractivePasswordSecurityContextImpl() {
    }

    public InteractivePasswordSecurityContextImpl(PasswordAuthentication credentials) {
        setCredentials(credentials);
    }

    public void setCredentials(Object credentials, String alias) {
        setCredentials(credentials);
    }

    public synchronized Object getCredentials() {
        Object credentials = getCredentials();
        if (credentials == null) {
            credentials = CredentialsDialog.showCredentialsDialog(hostName, username);
            setCredentials(credentials);
        }
        return credentials;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
