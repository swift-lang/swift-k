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
 * Created on Oct 18, 2004
 */
package org.globus.cog.karajan.util;

import java.util.NoSuchElementException;

public class Range implements Iterable<Number> {

	private final int start, end, step;
	
	public Range(int start, int end, int step) {
		if (step == 0) {
			throw new IllegalArgumentException("Step must be greater than 0");
		}
		this.start = start;
		this.end = end;
		this.step = step;
	}
	
	public Range(int start, int end) {
		this(start, end, 1);
	}
	
	@Override
	public java.util.Iterator<Number> iterator() {
		return new Iterator();
	}



	private class Iterator implements java.util.Iterator<Number> {
		private int crt;
		
		protected Iterator() {
			this.crt = start;
		}

		public int current() {
			return crt;
		}

		public void remove() {
			throw new RuntimeException("Unsupported operation");
		}

		public boolean hasNext() {
			return crt <= end;
		}

		public Number next() {
			if (crt > end) {
				throw new NoSuchElementException();
			}
			try {
				return new Double(crt);
			}
			finally {
				crt += step;
			}
		}

		public Number peek() {
			return new Double(crt);
		}

		public boolean isClosed() {
			return false;
		}
	}

	public String toString() {
		if (step != 1) {
			return "[" + start + " .. " + end + " step " + step + "]";
		}
		else {
			return "[" + start + " .. " + end + "]";
		}
	}
}
