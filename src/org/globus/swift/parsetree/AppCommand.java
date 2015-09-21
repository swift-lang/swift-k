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
 * Created on Sep 16, 2015
 */
package org.globus.swift.parsetree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppCommand extends AbstractNode {
    private String executable;
    private final List<Expression> arguments;
    private final Map<String, Expression> redirects;
    
    public AppCommand() {
        arguments = new ArrayList<Expression>();
        redirects = new HashMap<String, Expression>();
    }
    
    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
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

    @Override
    public String getNodeName() {
        return null;
    }

    @Override
    public List<? extends Node> getSubNodes() {
        return null;
    }
}
