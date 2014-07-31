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
 * Created on Dec 23, 2012
 */
package k.rt;

import java.util.Collection;


public abstract class ChannelOperator<T, S> extends Sink<T> {
	private S value;
	
	public ChannelOperator(S initialValue) {
		this.value = initialValue;
	}

	@Override
	public boolean add(T value) {
		this.value = update(this.value, value);
		return true;
	}
	
	protected abstract S update(S crt, T value);

	@Override
	public boolean addAll(Collection<? extends T> values) {
		for (T v : values) {
			add(v);
		}
		return true;
	}
	
	public S getValue() {
		return value;
	}
	
	public static abstract class Double<P> extends Sink<P> {
		private double value;
		
		public Double(double initialValue) {
			this.value = initialValue;
		}
	
		@Override
		public synchronized boolean add(P value) {
			this.value = update(this.value, value);
			return true;
		}
		
		protected abstract double update(double crt, P value);
	
		@Override
		public synchronized boolean addAll(Collection<? extends P> values) {
			for (P v : values) {
				this.value = update(this.value, v);
			}
			return true;
		}
	
		public synchronized Object getValue() {
			return value;
		}
	}
}
