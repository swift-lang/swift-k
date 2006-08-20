// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.stack;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.NamedArgumentsImpl;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.arguments.VariableArgumentsImpl;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionContext;

public final class FastStack implements VariableStack {
	private static final Logger logger = Logger.getLogger(FastStack.class);

	private static final String NARGS = "#nargs";

	private static final String VARGS = "#vargs";

	private static final NamedArguments EMPTYNARGS = new NamedArgumentsImpl();

	private static final VariableArguments EMPTYVARGS = new VariableArgumentsImpl();

	private StackFrame[] stack;

	private int frameCount;

	private transient String lastName;

	private transient Object lastVar;

	private final ExecutionContext executionContext;

	public FastStack(ExecutionContext ec) {
		stack = new StackFrame[16];
		addFirst(newFrame());
		this.executionContext = ec;
	}

	private FastStack(FastStack stack) {
		this.stack = new StackFrame[stack.stack.length];
		System.arraycopy(stack.stack, 0, this.stack, 0, this.stack.length);
		this.frameCount = stack.frameCount;
		this.executionContext = stack.executionContext;
	}

	private StackFrame newFrame() {
		return new DefaultStackFrame();
	}

	public VariableStack newInstance() {
		return new FastStack(executionContext);
	}

	private void addFirst(final StackFrame frame) {
		stack[frameCount++] = frame;
		if (frameCount >= stack.length) {
			StackFrame[] newStack = new StackFrame[stack.length << 1];
			System.arraycopy(stack, 0, newStack, 0, stack.length);
			stack = newStack;
		}
	}

	public void enter() {
		addFirst(newFrame());
	}

	private void addFrame(final StackFrame frame) {
		addFirst(frame);
	}

	public void leave() {
		stack[--frameCount] = null;
		if ((frameCount < stack.length >> 2) && (frameCount > 16)) {
			StackFrame[] newStack = new StackFrame[stack.length >> 1];
			System.arraycopy(stack, 0, newStack, 0, frameCount);
			stack = newStack;
		}
	}

	public int frameCount() {
		return frameCount;
	}

	public StackFrame getFrame(final int index) {
		return stack[index];
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
		for (int i = frameCount - 1; i >= 0; i--) {
			StackFrame frame = stack[i];
			Object o = frame.getVar(varName);
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

	private Object getVarFromFrame(final String name, final int index, final boolean ignoreBarrier)
			throws VariableNotFoundException {

		for (int i = index; i >= 0; i--) {
			StackFrame frame = stack[i];
			Object o = frame.getVar(name);
			if (o != null) {
				return o;
			}
			else if (frame.isDefined(name)) {
				return null;
			}
			if (!ignoreBarrier && frame.hasBarrier()) {
				if (firstFrame().isDefined(name)) {
					return firstFrame().getVar(name);
				}
				break;
			}
		}
		throw new VariableNotFoundException("Variable not found: " + name);
	}

	private Object _getVarFromFrame(final String name, final int index)
			throws VariableNotFoundException {
		if (name.charAt(0) == '#') {
			return getVarFromFrame(name, index, true);
		}
		else {
			return getVarFromFrame(name, index, false);
		}
	}

	public Object getVarFromFrame(final String name, final int index)
			throws VariableNotFoundException {
		return _getVarFromFrame(name, frameCount - index - 1);
	}

	public Object getDeepVar(final String name) throws VariableNotFoundException {
		return getVarFromFrame(name, frameCount - 1, true);
	}

	public Object getShallowVar(final String name) throws VariableNotFoundException {
		return getVarFromFrame(name, frameCount - 1, false);
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

	public List getAllVars(final String name) {
		List vars = new LinkedList();
		synchronized (stack) {
			for (int i = frameCount - 1; i >= 0; i--) {
				StackFrame frame = stack[i];
				if (frame.isDefined(name)) {
					vars.add(frame.getVar(name));
				}
			}
		}
		return vars;
	}

	public String getVarAsString(final String varName) throws VariableNotFoundException {
		Object o = getVar(varName);
		if (o == null) {
			throw new VariableNotFoundException("Variable not found: " + varName);
		}
		else {
			return TypeUtil.toString(o);
		}
	}

	public StackFrame currentFrame() {
		return stack[frameCount - 1];
	}

	public StackFrame parentFrame() {
		return stack[frameCount - 2];
	}

	public StackFrame firstFrame() {
		return stack[0];
	}

	public void setVar(final String name, final Object value) {
		if (name == null) {
			throw new RuntimeException(
					"Trying to set a variable with null name. This may indicate an error.");
		}
		stack[frameCount - 1].setVar(name, value);
	}

	public void exportVar(final String name) {
		if (stack.length <= 1) {
			return;
		}
		if (stack[frameCount - 1].isDefined(name)) {
			stack[frameCount - 2].setVar(name, stack[frameCount - 1].getVarAndDelete(name));
		}
	}

	public VariableStack copy() {
		return new FastStack(this);
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

	public String trace() {
		StringBuffer buf = new StringBuffer();
		for (int li = frameCount - 1; li >= 0; li--) {
			StackFrame frame = getFrame(li);
			if (frame.isDefined("#caller")) {
				buf.append('\t');
				buf.append(frame.getVar("#caller"));
				buf.append('\n');
			}
		}
		return buf.toString();
	}

	public Regs getRegs() {
		return currentFrame().getRegs();
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}
}