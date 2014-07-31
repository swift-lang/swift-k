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

package org.globus.cog.abstraction.interfaces;

import java.util.Enumeration;

import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;

/**
 * The <code>TaskGraph</code> enables remote execution of constituent
 * <code>ExecutableObject</code> s by maintaining the control dependencies
 * between them.
 * 
 * @deprecated Not being maintained
 */
public interface TaskGraphHandler {
    /**
     * Submits all {@link Task}objects (including those of children
     * {@link TaskGraph}) to the SAME {@link TaskHandler}.
     */
    public static final int CASCADED_TASK_HANDLER = 1;

    /**
     * Submits all tasks of a {@link TaskGraph}to the same {@link TaskHandler}.
     * However, tasks of different {@link TaskGraph}are submitted to DIFFERENT
     * {@link TaskHandler}respectively.
     */
    public static final int NON_CASCADED_TASK_HANDLER = 2;

    /**
     * Submits the given {@link TaskGraph}for execuiton. The
     * <code>TaskGraph</code> is submitted to the <code>TaskHandler</code>
     * in an asynchronous mode, whereby the method returns immediately after
     * submitting the task graph. The status and output of the tasks can be
     * monitored asynchronously by the
     * {@link ExecutableObject#addStatusListener(StatusListener)}and
     * {@link Task#addOutputListener(OutputListener)}methods respectively.
     * 
     * @param taskgraph
     *            the <code>TaskGraph</code> to be submitted
     * @throws IllegalSpecException
     *             when the {@link Specification}associated with any task in
     *             this code>TaskGraph</code> is illegal
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with with any
     *             task in this code>TaskGraph</code> is invalid
     * @throws InvalidServiceContactException
     *             when the {@link ServiceContact}associated with any task in
     *             this code>TaskGraph</code> is invalid
     * @throws TaskSubmissionException
     *             when generic errors occur during taskgraph submission
     */
    public void submit(TaskGraph taskgraph) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException;

    /**
     * Suspends an {@link ExecutableObject}that is currently active in the
     * taskgraph. A suspended object can be resumed using the
     * {@link TaskGraphHandler#resume(Identity)}method
     * 
     * @param identity
     *            the identity of the <code>ExecutableObject</code> to be
     *            suspended
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with
     *             <code>ExecutableObject</code> is invalid
     * @throws TaskSubmissionException
     *             when generic errors occur
     */
    public boolean suspend(Identity identity)
            throws InvalidSecurityContextException, TaskSubmissionException;

    /**
     * Resumes the execution of an {@link ExecutableObject}in the taskgraph
     * that was previously suspended by the
     * {@link TaskGraphHandler#suspend(Identity)}method.
     * 
     * @param identity
     *            the identity of the <code>ExecutableObject</code> to be
     *            suspended
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with
     *             <code>ExecutableObject</code> is invalid
     * @throws TaskSubmissionException
     *             when generic errors occur
     */
    public boolean resume(Identity identity)
            throws InvalidSecurityContextException, TaskSubmissionException;

    /**
     * Cancels the execution of an <code>ExecutableObject</code>.
     * 
     * @param identity
     *            the identity of the <code>ExecutableObject</code> to be
     *            suspended
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with
     *             <code>ExecutableObject</code> is invalid
     * @throws TaskSubmissionException
     *             when generic errors occur
     */
    public boolean cancel(Identity identity)
            throws InvalidSecurityContextException, TaskSubmissionException;

    public TaskGraph getGraph();

    public Enumeration getUnsubmittedNodes();

    public Enumeration getSubmittedNodes();

    public Enumeration getActiveNodes();

    public Enumeration getFailedNodes();

    public Enumeration getCompletedNodes();

    public Enumeration getSuspendedNodes();

    public Enumeration getCanceledNodes();

    public void setTaskHandlerPolicy(int policy);

    public int getTaskHandlerPolicy();
}