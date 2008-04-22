//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class NotificationManager {
    public static final Logger logger = Logger
            .getLogger(NotificationManager.class);

    private static NotificationManager def;

    public static synchronized NotificationManager getDefault() {
        if (def == null) {
            def = new NotificationManager();
        }
        return def;
    }

    private Map tasks;
    private Map pending;

    public NotificationManager() {
        tasks = new HashMap();
        pending = new HashMap();
    }

    public void registerTask(String id, Task task) {
        LinkedList p;
        synchronized (tasks) {
            tasks.put(id, task);
            p = (LinkedList) pending.remove(id);
        }
        if (p != null) {
            Iterator i = p.iterator();
            while (i.hasNext()) {
                setStatus(task, (Status) i.next());
            }
        }
    }

    public void notificationReceived(String id, Status s) {
        Task task;
        synchronized (tasks) {
            if (s.isTerminal()) {
                task = (Task) tasks.remove(id);
            }
            else {
                task = (Task) tasks.get(id);
            }
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

    private void setStatus(Task t, Status s) {
        try {
            t.setStatus(s);
        }
        catch (Exception e) {
            logger.warn("Could not set task status", e);
        }
    }

    private void addPending(String id, Status status) {
        LinkedList p = (LinkedList) pending.get(id);
        if (p == null) {
            p = new LinkedList();
            pending.put(id, p);
        }
        p.addLast(status);
    }

}
