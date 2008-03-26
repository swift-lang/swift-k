// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.OutputListener;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Specification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.xml.MarshalException;
import org.globus.cog.abstraction.xml.TaskMarshaller;
import org.globus.cog.util.CopyOnWriteHashSet;

public class TaskImpl implements Task {
    public static final Logger logger = Logger.getLogger(TaskImpl.class);

    public static final Status STATUS_NONE = new StatusImpl();

    private Identity id = null;
    private String name = null;
    private int type = 0;
    private String provider = null;
    private Specification specification = null;
    private String output = null;
    private String error = null;
    private Status status = STATUS_NONE;

    private CopyOnWriteHashSet statusListeners, outputListeners;

    private Map attributes;

    private ArrayList serviceList;
    private int requiredServices = 0;

    private boolean anythingWaiting;

    public TaskImpl() {
        this.id = new IdentityImpl();
        this.serviceList = new ArrayList(2);
        this.status = new StatusImpl();
        statusListeners = new CopyOnWriteHashSet();
        outputListeners = new CopyOnWriteHashSet();
    }

    public TaskImpl(String name, int type) {
        this();
        this.setName(name);
        this.setType(type);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setIdentity(Identity id) {
        this.id = id;
    }

    public Identity getIdentity() {
        return this.id;
    }

    public int getObjectType() {
        return ExecutableObject.TASK;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setProvider(String provider) {
        if (provider != null) {
            this.provider = provider.toLowerCase();
        }
    }

    public String getProvider() {
        return this.provider;
    }

    public void setService(int index, Service service) {
    	while (serviceList.size() < index) {
    		serviceList.add(null);
    	}
        int sz = serviceList.size();
        if (sz == index) {
            this.serviceList.add(index, service);
        }
        else if (sz > index) {
            this.serviceList.set(index, service);
        }
        else {
            throw new IllegalArgumentException("index(" + index + ") > size("
                    + sz + ")");
        }
    }

    public void addService(Service service) {
        serviceList.add(service);
    }

    public Service removeService(int index) {
        return (Service) serviceList.remove(index);
    }

    public Service getService(int index) {
        if (index >= serviceList.size()) {
            return null;
        }
        return (Service) serviceList.get(index);
    }

    public Collection removeAllServices() {
        Collection services = this.serviceList;
        this.serviceList = new ArrayList(2);
        return services;
    }

    public void removeService(Collection collection) {
        this.serviceList.removeAll(collection);
    }

    public Collection getAllServices() {
        return serviceList;
    }

    public void setRequiredService(int value) {
        this.requiredServices = value;
    }

    public int getRequiredServices() {
        return this.requiredServices;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    public Specification getSpecification() {
        return this.specification;
    }

    public void setStdOutput(String output) {
        this.output = output;
        OutputEvent event = new OutputEvent(this, this.output);
        Iterator i = outputListeners.iterator();
        try {
            while (i.hasNext()) {
                OutputListener listener = (OutputListener) i.next();
                listener.outputChanged(event);
            }
        }
        finally {
            outputListeners.release();
        }
    }

    public String getStdOutput() {
        return this.output;
    }

    public void setStdError(String error) {
        this.error = error;
    }

    public String getStdError() {
        return this.error;
    }

    public void setStatus(Status status) {
        int next = status.getStatusCode();
        int pred = StatusOrder.pred(next);
        boolean missing = false;
        boolean discard = false;
        synchronized (this) {
            int crt = this.status.getStatusCode();
            if (StatusOrder.greaterThan(crt, next) || crt == next) {
                // discard late arrivals
                discard = true;
            }
            else if (pred != crt && pred != -1) {
                missing = true;
            }
        }
        if (missing) {
            setStatus(pred);
        }
        if (!discard) {
            // not much choice left
            if (logger.isDebugEnabled()) {
                logger.debug(this + " setting status to " + status);
            }
            synchronized(this) {
                this.status = status;
            }
            notifyListeners(status);
        }
        // Now prove that this works correctly with concurrent updates.
        // I will pay $20 for the first such proof that I receive.
    }

    protected void notifyListeners(Status status) {
        StatusEvent event = new StatusEvent(this, status);
        Iterator i = statusListeners.iterator();
        try {
            while (i.hasNext()) {
                StatusListener listener = (StatusListener) i.next();
                listener.statusChanged(event);
            }
        }
        finally {
            statusListeners.release();
        }
        synchronized (this) {
            if (anythingWaiting) {
                notifyAll();
            }
        }
    }

    public void setStatus(int status) {
        Status newStatus = new StatusImpl();
        newStatus.setPrevStatusCode(this.status.getStatusCode());
        newStatus.setStatusCode(status);
        this.setStatus(newStatus);
    }

    public Status getStatus() {
        return this.status;
    }

    public void setAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new HashMap();
        }
        attributes.put(name.toLowerCase(), value);
    }

    public Object getAttribute(String name) {
        if (attributes != null) {
            return attributes.get(name.toLowerCase());
        }
        else {
            return null;
        }
    }

    public Collection getAttributeNames() {
        if (attributes != null) {
            return attributes.keySet();
        }
        else {
            return Collections.EMPTY_MAP.keySet();
        }
    }

    public void addStatusListener(StatusListener listener) {
        this.statusListeners.add(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        this.statusListeners.remove(listener);
    }

    public void addOutputListener(OutputListener listener) {
        this.outputListeners.add(listener);
    }

    public void removeOutputListener(OutputListener listener) {
        this.outputListeners.remove(listener);
    }

    public void toXML(File file) throws MarshalException {
        TaskMarshaller.marshal(this, file);
    }

    public String toString() {
        return "Task(type=" + typeString(type) + ", identity=" + id + ")";
    }

    public static String typeString(int type) {
        switch (type) {
            case JOB_SUBMISSION:
                return "JOB_SUBMISSION";
            case FILE_TRANSFER:
                return "FILE_TRANSFER";
            case FILE_OPERATION:
                return "FILE_OPERATION";
            case INFORMATION_QUERY:
                return "INFORMATION_QUERY";
            default:
                return "UNKNOWN";
        }
    }

    public boolean isUnsubmitted() {
        return (this.status.getStatusCode() == Status.UNSUBMITTED);
    }

    public boolean isActive() {
        return (this.status.getStatusCode() == Status.ACTIVE);
    }

    public boolean isCompleted() {
        return (this.status.getStatusCode() == Status.COMPLETED);
    }

    public boolean isSuspended() {
        return (this.status.getStatusCode() == Status.SUSPENDED);
    }

    public boolean isFailed() {
        return (this.status.getStatusCode() == Status.FAILED);
    }

    public boolean isCanceled() {
        return (this.status.getStatusCode() == Status.CANCELED);
    }

    public boolean equals(Object object) {
        return this.id.equals(((ExecutableObject) object).getIdentity());
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public synchronized void waitFor() throws InterruptedException {
        anythingWaiting = true;
        while (!isFailed() && !isCompleted() && !isCanceled()) {
            wait();
        }
    }
}