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

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * The <code>JobSpecification</code> represents all the parameters required
 * for the remote job execution <code>Task</code>.
 */
public interface JobSpecification extends Specification {

    /**
     * Sets the name of the executable to be run remotely.
     *
     * @param executable
     *            a string representing the absolute location of the executable.
     */
    public void setExecutable(String executable);

    /**
     * Returns the absolute location of the executable
     */
    public String getExecutable();

    /**
     * Allows specifying the location of the executable. The default is
     * {@link FileLocation.REMOTE}.
     *
     * @throws IllegalArgumentException if the location is {@link FileLocation.MEMORY}
     */
    public void setExecutableLocation(FileLocation location);

    /**
     * Retrieves the executable location. By default it is
     * {@link FileLocation.REMOTE}
     */
    public FileLocation getExecutableLocation();

    /**
     * Sets the working directory on the remote machine.
     *
     * @param directory
     *            a string representing the absolute path name of the remote
     *            working directory.
     */
    public void setDirectory(String directory);

    /**
     * Returns the absolute path name of the remote working directory.
     */
    public String getDirectory();

    /**
     * Sets the comandline arguments for the remote executable. A set of space
     * seperated arguments can be supplied: "arg1 arg2 agr3 ..."
     *
     * @param arguments
     *            a string representing the set of arguments for the remote
     *            executable.
     */
    public void setArguments(String arguments);

    /**
     * Sets the command line arguments for the job
     *
     * @param a
     *            list with the values of the arguments
     */
    void setArguments(List<String> arguments);

    /**
     * Returns the set of space-separated arguments supplied for the remote
     * executable.
     */
    public String getArguments();

    /**
     * Returns the set of space-separated arguments supplied for the remote
     * executable.
     */
    public String getArgumentsAsString();

    /**
     * Adds a commandline argument for the remote exectable. Multiple
     * commandline arguments can be set by making multiple calls to this
     * function.
     *
     * @param argument
     *            a string representing an argument for the remote executable.
     */
    public void addArgument(String argument);

    /**
     * Adds a commandline argument for the remote exectable at the given index.
     * Multiple commandline arguments can be set by making multiple calls to
     * this function.
     *
     * @param index
     *            the index in the argument list
     * @param argument
     *            a string representing an argument for the remote executable.
     *
     * @deprecated this method allows the creation non-contiguous argument
     *             lists, which makes no sense. Use setArgument(index, argument)
     *             to change an existing argument.
     */
    @Deprecated
    public void addArgument(int index, String argument);

    /**
     * Replaces an existing argument in the argument list
     *
     * @param index
     *            the index of the argument to replace
     * @param argument
     *            the value of the argument
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index does not point to an existing argument
     */
    public void setArgument(int index, String argument);

    /**
     * Removes the given argument from the argument list. The arguments
     * following the removed arument up to the end of the argument list will be
     * shifted down.
     *
     * @param argument
     *            the String argument to be removed
     *
     * @deprecated Removing arguments by value is error prone. Use index based
     *             removal.
     */
    @Deprecated
    public void removeArgument(String argument);

    /**
     * Removes the argument at the given index from the argument list
     *
     * @param index
     *            the index of the argument to be removed
     */
    public String removeArgument(int index);

    /**
     * Returns a Vector representing the set of commandline arguments for the
     * executable.
     *
     * @deprecated Moving to the new collections classes
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Vector getArgumentsAsVector();

    /**
     *
     * @return Returns the list of arguments. Modifications to the list returned
     *         by this method will affect the argument list in the
     *         specification.
     */
    public List<String> getArgumentsAsList();

    /**
     * Adds an environment variable to the remote execution environment.
     * Multiple environment variables can be created by making multiple calls to
     * this method.
     *
     * @param name
     *            the name of the environment variable
     * @param value
     *            the value of the environment variable
     */
    public void addEnvironmentVariable(String name, String value);

    public void addEnvironmentVariable(String name, int i);
    
    @Deprecated
    public void setEnvironmentVariables(Map<String, String> env);
    
    public void setEnvironmentVariables(List<EnvironmentVariable> env);
    
    public void addEnvironmentVariable(EnvironmentVariable e);

    /**
     * Removes the environment variable with the given name from the remote
     * execution environment.
     *
     * @param name
     *            the name of the environment variable
     * @return the value of the environment variable
     */
    public String removeEnvironmentVariable(String name);

    /**
     * Returns the environment variable with the given name.
     *
     * @param name
     *            the name of the environment variable
     * @return the value of the environment variable
     */
    public String getEnvironmentVariable(String name);

    /**
     * Returns a collection representing all the environment variable names
     * associated with the remote execution environment.
     *
     */
    public List<EnvironmentVariable> getEnvironment();

    /**
     * Returns a collection representing all the environment variable names
     * associated with the remote execution environment.
     *
     */
    @Deprecated
    public Collection<String> getEnvironmentVariableNames();

    /**
     * Sets the file for redirecting the output produced on the stdout of the
     * remote machine.
     *
     * @param output
     *            a string representing the file for redirecting the remote
     *            stdout.
     */
    public void setStdOutput(String output);

    /**
     * Returns the file used for redirecting the output produced on the stdout
     * of the remote machine.
     */
    public String getStdOutput();

    /**
     * Allows specifying the location where the stdout stream of the job should
     * be redirected.
     */
    public void setStdOutputLocation(FileLocation type);

    /**
     * Retrieves the type of the standard output stream.
     */
    public FileLocation getStdOutputLocation();

    /**
     * Sets the file from which to redirect the data as stdin on the remote
     * machine.
     *
     * @param output
     *            a string representing the file for stdin
     */
    public void setStdInput(String input);

    /**
     * Returns the file used as stdin on the remote machine.
     */
    public String getStdInput();

    /**
     * Allows specifying the location of the file to be used for standard input
     * for this job.
     */
    public void setStdInputLocation(FileLocation type);

    /**
     * Retrieves the type of the standard input stream.
     */
    public FileLocation getStdInputLocation();

    /**
     * Sets the file for redirecting the error produced on the stderr of the
     * remote machine.
     *
     * @param error
     *            a string representing the file for redirecting the remote
     *            error.
     */
    public void setStdError(String error);

    /**
     * Returns the file used for redirecting the error produced on the stderr of
     * the remote machine.
     */
    public String getStdError();

    /**
     * Allows specifying the location where the stderr stream of the job should
     * be redirected.
     */
    public void setStdErrorLocation(FileLocation type);

    /**
     * Retrieves the type of the standard error stream.
     */
    public FileLocation getStdErrorLocation();

    /**
     * Specifies that the <code>Task</code> is to be executed as a batch job.
     * If it is a batch job, then the client machine will not be notified
     * regarding the stautus of the remote execution. From the client's
     * perspective, the <code>Task</code> is completed as soon as it is
     * submitted remotely. The execution status and the output/error must be
     * retrieved by the user in an offline fashion.
     *
     * @param bool
     *            a boolean value indicating if the <code>Task</code> is a
     *            batch job.
     */
    public void setBatchJob(boolean bool);

    /**
     * Chacks if the <code>Task</code> is to be executed as a batch job.
     */
    public boolean isBatchJob();

    /**
     * Specifies that the stdout and stderr streams of the task are to be
     * redirected.
     * <p>
     * If filename for the <code>setStdOutput</code> in this
     * <code>Specification</code> is <code>null</code> and the
     * <code>setRedirected</code> is <code>true</code>, then the remote
     * stdout is redirected to the local machine and can be retrieved from the
     * <code>getOutput</code> method of the container {@link Task}.
     *
     * @deprecated Use one of the setStd*Type methods
     *
     */
    @Deprecated
    public void setRedirected(boolean bool);

    /**
     * Checks if the stdout and stderror is redirected to the local machine.
     *
     * @deprecated Use one of the getStd*Type methods.
     */
    @Deprecated
    public boolean isRedirected();

    /**
     * Specifies that the stdin must be staged-in from the local machine.
     *
     * @deprecated Use {@link setStdInputType}
     */
    @Deprecated
    public void setLocalInput(boolean bool);

    /**
     * Checks if the stdin is staged-in from the local machine.
     *
     * @deprecated Use {@link getStdInputType}
     */
    @Deprecated
    public boolean isLocalInput();

    /**
     * Specifies that the executable must be staged-in from the local machine.
     *
     * @deprecated Use {@link setExecutableLocation}
     */
    @Deprecated
    public void setLocalExecutable(boolean bool);

    /**
     * Checks if the executable is staged-in from the local machine.
     *
     * @deprecated Use {@link getExecutableLocation}
     */
    @Deprecated
    public boolean isLocalExecutable();

    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);

    /**
       Remove attribute.
       @return If there was a non-null attribute with given name,
               return that attribute value, else return null.
     */
    public Object removeAttribute(String name);

    /**
     * Unpacks attribute assignments from special attribute
     * providerAttributes and assigns them via {@link setAttribute}.
     * Allows attributes to be passed through one provider and
     * unpacked in a subordinate provider.
     * E.g., attributes can be passed through coasters without
     * processing and unpacked by a subordinate provider such as PBS.
     * The payload is formatted as key=value pairs, split on
     * semicolon or newline
     */
    public void unpackProviderAttributes();

    /**
     * @deprecated use getAttributeNames()
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Enumeration getAllAttributes();

    /**
     *
     * @return a collection of all the attribute names that were added
     *         previously to this specification. The names are returned in no
     *         particular order
     */
    public Collection<String> getAttributeNames();

    /**
     * Queries whether delegation is enabled for this job. At this time
     * delegation settings only apply to Globus providers and can take one of
     * the following values:
     * <ul>
     * <li>{@see org.globus.cog.abstraction.interfaces.Delegation#NO_DELEGATION}
     * <li>{@see org.globus.cog.abstraction.interfaces.Delegation#FULL_DELEGATION}
     * <li>{@see org.globus.cog.abstraction.interfaces.Delegation#LIMITED_DELEGATION}
     * </ul>
     *
     * Most of the currently implemented providers will default to using the
     * lowest possible setting, with respect to the following order:
     * <code>NO_DELEGATION</code>, <code>LIMITED_DELEGATION</code>, and
     * <code>FULL_DELEGATION</code>
     */
    public int getDelegation();

    /**
     * Enables credential delegation for this job. Not all providers may support
     * credential delegation. At this time delegation settings only apply to
     * Globus providers and can take one of the following values:
     * <ul>
     * <li>{@see org.globus.cog.abstraction.interfaces.Delegation#NO_DELEGATION}
     * <li>{@see org.globus.cog.abstraction.interfaces.Delegation#FULL_DELEGATION}
     * <li>{@see org.globus.cog.abstraction.interfaces.Delegation#LIMITED_DELEGATION}
     * </ul>
     *
     * Most of the currently implemented providers will default to using the
     * lowest possible setting, with respect to the following order:
     * <code>NO_DELEGATION</code>, <code>LIMITED_DELEGATION</code>, and
     * <code>FULL_DELEGATION</code>
     */
    public void setDelegation(int delegation);

    /**
     * Queries whether delegation is enabled for this job
     *
     * @deprecated Use {@link #getDelegation()}
     */
    @Deprecated
    public boolean isDelegationEnabled();

    /**
     * Enables credential delegation for this job. Not all providers may support
     * credential delegation. Delegation is disabled by default.
     *
     * @deprecated Use {@link #setDelegation(int)}
     */
    @Deprecated
    public void setDelegationEnabled(boolean delegation);

    public void setStageIn(StagingSet stagein);

    public StagingSet getStageIn();

    public void setStageOut(StagingSet stageout);

    public StagingSet getStageOut();

    void setCleanUpSet(CleanUpSet cs);

    CleanUpSet getCleanUpSet();
}
