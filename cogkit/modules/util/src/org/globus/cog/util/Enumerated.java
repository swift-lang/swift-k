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

// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.util;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public abstract class Enumerated {
	private final String literal;
	private final int value;
	private static Map classes = new Hashtable();

	protected Enumerated(String literal, int value) {
		this.literal = literal;
		this.value = value;
		checkForConflict();
	}

	private void checkForConflict() {
		Set values = (Set) classes.get(getClass());
		if (values == null) {
			values = new HashSet();
			classes.put(getClass(), values);
		}
		Integer val = new Integer(value);
		if (values.contains(val)) {
			int next = 0;
			while (values.contains(new Integer(next))) {
				next++;
			}
			throw new RuntimeException("Duplicate value for " + getClass() + ": " + value
					+ ". Next available value is " + next);
		}
		values.add(val);
	}

	public String toString() {
		return literal;
	}

	public boolean equals(Object other) {
		if (other instanceof Enumerated) {
			return ((Enumerated) other).value == value;
		}
		return false;
	}

	public int hashCode() {
		return value;
	}
	
	public final int getValue() {
		return value;
	}
}