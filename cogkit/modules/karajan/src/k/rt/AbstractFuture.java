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
 * Created on May 12, 2012
 */
package k.rt;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractFuture implements Future {
	private LinkedList<FutureListener> listeners;
	
	protected abstract boolean isClosed();
	
	@Override
	public void addListener(FutureListener l, ConditionalYield y) {
		boolean closed;
		synchronized(this) {
			if (listeners == null) {
				listeners = new LinkedList<FutureListener>();
			}
			listeners.add(l);
			closed = isClosed();
		}
		if (closed) {
			notifyListeners();
		}
	}
	
	protected void notifyListeners() {
		List<FutureListener> ls;
		synchronized(this) {
			if (listeners == null) {
				return;
			}
			ls = listeners;
			listeners = null;
		}
		for (FutureListener l : ls) {
			l.futureUpdated(this);
		}
	}
}
