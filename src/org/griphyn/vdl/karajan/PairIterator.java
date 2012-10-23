/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.Iterator;
import java.util.Map;

import org.globus.cog.karajan.util.KarajanIterator;

public class PairIterator implements KarajanIterator {
	private Iterator<?> it;
	private int crt, count;
	private Pair crto;
	
	public PairIterator(Map<?, ?> map) {
		this.it = map.entrySet().iterator();
		this.count = map.size();
	}

	public int current() {
		return crt;
	}

	public int count() {
		return count;
	}

	public Object peek() {
		if (crto == null) {
			crto = convert(it.next());
		}
		return crto;
	}

	public void remove() {
		throw new UnsupportedOperationException("remove");
	}

	public boolean hasNext() {
		return it.hasNext() || crto != null;
	}

	public Object next() {
		crt++;
		if (crto != null) {
			Object o = crto;
			crto = null;
			return o;
		}
		else {
			return convert(it.next());
		}
	}
	
	private Pair convert(Object o) {
		Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
		return new Pair(e.getKey(), e.getValue());
	}
	
}
