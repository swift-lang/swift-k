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

import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.analyzer.Scope.Def;
import org.globus.cog.karajan.parser.WrapperNode;

public class RecursiveFunctionChannel extends StaticChannel {
	public static final Object PRESET = new Object();
	
	private final WrapperNode owner; 
	private Scope scope;
	private String name;
	private boolean set;
	private NamedValue value;
	
	public RecursiveFunctionChannel(WrapperNode owner, Scope scope, String name) {
		this.owner = owner;
		this.scope = scope;
		this.name = name;
	}

	@Override
	public boolean append(Object value) {
		if (set) {
			throw new IllegalArgumentException("Illegal extra argument to " + owner + " (" + value + ")");
		}
		if (dynamic) {
			return false;
		}
		if (value instanceof NamedValue) {
			NamedValue nv = (NamedValue) value;
			scope.addDef(nv.ns, name, (Def) nv.value);
			this.value = nv;
		}
		else {
			throw new IllegalArgumentException("Expected function definition (" + owner + ")");
		}
		set = true;
		return true;
	}
	
	@Override
	public int size() {
		return set ? 1 : 0;
	}

	@Override
	public boolean isEmpty() {
		return !set;
	}

	@Override
	public List<Object> getAll() {
		List<Object> l = new LinkedList<Object>();
		l.add(value);
		return l;
	}
}
