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

import java.util.Map;

import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;

/**
 * Implements the semantics to execute the given {@link Task} in a separate
 * thread, thereby not blocking the calls to the {@link TaskHandler}. The
 * <code>DelegatedTaskHandler</code> can handle only one task at a time.
 */
public interface DelegatedTaskHandler {

    /**
     * This method is responsible for submitting the given <code>Task</code>
     * to the remote <code>Service</code> in compliance with the appropriate
     * provider. It executes in a seperate thread so that it doesnt block the
     * submission of other tasks.
     *
     * @param task
     *            the task to be executed
     * @throws IllegalSpecException
     *             when the {@link Specification}does not confirm to the type
     *             of the task and the given provider.
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with the task in
     *             invalid.
     * @throws InvalidServiceContactException
     *             when the {@link ServiceContact}of the remote service in
     *             invalid.
     * @throws TaskSubmissionException
     *             when a generic exception occurs prohibiting the submission of
     *             the task.
     */
    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException;

    /**
     * Suspends the currently active task. A suspended task can be resumed using
     * the {@link DelegatedTaskHandler#resume()}method
     *
     * @param task
     *            the <code>Task</code> to be suspended
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with the task is
     *             invalid
     * @throws TaskSubmissionException
     *             when generic errors occur
     */
    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException;

    /**
     * Resumes the execution of a task that was previously suspended by the
     * {@link DelegatedTaskHandler#suspend()}method.
     *
     * @param task
     *            the <code>Task</code> to be resumed
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with the task is
     *             invalid
     * @throws TaskSubmissionException
     *             when generic errors occur
     */
    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException;

    /**
     * Cancels the execution of a task that was previously submitted by the
     * {@link DelegatedTaskHandler#submit(Task)}method. Tasks once canceled
     * cannot be resumed for execution later.
     *
     * @param task
     *            the <code>Task</code> to be canceled
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with the task is
     *             invalid
     * @throws TaskSubmissionException
     *             when generic errors occur
     */
    public void cancel() throws InvalidSecurityContextException,
            TaskSubmissionException;

    public void cancel(String message) throws InvalidSecurityContextException,
            TaskSubmissionException;
    
    public void setAttributes(Map<String, Object> attributes);
}
