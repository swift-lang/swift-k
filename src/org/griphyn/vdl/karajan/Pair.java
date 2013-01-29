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

public class Pair extends AbstractList<Object> {
	private Object[] elements = new Object[2];

	public Pair(Object o1, Object o2) {
		elements[0] = o1;
		elements[1] = o2;
	}

	public Object get(int index) {
		return elements[index];
	}

	public int size() {
		return 2;
	}
}
