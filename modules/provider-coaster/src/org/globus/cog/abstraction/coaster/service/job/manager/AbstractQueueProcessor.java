//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.channels.ChannelContext;

public abstract class AbstractQueueProcessor extends Thread implements QueueProcessor {
    private final BlockingQueue<AssociatedTask> q;
    private boolean shutdownFlag;
    private boolean wrap;
    private final LocalTCPService localService;

    public AbstractQueueProcessor(String name, LocalTCPService localService) {
        super(name);
        this.localService = localService;
        q = new LinkedBlockingQueue<AssociatedTask>();
    }

    public LocalTCPService getLocalService() {
        return localService;
    }

    public void enqueue(Task t) {
        if (shutdownFlag) {
            throw new IllegalStateException("Queue is shut down");
        }
        q.offer(new AssociatedTask(t));
    }

    public void shutdown() {
        shutdownFlag = true;
    }

    protected boolean getShutdownFlag() {
        return shutdownFlag;
    }

    protected final AssociatedTask take() throws InterruptedException {
        return q.take();
    }

    protected final boolean hasWrapped() {
        return wrap;
    }

    protected int queuedTaskCount() {
        return q.size();
    }

    @Override
    public void setClientChannelContext(ChannelContext channelContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void start() {
        if (this.isAlive()) {
            throw new RuntimeException(this.getName() + " already started");
        }
        super.start();
    }
}
