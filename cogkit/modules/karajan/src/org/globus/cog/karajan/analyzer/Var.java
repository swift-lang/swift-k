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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.globus.cog.karajan.compiled.nodes.functions.Variable;

public class Var {
	public final String name;
	private Object value;
	private int frame, index = -1;
	protected boolean dynamic, canBeNull;
	private List<Variable> readers;
	
	public Var(String name) {
	    this.name = name;
	    this.canBeNull = true;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getFrame() {
		return frame;
	}

	public int getIndex() {
		return index;
	}

	public void setDynamic() {
		this.dynamic = true;
	}
	
	public boolean isDynamic() {
		return dynamic;
	}
	
	public boolean getCanBeNull() {
		return canBeNull;
	}

	public void setCanBeNull(boolean canBeNull) {
		this.canBeNull = canBeNull;
	}

	public boolean isSettableByName() {
		return true;
	}
	
	public void addReader(Variable reader) {
		if (readers == null) {
			readers = new ArrayList<Variable>();
		}
		readers.add(reader);
	}

	@Override
	public String toString() {
		if (getValue() == null) {
			return name + ": " + index;
		}
		else {
			return name + "=" + (dynamic ? "?" : "") + getValue();
		}
	}

	public static class Channel extends Var {
		private boolean disabled, singleValued, noBuffer, commutative;
		private String channelName;
		
		public Channel(String name) {
			super(Param.channelVarName(name));
			this.channelName = name;
		}
		
		public Channel(Var v) {
            super(v.name);
        }

		public String getChannelName() {
			return channelName;
		}

		public boolean append(Object o) {
			if (getValue() == null) {
				return false;
			}
			else {
				return getChannel().append(o);
			}
		}
		
		public void appendDynamic() {
			setDynamic();
			getChannel().appendDynamic();
		}
		
		public boolean isDynamic() {
			return getChannel().isDynamic();
		}

		
		public StaticChannel getChannel() {
			return (StaticChannel) getValue();
		}
		
		public List<Object> getAll() {
			if (getChannel() != null) {
				return getChannel().getAll();
			}
			else {
				return Collections.emptyList();
			}
		}
		
		public int size() {
			if (getChannel() != null) {
				return getChannel().size();
			}
			else {
				return 0;
			}
		}
		
		public void disable() {
			disabled = true;
		}
		
		public boolean isDisabled() {
			return disabled;
		}

		public boolean getNoBuffer() {
			return noBuffer;
		}

		public void setNoBuffer(boolean noBuffer) {
			this.noBuffer = noBuffer;
		}

		public boolean isSingleValued() {
			return singleValued;
		}

		public void setSingleValued(boolean singleValued) {
			this.singleValued = singleValued;
		}

		public boolean isCommutative() {
			return commutative;
		}

		public void setCommutative(boolean commutative) {
			this.commutative = commutative;
		}

		public void clear() {
			dynamic = false;
			getChannel().clear();
		}
	}
	
	public static class Arg extends Var {
		public Arg(Param p) {
			super(p.varName());
		}
	}
}
