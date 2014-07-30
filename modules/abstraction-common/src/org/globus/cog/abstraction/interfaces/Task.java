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

import java.io.File;
import java.util.Collection;

import org.globus.cog.abstraction.xml.MarshalException;

/**
 * A <code>Task</code> is the atomic unit of execution in the Java CoG Kit
 * abstractions framework. It represents a generic Grid functionality such as
 * remote job execution, file transfer request, file access operation, or
 * information query.
 * <p>
 * It is a container object encapsulating the task identity, task execution
 * status, task specification, remote service(s), task output, task submission-,
 * and completion-times.
 */
public interface Task extends ExecutableObject, Cloneable {

    /**
     * Represents a remote job submission task
     */
    public static final int JOB_SUBMISSION = 1;

    /**
     * Represents a file transfer task between two file servers
     */
    public static final int FILE_TRANSFER = 2;

    /**
     * Represents an information query task. Not implemented at this time
     */
    public static final int INFORMATION_QUERY = 3;

    /**
     * Represents a file operation task
     */
    public static final int FILE_OPERATION = 4;
    
    /**
     * Represents a WS invocation task
     */
    public static final int WS_INVOCATION = 5;

    /**
     * Sets the type of this <code>Task</code>. Supported task types include:
     * <code>JOB_SUBMISSION</code>,<code>FILE_TRANSFER</code>, and
     * <code>FILE_OPERATION</code>.
     * 
     * @param type
     *            the type of this <code>Task</code>.
     */
    public void setType(int type);

    /**
     * Retruns the type of this <code>Task</code>.
     */
    public int getType();

    /**
     * @deprecated Use {@link Service#setProvider(String)}instead.
     */
    public void setProvider(String provider);

    /**
     * @deprecated Use {@link Service#getProvider()}instead.
     */
    public String getProvider();

    /**
     * Sets one of the {@link Service}required to execute this task. The index
     * at which this service is inserted determines its semantic role in the
     * execution of the task. A task can require more than one service for
     * execution. Thus, services are an ordered list.
     * 
     * @param index
     *            the index representing the position of the service in the
     *            ordered list.
     * @param service
     *            the remote Grid service.
     */
    public void setService(int index, Service service);

    /**
     * Adds the {@link Service}to the tail of the ordered list of services.
     * 
     * @param service
     *            the remote Grid service
     */
    public void addService(Service service);

    /**
     * Removes the {@link Service}from the given position in the ordered list
     * of Grid services.
     * 
     * @param index
     *            the index representing the position of the service in the
     *            ordered list.
     * @return the remote Grid service.
     */
    public Service removeService(int index);

    /**
     * Removes all the services associated with this task.
     * 
     * @return a <code>Collection</code> of services
     */
    public Collection<Service> removeAllServices();

    /**
     * Removes all the services associated with this task that also belongs to
     * the given <code>Collection</code> of services.
     * 
     * @return a <code>Collection</code> of services
     */
    public void removeService(Collection<Service> collection);

    /**
     * Returns the {@link Service} at the given index of the ordered list of
     * services
     * 
     * @param index
     *            the index representing the position of the service in the
     *            ordered list.
     * @return the remote Grid service.
     */
    public Service getService(int index);

    /**
     * Returns all the services associated with this task.
     * 
     * @return a <code>Collection</code> of services
     */
    public Collection<Service> getAllServices();

    /**
     * Sets the maximum number of services required for this task. For example,
     * the <code>JOB_SUBMISSION</code> task requires a maximum of 1 service by
     * default, where as the <code>FILE_TRANSFER</code> task requires a
     * maximum of 2 services by default.
     * 
     * @param value
     *            the maximum number of services required
     */
    public void setRequiredService(int value);

    /**
     * Returns the maximum number of services required for this task.
     * 
     * @return number of services
     */
    public int getRequiredServices();

    /**
     * Sets the {@link Specification}describing the execution parameters of
     * this <code>Task</code>.
     * 
     * @param specification
     *            the execution specification associated with this
     *            <code>Task</code>.
     */
    public void setSpecification(Specification specification);

    /**
     * Returns the {@link Specification}associated with this <code>Task</code>.
     */
    public Specification getSpecification();

    /**
     * Sets the standard output produced by this <code>Task</code>. This
     * method is used by the {@link TaskHandler}to store the stdout of the
     * remote task execution if it is redirected to the local machine.
     */
    public void setStdOutput(String output);

    /**
     * Returns the standard output of the remote task execution.
     * 
     * @return the standard out of the remote task execution. null if no output
     *         was produced on the remote stdOut.
     */
    public String getStdOutput();

    /**
     * Sets the standard error produced by this <code>Task</code>. This
     * method is used by the {@link TaskHandler}to store the stderr of the
     * remote task execution if it is redirected to the local machine.
     */
    public void setStdError(String error);

    /**
     * Returns the standard error of the remote task execution.
     * 
     * @return the standard error of the remote task execution. null if no error
     *         was produced on the remote stderr.
     */
    public String getStdError();

    /**
     * Sets an attribute for this <code>Task</code>. The interpretation of
     * these attributes are {@link TaskHandler}specific and may not be utilized
     * at all by some handlers.
     * 
     * @param name
     *            a string representing the name of the attribute value an
     *            object representing the value of this attribute
     */
    public void setAttribute(String name, Object value);

    /**
     * Returns the value associated with the given attribute name.
     * 
     * @param name
     *            the name of the desired attribute
     * @return the value of the given attribute name. null if attribute not
     *         available
     */
    public Object getAttribute(String name);

    /**
     * Returns all the attribute names associated with this <code>Task</code>.
     * 
     * 
     * @return an enumeration of all the attribute names. null if no attribute
     *         available
     */
    public Collection<String> getAttributeNames();

    /**
     * Adds a listener to recieve the output events associated with this
     * <code>Task</code>. The listener reveives events related to the changes
     * in the stdout of this <code>Task</code>.
     * 
     * @param listener
     *            the output listener for this <code>Task</code>.
     */
    public void addOutputListener(OutputListener listener);

    /**
     * Removes the output listener.
     */
    public void removeOutputListener(OutputListener listener);

    /**
     * Checkpoints the current state of this <code>Task</code> in XML format.
     * Using the {@link org.globus.cog.abstraction.xml.TaskUnmarshaller}, the
     * checkpointed <code>Task</code> can once again be re-instantiated.
     * 
     * @param file
     *            the file to store the checkpointed task.
     * @throws MarshalException
     *             during an error in XML translation.
     */
    public void toXML(File file) throws MarshalException;

    public String toString();

    /**
     * Returns a boolean indicating if the task is unsubmitted.
     */
    public boolean isUnsubmitted();

    /**
     * Returns a boolean indicating if the task is active.
     */
    public boolean isActive();

    /**
     * Returns a boolean indicating if the task is completed.
     */
    public boolean isCompleted();

    /**
     * Returns a boolean indicating if the task is suspended.
     */
    public boolean isSuspended();

    /**
     * Returns a boolean indicating if the task is failed.
     */
    public boolean isFailed();

    /**
     * Returns a boolean indicating if the task is canceled.
     */
    public boolean isCanceled();
    
    /**
     * Blocks until the task reaches one of the terminal states (completed, failed or canceled)
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void waitFor() throws InterruptedException;
    
    public Object clone();
}
