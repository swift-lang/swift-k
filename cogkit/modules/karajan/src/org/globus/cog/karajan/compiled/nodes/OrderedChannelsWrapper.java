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
 * Created on Dec 30, 2012
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;

public class OrderedChannelsWrapper extends Node {
	private Node child;
	private List<ChannelRef<Object>> channels;
	private final boolean autoClose;
	
	public OrderedChannelsWrapper(Node child) {
		this(child, true);
	}
	
	public OrderedChannelsWrapper(Node child, boolean autoClose) {
		this.child = child;
		child.setParent(this);
		this.autoClose = autoClose;
	}

	@Override
	public void run(LWThread thr) {
		int i = thr.checkSliceAndPopState();
		int fc = thr.popIntState();
		Stack stack = thr.getStack();
		try {
			switch(i) {
				case 0:
					fc = stack.frameCount();
					i++;
				case 1:
					try {
						child.run(thr);
						closeArgs(stack);
					}
					catch (ExecutionException e) {
						stack.dropToFrame(fc);
						closeArgs(stack);
						throw e;
					}
			}
		}
		catch (Yield y) {
			y.getState().push(fc);
			y.getState().push(i);
			throw y;
		}
	}

	public void closeArgs(Stack stack) {
		if (channels != null && autoClose) {
			for (ChannelRef<Object> cr : channels) {
				cr.close(stack);
			}
		}
	}

	public void initializeArgs(Stack stack) {
		if (channels != null) {
			for (ChannelRef<Object> cr : channels) {
				cr.create(stack);
			}
		}
	}

	public void addChannel(ChannelRef<Object> c) {
		if (channels == null) {
			channels = new LinkedList<ChannelRef<Object>>();
		}
		channels.add(c);
	}
	
	@Override
    public void dump(PrintStream ps, int level) throws IOException {
        super.dump(ps, level);
        child.dump(ps, level + 1);
    }

	@Override
	public String toString() {
		return "OrderedChannelsWrapper";
	}
}
