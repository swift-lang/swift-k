// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.broker.interfaces.ClassAd;
import org.globus.cog.broker.interfaces.ServiceManager;

public class ServiceManagerImpl implements ServiceManager {
    private Hashtable attributes = null;
    private Hashtable services = null;

    public ServiceManagerImpl() {
        this.services = new Hashtable();
        this.attributes = new Hashtable();
    }

    public void addService(Service service) {
        this.services.put(service.getServiceContact(), service);
    }

    public Service removeService(ServiceContact serviceContact) {
        return (Service) this.services.remove(serviceContact);
    }

    public Service getService(ServiceContact serviceContact) {
        return (Service) this.services.get(serviceContact);
    }

    public boolean containsService(ServiceContact serviceContact) {
        return this.services.containsKey(serviceContact);
    }

    public Collection getAllServices() {
        ArrayList list = new ArrayList();
        Enumeration en = this.services.elements();
        while (en.hasMoreElements()) {
            Service svc = (Service) en.nextElement();
            list.add(svc);
        }
        return list;
    }

    public Collection getServices(ClassAd classAd) {
        ArrayList list = new ArrayList();
        Enumeration en = this.services.elements();
        while (en.hasMoreElements()) {
            Service svc = (Service) en.nextElement();
            ClassAd svcClassad = (ClassAd)svc.getAttribute("classad");
            if (svcClassad != null
                    && classAd != null
                    && (ClassAdImpl.match(classAd, svcClassad)) != null) {
                list.add(svc);
            }
        }
        return list;
    }

    public Collection getServices(String provider, int type) {
        ArrayList list = new ArrayList();
        Enumeration en = this.services.elements();
        while (en.hasMoreElements()) {
            Service svc = (Service) en.nextElement();
            if (svc.getProvider() != null && provider != null
                    && svc.getProvider().equalsIgnoreCase(provider)
                    && svc.getType() == type) {
                list.add(svc);
            }
        }
        return list;
    }

    public Collection getServices(int type) {
        ArrayList list = new ArrayList();
        Enumeration en = this.services.elements();
        while (en.hasMoreElements()) {
            Service svc = (Service) en.nextElement();
            if (svc.getType() == type) {
                list.add(svc);
            }
        }
        return list;
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Enumeration getAllAttributes() {
        return this.attributes.keys();
    }

}