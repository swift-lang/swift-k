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
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.util;

import java.lang.reflect.Method;

public class ThreadedClassRunner extends Thread {
	private final ThreadedClassRunnerListener l;
	private final String main;
	private final String[] args;

	public ThreadedClassRunner(ThreadedClassRunnerListener l, String main, String[] args) {
		this.l = l;
		this.main = main;
		this.args = args;
	}

	public void run() {
		try {
			Class targ = Class.forName(main);
			Class[] ma = new Class[1];
			ma[0] = String[].class;
			Method mm = targ.getMethod("main", ma);
			Object[] ia = new Object[1];
			ia[0] = args;
			mm.invoke(null, ia);
			l.completed(this);
		}
		catch (Exception e) {
			l.failed(this, e);
		}

	}
}
