//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.Iterator;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.util.Queue;

public abstract class QueueProcessor extends Thread {
    private final Queue q;
    private Queue.Cursor cursor;
    private boolean shutdownFlag;
    private Iterator i;
    private boolean wrap;

    public QueueProcessor(String name) {
        super(name);
        q = new Queue();
    }

    public void enqueue(Task t) {
        synchronized (q) {
            q.enqueue(new AssociatedTask(t));
            q.notifyAll();
        }
    }

    public void shutdown() {
        shutdownFlag = true;
    }

    protected boolean getShutdownFlag() {
        return shutdownFlag;
    }

    protected final Queue getQueue() {
        return q;
    }

    protected final AssociatedTask take() throws InterruptedException {
        return (AssociatedTask) q.take();
    }

    protected final AssociatedTask next() throws InterruptedException {
        synchronized (q) {
            while (q.isEmpty()) {
                q.wait();
            }
            if (cursor == null) {
                cursor = q.cursor();
            }
            if (!cursor.hasNext()) {
                cursor.reset();
                wrap = true;
            }
            else {
            	wrap = false;
            }
            return (AssociatedTask) cursor.next();
        }
    }

    protected final boolean hasWrapped() {
        synchronized (q) {
            return wrap;
        }
    }

    protected final void remove() {
        synchronized (q) {
            if (cursor == null) {
                throw new IllegalThreadStateException(
                        "next() was never called");
            }
            else {
                cursor.remove();
            }
        }
    }

    protected int queuedTaskCount() {
        synchronized (q) {
            return q.size();
        }
    }
}
