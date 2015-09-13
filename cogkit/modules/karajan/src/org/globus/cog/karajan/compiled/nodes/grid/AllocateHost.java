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

package org.globus.cog.karajan.compiled.nodes.grid;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import k.rt.ConditionalYield;
import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.ContainerScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.scheduler.ContactAllocationTask;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.util.Contact;

public class AllocateHost extends InternalFunction {
	public static final Logger logger = Logger.getLogger(AllocateHost.class);
	
	private String name;
	private ArgRef<Object> constraints;
	private Node body;
	
	private VarRef<Context> context;
	private VarRef<Contact> var;
	private int frameSize;

	@Override
	protected Signature getSignature() {
		return new Signature(params(identifier("name"), optional("constraints", null), block("body")));
	}
	
	@Override
	protected void addLocals(Scope scope) {
		super.addLocals(scope);
		context = scope.getVarRef("#context");
	}

	@Override
	public void runBody(LWThread thr) throws ExecutionException {
		int i = thr.checkSliceAndPopState(4);
		int fc = thr.popIntState();
		Stack stack = thr.getStack();
		try {
			switch (i) {
				case 0:
					fc = stack.frameCount();
					i++;
				case 1:
				    allocateHost(thr);
				    i++;
				case 2:
					stack.enter(this, frameSize);
					i++;
				case 3:
					if (CompilerSettings.PERFORMANCE_COUNTERS) {
						startCount++;
					}
					body.run(thr);
					stack.dropToFrame(fc);
					_finally(thr.getStack());
			}
		}
		catch (ExecutionException e) {
			stack.dropToFrame(fc);
			_finally(thr.getStack());
			throw e;
		}
		catch (Yield y) {
			y.getState().push(fc);
			y.getState().push(i, 4);
			throw y;
		}
	}

	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		var = scope.getVarRef(scope.addVar(name));
		ContainerScope cs = new ContainerScope(w, scope);
		super.compileBlocks(w, sig, blocks, cs);
		frameSize = cs.size();
	}

	protected void allocateHost(LWThread thr) {
		int i = thr.checkSliceAndPopState(1);
		TaskFuture tf = (TaskFuture) thr.popState();
		Stack stack = thr.getStack();
		try {
			switch(i) {
				case 0:
					this.var.setValue(stack, null);
					Object constraints = this.constraints.getValue(stack);
					try {
						Scheduler s = getScheduler(stack);
						if (constraints == null) {
							Contact contact = s.allocateContact();
							if (logger.isDebugEnabled()) {
								logger.debug("Allocated host " + contact);
							}
							this.var.setValue(stack, contact);
							return;
						}
						else {
							ContactAllocationTask t = new ContactAllocationTask();
							tf = new TaskFuture(stack, t);
							Contact contact = s.allocateContact(constraints);
							t.setVirtualContact(contact);
							
							s.enqueue(t, new Contact[] { contact }, tf);
							i++;
							throw new ConditionalYield(tf);
						}
					}
					catch (Exception e) {
						throw new ExecutionException(this, e);
					}
				default:
					// check if failed
					tf.getValue();
			}
		}
		catch (Yield y) {
			y.getState().push(tf);
			y.getState().push(i, 1);
			throw y;
		}
	}
	
	protected Scheduler getScheduler(Stack stack) {
	    return (Scheduler) context.getValue(stack).getAttribute(SchedulerNode.CONTEXT_ATTR_NAME);
	}
	
	private class TaskFuture extends TaskStateFuture {

		public TaskFuture(Stack stack, Task task) {
			super(stack, task);
		}

		public void statusChanged(StatusEvent event) {
			Status status = event.getStatus();
			ContactAllocationTask t = (ContactAllocationTask) getTask();
			Stack stack = getStack();
			try {
				Scheduler s = getScheduler(stack);
				
				int code = status.getStatusCode();
				if (code == Status.FAILED) {
					Exception e = status.getException();
					if (e == null) {
						fail(new ExecutionException("Failed to allocate host: " + status.getMessage()));
					}
					else {
						fail(new ExecutionException(status.getMessage(), e));
					}
				}
				else if (code == Status.COMPLETED) {
				    AllocateHost.this.var.setValue(stack, t.getContact());
					resume();
				}
			}
			catch (Exception e) {
				fail(new ExecutionException(AllocateHost.this, e));
			}
		}
	}

	protected void _finally(Stack stack) throws ExecutionException {
		Scheduler s = getScheduler(stack);
		Contact c = var.getValue(stack);
		if (s != null && c != null) {
			s.releaseContact(c);
		}
	}
	
	@Override
    public void dump(PrintStream ps, int level) throws IOException {
        super.dump(ps, level);
        if (body != null) {
            body.dump(ps, level + 1);
        }
    }
}
