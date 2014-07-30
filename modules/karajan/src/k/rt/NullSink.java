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
 * Created on Dec 8, 2012
 */
package k.rt;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NullSink extends Sink<Object> {
	private static final Iterator<Object> emptyIt = Collections.emptyList().iterator();
	
	@Override
	public boolean add(Object value) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Object> values) {
		return false;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Iterator<Object> iterator() {
		return emptyIt;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public List<Object> getAll() {
		return Collections.emptyList();
	}	
}
