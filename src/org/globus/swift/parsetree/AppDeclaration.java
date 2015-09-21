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
import java.util.List;


public class AppDeclaration extends FunctionDeclaration {
    private final List<AppProfile> profiles;
    private final List<AppCommand> commands;
    
    public AppDeclaration() {
        profiles = new ArrayList<AppProfile>();
        commands = new ArrayList<AppCommand>(1);
    }
    
    public AppDeclaration(FunctionDeclaration fdecl) {
        this();
        setLine(fdecl.getLine());
        setName(fdecl.getName());
        setParameters(fdecl.getParameters());
        setReturns(fdecl.getReturns());
    }
    
    public void addProfile(AppProfile p) {
        profiles.add(p);
    }

    public List<AppProfile> getProfiles() {
        return profiles;
    }
    
    public void addCommand(AppCommand cmd) {
        commands.add(cmd);
    }

    public List<AppCommand> getCommands() {
        return commands;
    }
}
