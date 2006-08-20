// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public class ServiceContactImpl implements ServiceContact {

    static Logger logger = Logger.getLogger(ServiceContactImpl.class.getName());

    private static final byte HOST = 1;

    private static final byte PORT = 2;

    private static final byte CONTACT = 3;

    public static final ServiceContact LOCALHOST = new ServiceContactImpl(
            "localhost");

    private String host = null;

    private int port = -1;

    private String contact = null;

    public ServiceContactImpl() {
    }

    public ServiceContactImpl(String contact) {
        this.contact = contact;
    }

    public ServiceContactImpl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return get(HOST);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        String p = get(PORT);
        if (p == null) {
            return -1;
        }
        return Integer.parseInt(p);

    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContact() {
        return get(CONTACT);
    }

    public boolean equals(ServiceContact serviceContact) {
        return this.getContact().equalsIgnoreCase(serviceContact.getContact());
    }

    public boolean equals(Object object) {
    	if (!(object instanceof ServiceContact)) {
    		return false;
    	}
        return this.toString().equalsIgnoreCase(
                ((ServiceContact) object).toString());
    }

    public int hashCode() {
        return this.getContact().toLowerCase().hashCode();
    }

    private String get(byte element) {
        switch (element) {
        case CONTACT:
            // if the service contact url is already set
            if (this.contact != null) {
                return this.contact;
            }
            // if not try to generate one
            else if (this.host != null) {
                if (this.port != -1) {
                    return this.host + ":" + Integer.toString(this.port);
                } else {
                    return this.host;
                }
            }
            return null;
        case HOST:
            if (this.host == null && this.contact != null) {
                // try to cast the contact into a URI and then get the
                // host and port from it
                if (this.contact.indexOf("://") != -1) {
                    try {
                        URI uri = new URI(this.contact);
                        logger.debug("Host from URI: " + uri.getHost());
                        return uri.getHost();
                    } catch (URISyntaxException e) {
                        logger
                                .debug("Cannot retreive host information from the URI");
                        return null;
                    }
                } else {
                    String c = this.contact;
                    StringTokenizer st = new StringTokenizer(c, ":");
                    try {
                        String h = st.nextToken();
                        logger.debug("Host from contact: " + h);
                        return h;
                    } catch (Exception ex) {
                        logger
                                .debug("Cannot retreive port information from the contact");
                        return null;
                    }
                }
            }
            return this.host;
        case PORT:
            if (this.port == -1 && this.contact != null) {
                // try to cast the contact into a URI and then get the
                // port from it
                if (this.contact.indexOf("://") != -1) {
                    try {
                        URI uri = new URI(this.contact);
                        logger.debug("Port from URI: " + uri.getPort());
                        return Integer.toString(uri.getPort());
                    } catch (URISyntaxException e) {
                        logger
                                .debug("Cannot retreive port information from the URI");
                        return null;
                    }
                } else {
                    String c = this.contact;
                    StringTokenizer st = new StringTokenizer(c, ":");
                    try {
                        st.nextToken();
                        String p = st.nextToken();
                        logger.debug("Port from contact: " + p);
                        return p;
                    } catch (Exception ex) {
                        logger
                                .debug("Cannot retreive port information from the contact");
                        return null;
                    }
                }
            }
            return Integer.toString(this.port);
        default:
            break;
        }
        return null;
    }

    public String toString() {
        return getContact();
    }
}