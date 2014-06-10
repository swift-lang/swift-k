// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import java.util.Collection;

import org.globus.cog.abstraction.impl.common.task.ActiveTaskException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;

/**
 * The <code>TaskHandler</code> can be viewed as an adaptor that translates
 * the abstract definitions of a {@link Task}into implementation-specific
 * constructs understood by the backend Grid services.
 * <p>
 * For example, a GT4 TaskHandler will extract the appropriate attributes from
 * the <code>Task</code> and make the necessary calls to the remote Grid
 * service factory, retrieve the Grid service handle, and interact with the
 * newly created service instance. Symmetric translations would be done for
 * other Grid implementations. A handler is specific to the backed
 * implementation (GT2, GT3, GT4, Condor, or SSH) and is the only part of
 * abstractions framework that needs to be extended for supporting additional
 * Grid implementations.
 */
public interface TaskHandler {
    /**
     * Represents a generic task handler
     */
    public static final int GENERIC = 1;

    /**
     * Represents a task handler that can handle remote job submission tasks
     */
    public static final int EXECUTION = 2;

    /**
     * Represents a task handler that can handle file operation tasks
     */
    public static final int FILE_OPERATION = 3;

    /**
     * Represents a task handler that can handle file transfer tasks
     */
    public static final int FILE_TRANSFER = 4;

    /**
     * Sets the type of tasks handled by this <code>TaskHandler</code>
     * 
     * @param type
     *            an integer representing the type of <code>TaskHandler</code>.
     *            Valid options are {@link TaskHandler#GENERIC},
     *            {@link TaskHandler#EXECUTION},
     *            {@link TaskHandler#FILE_OPERATION}, and
     *            {@link TaskHandler#FILE_TRANSFER}
     */
    public void setType(int type);

    /**
     * Returns the type of tasks handled by this <code>TaskHandler</code>
     */
    public int getType();
    
    public String getName();
    
    public void setName(String provider);

    /**
     * Submits the given {@link Task}for execuiton. The <code>Task</code> is
     * submitted to the <code>TaskHandler</code> in an asynchronous mode,
     * whereby the method returns immediately after submitting the task to the
     * remote machine. The status and output of the task can be monitored
     * asynchronously by the
     * {@link ExecutableObject#addStatusListener(StatusListener)}and
     * {@link Task#addOutputListener(OutputListener)}methods respectively.
     * 
     * If the submission fails, the task handler should throw an exception
     * and not set the status of the task to {@link Task#FAILED}.
     * 
     * @param task
     *            the <code>Task</code> to be submitted
     * @throws IllegalSpecException
     *             when the {@link Specification} associated with the task is
     *             illegal
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext} associated with the task is
     *             invalid
     * @throws InvalidServiceContactException
     *             when the {@link ServiceContact} associated with the task is
     *             invalid
     * @throws TaskSubmissionException
     *             when a generic errors occur during task submission
     */
    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException;

    /**
     * Suspends the currently active task. A suspended task can be resumed using
     * the {@link TaskHandler#resume(Task)}method
     * 
     * @param task
     *            the <code>Task</code> to be suspended
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with the task is
     *             invalid
     * @throws TaskSubmissionException
     *             when generic errors occur
     */
    public void suspend(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException;

    /**
     * Resumes the execution of a task that was previously suspended by the
     * {@link TaskHandler#suspend(Task)}method.
     * 
     * @param task
     *            the <code>Task</code> to be resumed
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with the task is
     *             invalid
     * @throws TaskSubmissionException
     *             when generic errors occur
     */
    public void resume(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException;

    /**
     * Cancels the execution of a task that was previously submitted by the
     * {@link TaskHandler#submit(Task)}method. Tasks once canceled cannot be
     * resumed for execution later.
     * 
     * @param task
     *            the <code>Task</code> to be canceled
     * @throws InvalidSecurityContextException
     *             when the {@link SecurityContext}associated with the task is
     *             invalid
     * @throws TaskSubmissionException
     *             when generic errors occur
     */
    public void cancel(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException;
    
    public void cancel(Task task, String message) throws InvalidSecurityContextException,
            TaskSubmissionException;

    /**
     * Removes the given <code>Task</code> from the <code>TaskHandler</code>
     * cache. After invoking this method, the <code>TaskHandler</code> will
     * lose all references to the given <code>Task</code>
     * 
     * @param task
     *            the <code>Task</code> object to be removed from the cache
     * @throws ActiveTaskException
     *             when the task to be removed in an {@link Status#ACTIVE}
     *             state.
     */
    public void remove(Task task) throws ActiveTaskException;

    /**
     * Returns a collection of all the <code>Tasks</code> submitted to this
     * <code>TaskHandler</code>
     * 
     * @return a collection of {@link Task}objects
     */
    public Collection<Task> getAllTasks();

    /**
     * Returns a collection of all the <code>Tasks</code> submitted to this
     * <code>TaskHandler</code> that are currently in the
     * {@link Status#ACTIVE}state
     * 
     * @return a collection of {@link Task}objects
     */
    public Collection<Task> getActiveTasks();

    /**
     * Returns a collection of all the <code>Tasks</code> submitted to this
     * <code>TaskHandler</code> that are currently in the
     * {@link Status#FAILED}state
     * 
     * @return a collection of {@link Task}objects
     */
    public Collection<Task> getFailedTasks();

    /**
     * Returns a collection of all the <code>Tasks</code> submitted to this
     * <code>TaskHandler</code> that are currently in the
     * {@link Status#COMPLETED}state
     * 
     * @return a collection of {@link Task}objects
     */
    public Collection<Task> getCompletedTasks();

    /**
     * Returns a collection of all the <code>Tasks</code> submitted to this
     * <code>TaskHandler</code> that are currently in the
     * {@link Status#SUSPENDED}state
     * 
     * @return a collection of {@link Task}objects
     */
    public Collection<Task> getSuspendedTasks();

    /**
     * Returns a collection of all the <code>Tasks</code> submitted to this
     * <code>TaskHandler</code> that are currently in the
     * {@link Status#RESUMED}state
     * 
     * @return a collection of {@link Task}objects
     */
    public Collection<Task> getResumedTasks();

    /**
     * Returns a collection of all the <code>Tasks</code> submitted to this
     * <code>TaskHandler</code> that are currently in the
     * {@link Status#CANCELED}state
     * 
     * @return a collection of {@link Task}objects
     */
    public Collection<Task> getCanceledTasks();
}