//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 16, 2009
 */
package org.griphyn.vdl.karajan.lib;

import java.util.LinkedList;
import java.util.List;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferTask;
import org.globus.cog.abstraction.impl.common.task.FileTransferTaskHandler;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FuturesMonitor;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class FileCopier implements Future, StatusListener {
    private static final TaskHandler fth = new FileTransferTaskHandler();

    private FileTransferTask task;
    private List actions;
    private Exception exception;
    private boolean closed;

    public FileCopier(PhysicalFormat src, PhysicalFormat dst) {
        AbsFile fsrc = (AbsFile) src;
        AbsFile fdst = (AbsFile) dst;
        FileTransferSpecification fts = new FileTransferSpecificationImpl();
        fts.setDestinationDirectory(fdst.getDir());
        fts.setDestinationFile(fdst.getPath());
        fts.setSourceDirectory(fsrc.getDir());
        fts.setSourceFile(fsrc.getPath());
        fts.setThirdPartyIfPossible(true);
        task = new FileTransferTask();
        task.setSpecification(fts);
        task.setService(Service.FILE_TRANSFER_SOURCE_SERVICE, new ServiceImpl(
            fsrc.getProtocol(), new ServiceContactImpl(fsrc.getHost()), null));
        task.setService(Service.FILE_TRANSFER_DESTINATION_SERVICE,
            new ServiceImpl(fdst.getProtocol(), new ServiceContactImpl(fdst
                .getHost()), null));
        task.addStatusListener(this);
    }

    public synchronized void addModificationAction(EventListener target,
            Event event) {
        if (actions == null) {
            actions = new LinkedList();
        }
        EventTargetPair etp = new EventTargetPair(event, target);
        if (FuturesMonitor.debug) {
            FuturesMonitor.monitor.add(etp, this);
        }
        synchronized (actions) {
            actions.add(etp);
        }
        if (closed) {
            actions();
        }
    }

    public List getModificationActions() {
        return actions;
    }

    private void actions() {
        if (actions != null) {
            synchronized (actions) {
                java.util.Iterator i = actions.iterator();
                while (i.hasNext()) {
                    EventTargetPair etp = (EventTargetPair) i.next();
                    if (FuturesMonitor.debug) {
                        FuturesMonitor.monitor.remove(etp);
                    }
                    i.remove();
                    EventBus.post(etp.getTarget(), etp.getEvent());
                }
            }
        }
    }

    public void fail(FutureEvaluationException e) {
        this.exception = e;
        actions();
    }

    public Object getValue() throws ExecutionException {
        return null;
    }

    public boolean isClosed() {
        return closed;
    }

    public void start() throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        fth.submit(task);
    }

    public void close() {
        closed = true;
        actions();
    }

    public void statusChanged(StatusEvent event) {
        Status s = event.getStatus();
        if (s.isTerminal()) {
            if (s.getStatusCode() == Status.COMPLETED) {
                close();
            }
            else {
                this.exception = new Exception(s.getMessage(), s.getException());
                close();
            }
        }
    }
    
    public Exception getException() {
        return exception;
    }
}
