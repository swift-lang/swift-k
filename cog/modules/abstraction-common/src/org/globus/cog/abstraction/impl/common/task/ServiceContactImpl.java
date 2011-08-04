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