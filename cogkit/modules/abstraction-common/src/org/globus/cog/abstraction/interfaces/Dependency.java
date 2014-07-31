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

/**
 * This interface provides the control dependencies between various
 * {@link ExecutableObject}s.
 * <p>
 * Dependencies are specified as ("from","to") pairs, indicating that the
 * <code>ExecutableObject</code> representing "from" will be executed before
 * that representing "to".
 * 
 * @deprecated Not being maintained
 */
public interface Dependency {
    /**
     * Represents no dependencies at all. Valid for {@link Set}
     */
    public static final int NONE = 0;

    /**
     * Represents a queue-like dependency (Also refered to as FIFO dependency).
     * Valid for {@link Queue}
     */
    public static final int QUEUE = 1;

    /**
     * Represents a grpah-like dependency. Valid for all classes of
     * {@link TaskGraph}
     */
    public static final int GRAPH = 2;

    /**
     * Represent a tree-like dependency. Not yet implemented
     */
    public static final int TREE = 3;

    /**
     * Sets the type of dependency pattern represented by this
     * <code>Dependency</code> object.
     */
    public void setType(int type);

    /**
     * Returns the type of dependency pattern represented by this
     * <code>Dependency</code> object.
     */
    public int getType();

    /**
     * Returns all the
     * {@link org.globus.cog.abstraction.impl.common.taskgraph.DependencyPair}
     * encapsulated within this <code>Dependency</code> object.
     * 
     * @return an enumeration of
     *         {@link org.globus.cog.abstraction.impl.common.taskgraph.DependencyPair}
     *         objects
     */
    public Enumeration elements();

    /**
     * Returns all the <code>ExecutableObject</code> s that depend on the
     * given <code>ExecutableObject</code>.
     * 
     * @param executableObject
     *            the <code>ExecutableObject</code> representing the parent.
     * @return an enumeration of {@link ExecutableObject}objects
     */
    public Enumeration getDependents(ExecutableObject executableObject);

    /**
     * Returns all the <code>ExecutableObject</code> s on which the given
     * <code>ExecutableObject</code> is dependent.
     * 
     * @param executableObject
     *            the <code>ExecutableObject</code> representing the dependent
     *            <code>ExecutableObject</code>.
     * @return an enumeration of {@link ExecutableObject}objects
     */
    public Enumeration getDependsOn(ExecutableObject executableObject);

    /**
     * Adds the dependency between the "from" <code>ExecutableObject</code>
     * and "to" <code>ExecutableObject</code>.
     * 
     * @param from
     *            the parent <code>ExecutableObject</code>.
     * @param to
     *            the child <code>ExecutableObject</code>.
     */
    public void add(ExecutableObject from, ExecutableObject to);

    /**
     * Removes the dependency between the "from" <code>ExecutableObject</code>
     * and "to" <code>ExecutableObject</code>.
     * 
     * @param from
     *            the parent <code>ExecutableObject</code>.
     * @param to
     *            the child <code>ExecutableObject</code>.
     */
    public boolean remove(ExecutableObject from, ExecutableObject to);

    /**
     * Removes all dependencies that has the given <code>ExecutableObject</code>
     * as the parent.
     * 
     * @param id
     *            the parent <code>ExecutableObject</code>
     * @return <code>true</code> if all the dependencies are removed.
     *         <code>false</code> otherwise.
     */
    public boolean removeAllDependents(ExecutableObject id);

    /**
     * Removes all dependencies that has the given <code>ExecutableObject</code>
     * as the child.
     * 
     * @param id
     *            the child <code>ExecutableObject</code>
     * @return <code>true</code> if all the dependencies are removed.
     *         <code>false</code> otherwise.
     */
    public boolean removeAllDependsOn(ExecutableObject executableObject);

    /**
     * Specifies if the given <code>ExecutableObject</code> has any dependent
     * (children).
     * 
     * @param executableObject
     *            the parent <code>ExecutableObject</code>
     * @return <code>true</code> if the given <code>ExecutableObject</code>
     *         has dependents. <code>false</code> otherwise.
     */
    public boolean hasDependents(ExecutableObject executableObject);

    /**
     * Specifies if the given <code>ExecutableObject</code> is a dependent
     * (child) of any other <code>ExecutableObject</code>.
     * 
     * @param executableObject
     *            the child <code>ExecutableObject</code>
     * @return <code>true</code> if the given <code>ExecutableObject</code>
     *         is a child in some dependency. <code>false</code> otherwise.
     */
    public boolean isDependent(ExecutableObject executableObject);

    /**
     * Specifies if this <code>Dependency</code> object contains a dependency
     * between the given <code>ExecutableObjects</code>.
     * 
     * @param from
     *            the parent <code>ExecutableObject</code>
     * 
     * @param to
     *            the child <code>ExecutableObject</code>
     * @return <code>true</code> if there exists a dependency between the
     *         given <code>ExecutableObjects</code>.<code>false</code>
     *         otherwise.
     */
    public boolean contains(ExecutableObject from, ExecutableObject to);

    /**
     * Returns the total number of dependencies.
     */
    public int size();
}