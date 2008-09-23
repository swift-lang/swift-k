package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.globus.cog.karajan.util.TypeUtil;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/** this is an icky class that does too much with globals, but is for
proof of concept. */

public class RuntimeStats extends FunctionsCollection {

	public static final Arg PA_STATE = new Arg.Positional("state");
	public static final int MIN_PERIOD_MS=5000;
	public static final int MAX_PERIOD_MS=60000;

	public static final String[] preferredOutputOrder = {
                "uninitialized",
		"Initializing",
		"Selecting site",
		"Stage in",
		"Submitting",
		"Submitted",
		"Active",
		"Stage out",
		"Failed",
		"Replicating",
		"Finished successfully"
	};

	static {
		setArguments("vdl_startprogressticker", new Arg[0]);
		setArguments("vdl_stopprogressticker", new Arg[0]);
		setArguments("vdl_setprogress", new Arg[] { PA_STATE } );
		setArguments("vdl_initprogressstate", new Arg[] { PA_STATE });
	}

	public Object vdl_startprogressticker(VariableStack stack) throws ExecutionException {
		ProgressTicker t = new ProgressTicker();
		t.setDaemon(true);
		t.start();
		stack.parentFrame().setVar("#swift-runtime-progress-ticker",t);
		return null;
	}


	public Object vdl_setprogress(VariableStack stack) throws ExecutionException {
		setProgress(stack, TypeUtil.toString(PA_STATE.getValue(stack)));
		return null;
	}

	static public void setProgress(VariableStack stack, String newState) throws ExecutionException {
		RuntimeProgress rp = (RuntimeProgress)stack.getVar("#swift-runtime-progress");
		rp.status = newState;
		ProgressTicker p = (ProgressTicker)stack.getVar("#swift-runtime-progress-ticker");
		p.dumpState();
	}

	public Object vdl_initprogressstate(VariableStack stack) throws ExecutionException {
		RuntimeProgress rp = new RuntimeProgress();
		ProgressTicker p = (ProgressTicker)stack.getVar("#swift-runtime-progress-ticker");
		synchronized (p.states) {
			p.states.add(rp);
		}
		stack.parentFrame().setVar("#swift-runtime-progress",rp);
		p.dumpState();
		return null;
	}

	public synchronized Object vdl_stopprogressticker(VariableStack stack) throws ExecutionException {
		ProgressTicker p = (ProgressTicker)stack.getVar("#swift-runtime-progress-ticker");
		p.finalDumpState();
		p.stop();
		return null;
	}


	static public class ProgressTicker extends Thread {

		List states = new ArrayList();

		long lastDumpTime = 0;

		public ProgressTicker() {
			super("Progress ticker");
		}

		public void run() {
			while(true) {
				dumpState();

				try {
					Thread.sleep(MAX_PERIOD_MS);
				} catch(InterruptedException e) {

					System.err.println("Runtime ticker interrupted. Looping immediately.");
				}
			}
		}

		void dumpState() {
			long now = System.currentTimeMillis();
			if(lastDumpTime + MIN_PERIOD_MS > now) return;
			lastDumpTime = now;
			printStates("Progress: ");
		}

		void finalDumpState() {
			printStates("Final status: ");
		}

		void printStates(String header) {
			Map summary = new HashMap();
			synchronized(states) {
				Iterator stateIterator = states.iterator();

				// summarize details of known states into summary, with
				// one entry per state type, storing the number of
				// jobs in that state.
				while(stateIterator.hasNext()) {
					String key = ((RuntimeProgress)stateIterator.next()).status;
					Integer count = (Integer) summary.get(key);
					if(count == null) {
						summary.put(key,new Integer(1));
					} else {
						summary.put(key,new Integer(count.intValue()+1));
					}
				}
			}

			// output the results of summarization, in a relatively
			// pretty form - first the preferred order listed elements,
			// and then anything remaining
			System.err.print(header);

			for(int pos = 0; pos < preferredOutputOrder.length; pos++) {
				String key = preferredOutputOrder[pos];
				Object value = summary.get(key);
				if(value != null)
					System.err.print(" "+key+":"+value);
				summary.remove(key);
			}

			Iterator summaryIterator = summary.keySet().iterator();
			while(summaryIterator.hasNext()) {
				Object key = summaryIterator.next();
				System.err.print(" "+key+":"+summary.get(key));
			}
			System.err.println("");
		}

	}

	class RuntimeProgress {
		String status = "uninitialized";
	}

}
