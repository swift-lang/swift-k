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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.globus.cog.karajan.compiled.nodes.functions.ConstantOp;
import org.globus.swift.data.policy.Policy;
import org.griphyn.vdl.mapping.AbsFile;

/**
   Karajan-accessible read-queries to CDM functionality.
*/
public class Query {
    private static final Logger logger = Logger.getLogger(Query.class);
    
    /**
       Do CDM policy lookup based on the CDM file.
    */
    public static class Q extends AbstractSingleValuedFunction {
    	private ArgRef<AbsFile> query;

        @Override
        protected Param[] getParams() {
            return params("query");
        }

        @Override
        public Object function(Stack stack) {
            AbsFile file = query.getValue(stack);
            Policy policy = Director.lookup(file.getPath());
            if (logger.isDebugEnabled()) {
                logger.debug("Director.lookup(): " + file + " -> " + policy);
            }
            return policy.toString();
        }
    }

    /** 
        Get a CDM property
    */
    public static class Get extends AbstractSingleValuedFunction {
        private ArgRef<String> name;

        @Override
        protected Param[] getParams() {
            return params("name");
        }

        @Override
        public Object function(Stack stack) {
            String name = this.name.getValue(stack);
            String value = Director.property(name);
            return value;
        }
    }


    /**
       Obtain the CDM policy file given on the command-line,
       conventionally "fs.data".  If not set, returns an empty String.
    */
    public static class File extends ConstantOp<String> {
        @Override
        protected String value() {
            String file = "";
            if (Director.policyFile != null) {
                file = Director.policyFile.toString();
            }
            return file;
        }
    }
}
