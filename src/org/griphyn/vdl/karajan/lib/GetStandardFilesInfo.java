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
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.Channel;
import k.rt.Null;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.FileNameExpander;
import org.griphyn.vdl.karajan.FileNameExpander.Transform;
import org.griphyn.vdl.mapping.DSHandle;

/**
 * This function does the following:
 * <ul>
 *  <li>extracts all files from the stagein variables and returns them</li>
 *  <li>extracts all files from the stageout variables and returns them</li>
 *  <li>extracts and returns the file name from stdin (which could either 
 *      be a string, null, or a FileNameExpander)</li>
 *  <li>extracts and returns the file names from stdout/stderr (also either 
 *      a string, null, or FileNameExpanders). These file names are used by the
 *      -out and -err parameters to _swiftwrap. The actual staging is 
 *      handled through the stagein and stageout variables above</li>
 *  <li>for each of the above files, if defaultScheme is not null, and if 
 *      the file protocol is not otherwise specified, it sets it to the
 *      value of defaultScheme</li>
 *  <li>builds and returns the list of all directories from the above files 
 *      and transforms them to remote directory names if the protocol is not 
 *      "direct"</li>    
 * </ul>
 *
 */
public class GetStandardFilesInfo extends SwiftFunction {
	
    private ArgRef<Object> stdin;
    private ArgRef<Object> stdout;
    private ArgRef<Object> stderr;
    private ArgRef<String> defaultScheme;
    private ChannelRef<Object> cr_vargs;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("stdin", "stdout", "stderr", optional("defaultScheme", null)), 
            returns(channel("...", 3)));
    }
    
    @Override
    public Object function(Stack stack) {
        Object stdin = this.stdin.getValue(stack);
        Object stdout = this.stdout.getValue(stack);
        Object stderr = this.stderr.getValue(stack);
        String defaultScheme = this.defaultScheme.getValue(stack);
        Channel<Object> ret = cr_vargs.get(stack);
        
        if (stdin != Null.VALUE) {
            ret.add(getPath((DSHandle) stdin, defaultScheme, "stdin"));
        }
        else {
            ret.add(Null.VALUE);
        }
        
        if (stdout != Null.VALUE) {
            ret.add(getPath((DSHandle) stdout, defaultScheme, "stdout"));
        }
        else {
            ret.add("stdout.txt");
        }
        
        if (stderr != Null.VALUE) {
            ret.add(getPath((DSHandle) stderr, defaultScheme, "stderr"));
        }
        else {
            ret.add("stderr.txt");
        }
        
        return null;
    }


    private String getPath(DSHandle h, String defaultScheme, String name) {
        Object o = h.getValue();
        if (o instanceof String) {
            return (String) o;
        }
        else if (o instanceof FileNameExpander) {
            FileNameExpander e = (FileNameExpander) o;
            if ("direct".equals(defaultScheme)) {
                e.setTransform(Transform.NONE);
            }
            if (defaultScheme != null) {
                e.setDefaultScheme(defaultScheme);
            }
            String[] fns = e.toStringArray();
            if (fns.length != 1) {
                throw new IllegalArgumentException("Multiple filenames passed to " + name);
            }
            return fns[0];
        }
        else {
            throw new IllegalArgumentException("Invalid value for " + name + ": '" + o + "'");
        }
    }
}
