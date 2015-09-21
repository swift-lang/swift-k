//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 18, 2015
 */
package org.globus.cog.abstraction.impl.common.task;

import org.globus.cog.abstraction.interfaces.EnvironmentVariable;

public class EnvironmentVariableImpl implements EnvironmentVariable {
    private String name;
    private String value;
    
    public EnvironmentVariableImpl() {
    }
    
    public EnvironmentVariableImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public String toString() {
        return name + " = " + value;
    }
}
