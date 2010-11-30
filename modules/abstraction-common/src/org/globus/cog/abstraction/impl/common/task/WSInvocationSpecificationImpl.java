// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.globus.cog.abstraction.interfaces.Specification;
import org.globus.cog.abstraction.interfaces.WSInvocationSpecification;

public class WSInvocationSpecificationImpl implements WSInvocationSpecification {

    private static final long serialVersionUID = 1L;
    
    private int type;
    private String method;
    private Hashtable<String,Object> additionalAttributes;
    private Vector<String> arguments;

    public WSInvocationSpecificationImpl() {
        this.type = Specification.WS_INVOCATION;
        this.additionalAttributes = new Hashtable<String,Object>();
        this.arguments = new Vector<String>();
    }

    public void setType(int type) {
        // By default = WS_INVOCATION
    }

    public int getType() {
        return this.type;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {

        return this.method;
    }

    public String[] getArgumentsAsArray() {
        int count = this.arguments.size();
        String args[] = new String[count];
        for (int i = 0; i < count; i++) {
            args[i] = (String) this.arguments.get(i);
        }
        return args;
    }

    public void addArgument(String argument) {
        this.arguments.add(argument);
    }

    public void addArgument(int index, String argument) {
        this.arguments.ensureCapacity(index + 1);
        this.arguments.add(index, argument);
    }

    public void removeArgument(String argument) {
        this.arguments.remove(argument);
    }

    public String removeArgument(int index) {
        return (String) this.arguments.remove(index);
    }

    public Vector<String> getArgumentsAsVector() {
        return this.arguments;
    }

    public void setArguments(Vector<String> arguments) {
        this.arguments = arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = new Vector<String>();
        StringTokenizer st = new StringTokenizer(arguments);
        while (st.hasMoreTokens()) {
            this.arguments.add(st.nextToken());
        }
    }

    public String getArguments() {
        return getArgumentsAsString();
    }

    public String getArgumentsAsString() {
        String arg;
        if (!this.arguments.isEmpty()) {
            arg = "";
            Enumeration en = this.arguments.elements();
            while (en.hasMoreElements()) {
                String str = (String) en.nextElement();
                arg = arg + str + " ";
            }
            arg = arg.trim();
        } else {
            arg = null;
        }
        return arg;
    }

    public void setAttribute(String name, Object value) {
        this.additionalAttributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return (String) this.additionalAttributes.get(name);
    }

    public Enumeration getAllAttributes() {
        return this.additionalAttributes.keys();
    }

    public void setSpecification(String specification) {
    }

    public String getSpecification() {

        return null;
    }
    
    @SuppressWarnings("unchecked")
    public Object clone() {
        WSInvocationSpecificationImpl result = null;
        try {
            result = (WSInvocationSpecificationImpl) super.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        result.arguments = (Vector<String>) arguments.clone();
        return result;
    }
}
