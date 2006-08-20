// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.Specification;

public class FileOperationSpecificationImpl implements
        FileOperationSpecification {

    private int type;
    private String operation;
    private Hashtable attributes;
    private ArrayList arguments;

    public FileOperationSpecificationImpl() {
        this.type = Specification.FILE_TRANSFER;
        this.attributes = new Hashtable();
        arguments = new ArrayList();
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

        return (String) this.arguments.get(n);
    }

    public int addArgument(String argument) {

        this.arguments.add(argument);
        return this.arguments.size();
    }

    public Collection getArguments() {
        return this.arguments;

    }

    public int getArgumentSize() {
        return this.arguments.size();
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name.toLowerCase(), value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name.toLowerCase());
    }

    public Enumeration getAllAttributes() {

        return this.attributes.elements();
    }

    public String toString() {
        return operation + arguments;
    }

}