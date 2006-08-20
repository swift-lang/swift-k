// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.impl;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.broker.interfaces.ClassAd;
import org.globus.cog.broker.interfaces.TaskToServiceMapper;
import org.globus.cog.broker.interfaces.ServiceManager;


public class RandomTaskToServiceMapper implements TaskToServiceMapper {
    private Hashtable attributes = null;
    private Random random = null;
    private ServiceManager serviceManager = null;

    // Random matchmaker
    public RandomTaskToServiceMapper(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        this.attributes = new Hashtable();
        this.random = new Random(System.currentTimeMillis());
    }

    public boolean match(Task task) {
        Collection services = null;
        ClassAd classAd =  (ClassAd)task.getAttribute("classad");

        // if classAd available, then retrieve services based on classAd
        if (classAd != null) {
            // retrieve all services that match the ClassAd
            synchronized (this.serviceManager) {
                services = this.serviceManager.getServices(classAd);
            }
        } else {
            if (task.getProvider() != null) {
                // retrieve services based on provider and type
                services = this.serviceManager.getServices(task.getProvider(),
                        task.getType());
            } else {
                // retrieve services based on type
                services = this.serviceManager.getServices(task.getType());
            }
        }
        if (services.size() > 0) {
            // gets a random number between 0 (incl) and size of list (excl)
            int index = random.nextInt(services.size());
            Object[] s = services.toArray();
            Service svc = (Service) s[index];
            task.addService(svc);
            return true;
        }
        return false;
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