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
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.slocal;

public class ProcessProcessor extends StreamProcessor {
	private static final StreamProcessor NULL = new NullProcessor();

	private Process process;
	private StreamProcessor stdout, stderr;
	private boolean closed;
	private ProcessListener listener;

	public ProcessProcessor(Process process, StreamProcessor stdout, StreamProcessor stderr,
			ProcessListener listener) {
		this.process = process;
		this.listener = listener;
		if (stdout == null) {
			this.stdout = NULL;
		}
		else {
			this.stdout = stdout;
		}
		if (stderr == null) {
			this.stderr = NULL;
		}
		else {
			this.stderr = stderr;
		}
	}

	public boolean poll() {
		if (closed) {
			return true;
		}
		boolean result = stdout.poll() || stderr.poll();
		if (!isAlive()) {
			close();
		}
		return !closed;
	}
	
	protected boolean isAlive() {
		try {
			process.exitValue();
			return false;
		}
		catch(IllegalThreadStateException e) {
			return true;
		}
	}

	protected boolean isClosed() {
		return closed;
	}

	public void close() {
		stdout.close();
		stderr.close();
		closed = true;
		synchronized (this) {
			notify();
		}
		if (listener != null) {
			listener.processCompleted(process.exitValue());
		}
	}

	public synchronized void await() {
		while (!closed) {
			try {
				wait();
			}
			catch (InterruptedException e) {
			}
		}
	}

}
