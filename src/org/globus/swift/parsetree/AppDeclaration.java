/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created on Apr 19, 2015
 */
package org.globus.swift.parsetree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AppDeclaration extends FunctionDeclaration {
    
    private String executable;
    private final List<AppProfile> profiles;
    private final List<Expression> arguments;
    private final Map<String, Expression> redirects;
    
    public AppDeclaration() {
        profiles = new ArrayList<AppProfile>();
        arguments = new ArrayList<Expression>();
        redirects = new HashMap<String, Expression>();
    }
    
    public AppDeclaration(FunctionDeclaration fdecl) {
        this();
        setLine(fdecl.getLine());
        setName(fdecl.getName());
        setParameters(fdecl.getParameters());
        setReturns(fdecl.getReturns());
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }
    
    public void addProfile(AppProfile p) {
        profiles.add(p);
    }

    public List<AppProfile> getProfiles() {
        return profiles;
    }
    
    public void addArgument(Expression arg) {
        arguments.add(arg);
    }

    public List<Expression> getArguments() {
        return arguments;
    }
    
    public void addRedirect(String name, Expression expr) {
        redirects.put(name, expr);
    }

    public Map<String, Expression> getRedirects() {
        return redirects;
    }

    public Expression getRedirect(String name) {
        return redirects.get(name);
    }
}
