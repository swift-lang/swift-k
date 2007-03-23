// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

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

    private Hashtable attributes = null;
    private Calendar submittedTime = null;
    private Calendar completedTime = null;
    private ArrayList serviceList = null;
    private int requiredServices = 0;

    private boolean anythingWaiting;

    public TaskImpl() {
        this.id = new IdentityImpl();
        this.attributes = new Hashtable();
        this.serviceList = new ArrayList();
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
        this.serviceList.ensureCapacity(index + 1);
        int sz = this.serviceList.size();
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
        this.serviceList.add(service);
    }

    public Service removeService(int index) {
        return (Service) this.serviceList.remove(index);
    }

    public Service getService(int index) {
        if (index >= serviceList.size()) {
            return null;
        }
        return (Service) serviceList.get(index);
    }

    public Collection removeAllServices() {
        Collection services = this.serviceList;
        this.serviceList = new ArrayList();
        return services;
    }

    public void removeService(Collection collection) {
        this.serviceList.removeAll(collection);
    }

    public Collection getAllServices() {
        return this.serviceList;
    }

    public void setRequiredService(int value) {
        this.requiredServices = value;
        this.serviceList.ensureCapacity(value);
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
        if (logger.isDebugEnabled()) {
            logger.debug(this + " setting status to " + status);
        }
        this.status = status;

        if (this.status.getStatusCode() == Status.SUBMITTED) {
            this.submittedTime = this.status.getTime();
        }
        else if (this.status.getStatusCode() == Status.COMPLETED) {
            this.completedTime = this.status.getTime();
        }

        StatusEvent event = new StatusEvent(this, this.status);
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
        this.attributes.put(name.toLowerCase(), value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name.toLowerCase());
    }

    public Enumeration getAllAttributes() {
        return this.attributes.keys();
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
        return "Task(type=" + type + ", identity=" + id + ")";
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

    public Calendar getSubmittedTime() {
        return this.submittedTime;
    }

    public Calendar getCompletedTime() {
        return this.completedTime;
    }

    public boolean equals(Object object) {
        return this.id.equals(((ExecutableObject) object).getIdentity());
    }

    public int hashCode() {
        return (int) this.id.getValue();
    }

    public synchronized void waitFor() throws InterruptedException {
        anythingWaiting = true;
        while (!isFailed() && !isCompleted() && !isCanceled()) {
            wait();
        }
    }
}