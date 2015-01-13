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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.ExtendedStatusListener;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

/**
 * This class is used to keep track of tasks sent to workers, since the worker
 * is only aware of the task ID. A notification from a worker needs to be
 * coupled with a Task object based on the ID.
 */
public class NotificationManager {
    public static final Logger logger = 
        Logger.getLogger(NotificationManager.class);

    private static NotificationManager def;

    public static synchronized NotificationManager getDefault() {
        if (def == null) {
            def = new NotificationManager();
        }
        return def;
    }

    /** 
       Map from Task IDs to Tasks
     */
    private Map<String, TaskListenerPair> listeners;
    
    /**
       Map from Task IDs to Status updates that arrived before the 
       Task existed in the Map {@link tasks}
     */
    private Map<String, List<ExtendedStatus>> pending;
    private long lastNotificationTime;

    public NotificationManager() {
        listeners = new HashMap<String, TaskListenerPair>();
        pending = new HashMap<String, List<ExtendedStatus>>();
        lastNotificationTime = System.currentTimeMillis();
    }

    public void registerListener(String id, Task task, ExtendedStatusListener l) {
        List<ExtendedStatus> p;
        synchronized (listeners) {
            TaskListenerPair tlp = listeners.get(id);
            if (tlp == null) {
                tlp = new TaskListenerPair(task, l);
                listeners.put(id, tlp);
            }
            else {
                tlp.addListener(l);
            }
            p = pending.remove(id);
        }
        if (p != null) {
            for (ExtendedStatus e : p) {
                notify(l, e);
            }
        }
    }

    public void notificationReceived(String id, Status s, String out, String err) {
        if (logger.isDebugEnabled())
            logger.debug("recvd: for: " + id + " " + s);
        TaskListenerPair ls;
        synchronized (listeners) {
            if (s.isTerminal()) {
                ls = listeners.remove(id);
            }
            else {
                ls = listeners.get(id);
            }
            lastNotificationTime = System.currentTimeMillis();
            if (ls == null) {
            	addPending(id, new ExtendedStatus(s, out, err));
            }
        }
        if (ls != null) {
            for (ExtendedStatusListener l : ls.listeners) {
                l.statusChanged(s, out, err);
            }
        }
    }

    public long getIdleTime() {
        synchronized (listeners) {
            if (listeners.size() == 0 && lastNotificationTime != 0) {
                return System.currentTimeMillis() - lastNotificationTime;
            }
            else {
                return 0;
            }
        }
    }

    public void notIdle() {
        synchronized (listeners) {
            lastNotificationTime = System.currentTimeMillis();
        }
    }

    public int getActiveTaskCount() {
        synchronized (listeners) {
            return listeners.size();
        }
    }

    private void notify(ExtendedStatusListener l, ExtendedStatus e) {
        try {
            l.statusChanged(e.status, e.out, e.err);
        }
        catch (Exception ex) {
            logger.warn("Could not set task status", ex);
        }
    }

    private void addPending(String id, ExtendedStatus es) {
        List<ExtendedStatus> p = pending.get(id);
        if (p == null) {
            p = new LinkedList<ExtendedStatus>();
            pending.put(id, p);
        }
        p.add(es);
    }

    public void serviceTaskEnded(org.globus.cog.abstraction.interfaces.Service service, String msg) {
        List<Map.Entry<String, TaskListenerPair>> ts;
        synchronized (listeners) {
            ts = new ArrayList<Map.Entry<String, TaskListenerPair>>(listeners.entrySet());
        }
        logger.info(service.toString());
        for (Map.Entry<String, TaskListenerPair> e: ts) {
            org.globus.cog.abstraction.interfaces.Service service2 = e.getValue().task.getService(0);
            logger.info(service2.toString());
            if (service2.equals(service)) {
                notificationReceived(e.getKey(), 
                    new StatusImpl(Status.FAILED, msg, null), null, null);
            }
        }
    }
    
    private static final class ExtendedStatus {
        public final Status status;
        public final String out;
        public final String err;
        
        public ExtendedStatus(Status status, String out, String err) {
            this.status = status;
            this.out = out;
            this.err = err;
        }
    }
    
    private static final class TaskListenerPair {
        public final Task task;
        public final List<ExtendedStatusListener> listeners;
        
        public TaskListenerPair(Task task, ExtendedStatusListener listener) {
            this.task = task;
            this.listeners = new LinkedList<ExtendedStatusListener>();
            this.listeners.add(listener);
        }
        
        public void addListener(ExtendedStatusListener l) {
            this.listeners.add(l);
        }
    }
}
