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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public class ServiceContactImpl implements ServiceContact {

    static Logger logger = Logger.getLogger(ServiceContactImpl.class.getName());

    public static final ServiceContact LOCALHOST = new ServiceContactImpl(
            "localhost");

    private String host, path, contact;
    private int port;

    public ServiceContactImpl() {
    }

    public ServiceContactImpl(String contact) {
        this.contact = contact;
        parse(contact);
    }

    public ServiceContactImpl(String host, int port) {
        this.host = host;
        this.port = port;
        buildContact();
    }

    public void setHost(String host) {
        this.host = host;
        port = -1;
        buildContact();
    }
    

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
        buildContact();
    }

    public int getPort() {
        return port;
    }

    public void setContact(String contact) {
        this.contact = contact;
        parse(contact);
    }

    public String getContact() {
        return contact;
    }

    public boolean equals(Object o) {
    	if (o instanceof ServiceContact) {
    		ServiceContact sc = (ServiceContact) o;
    		if (contact == null) {
    		    return sc.getContact() == null;
    		}
    		else {
    		    return contact.equals(sc.getContact());
    		}
    	}
        return false;
    }

    public int hashCode() {
        if (contact == null) {
            return 1;
        }
        else {
            return contact.hashCode();
        }
    }

    private void parse(String contact) {
        int schemesep = contact.indexOf("://");
        if (schemesep == -1) {
            schemesep = 0;
        }
        else {
            schemesep += 3;
        }
        int portsep = contact.indexOf(':', schemesep);
        int pathsep = contact.indexOf('/', schemesep);
        if (portsep != -1 && (pathsep == -1 || portsep < pathsep)) {
            host = contact.substring(schemesep, portsep);
            String sPort;
            if (pathsep == -1) {
                sPort = contact.substring(portsep + 1);
                path = null;
            }
            else {
                sPort = contact.substring(portsep + 1, pathsep);
                path = contact.substring(pathsep);
            }
            if (!sPort.equals("")) {
                try {
                    port = Integer.parseInt(sPort);
                }
                catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid port number: '" + sPort + "'");
                }
            }
        }
        else if (pathsep != -1) {
            host = contact.substring(schemesep, pathsep);
            port = -1;
            path = contact.substring(pathsep);
        }
        else {
            host = contact.substring(schemesep);
            port = -1;
            path = null;
        }
    }
    
    private void buildContact() {
        this.contact = host + (port == -1 ? "" : (":" + port));
    }

    public String toString() {
        return getContact();
    }
}