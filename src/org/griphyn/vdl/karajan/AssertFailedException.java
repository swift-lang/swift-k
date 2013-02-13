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



package org.griphyn.vdl.karajan;

import k.rt.ExecutionException;

import org.globus.cog.karajan.compiled.nodes.Node;

/** 
 * Generated only by SwiftScript @assert(). 
 * 
 * Created on September 27, 2010
 * @author wozniak
 */
public class AssertFailedException extends ExecutionException {

    private static final long serialVersionUID = 1L;
   
    String message = null;
    
	public AssertFailedException(Node n, String message) {
		super(n, message);
	}
}
