//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 22, 2014
 */
package org.globus.swift.catalog.site;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Application {
    private String name, executable;
    private Map<String, String> env;
    private Map<String, Object> properties;
    
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    
    }
    public String getExecutable() {
        return executable;
    }
    
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void setEnv(String name, String value) {
        if (env == null) {
            env = new HashMap<String, String>();
        }
        env.put(name, value);
    }

    public void addProperty(String name, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(name, value);
    }
    
    public Map<String, String> getEnv() {
        if (env == null) {
            return Collections.emptyMap();
        }
        else {
            return env;
        }
    }

    public Map<String, Object> getProperties() {
        if (properties == null) {
            return Collections.emptyMap();
        }
        else {
            return properties;
        }
    }

    public boolean executableIsWildcard() {
        return "*".equals(executable);
    }
}
