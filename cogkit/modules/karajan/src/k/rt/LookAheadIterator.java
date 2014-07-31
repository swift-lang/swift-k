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
 * Created on Jun 12, 2008
 */
package k.rt;

import java.util.Iterator;

public final class LookAheadIterator<T> implements Iterator<T> {
	private final Iterator<T> it;
	private T last;

	public LookAheadIterator(Iterator<T> it) {
		this.it = it;
	}

	public boolean hasNext() {
		return last != null || it.hasNext();
	}

	public T next() {
		try {
			if (last != null) {
				return last;
			}
			else {
				return it.next();
			}
		}
		finally {
			last = null;
		}
	}

	public void remove() {
		it.remove();
	}

	public T peek() {
		if (last != null) {
			return last;
		}
		else {
			return last = it.next();
		}
	}
}
