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

import org.apache.log4j.Logger;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;

import org.globus.swift.data.policy.Policy;

/**
   Karajan-accessible read-queries to CDM functionality.
*/
public class Query extends FunctionsCollection {
    private static final Logger logger = Logger.getLogger(Query.class);
    
    public static final Arg PA_QUERY = new Arg.Positional("query");
    public static final Arg PA_NAME  = new Arg.Positional("name");

    static {
        setArguments("cdm_query", new Arg[]{ PA_QUERY });
        setArguments("cdm_get", new Arg[]{ PA_NAME });
        setArguments("cdm_file", new Arg[]{});
    }

    /**
       Do CDM policy lookup based on the CDM file.
    */
    public String cdm_query(VariableStack stack) throws ExecutionException {
        String file = (String) PA_QUERY.getValue(stack);
        Policy policy = Director.lookup(file);
        logger.debug("Director.lookup(): " + file + " -> " + policy);
        return policy.toString();
    }

    /** 
        Get a CDM property
    */
    public String cdm_get(VariableStack stack) throws ExecutionException {
        String name  = (String) PA_NAME.getValue(stack);
        String value = Director.property(name);
        return value;
    }

    /**
       Obtain the CDM policy file given on the command-line,
       conventionally "fs.data".  If not set, returns an empty String.
    */
    public String cdm_file(VariableStack stack) throws ExecutionException {
        String file = "";
        if (Director.policyFile != null)
            file = Director.policyFile.toString();
        return file;
    }
}
