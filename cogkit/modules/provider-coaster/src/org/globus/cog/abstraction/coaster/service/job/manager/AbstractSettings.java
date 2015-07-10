//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 7, 2015
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public abstract class AbstractSettings {
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
    
    public void set(String name, String value)
        throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting " + name + " to " + value);
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("Empty string Settings key "
                                            + "(value was \"" + value + "\"");
        }

        boolean complete = false;
        Method[] methods = getClass().getMethods();
        String setterName = "set" +
            Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(setterName)) {
                    set(method, value);
                    complete = true;
                    break;
                }
            }
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException
                ("Cannot set: " + name + " to: " + value);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (!complete) {
            setAttribute(name, value);
        }
    }

    protected void set(Method method, String value)
        throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = method.getParameterTypes()[0];
        Object[] args = null;
        if (clazz.equals(String.class)) {
            args = new Object[] { value };
        }
        else if (clazz.equals(int.class)) {
            args = new Object[] { Integer.valueOf(value) };
        }
        else if (clazz.equals(double.class)) {
            args = new Object[] { Double.valueOf(value) };
        }
        else if (clazz.equals(boolean.class)) {
            args = new Object[] { Boolean.valueOf(value) };
        }
        else if (clazz.equals(TimeInterval.class)) {
            args = new Object[]
                { TimeInterval.fromSeconds(Integer.parseInt(value)) };
        }
        else if (clazz.equals(ServiceContact.class)) {
            args = new Object[]
                { new ServiceContactImpl(value) };
        }
        else {
            throw new IllegalArgumentException
                ("Don't know how to set option with type " + clazz);
        }
        method.invoke(this, args);
    }

    private static final Object[] NO_ARGS = new Object[0];

    public Object get(String name) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Method[] ms = getClass().getMethods();
        String getterName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (int i = 0; i < ms.length; i++) {
            if (ms[i].getName().equals(getterName)) {
                return ms[i].invoke(this, NO_ARGS);
            }
        }
        return null;
    }
    
    public String[] getNames() {
        return new String[0];
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
