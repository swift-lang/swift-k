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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.GenericTaskHandler;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.ChangeListener;
import org.globus.cog.abstraction.interfaces.Dependency;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.interfaces.TaskGraphHandler;

public class TaskGraphHandlerImpl implements TaskGraphHandler, StatusListener,
        ChangeListener {
    static Logger logger = Logger.getLogger(TaskGraphHandlerImpl.class
            .getName());

    private TaskGraph taskgraph = null;
    private Vector available = null;
    private Vector pending = null;
    private Hashtable handlerMapping;
    private Hashtable statusMapping;
    private int taskHandlerPolicy = TaskGraphHandler.NON_CASCADED_TASK_HANDLER;
    private GenericTaskHandler taskHandler;

    public TaskGraphHandlerImpl() {
        this.available = new Vector();
        this.pending = new Vector();
        this.handlerMapping = new Hashtable();
        this.statusMapping = new Hashtable();
        this.taskHandler = new GenericTaskHandler();
    }

    public TaskGraphHandlerImpl(GenericTaskHandler taskHandler) {
        this.available = new Vector();
        this.pending = new Vector();
        this.handlerMapping = new Hashtable();
        this.statusMapping = new Hashtable();
        this.taskHandler = taskHandler;
    }

    public void submit(TaskGraph taskgraph) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        if (this.taskgraph != null) {
            throw new TaskSubmissionException(
                    "TaskGraphHandler cannot handle multiple graphs simultaneously");
        } else {
            this.taskgraph = taskgraph;
            if (!TaskGraphVerifier.verify(this.taskgraph)) {
                throw new TaskSubmissionException("TaskGraph is not acyclic");
            } else {
                organizeNodes();
                this.taskgraph.addChangeListener(this);
                /*
                 * shallow copy required to allow concurrent modification of the
                 * this.available Vector in the statusChanged() method
                 */
                Vector avail = new Vector(this.available);
                Iterator iterator = avail.iterator();
                while (iterator.hasNext()) {
                    ExecutableObject executableObject = (ExecutableObject) iterator
                            .next();
                    submitExecutableObject(executableObject);
                }
            }
        }
    }

    public boolean suspend(Identity identity)
            throws InvalidSecurityContextException, TaskSubmissionException {
        return invokeFunction(identity, 1);
    }

    public boolean resume(Identity identity)
            throws InvalidSecurityContextException, TaskSubmissionException {
        return invokeFunction(identity, 2);
    }

    public boolean cancel(Identity identity)
            throws InvalidSecurityContextException, TaskSubmissionException {
        return invokeFunction(identity, 3);
    }

    public TaskGraph getGraph() {
        return this.taskgraph;
    }

    public synchronized void statusChanged(StatusEvent event) {
        ExecutableObject executableObject = event.getSource();
        Status status = event.getStatus();

        this.statusMapping.put(executableObject.getIdentity(), status);
        setGraphStatus();

        if (status.getStatusCode() == Status.COMPLETED
                || status.getStatusCode() == Status.FAILED) {
            this.available.remove(executableObject);

            if (executableObject.getObjectType() == ExecutableObject.TASK) {
                try {
                    this.taskHandler.remove((Task) executableObject);
                } catch (Exception e) { /* do nothing */
                }
            } else {
                this.handlerMapping.remove(executableObject.getIdentity());
            }

            if ((status.getStatusCode() == Status.COMPLETED)
                    || (status.getStatusCode() == Status.FAILED && this.taskgraph
                            .getFailureHandlingPolicy() == TaskGraph.ContinueOnFailure)) {
                try {
                    handleDependents(executableObject);
                } catch (Exception e) {
                    logger.error("Unable to handle dependents", e);
                }
            }
        }
    }

    public Enumeration getUnsubmittedNodes() {
        return getNodesWithStatus(Status.UNSUBMITTED);
    }

    public Enumeration getSubmittedNodes() {
        return getNodesWithStatus(Status.SUBMITTED);
    }

    public Enumeration getActiveNodes() {
        return getNodesWithStatus(Status.ACTIVE);
    }

    public Enumeration getFailedNodes() {
        return getNodesWithStatus(Status.FAILED);
    }

    public Enumeration getCompletedNodes() {
        return getNodesWithStatus(Status.COMPLETED);
    }

    public Enumeration getSuspendedNodes() {
        return getNodesWithStatus(Status.SUSPENDED);
    }

    public Enumeration getCanceledNodes() {
        return getNodesWithStatus(Status.CANCELED);
    }

    private void submitExecutableObject(ExecutableObject executableObject)
            throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {
        // submit only if unsubmitted
      //  if (executableObject.getStatus().getStatusCode() == Status.UNSUBMITTED) {

            // if the executableObject is a Task
            if (executableObject.getObjectType() == ExecutableObject.TASK) {
                Task task = (Task) executableObject;
                task.addStatusListener(this);
                this.taskHandler.submit(task);
            }

            // if the executableObject is a TaskGraph
            else if (executableObject.getObjectType() == ExecutableObject.TASKGRAPH) {
                TaskGraph g = (TaskGraph) executableObject;
                g.addStatusListener(this);
                TaskGraphHandler handler;
                if (this.taskHandlerPolicy == TaskGraphHandler.CASCADED_TASK_HANDLER) {
                    handler = new TaskGraphHandlerImpl(this.taskHandler);
                } else {
                    handler = new TaskGraphHandlerImpl();
                }
                this.handlerMapping
                        .put(executableObject.getIdentity(), handler);
                handler.submit(g);
            } else {
                throw new IllegalSpecException("Illegal ExecutableObject Type");
            }
        //}
    }

    private void organizeNodes() {
        Enumeration en = this.taskgraph.elements();
        while (en.hasMoreElements()) {
            ExecutableObject executableObject = (ExecutableObject) en
                    .nextElement();
            this.statusMapping.put(executableObject.getIdentity(),
                    executableObject.getStatus());

            /*
             * If this node is completed, do nothing else if all its dependsOn
             * nodes are completed, put it in AVAILABLE else put it in PENDING
             */
            if (executableObject.getStatus().getStatusCode() == Status.COMPLETED) {
                // do nothing
                continue;
            } else if (isAvailable(executableObject)) {
                this.available.add(executableObject);
            } else {
                this.pending.add(executableObject);
            }
        }
    }

    private boolean isAvailable(ExecutableObject executableObject) {
        boolean available = true;
        // if a dependency is specified then process it.
        Dependency dependency = this.taskgraph.getDependency();
        if (dependency != null) {
            Enumeration en = dependency.getDependsOn(executableObject);
            while (en.hasMoreElements()) {
                ExecutableObject eo = (ExecutableObject) en.nextElement();
                // if there exists a dangling dependency, do not process the
                // dependent
                if (eo == null) {
                    available = false;
                    break;
                }

                if ((eo.getStatus().getStatusCode() == Status.COMPLETED)
                        || (eo.getStatus().getStatusCode() == Status.FAILED && this.taskgraph
                                .getFailureHandlingPolicy() == TaskGraph.ContinueOnFailure)) {
                    continue;
                } else {
                    available = false;
                    break;
                }
            }
        }
        return available;
    }

    private Enumeration getNodesWithStatus(int status) {
        Vector list = new Vector();
        Enumeration en = this.taskgraph.elements();
        while (en.hasMoreElements()) {
            ExecutableObject executableObject = (ExecutableObject) en
                    .nextElement();
            if (executableObject.getStatus().getStatusCode() == status) {
                list.add(executableObject);
            }
        }
        return list.elements();
    }

    private boolean invokeFunction(Identity identity, int functionCode)
            throws InvalidSecurityContextException, TaskSubmissionException {
        /*
         * LOGIC: if the id belongs to this taskgraph, invoke function on all
         * nodes else check if this identity belongs to a task. if so invoke
         * cancel on its handler else if it belongs to a child tg, pass it to
         * its handler or pass it to all tgh in a dfs manner
         */

        if (identity.equals(this.taskgraph.getIdentity())) {
            // invoke the function on every executable node in this task graph
            Enumeration en = this.taskgraph.elements();
            while (en.hasMoreElements()) {
                ExecutableObject eo = (ExecutableObject) en.nextElement();
                if (eo.getObjectType() == ExecutableObject.TASK) {
                    switch (functionCode) {
                    case 1:
                        this.taskHandler.suspend((Task) eo);
                        break;
                    case 2:
                        this.taskHandler.resume((Task) eo);
                        break;
                    case 3:
                        this.taskHandler.cancel((Task) eo);
                        break;
                    default:
                        break;
                    }
                } else if (eo.getObjectType() == ExecutableObject.TASKGRAPH) {
                    TaskGraphHandler tgh = (TaskGraphHandler) this.handlerMapping
                            .get(eo.getIdentity());
                    if (tgh != null) {
                        switch (functionCode) {
                        case 1:
                            tgh.suspend(eo.getIdentity());
                            break;
                        case 2:
                            tgh.resume(eo.getIdentity());
                            break;
                        case 3:
                            tgh.cancel(eo.getIdentity());
                            break;
                        default:
                            break;
                        }
                    }
                } else {
                    throw new TaskSubmissionException(
                            "Invalid ExecutableObject type: "
                                    + eo.getObjectType());
                }
            }
            return true;
        }

        //		check if the identity belongs to a direct child
        else if (this.taskgraph.contains(identity)) {
            ExecutableObject eo = this.taskgraph.get(identity);
            if (eo.getObjectType() == ExecutableObject.TASK) {
                switch (functionCode) {
                case 1:
                    this.taskHandler.suspend((Task) eo);
                    break;
                case 2:
                    this.taskHandler.resume((Task) eo);
                    break;
                case 3:
                    this.taskHandler.cancel((Task) eo);
                    break;
                default:
                    break;
                }
            } else if (eo.getObjectType() == ExecutableObject.TASKGRAPH) {
                TaskGraphHandler tgh = (TaskGraphHandler) this.handlerMapping
                        .get(eo.getIdentity());
                if (tgh != null) {
                    switch (functionCode) {
                    case 1:
                        tgh.suspend(eo.getIdentity());
                        break;
                    case 2:
                        tgh.resume(eo.getIdentity());
                        break;
                    case 3:
                        tgh.cancel(eo.getIdentity());
                        break;
                    default:
                        break;
                    }
                }
            } else {
                throw new TaskSubmissionException(
                        "Invalid ExecutableObject type: " + eo.getObjectType());
            }
            return true;
        }

        // perform a depth first search for the appropriate handler
        else {
            Enumeration en = this.handlerMapping.elements();
            while (en.hasMoreElements()) {
                TaskGraphHandler tgh = (TaskGraphHandler) en.nextElement();
                if (tgh != null) {
                    switch (functionCode) {
                    case 1:
                        if (tgh.suspend(identity)) {
                            // found the appropriate handler
                            return true;
                        }
                        break;
                    case 2:
                        if (tgh.resume(identity)) {
                            // found the appropriate handler
                            return true;
                        }
                        break;
                    case 3:
                        if (tgh.cancel(identity)) {
                            // found the appropriate handler
                            return true;
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
        }
        return false;
    }

    private void setGraphStatus() {
        Enumeration en = this.statusMapping.elements();
        int completed = 0;
        int canceled = 0;
        int suspended = 0;
        int active = 0;
        int resumed = 0;
        int unsubmitted = 0;
        int submitted = 0;
        Status status;
        while (en.hasMoreElements()) {
            status = (Status) en.nextElement();
            if (status.getStatusCode() == Status.FAILED) {
                if (this.taskgraph.getStatus().getPrevStatusCode() != Status.FAILED) {
                    this.taskgraph.setStatus(Status.FAILED);
                }
                return;
            }
            switch (status.getStatusCode()) {
            case Status.ACTIVE:
                active++;
                break;
            case Status.CANCELED:
                canceled++;
                break;
            case Status.COMPLETED:
                completed++;
                break;
            case Status.RESUMED:
                resumed++;
                break;
            case Status.SUBMITTED:
                submitted++;
                break;
            case Status.SUSPENDED:
                suspended++;
                break;
            case Status.UNSUBMITTED:
                unsubmitted++;
                break;
            default:
                break;
            }
        }

        int size = this.taskgraph.getSize();

        /*
         * First check if it is unsubmited If not, see if any task in the
         * subtree is suspended. If not , see if any task is actively running If
         * not, check if any task is submitted to a handler If not check if
         * completed If none of the above (impossible), do not change the
         * current status
         */

        if (unsubmitted == size) {
            if (this.taskgraph.getStatus().getStatusCode() != Status.UNSUBMITTED) {
                this.taskgraph.setStatus(Status.UNSUBMITTED);
            }
            return;
        }

        if (suspended > 0) {
            if (this.taskgraph.getStatus().getStatusCode() != Status.SUSPENDED) {
                this.taskgraph.setStatus(Status.SUSPENDED);
            }
            return;
        }

        if ((active + resumed > 0)
                || ((completed + canceled) > 1 && (completed + canceled) < size)) {
            if (this.taskgraph.getStatus().getStatusCode() != Status.ACTIVE) {
                this.taskgraph.setStatus(Status.ACTIVE);
            }
            return;
        }

        if (submitted > 0) {
            if (this.taskgraph.getStatus().getStatusCode() != Status.SUBMITTED
                    && this.taskgraph.getStatus().getStatusCode() != Status.ACTIVE) {
                this.taskgraph.setStatus(Status.SUBMITTED);
            }
            return;
        }

        if (completed + canceled == size) {
            if (this.taskgraph.getStatus().getStatusCode() != Status.COMPLETED) {
                this.taskgraph.setStatus(Status.COMPLETED);
            }
            return;
        }
    }

    private void handleDependents(ExecutableObject executableObject)
            throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {
        Dependency dependency = this.taskgraph.getDependency();
        if (dependency == null) {
            // No dependency exists -- true for a Set
            return;
        }

        Enumeration dependents = dependency.getDependents(executableObject);
        while (dependents.hasMoreElements()) {
            // for each dependent check if it is available for submission
            ExecutableObject dependentEO = (ExecutableObject) dependents
                    .nextElement();

            // Add the status of the dependent to the statusMapping
            // This is done to ensure that statusMapping works correctly
            // for "live" taskgraphs (queues and sets).
            this.statusMapping.put(dependentEO.getIdentity(), dependentEO
                    .getStatus());

            if (isAvailable(dependentEO)) {
                // since the task is avaliable, move it to available list
                this.pending.remove(dependentEO);
                try {
                    this.available.add(dependentEO);
                } catch (Exception ex) {
                    // continue with the next dependent.
                    continue;
                }
                submitExecutableObject(dependentEO);
            }
        }
        setGraphStatus();
    }

    protected boolean executeIfAvailable(ExecutableObject node)
            throws Exception {
        boolean available = true;
        // process only if unsubmitted
        if (node.getStatus().getStatusCode() == Status.UNSUBMITTED) {
            Identity identity = node.getIdentity();

            this.statusMapping.put(identity, node.getStatus());
            setGraphStatus();

            if (isAvailable(node)) {
                // since the node is avaliable, move it to available list
                this.available.add(node);
                submitExecutableObject(node);
            } else {
                // If task not available, then put it in pending
                this.pending.add(node);
                available = false;
            }
        }
        return available;
    }

    public void graphChanged(ChangeEvent event) throws Exception {
        if (event.getType() == ChangeEvent.ADD) {
            ExecutableObject node = event.getNode();
            executeIfAvailable(node);
        }
    }

    public void setTaskHandlerPolicy(int policy) {
        this.taskHandlerPolicy = policy;
    }

    public int getTaskHandlerPolicy() {
        return this.taskHandlerPolicy;
    }
}