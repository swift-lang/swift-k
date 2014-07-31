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


package org.globus.swift.data;

import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.swift.data.policy.Policy;

/**
 * Karajan-accessible CDM functions that change something.
 * */
public class Action {
    private static final Logger logger = Logger.getLogger(Action.class);

    /**
       Register a file for broadcast by CDM.
       The actual broadcast is triggered by {@link cdm_wait}.
    */
    public static class Broadcast extends InternalFunction {
        private ArgRef<String> srcfile;
        private ArgRef<String> srcdir;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("srcfile", "srcdir"));
        }

        @Override
        protected void runBody(LWThread thr) {
            Stack stack = thr.getStack();
            String srcfile = this.srcfile.getValue(stack);
            String srcdir  = this.srcdir.getValue(stack);

            logger.debug("cdm_broadcast()");
        
            Policy policy = Director.lookup(srcfile);
        
            if (!(policy instanceof org.globus.swift.data.policy.Broadcast)) {
                throw new RuntimeException("Attempting to BROADCAST the wrong file: " +
                    srcdir + " " + srcfile + " -> " + policy);
            }
        
            if (srcdir == "") { 
                srcdir = ".";
            }

            Director.addBroadcast(srcdir, srcfile);
        }
    }
    
    public static class External extends InternalFunction {
        private ArgRef<String> srcfile;
        private ArgRef<String> srcdir;
        private ArgRef<BoundContact> desthost;
        private ArgRef<String> destdir;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("srcfile", "srcdir", "desthost", "destdir"));
        }

        @Override
        protected void runBody(LWThread thr) {
            Stack stack = thr.getStack();
            String srcfile  = this.srcfile.getValue(stack);
            String srcdir   = this.srcdir.getValue(stack);
            BoundContact bc = this.desthost.getValue(stack);
            String destdir  = this.destdir.getValue(stack);
        
            if (srcdir.length() == 0) {
                srcdir = ".";
            }
            String desthost = bc.getName();
            String workdir = (String) bc.getProperty("workdir");
            
            if (workdir != null && !workdir.startsWith("/")) {
                workdir = System.getProperty("user.dir") + "/" + workdir;
            }
            
            org.globus.swift.data.policy.External.doExternal(srcfile, srcdir, 
                                desthost, workdir + "/" + destdir);
        }
    }
    
    /**
       Wait until CDM has ensured that all data has been propagated.
    */
    public static class Wait extends InternalFunction {

        @Override
        protected Signature getSignature() {
            return new Signature(params());
        }

        @Override
        protected void runBody(LWThread thr) {
            // TODO busy waiting is not good
            logger.debug("cdm_wait()");
            Director.doBroadcast();
        }
    }
}
