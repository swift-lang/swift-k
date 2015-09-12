/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.taskgraph;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.ChangeListener;
import org.globus.cog.abstraction.interfaces.Dependency;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.TaskGraph;

public class TaskGraphImpl implements TaskGraph {
    private Identity id;
    private String name;
    private Hashtable nodes;
    private Dependency dependency = null;
    private Hashtable attributes;
    private Vector statusListeners;
    private Status status;
    private Calendar submittedTime = null;
    private Calendar completedTime = null;
    private int failurePolicy = TaskGraph.AbortOnFailure;
    private Vector changeListeners = new Vector();

    public TaskGraphImpl() {
        this.id = new IdentityImpl();
        this.nodes = new Hashtable();
        this.attributes = new Hashtable();
        this.status = new StatusImpl();
    }

    public TaskGraphImpl(Identity id) {
        this.id = id;
        this.nodes = new Hashtable();
        this.attributes = new Hashtable();
        this.status = new StatusImpl();
    }

    public void setIdentity(Identity id) {
        this.id = id;
    }

    public Identity getIdentity() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public int getObjectType() {
        return ExecutableObject.TASKGRAPH;
    }

    public void add(ExecutableObject node) throws Exception {
        this.nodes.put(node.getIdentity(), node);

        Enumeration en = this.changeListeners.elements();
        ChangeEvent event = new ChangeEvent(this, node, ChangeEvent.ADD);
        while (en.hasMoreElements()) {
            ChangeListener listener = (ChangeListener) en.nextElement();
            listener.graphChanged(event);
        }
    }

    public ExecutableObject remove(Identity id) {
        ExecutableObject eo = this.get(id);
        this.dependency.removeAllDependents(eo);
        this.dependency.removeAllDependsOn(eo);
        return (ExecutableObject) this.nodes.remove(id);
    }

    public ExecutableObject get(Identity id) {
        return (ExecutableObject) this.nodes.get(id);
    }

    public ExecutableObject[] toArray() {
        Enumeration en = this.nodes.elements();
        int size = this.nodes.size();
        ExecutableObject[] graphnodes = new ExecutableObject[size];
        for (int i = 0; en.hasMoreElements(); i++) {
            graphnodes[i] = (ExecutableObject) en.nextElement();
        }
        return graphnodes;
    }

    public Enumeration elements() {
        return this.nodes.elements();
    }

    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return this.dependency;
    }

    public void addDependency(ExecutableObject from, ExecutableObject to) {
        if (this.dependency == null) {
            this.dependency = new DependencyImpl();
        }
        this.dependency.add(from, to);
    }

    public boolean removeDependency(ExecutableObject from, ExecutableObject to) {
        if (this.dependency != null) {
            return this.dependency.remove(from, to);
        }
        return false;
    }

    public void setStatus(Status status) {
        this.status = status;

        if (this.statusListeners == null) {
            return;
        }
        int size = this.statusListeners.size();
        StatusEvent event = new StatusEvent(this, this.status);
        for (int i = 0; i < size; i++) {
            StatusListener listener =
                (StatusListener) this.statusListeners.elementAt(i);
            listener.statusChanged(event);
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

    public int getSize() {
        return this.nodes.size();
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

    public void addStatusListener(StatusListener listener) {
        if (this.statusListeners == null) {
            this.statusListeners = new Vector();
        }
        this.statusListeners.addElement(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        if (this.statusListeners != null) {
            this.statusListeners.remove(listener);
        }
    }

    public boolean contains(Identity id) {
        return this.nodes.containsKey(id);
    }

    public boolean isEmpty() {
        return this.nodes.isEmpty();
    }

    public int getUnsubmittedCount() {
        int count = 0;
        Enumeration en = this.elements();
        ExecutableObject eo;
        while (en.hasMoreElements()) {
            eo = (ExecutableObject) en.nextElement();
            if (eo.getStatus().getStatusCode() == Status.UNSUBMITTED) {
                count++;
            }
        }
        return count;
    }

    public int getSubmittedCount() {
        int count = 0;
        Enumeration en = this.elements();
        ExecutableObject eo;
        while (en.hasMoreElements()) {
            eo = (ExecutableObject) en.nextElement();
            if (eo.getStatus().getStatusCode() == Status.SUBMITTED) {
                count++;
            }
        }
        return count;
    }

    public int getActiveCount() {
        int count = 0;
        Enumeration en = this.elements();
        ExecutableObject eo;
        while (en.hasMoreElements()) {
            eo = (ExecutableObject) en.nextElement();
            if (eo.getStatus().getStatusCode() == Status.ACTIVE) {
                count++;
            }
        }
        return count;
    }

    public int getCompletedCount() {
        int count = 0;
        Enumeration en = this.elements();
        ExecutableObject eo;
        while (en.hasMoreElements()) {
            eo = (ExecutableObject) en.nextElement();
            if (eo.getStatus().getStatusCode() == Status.COMPLETED) {
                count++;
            }
        }
        return count;
    }

    public int getSuspendedCount() {
        int count = 0;
        Enumeration en = this.elements();
        ExecutableObject eo;
        while (en.hasMoreElements()) {
            eo = (ExecutableObject) en.nextElement();
            if (eo.getStatus().getStatusCode() == Status.SUSPENDED) {
                count++;
            }
        }
        return count;
    }

    public int getResumedCount() {
        int count = 0;
        Enumeration en = this.elements();
        ExecutableObject eo;
        while (en.hasMoreElements()) {
            eo = (ExecutableObject) en.nextElement();
            if (eo.getStatus().getStatusCode() == Status.RESUMED) {
                count++;
            }
        }
        return count;
    }

    public int getFailedCount() {
        int count = 0;
        Enumeration en = this.elements();
        ExecutableObject eo;
        while (en.hasMoreElements()) {
            eo = (ExecutableObject) en.nextElement();
            if (eo.getStatus().getStatusCode() == Status.FAILED) {
                count++;
            }
        }
        return count;
    }

    public int getCanceledCount() {
        int count = 0;
        Enumeration en = this.elements();
        ExecutableObject eo;
        while (en.hasMoreElements()) {
            eo = (ExecutableObject) en.nextElement();
            if (eo.getStatus().getStatusCode() == Status.FAILED) {
                count++;
            }
        }
        return count;
    }

    public Calendar getSubmittedTime() {
        return this.submittedTime;
    }

    public Calendar getCompletedTime() {
        return this.completedTime;
    }

    public void setFailureHandlingPolicy(int policy) {
        this.failurePolicy = policy;
    }

    public int getFailureHandlingPolicy() {
        return this.failurePolicy;
    }

    public void addChangeListener(ChangeListener listener) {
        this.changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        this.changeListeners.remove(listener);
    }
    
    public boolean equals(Object object) {
        return this.id.equals(((ExecutableObject)object).getIdentity());
    }

    public int hashCode() {
        return this.id.hashCode();
    }
}
