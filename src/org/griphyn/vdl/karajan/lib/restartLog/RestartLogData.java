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
package org.griphyn.vdl.karajan.lib.restartLog;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RestartLogData {
	private final FlushableLockedFileWriter writer;
	private boolean closed;
	private Map<LogEntry, Object> data;

	public RestartLogData(FlushableLockedFileWriter writer, Map<LogEntry, Object> data) {
		this.writer = writer;
		this.data = data;
	}

	public void add(String str) {
		if (closed) {
			return;
		}
		try {
			writer.write(str);
			if (!str.endsWith("\n")) {
				writer.write('\n');
			}
			writer.flush();
		}
		catch (IOException e) {
			throw new RuntimeException("Exception caught while writing to log file", e);
		}
	}

	public synchronized void close() {
		if (!closed) {
			try {
				writer.close();
			}
			catch (IOException e) {
				throw new RuntimeException("Failed to close restart log", e);
			}
			closed = true;
		}
	}

	public File getFile() {
		return writer.getFile();
	}

	public boolean isEmpty() {
		return data == null || data.isEmpty();
	}

	public boolean contains(LogEntry entry) {
		return data.containsKey(entry);
	}
}