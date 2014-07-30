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
 * Created on Dec 6, 2012
 */
package k.rt;

import java.util.Collection;
import java.util.List;

public class FixedArgChannel<T> extends Sink<T> {
	private final Frame f;
	private int index;
	private final int bounds;
	
	public FixedArgChannel(Frame f, int startIndex, int endIndex) {
		this.f = f;
		this.index = startIndex;
		this.bounds = endIndex + (startIndex << 16);
	}
	
	public void setNames(List<String> names) {
	}
	
	public boolean isEmpty() {
	    return index > getStartIndex();
	}

	private int getStartIndex() {
		return bounds >> 16;
	}
	
	protected int getIndex() {
		return index;
	}
	
	protected int getBounds() {
		return bounds;
	}
	
	protected Frame getFrame() {
		return f;
	}

	@Override
	public int size() {
		return index - getStartIndex();
	}

	@Override
	public synchronized boolean add(T value) {
		if (index > (bounds & 0x0000ffff)) {
			throw new IllegalExtraArgumentException(value);
		}
		f.set(index++, value);
		return true;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> values) {
		if (index + values.size() - 1 > (bounds & 0x0000ffff)) {
			throw new IndexOutOfBoundsException();
		}
		for (Object o : values) {
			f.set(index++, o);
		}
		return true;
	}

	@Override
	public synchronized void addAll(Channel<? extends T> c) {
		if (index + c.size() - 1 > (bounds & 0x0000ffff)) {
			throw new IndexOutOfBoundsException();
		}
		for (Object o : c) {
            f.set(index++, o);
        }
	}
	
	public String toString() {
		return "<" + index + ", " + (bounds & 0x0000ffff) + ">";
	}
}
