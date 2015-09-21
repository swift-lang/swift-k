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

import java.util.AbstractList;

public class Pair<T> extends AbstractList<T> {
	private T o1, o2;

	public Pair(T o1, T o2) {
		this.o1 = o1;
		this.o2 = o2;
	}

    public T get(int index) {
	    switch (index) {
	        case 0:
	            return o1;
	        case 1:
	            return o2;
	        default:
	            throw new IndexOutOfBoundsException();
	    }
	}

	public int size() {
		return 2;
	}

    public T getFirst() {
        return o1;
    }

    public T getSecond() {
        return o2;
    }
}
