// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 14, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class EventWait extends SequentialWithArguments implements ActionListener, WindowListener {
	private static final Logger logger = Logger.getLogger(EventWait.class);

	private final Map groups, stacks, rstacks;

	public EventWait() {
		groups = new HashMap();
		stacks = new HashMap();
		rstacks = new HashMap();
	}

	static {
		setArguments(EventWait.class, new Arg[] { Arg.VARGS });
	}

	public synchronized void post(VariableStack stack) throws ExecutionException {
		Object[] vargs = Arg.VARGS.asArray(stack);
		Map group = new HashMap();
		for (int i = 0; i < vargs.length; i++) {
			Object arg = vargs[i];
			if (!(arg instanceof List)) {
				throw new ExecutionException("Each argument must be a list");
			}
			List list = (List) arg;
			if (list.size() != 3) {
				throw new ExecutionException("Each argument must be a list containing 3 items");
			}
			try {
				String ret = (String) list.get(0);
				String type = (String) list.get(1);
				Object source = list.get(2);
				group.put(source, ret);
				List grlist;
				if (groups.containsKey(source)) {
					grlist = (List) groups.get(source);
				}
				else {
					grlist = new LinkedList();
				}
				grlist.add(group);
				groups.put(source, grlist);
				if ("java.awt.events.ActionEvent".equals(type)) {
					addListener(source, "addActionListener", ActionListener.class);
				}
				else if ("java.awt.events.WindowEvent".equals(type)) {
					addListener(source, "addWindowListener", WindowListener.class);
				}
				else {
					throw new ExecutionException("Unknown event type: " + type);
				}
			}
			catch (ExecutionException e) {
				throw e;
			}
			catch (Exception e) {
				throw new ExecutionException("Exception caught while adding listener", e);
			}
		}
		stacks.put(group, stack);
		rstacks.put(stack, group);
		stack.getExecutionContext().getStateManager().registerElement(this, stack);
	}

	protected void addListener(Object source, String methodName, Class argType)
			throws ExecutionException {
		try {
			Method method = source.getClass().getMethod(methodName, new Class[] { argType });
			method.invoke(source, new Object[] { this });
		}
		catch (SecurityException e) {
			throw new ExecutionException("No access to " + methodName + " method", e);
		}
		catch (NoSuchMethodException e) {
			throw new ExecutionException("Object does not have a " + methodName + "("
					+ argType.toString() + ") method", e);
		}
		catch (IllegalArgumentException e) {
			throw new ExecutionException("Unsupported event type: " + argType.getName(), e);
		}
		catch (IllegalAccessException e) {
			throw new ExecutionException(
					"Cannot invoke " + methodName + " on " + source.toString(), e);
		}
		catch (InvocationTargetException e) {
			throw new ExecutionException(methodName + " threw an exception", e);
		}
	}

	protected synchronized void processEvent(Object source) {
		if (groups.containsKey(source)) {
			List grlist = (List) groups.get(source);
			Iterator i = grlist.iterator();
			while (i.hasNext()) {
				Map group = (Map) i.next();
				i.remove();
				VariableStack stack = (VariableStack) stacks.remove(group);
				rstacks.remove(stack);
				if (stack != null) {
					Object ret = group.get(source);
					try {
						// TODO remove listeners
						ArgUtil.getVariableReturn(stack).append(ret);
							stack.getExecutionContext().getStateManager().unregisterElement(this,
									stack);
							complete(stack);
					}
					catch (ExecutionException ex) {
						failImmediately(stack, ex);
					}
				}
			}
		}
	}

	public synchronized void actionPerformed(ActionEvent e) {
		processEvent(e.getSource());
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		processEvent(e.getSource());
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	protected synchronized void abort(VariableStack stack) throws ExecutionException {
		stack.getExecutionContext().getStateManager().unregisterElement(this, stack);
		Object group = rstacks.get(stack);
		groups.remove(group);
		stacks.remove(group);
		rstacks.remove(stack);
		super.abort(stack);
	}
}