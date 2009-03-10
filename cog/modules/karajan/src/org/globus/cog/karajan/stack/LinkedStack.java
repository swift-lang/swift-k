// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.stack;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.events.EventListener;

public final class LinkedStack implements VariableStack {
	private static final Logger logger = Logger.getLogger(LinkedStack.class);

	private Entry top;

	private int frameCount;

	private transient String lastName;

	private transient Object lastVar;

	private final ExecutionContext executionContext;

	public LinkedStack(ExecutionContext ec) {
		enter();
		this.executionContext = ec;
	}

	private LinkedStack(LinkedStack stack) {
		this.frameCount = stack.frameCount;
		this.executionContext = stack.executionContext;
		top = stack.top;
	}

	public VariableStack newInstance() {
		return new LinkedStack(executionContext);
	}

	public void enter() {
		top = new Entry(top);
		frameCount++;
	}

	public void leave() {
		if (frameCount == 0) {
			throw new EmptyStackException();
		}
		top = top.prev;
		frameCount--;
	}

	public int frameCount() {
		return frameCount;
	}

	public StackFrame getFrame(final int index) {
		return skip(frameCount - index - 1).frame;
	}

	private Entry skip(int count) {
		Entry crt = top;
		for (int i = 0; i < count; i++) {
			crt = crt.prev;
		}
		return crt;
	}

	public boolean isDefined(final String varName) {
		if (varName.charAt(0) == '#') {
			return isDefined(varName, true);
		}
		else {
			return isDefined(varName, false);
		}
	}

	private boolean isDefined(final String varName, final boolean ignoreBarrier) {
		for (Entry crt = top; crt != null; crt = crt.prev) {
			final StackFrame frame = crt.frame;
			final Object o = frame.getVar(varName);
			if (o != null) {
				lastName = varName;
				lastVar = o;
				return true;
			}
			else if (frame.isDefined(varName)) {
				lastName = varName;
				lastVar = null;
				return true;
			}
			if (!ignoreBarrier && frame.hasBarrier()) {
				return this.firstFrame().isDefined(varName);
			}
		}
		return false;
	}

	private Object getShallowVarFromFrame(final String name, final int index)
			throws VariableNotFoundException {

		for (Entry crt = skip(frameCount - index - 1); crt != null; crt = crt.prev) {
			final StackFrame frame = crt.frame;
			final Object o = frame.getVar(name);
			if (o != null) {
				return o;
			}
			else if (frame.isDefined(name)) {
				return null;
			}
			if (frame.hasBarrier()) {
				final StackFrame first = firstFrame();
				final Object g = first.getVar(name);
				if (g != null) {
					return g;
				}
				else if (first.isDefined(name)) {
					return null;
				}
				break;
			}
		}
		throw new VariableNotFoundException(name);
	}
	
	private Object getDeepVarFromFrame(final String name, final int index)
			throws VariableNotFoundException {

		for (Entry crt = skip(frameCount - index - 1); crt != null; crt = crt.prev) {
			final StackFrame frame = crt.frame;
			if (frame.isDefined(name)) {
				return frame.getVar(name);
			}
		}
		throw new VariableNotFoundException(name);
	}


	private Object _getVarFromFrame(final String name, final int index)
			throws VariableNotFoundException {
		if (name.charAt(0) == '#') {
			return getDeepVarFromFrame(name, index);
		}
		else {
			return getShallowVarFromFrame(name, index);
		}
	}

	public Object getVarFromFrame(final String name, final int index)
			throws VariableNotFoundException {
		return _getVarFromFrame(name, frameCount - index - 1);
	}

	public Object getVar(final String name) throws VariableNotFoundException {
		if (lastName != null) {
			if (name.equals(lastName)) {
				lastName = null;
				return lastVar;
			}
			lastName = null;
		}
		return _getVarFromFrame(name, frameCount - 1);
	}

	public Object getDeepVar(final String name) throws VariableNotFoundException {
		return getDeepVarFromFrame(name, frameCount - 1);
	}

	public Object getShallowVar(final String name) throws VariableNotFoundException {
		return getShallowVarFromFrame(name, frameCount - 1);
	}

	public List getAllVars(final String name) {
		final List vars = new LinkedList();
		synchronized (executionContext) {
			for (Entry crt = top; crt != null; crt = crt.prev) {
				final StackFrame frame = crt.frame;
				if (frame.isDefined(name)) {
					vars.add(frame.getVar(name));
				}
			}
		}
		return vars;
	}

	public String getVarAsString(final String varName) throws VariableNotFoundException {
		final Object o = getVar(varName);
		if (o == null) {
			throw new VariableNotFoundException("Variable not found: " + varName);
		}
		else {
			return TypeUtil.toString(o);
		}
	}

	public StackFrame currentFrame() {
		return top.frame;
	}

	public StackFrame parentFrame() {
		return top.prev.frame;
	}

	public StackFrame firstFrame() {
		return skip(frameCount - 1).frame;
	}

	public void setVar(final String name, final Object value) {
		if (name == null) {
			throw new RuntimeException(
					"Trying to set a variable with null name. This may indicate an error.");
		}
		top.frame.setVar(name, value);
	}

	public void exportVar(final String name) {
		if (top.prev == null) {
			return;
		}
		if (top.frame.isDefined(name)) {
			top.prev.frame.setVar(name, top.frame.getVarAndDelete(name));
		}
	}

	public VariableStack copy() {
		return new LinkedStack(this);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Stack dump:\n");
		int i = frameCount;
		for (int li = frameCount - 1; li >= 0; li--) {

			StackFrame frame = getFrame(li);
			if (frame == null) {
				buf.append("Null frame!\n");
				continue;
			}
			if (frame.hasBarrier()) {
				buf.append("Level " + i + "[barrier]\n");
			}
			else {
				buf.append("Level " + i + "\n");
			}
			buf.append(frame + "\n");
			i--;
		}
		return buf.toString();
	}

	public void dumpAll() {
		System.out.println(toString());
	}

	public void setVar(final String name, final int value) {
		setVar(name, new Integer(value));
	}

	public int getIntVar(final String name) throws VariableNotFoundException {
		return ((Integer) getVar(name)).intValue();
	}

	public void setVar(final String name, final boolean value) {
		setVar(name, Boolean.valueOf(value));
	}

	public boolean getBooleanVar(final String name) throws VariableNotFoundException {
		return ((Boolean) getVar(name)).booleanValue();
	}

	public void setBarrier() {
		currentFrame().setBarrier(true);
	}

	public void setGlobal(final String name, final Object value) {
		firstFrame().setVar(name, value);
	}

	public Object getGlobal(final String name) {
		return firstFrame().getVar(name);
	}

	public Regs getRegs() {
		return currentFrame().getRegs();
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}
	
	public EventListener getCaller() {
		Entry crt = top;
		while (crt != null) {
			EventListener caller = crt.frame.getRegs().getCaller();
			if (caller != null) {
				return caller;
			}
			else {
				crt = crt.prev;
			}
		}
		return null;
	}

	public void setCaller(EventListener caller) {
		currentFrame().getRegs().setCaller(caller);
	}
	
	public List getAllCallers() {
		List l = new ArrayList();
		Entry crt = top;
		while (crt != null) {
			EventListener caller = crt.frame.getRegs().getCaller();
			if (caller != null) {
				l.add(caller);
			}
			else {
				crt = crt.prev;
			}
		}
		return l;
	}

	private static final class Entry {
		private final StackFrame frame;
		private final Entry prev;

		public Entry(Entry prev) {
			this.prev = prev;
			frame = new DefaultStackFrame();
		}

		public Entry(Entry prev, StackFrame frame) {
			this.prev = prev;
			this.frame = frame;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (frame != null) {
				sb.append(frame.toString());
			}
			else {
				sb.append("No frame");
			}
			if (prev != null) {
				sb.append("-> \n");
				sb.append(prev.toString());
			}
			return sb.toString();
		}
	}
}