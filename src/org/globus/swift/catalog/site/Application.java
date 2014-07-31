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
