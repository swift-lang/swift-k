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

/**
 * Specification document for performing a file operation task
 */
public interface FileOperationSpecification extends Specification {

    /**
     * Initializes and starts the remote {@link FileResource}
     */
    public static final String START = "start";

    /**
     * Stops the remote {@link FileResource}
     */
    public static final String STOP = "stop";

    /**
     * Gets the present working directory on the remote resource
     */
    public static final String PWD = "pwd";

    /**
     * Changes the current working directory
     */
    public static final String CD = "cd";

    /**
     * Lists all the files in the current working directory
     */
    public static final String LS = "ls";

    /**
     * Creates a new directory on the remote resource
     */
    public static final String MKDIR = "mkdir";
    
    /**
     * Creates a full directory path on the remote resource
     */
    public static final String MKDIRS = "mkdirs";

    /**
     * Deletes the directory on the remote resource
     */
    public static final String RMDIR = "rmdir";

    /**
     * Deletes the file on the remote resource
     */
    public static final String RMFILE = "rmfile";

    /**
     * Transfers the file from the remote resource to the local machine
     */
    public static final String GETFILE = "getfile";

    /**
     * Transfers the file from the local machine to the remote resource
     */
    public static final String PUTFILE = "putfile";

    /**
     * Transfers the entire directory from the remote resource to the local
     * machine
     */
    public static final String GETDIR = "getdir";

    /**
     * Transfers the entire directory from the local machine to the remote
     * resource
     */
    public static final String PUTDIR = "putdir";

    /**
     * Transfers multiple files from the remote resource to the local machine
     */
    public static final String MGET = "mget";

    /**
     * Transfers multiple files from the local machine to the remote resource
     */
    public static final String MPUT = "mput";

    /**
     * Renames a file on the remote resource
     */
    public static final String RENAME = "rename";

    /**
     * Changes the file permissions of a remote file
     */
    public static final String CHMOD = "chmod";

    /**
     * Indicates whether a file with a given name exists
     */
    public static final String EXISTS = "exists";

    /**
     * Indicated whether a file with the given name is a directory on the remote
     * resource
     */
    public static final String ISDIRECTORY = "isdirectory";
    
    /**
     * Get info about a file
     */
    public static final String FILEINFO = "stat";

    /**
     * Sets the operation to be executed on the remote file resource. Valid
     * operations include {@link FileOperationSpecification#START},
     * {@link FileOperationSpecification#STOP},
     * {@link FileOperationSpecification#PWD},
     * {@link FileOperationSpecification#CD},
     * {@link FileOperationSpecification#LS},
     * {@link FileOperationSpecification#MKDIR},
     * {@link FileOperationSpecification#RMDIR},
     * {@link FileOperationSpecification#RMFILE},
     * {@link FileOperationSpecification#GETFILE},
     * {@link FileOperationSpecification#PUTFILE},
     * {@link FileOperationSpecification#GETDIR},
     * {@link FileOperationSpecification#PUTDIR},
     * {@link FileOperationSpecification#MGET},
     * {@link FileOperationSpecification#MPUT},
     * {@link FileOperationSpecification#RENAME},
     * {@link FileOperationSpecification#CHMOD},
     * {@link FileOperationSpecification#EXISTS},
     * {@link FileOperationSpecification#ISDIRECTORY}
     *  
     */
    public void setOperation(String operation);

    /**
     * Returns the file operation for this specification
     */
    public String getOperation();

    /**
     * Sets the arguments for the file operation associated with this
     * specification at the given index.
     */
    public void setArgument(String arguments, int index);

    /**
     * Adds an argument for the file operation at the tail of the ordered list
     * of arguments.
     */
    public int addArgument(String argument);

    /**
     * Returns all the arguments for the file operation
     */
    public Collection<String> getArguments();

    /**
     * Returns the nth argument for the file operation
     */
    public String getArgument(int n);

    /**
     * Returns the number of arguments
     */
    public int getArgumentSize();

    /**
     * Sets any additional information through attributes
     */
    public void setAttribute(String name, Object value);

    /**
     * Get the attribute value associated with the given attribute name
     */
    public Object getAttribute(String name);
    
    /**
     * Returns all the attributes
     * @deprecated Use getAttributeNames
     */
    @SuppressWarnings("unchecked")
    public Enumeration getAllAttributes();
    
    public Collection<String> getAttributeNames();

}