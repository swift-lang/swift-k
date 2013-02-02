//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 11, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.util.List;

import org.globus.cog.karajan.parser.WrapperNode;

public class ArgMappingChannel extends StaticChannel {
	private final WrapperNode owner; 
	private List<Param> params;
	private int index;
	private boolean hasVargs;
	
	public ArgMappingChannel(WrapperNode owner, List<Param> args, boolean hasVargs) {
		this.params = args;
		this.hasVargs = hasVargs;
		this.owner = owner;
	}

	@Override
	public boolean append(Object value) {
		if (dynamic) {
			return false;
		}
		if (index >= params.size() && !hasVargs) {
			throw new IllegalArgumentException("Illegal extra argument to " + owner);
		}
		while (index < params.size() && (params.get(index).getValue() != null || params.get(index).dynamic)) {
			index++;
		}
		if (index < params.size()) {
			params.get(index).setValue(value);
		}
		else {
			super.append(value);
		}
		index++;
		return true;
	}

	@Override
	public void appendDynamic() {
		super.appendDynamic();
		while (index < params.size() && (params.get(index).getValue() != null || params.get(index).dynamic)) {
            index++;
        }
		int i = index;
		while (i < params.size()) {
			params.get(i).setDynamic();
			i++;
		}
	}
}
