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
import java.util.Vector;

/**
 * The <code>WSInvocationSpecification</code> represents all the parameters required
 * for the remote Web service invocation <code>Task</code>.
 */
public interface WSInvocationSpecification extends Specification {
    
    /**
     * Sets the name of the remote method to be invoked on the Web service
     * 
     * @param method
     *            a string representing the remote method name
     */
    public void setMethod(String method);

    /**
     * Returns the name of the remote method to be invoked
     */
    public String getMethod();


    /**
     * Sets the arguments for the remote method. A set of space
     * seperated arguments can be supplied: "arg1 arg2 agr3 ..."
     * 
     * @param arguments
     *            a string representing the set of arguments for the remote
     *            method.
     */
    public void setArguments(String arguments);

    /**
     * Sets the arguments for the remote method.
     * 
     * @param arguments
     *            a Vector representing the set of arguments for the remote
     *            method.
     */
    public void setArguments(Vector<String> arguments);

    /**
     * Returns the set of space-separated arguments supplied for the remote
     * method.
     */
    public String getArgumentsAsString();
    
    /**
     * Returns the set of arguments supplied for the remote
     * method as an Array.
     */
    public String[] getArgumentsAsArray();

    /**
     * Adds an argument for the remote method. Multiple
     * arguments can be set by making multiple calls to this
     * function.
     * 
     * @param argument
     *            a string representing an argument for the remote method.
     */
    public void addArgument(String argument);

    /**
     * Adds an argument for the remote method at the given index.
     * Multiple arguments can be set by making multiple calls to
     * this function.
     * 
     * @param index
     *            the index in the argument list
     * @param argument
     *            a string representing an argument for the remote method.
     *  
     */
    public void addArgument(int index, String argument);

    /**
     * Removes the given argument from the argument list
     * 
     * @param argument
     *            the String argument to be removed
     */
    public void removeArgument(String argument);

    /**
     * Removes the argument at the given index from the argument list
     * 
     * @param index
     *            the index of the argument to be removed
     */
    public String removeArgument(int index);

    /**
     * Retruns a Vector representing the set of arguments for the
     * method.
     *  
     */
    public Vector<String> getArgumentsAsVector();

    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);

    public Enumeration<String> getAllAttributes();
}
