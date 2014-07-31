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

import org.globus.cog.karajan.analyzer.CompilerSettings;


public class Stack {
	private Frame top;
	private int count;
	
	public Stack() {
		top = null;
		count = 0;
	}
	
	private Stack(Stack old) {
		top = old.top;
		count = old.count;
	}

	public void enter(Object owner, int count) {
		if (CompilerSettings.DEBUG) {
			top = new DebugFrame(owner, count, top);
		}
		else {
			top = new Frame(count, top);
		}
		this.count++;
		//System.out.println(getCallerClass() + " - enter " + getDepth());
	}

	public Frame top() {
		return top;
	}

	public Frame getFrame(int frame) {
		Frame f = top;
		for (int i = 0; i < frame; i++) {
			f = f.prev;
			if (f == null) {
				throw new IllegalArgumentException("No such frame: " + frame);
			}
		}
		return f;
	}

	public void leave() {
		top = top.prev;
		count--;
		//System.out.println(getCallerClass() + " - leave " + getDepth());
	}

	private int getDepth() {
		Frame t = top;
		int i = 0;
		while (t != null) {
			i++;
			t = t.prev;
		}
		return i;
	}

	private String getCallerClass() {
		Throwable t = new Throwable();
		return t.getStackTrace()[2].getClassName();
	}

	public Stack copy() {
		return new Stack(this);
	}

	public void dropToFrame(int fcf) {
		while (count > fcf) {
			count--;
			top = top.prev;
		}
	}
	
	public int frameCount() {
		return count;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Frame f = top;
		while (f != null) {
			sb.append(f);
			f = f.prev;
			sb.append("\n");
		}
		return sb.toString();
	}
}
