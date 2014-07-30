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
 * Created on Dec 10, 2012
 */
package org.globus.cog.karajan.analyzer;


public class Param {
	public enum Type {
		POSITIONAL, OPTIONAL, CHANNEL, BLOCK, IDENTIFIER
	}
	
	public final String name;
	public final Param.Type type;
	public Object value;
	public int index;
	public boolean dynamic;
	public int arity;
	
	public static final Param VARGS = new Param("...", Type.CHANNEL);
	
	public Param(String name, Param.Type type) {
		this.name = name;
		this.type = type;
	}
	
	public Param(String name, Param.Type type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
	
	public Param copy() {
		Param c = new Param(name, type, value);
		c.index = index;
		c.dynamic = dynamic;
		c.arity = arity;
		return c;
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
		dynamic = false;
	}
	
	public void setDynamic() {
		this.dynamic = true;
	}

	@Override
	public String toString() {
		if (value == null) {
			return ppName();
		}
		else {
			return ppName() + "=" + value;
		}
	}

	private String ppName() {
		switch (type) {
			case OPTIONAL:
				return "optional(" + name + ")";
			case CHANNEL:
				return "channel(" + name + ")";
			case BLOCK:
				return "block(" + name + ")";
			case IDENTIFIER:
				return "identifier(" + name + ")";
			default:
				return name;
		}
	}

	public String varName() {
		switch (type) {
            case CHANNEL:
                return channelVarName(name);
            default:
                return paramVarName(name);
        }
	}

	public static String channelVarName(String name) {
		return "#channel#" + name;
	}
	
	public static String paramVarName(String name) {
        return "#param#" + name;
    }
	
	public String fieldName() {
		if (name.equals("...")) {
			return "vargs";
		}
		else {
			return name;
		}
	}

	public void setIndex(int index) {
		this.index = index;
	}
}