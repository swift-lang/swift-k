
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.interfaces;

import java.util.Collection;
import java.util.Enumeration;

import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public interface ServiceManager {
    public void addService(Service service);
    public Service removeService(ServiceContact serviceContact);
    public Service getService(ServiceContact serviceContact);
    public boolean containsService(ServiceContact serviceContact);

    public Collection getAllServices();
    public Collection getServices(ClassAd classAd);
    public Collection getServices(String provider, int type);
    public Collection getServices(int type);

    public void setAttribute(String name, Object value);
    public Object getAttribute(String name);
    public Enumeration getAllAttributes();
}
