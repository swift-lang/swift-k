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
 * Created on Sep 14, 2005
 */
package k.rt;

import java.util.Collections;
import java.util.List;

import k.thr.LWThread;
import k.thr.Scheduler.RootThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.Main;
import org.globus.cog.karajan.util.KarajanProperties;

/**
 * An execution context is a structure that is considered static for the duration of
 * one execution of an entire tree. It should not be re-used for multiple runs. Instead
 * different instances
 */
public class Executor {
	private static final Logger logger = Logger.getLogger(Executor.class);

	private final Main root;
	private transient boolean done, failed;
	private transient Throwable failure;
	private int id;
	private long startTime, endTime;
	private LWThread main;

	public Executor(Main root) {
		this(root, KarajanProperties.getDefault());
	}

	public Executor(Main root, KarajanProperties properties) {
		if (root == null) {
			throw new ExecutionException("The tree cannot be null");
		}
		this.root = root;
	}

	public Node getRoot() {
		return root;
	}

	public void start(Context context) {
		start(new RootThread(root, new Stack()), context);
	}

	public void start(LWThread main, Context context) {
		this.main = main;
		startTime = System.currentTimeMillis();
		List<String> arguments = context.getArguments();
		if (arguments == null) {
			arguments = Collections.emptyList();
		}
		root.setArgs(arguments);
		main.start();
	}

	protected void printFailure(Exception e) {
		System.err.print("\nExecution failed:\n");
		System.err.print(e.toString());
		System.err.print("\n");
	}

	protected synchronized void setDone() {
		this.done = true;
		this.endTime = System.currentTimeMillis();
		notifyAll();
	}

	public boolean done() {
		return this.done;
	}

	public void waitFor() {
		try {
			main.waitFor();
		}
		catch (Exception e) {
			logger.info("Run failed", e);
			failed = true;
			failure = e;
			printFailure(e);
		}
	}

	private Throwable getInitialCause(Throwable t) {
		if (t == null) {
			return null;
		}
		if (t.getCause() != null) {
			return getInitialCause(t.getCause());
		}
		else {
			return t;
		}
	}
	
	public static String getMeaningfulMessage(Throwable ex) {
		StringBuffer messages = new StringBuffer();
		Throwable t = ex;
		String prev = null;
		while (t != null) {
			String msg = t.getMessage();
			if (msg == null) {
				t = t.getCause();
				continue;
			}
			boolean changed = false;
			if (prev == null || prev.indexOf(msg) == -1) {
				prev = msg;
				messages.append(msg);
				changed = true;
			}
			t = t.getCause();
			if (t != null && changed) {
				if (msg.endsWith(":")) {
					messages.append(' ');
				}
				else if (!msg.endsWith(": ")) {
					messages.append(": ");
				}
			}
		}
		return messages.toString();
	}

	public boolean isFailed() {
		return failed;
	}

	public Throwable getFailure() {
		return failure;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getEndTime() {
		return endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	protected void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	protected void setStartTime(long startTime) {
		this.startTime = startTime;
	}
}
