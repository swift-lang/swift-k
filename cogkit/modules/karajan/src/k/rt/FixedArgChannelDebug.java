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

public class FixedArgChannelDebug<T> extends FixedArgChannel<T> {
	private List<String> names;
	
	public FixedArgChannelDebug(Frame f, int startIndex, int endIndex) {
		super(f, startIndex, endIndex);
	}
	
	public void setNames(List<String> names) {
		this.names = names;
	}
	

	@Override
	public synchronized boolean add(T value) {
		getFrame().setName(getIndex(), names.get(getIndex() - (getBounds() >> 16)));
		return super.add(value);
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> values) {
		int index = getIndex();
		int bounds = getBounds();
		Frame f = getFrame();
		for (Object o : values) {
			f.setName(index, names.get(index - (bounds >> 16)));
			index++;
		}
		return super.addAll(values);
	}

	@Override
	public synchronized void addAll(Channel<? extends T> c) {
	    int index = getIndex();
        int bounds = getBounds();
        Frame f = getFrame();
		for (Object o : c) {
			f.setName(index, names.get(index - (bounds >> 16)));
			index++;
        }
		super.addAll(c);
	}	
}
