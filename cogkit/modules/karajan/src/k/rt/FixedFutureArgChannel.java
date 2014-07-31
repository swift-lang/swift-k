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

import org.globus.cog.karajan.analyzer.CompilerSettings;

public class FixedFutureArgChannel<T> extends Sink<T> {
	private final Frame f;
	private int index, end, start;
	private List<String> names;
	
	public FixedFutureArgChannel(Frame f, int startIndex, int endIndex) {
		this.f = f;
		this.start = startIndex;
		this.index = startIndex;
		this.end = endIndex;
		setFutures();
    }
    
    private void setFutures() {
    	for (int i = start; i <= end; i++) {
    		f.set(i, new FutureObject());
    	}
	}
	
	public void setNames(List<String> names) {
		this.names = names;
	}
	

	@Override
	public synchronized boolean add(T value) {
		if (index > end) {
			throw new IndexOutOfBoundsException();
		}
		if (CompilerSettings.DEBUG) {
			f.setName(index, names.get(index - start));
		}
		((FutureObject) f.get(index++)).setValue(value);
		return true;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> values) {
		if (index + values.size() - 1 > end) {
			throw new IndexOutOfBoundsException();
		}
		for (Object o : values) {
			if (CompilerSettings.DEBUG) {
				f.setName(index, names.get(index - start));
			}
			((FutureObject) f.get(index++)).setValue(o);
		}
		return true;
	}

	@Override
	public synchronized void addAll(Channel<? extends T> c) {
		if (index + c.size() - 1 > end) {
			throw new IndexOutOfBoundsException();
		}
		for (Object o : c) {
			if (CompilerSettings.DEBUG) {
				f.setName(index, names.get(index - start));
			}
            ((FutureObject) f.get(index++)).setValue(o);
        }
	}
	
	public String toString() {
		return "F<" + index + ", " + end + ">";
	}
}
