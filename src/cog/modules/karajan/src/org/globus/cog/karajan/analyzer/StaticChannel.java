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
