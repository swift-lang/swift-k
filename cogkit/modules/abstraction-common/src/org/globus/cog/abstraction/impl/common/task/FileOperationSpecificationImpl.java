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

package org.globus.cog.abstraction.impl.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.Specification;

public class FileOperationSpecificationImpl implements
        FileOperationSpecification {

    private static final long serialVersionUID = 1L;
    
    private int type;
    private String operation;
    private Map<String, Object> attributes;
    private ArrayList<String> arguments;

    public FileOperationSpecificationImpl() {
        this.type = Specification.FILE_TRANSFER;
        arguments = new ArrayList<String>();
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getSpecification() {
        return operation;
    }

    public void setSpecification(String spec) {
        // N/A
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setArgument(String value, int index) {
        /*
         * AARGH! this.arguments.add(index, arguments);
         */
        while (index >= arguments.size()) {
            arguments.add(null);
        }
        arguments.set(index, value);
    }

    public String getArgument(int n) {
        return this.arguments.get(n);
    }

    public int addArgument(String argument) {

        this.arguments.add(argument);
        return this.arguments.size();
    }

    public Collection<String> getArguments() {
        return this.arguments;

    }

    public int getArgumentSize() {
        return this.arguments.size();
    }

    public void setAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(name.toLowerCase(), value);
    }

    public Object getAttribute(String name) {
        if (attributes == null) {
            return null;
        }
        else {
            return attributes.get(name.toLowerCase());
        }
    }

    @SuppressWarnings("unchecked")
    public Enumeration getAllAttributes() {
        if (attributes == null) {
            return Collections.enumeration(Collections.emptyList());
        }
        else {
            return Collections.enumeration(attributes.keySet());
        }
    }
    
    public Collection<String> getAttributeNames() {
        if (attributes == null) {
            return Collections.emptyList();
        }
        else {
            return attributes.keySet();
        }
    }

    public String toString() {
        return operation + arguments;
    }

    public Object clone() {
        FileOperationSpecificationImpl result = null;
        try {
            result = (FileOperationSpecificationImpl) super.clone();
            if (attributes != null) {
                result.attributes = new HashMap<String, Object>(attributes);
            }
            result.arguments = new ArrayList<String>(arguments);            
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
