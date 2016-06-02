//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 7, 2015
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public abstract class AbstractSettings extends IntrospectiveMap {
    public static final Logger logger = Logger.getLogger(AbstractSettings.class);
    
    private final Map<String, String> attributes;
    
    public AbstractSettings() {
        attributes = new HashMap<String, String>();
    }

    public Collection<URI> getLocalContacts(int port) {
        Set<URI> l = new HashSet<URI>();
        try {
            Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = e1.nextElement();
                Enumeration<InetAddress> e2 = ni.getInetAddresses();
                while (e2.hasMoreElements()) {
                    InetAddress addr = e2.nextElement();
                    if (addr instanceof Inet6Address)
                        continue;
                    if (!"127.0.0.1".equals(addr.getHostAddress())) {
                        l.add(new URI("http://" + addr.getHostAddress() + ":" + port));
                    }
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Local contacts: " + l);
            }
            return l;
        }
        catch (SocketException e) {
            logger.warn("Could not get network interface addresses", e);
            return null;
        }
        catch (URISyntaxException e) {
            logger.warn("Could not build URI from local network interface addresses", e);
            return null;
        }
    }

    public abstract void setCallbackURI(URI contact);

    public abstract void setCallbackURIs(Collection<URI> addrs);
    
    public abstract Collection<URI> getCallbackURIs();
    
    public Collection<String> getAttributeNames() {
        return attributes.keySet();
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }
    
    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }
    
    public String[] getNames() {
        return new String[0];
    }
    
    
    @Override
    public Object put(String name, Object value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting " + name + " to " + value);
        } 
        return super.put(name, value);
    }

    
    protected static String[] extend(String[] a1, String[] a2) {
        String[] r = new String[a1.length + a2.length];
        int i = 0;
        for (String s : a1) {
            r[i++] = s;
        }
        for (String s : a2) {
            r[i++] = s;
        }
        return r;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Settings {\n");
        String[] names = getNames();
        for (int i = 0; i < names.length; i++) {
            sb.append("\t");
            sb.append(names[i]);
            sb.append(" = ");
            try {
                sb.append(String.valueOf(get(names[i])));
            }
            catch (Exception e) {
                sb.append("<exception>");
                logger.warn(e);
            }
            sb.append('\n');
        }
        sb.append("\tattributes = " + attributes + "\n");
        sb.append("\tcallbackURIs = " + getCallbackURIs() + "\n");
        sb.append("}\n");
        return sb.toString();
    }
}
