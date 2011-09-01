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
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.ServiceContact;
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
    private Map<String, Task> tasks;
    
    /**
       Map from Task IDs to Status updates that arrived before the 
       Task existed in the Map {@link tasks}
     */
    private Map<String, List<Status>> pending;
    private long lastNotificationTime;

    public NotificationManager() {
        tasks = new HashMap<String, Task>();
        pending = new HashMap<String, List<Status>>();
        lastNotificationTime = System.currentTimeMillis();
    }

    public void registerTask(String id, Task task) {
        List<Status> p;
        synchronized (tasks) {
            tasks.put(id, task);
            p = pending.remove(id);
        }
        if (p != null)
            for (Status status : p)
                setStatus(task, status);
    }

    public void notificationReceived(String id, Status s) {
        if (logger.isDebugEnabled())
            logger.debug("recvd: for: " + id + " " + s);
        Task task;
        synchronized (tasks) {
            if (s.isTerminal()) {
                task = tasks.remove(id);
            }
            else {
                task = tasks.get(id);
            }
            lastNotificationTime = System.currentTimeMillis();
        }
        if (task != null) {
            setStatus(task, s);
        }
        else {
            synchronized (tasks) {
                addPending(id, s);
            }
        }
    }

    public long getIdleTime() {
        synchronized (tasks) {
            if (tasks.size() == 0 && lastNotificationTime != 0) {
                return System.currentTimeMillis() - lastNotificationTime;
            }
            else {
                return 0;
            }
        }
    }

    public void notIdle() {
        synchronized (tasks) {
            lastNotificationTime = System.currentTimeMillis();
        }
    }

    public int getActiveTaskCount() {
        synchronized (tasks) {
            return tasks.size();
        }
    }

    private void setStatus(Task t, Status s) {
        try {
            t.setStatus(s);
        }
        catch (Exception e) {
            logger.warn("Could not set task status", e);
        }
    }

    private void addPending(String id, Status status) {
        List<Status> p = pending.get(id);
        if (p == null) {
            p = new LinkedList<Status>();
            pending.put(id, p);
        }
        p.add(status);
    }

    public void serviceTaskEnded(ServiceContact contact1, 
                                 String msg) {
        List<Task> ts;
        synchronized (tasks) {
            ts = new ArrayList<Task>(tasks.values());
        }
        logger.info(contact1.toString());
        for (Task t : ts) {
            ServiceContact contact2 = 
                t.getService(0).getServiceContact();
            logger.info(contact2.toString());
            if (contact2.equals(contact1))
                notificationReceived(t.getIdentity().toString(), 
                                     new StatusImpl(Status.FAILED, 
                                                    msg, null));
        }
    }
}
