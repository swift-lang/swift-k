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
package org.griphyn.vdl.karajan;

import java.util.List;

import org.globus.swift.catalog.site.Application;
import org.globus.swift.catalog.site.SwiftContact;


public class Command {
    private String executable;
    private Object stdin, stdout, stderr;
    private List<Object> arguments;
    private Application application;
    
    public String getExecutable() {
        return executable;
    }
    
    public void setExecutable(String executable) {
        this.executable = executable;
    }
    
    public Object getStdin() {
        return stdin;
    }
    
    public void setStdin(Object stdin) {
        this.stdin = stdin;
    }
    
    public Object getStdout() {
        return stdout;
    }
    
    public void setStdout(Object stdout) {
        this.stdout = stdout;
    }
    
    public Object getStderr() {
        return stderr;
    }
    
    public void setStderr(Object stderr) {
        this.stderr = stderr;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }
    
    public String toString() {
        return executable + " " + arguments;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
