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
 * Created on Feb 6, 2006
 */
package org.globus.cog.karajan.futures;

import k.rt.Future;
import k.rt.FutureListener;
import k.rt.FutureMemoryChannel;

/**
 * This class allows splitting of a channel into multiple copies.
 * This allows the split channels to be used independently (including
 * removing objects from the channel).
 * 
 * @author Mihael Hategan
 *
 */
public class ChannelSplitter<T> implements FutureListener {
	private final FutureMemoryChannel<T> c;
	private final FutureMemoryChannel<T>[] out;
	
	@SuppressWarnings("unchecked")
	public ChannelSplitter(FutureMemoryChannel<T> c, int count) {
		this.c = c;
		out = new FutureMemoryChannel[count];
		for (int i = 0; i < count; i++) {
			out[i] = new FutureMemoryChannel<T>();
		}
		c.addListener(this, null);
	}
	
	public FutureMemoryChannel<T>[] getOuts() {
		return out;
	}

	public void futureUpdated(Future f) {
		while(c.available() > 0) {
			T o = c.removeFirst();
			for (int i = 0; i < out.length; i++) {
				out[i].add(o);
			}
		}
		if (c.isClosed()) {
			for (int i = 0; i < out.length; i++) {
				out[i].close();
			}
		}
	}
}
