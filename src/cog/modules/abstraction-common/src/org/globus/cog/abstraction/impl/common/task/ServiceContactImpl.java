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

    private String host, path;
    private int port;

    public ServiceContactImpl() {
    }

    public ServiceContactImpl(String contact) {
        parse(contact);
    }

    public ServiceContactImpl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
        port = -1;
    }

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setContact(String contact) {
        parse(contact);
    }

    public String getContact() {
        return host + (port == -1 ? "" : ":" + port) + (path == null ? "" : path);
    }

    public boolean equals(Object o) {
    	if (o instanceof ServiceContact) {
    		ServiceContact sc = (ServiceContact) o;
    		return getContact().equals(sc.getContact());
    	}
        return false;
    }

    public int hashCode() {
        return this.getContact().hashCode();
    }

    private void parse(String contact) {
        int portsep = contact.indexOf(':');
        int pathsep = contact.indexOf('/');
        if (portsep != -1 && (pathsep == -1 || portsep < pathsep)) {
            host = contact.substring(0, portsep);
            if (pathsep == -1) {
                port = Integer.parseInt(contact.substring(portsep + 1));
                path = null;
            }
            else {
                port = Integer.parseInt(contact.substring(portsep + 1, pathsep));
                path = contact.substring(pathsep);
            }
        }
        else if (pathsep != -1) {
            host = contact.substring(0, pathsep);
            port = -1;
            path = contact.substring(pathsep);
        }
        else {
            host = contact;
            port = -1;
            path = null;
        }
    }

    public String toString() {
        return getContact();
    }
}