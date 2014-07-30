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
 * Created on Dec 11, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.util.Collections;
import java.util.List;

import k.rt.MemoryChannel;

public class StaticChannel {
	private MemoryChannel<Object> vargs;
	protected boolean dynamic;
	
	public boolean append(Object value) {
		if (dynamic) {
			return false;
		}
		if (vargs == null) {
			vargs = new MemoryChannel<Object>();
		}
        vargs.add(value);
        return true;
	}
	
	public boolean appendAll(List<Object> l) {
		if (dynamic) {
			return false;
		}
		if (vargs == null) {
			vargs = new MemoryChannel<Object>();
		}
		vargs.addAll(l);
		return true;
	}
	
    public List<Object> getAll() {
        return vargs == null ? Collections.emptyList() : vargs.getAll();
    }
    
    public int size() {
		return vargs == null ? 0 : vargs.size();
	}
    
    protected void appendDynamic() {
    	dynamic = true;
    }
    
	public boolean isEmpty() {
		return vargs == null;
	}

	@Override
	public String toString() {
		if (dynamic) {
		    if (vargs == null) {
		        return "[?]";
		    }
		    else {
		        String vts = vargs.toString();
		        return vts.substring(0, vts.length() - 2) + ", ?]";
		    }
		}
		else {
		    if (vargs == null) {
		        return "[]";
		    }
		    else {
		        return vargs.toString();
		    }
		}
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void clear() {
		dynamic = false;
		vargs = null;
	}
}
