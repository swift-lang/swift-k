// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Nov 6, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class WaitNode extends SequentialWithArguments {
	public static final Logger logger = Logger.getLogger(WaitNode.class);
	
	public static final Arg A_DELAY = new Arg.Optional("delay");
	public static final Arg A_UNTIL = new Arg.Optional("until");

	private static Timer timer;
	private Set tasks = new HashSet();

	static {
		setArguments(WaitNode.class, new Arg[] { A_DELAY, A_UNTIL });
	}

	public void post(VariableStack stack) throws ExecutionException {
		logger.debug("Executing wait");
		if (stack.isDefined("#abort")) {
			logger.debug("Aborting wait");
			abort(stack);
			return;
		}
		logger.debug("Stateful element count: "
				+ stack.getExecutionContext().getStateManager().getExecuting().size());

		stack.getExecutionContext().getStateManager().registerElement(this, stack);
		synchronized (WaitNode.class) {
			if (timer == null) {
				timer = new Timer();
			}
		}
		if (A_DELAY.isPresent(stack)) {
			timer.schedule(newTask(stack), TypeUtil.toInt(A_DELAY.getValue(stack)));
		}
		else if (A_UNTIL.isPresent(stack)) {
			String until = TypeUtil.toString(A_UNTIL.getValue(stack));
			try {
				timer.schedule(newTask(stack), DateFormat.getDateTimeInstance().parse(until));
			}
			catch (ParseException e) {
				try {
					Calendar cal = Calendar.getInstance();
					cal.setTime(DateFormat.getDateInstance().parse(until));
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					timer.schedule(newTask(stack), DateFormat.getDateInstance().parse(until));
				}
				catch (ParseException e1) {
					try {
						Calendar now = Calendar.getInstance();
						Calendar cal = Calendar.getInstance();
						cal.setTime(DateFormat.getTimeInstance().parse(until));
						cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
						cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
						cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
						if (cal.before(now)) {
							cal.add(Calendar.DAY_OF_MONTH, 1);
						}
						timer.schedule(newTask(stack), cal.getTime());
					}
					catch (ParseException e2) {
						throw new ExecutionException("Could not parse date/time: " + until, e);
					}
				}
			}
		}
	}
	
	private synchronized Task newTask(VariableStack stack) {
		Task t = new Task(this, stack);
		tasks.add(t);
		return t;
	}

	public synchronized void delayElapsed(VariableStack stack, Task task) throws ExecutionException {
		tasks.remove(task);
		stack.getExecutionContext().getStateManager().unregisterElement(this, stack);
		logger.debug("Delay elapsed");
		try {
			complete(stack);
		}
		catch (Exception e) {
			logger.debug("Failed to complete");
			failImmediately(stack, e.getMessage());
		}
	}

	public synchronized void abort(VariableStack stack) throws ExecutionException {
		for (Iterator i = tasks.iterator(); i.hasNext();) {
			Task task = (Task) i.next();
			if (ThreadingContext.get(task.stack).equals(ThreadingContext.get(stack))) {
				i.remove();
				task.cancel();
				stack.getExecutionContext().getStateManager().unregisterElement(this, stack);
				super.abort(stack);
			}
		}
	}

	private class Task extends TimerTask {
		public final VariableStack stack;
		private final WaitNode node;

		public Task(WaitNode node, VariableStack stack) {
			this.node = node;
			this.stack = stack;
		}

		public void run() {
			try {
				node.delayElapsed(stack, this);
			}
			catch (ExecutionException e) {
				logger.error("Exception caught ", e);
			}
		}
	}

}