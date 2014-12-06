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
 * Created on Mar 22, 2006
 */
package org.globus.cog.karajan.compiled.nodes.restartLog;

import java.util.StringTokenizer;

import k.rt.ExecutionException;
import k.thr.LWThread;

import org.globus.cog.karajan.compiled.nodes.Node;

public class LogEntry {
	private String key;
	private String value;

	public static LogEntry parse(String line) {
		LogEntry entry = new LogEntry();
		StringTokenizer st = new StringTokenizer(line, "!");
		int tokCount = st.countTokens();
		if (tokCount == 1) {
			entry.key = line;
			return entry;
		}
		if (tokCount > 3) {
			throw new ExecutionException();
		}
		entry.key = st.nextToken();
		if (st.hasMoreTokens()) {
			entry.value = st.nextToken();
		}
		return entry;
	}

	public static LogEntry build(LWThread thr, Node n) throws ExecutionException {
		LogEntry entry = new LogEntry();
		entry.key = thr.getQualifiedName() + ":" + n.toString();
		return entry;
	}

	public static LogEntry build(String key) throws ExecutionException {
		LogEntry entry = new LogEntry();
		entry.key = key;
		return entry;
	}

	public boolean equals(Object obj) {
		if (obj instanceof LogEntry) {
			LogEntry other = (LogEntry) obj;
			return key.equals(other.key);
		}
		return false;
	}

	public int hashCode() {
		return key.hashCode();
	}

	public String toString() {
		if (value == null) {
			return key;
		}
		else {
			return key + "!" + value;
		}
	}

	public String getValue() {
		return value;
	}
}