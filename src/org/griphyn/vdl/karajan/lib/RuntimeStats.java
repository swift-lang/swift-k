package org.griphyn.vdl.karajan.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.griphyn.vdl.util.VDL2Config;

/** this is an icky class that does too much with globals, but is for
proof of concept. */

public class RuntimeStats extends FunctionsCollection {

	public static final String TICKER = "#swift-runtime-progress-ticker";
	public static final String PROGRESS = "#swift-runtime-progress";

	public static final Arg PA_STATE = new Arg.Positional("state");
	public static final int MIN_PERIOD_MS=1000;
	public static final int MAX_PERIOD_MS=30000;

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
	
	public static void setTicker(VariableStack stack, ProgressTicker ticker) {
	    stack.setGlobal(TICKER, ticker);
	}
	
	public static ProgressTicker getTicker(VariableStack stack) {
		return (ProgressTicker) stack.getGlobal(TICKER);
	}
	
	public static void setProgress(VariableStack stack, RuntimeProgress p) {
		stack.parentFrame().setVar(PROGRESS, p);
	}

	public static RuntimeProgress getProgress(VariableStack stack) throws VariableNotFoundException {
		return (RuntimeProgress) stack.getDeepVar(PROGRESS);
	}

	public Object vdl_startprogressticker(VariableStack stack) throws ExecutionException {
		ProgressTicker t = new ProgressTicker();
		t.setDaemon(true);
		t.start();
		setTicker(stack, t);
		return null;
	}


	public Object vdl_setprogress(VariableStack stack) throws ExecutionException {
		setProgress(stack, TypeUtil.toString(PA_STATE.getValue(stack)));
		return null;
	}

	static public void setProgress(VariableStack stack, String newState) throws ExecutionException {
		getProgress(stack).status = newState;
		getTicker(stack).dumpState();
	}

	public Object vdl_initprogressstate(VariableStack stack) throws ExecutionException {
		RuntimeProgress rp = new RuntimeProgress();
		ProgressTicker p = getTicker(stack);
		synchronized (p.states) {
			p.states.add(rp);
		}
		setProgress(stack, rp);
		p.dumpState();
		return null;
	}

	public synchronized Object vdl_stopprogressticker(VariableStack stack) throws ExecutionException {
		ProgressTicker p = getTicker(stack);
		p.finalDumpState();
		p.shutdown();
		return null;
	}


	static public class ProgressTicker extends Thread {

		List states = new ArrayList();

		long lastDumpTime = 0;
		private boolean disabled;
		private boolean shutdown;

		public ProgressTicker() {
			super("Progress ticker");
			try {
				if ("true".equalsIgnoreCase(VDL2Config.getConfig().getProperty("ticker.disable"))) {
					logger.info("Ticker disabled in configuration file");
					disabled = true;
				}
			}
			catch (IOException e) {
				logger.debug("Could not read swift properties", e);
			}
		}

		public void run() {
			if (disabled) {
				return;
			}
			while(!shutdown) {
				dumpState();

				try {
					Thread.sleep(MAX_PERIOD_MS);
				} catch(InterruptedException e) {
					System.err.println("Runtime ticker interrupted. Looping immediately.");
				}
			}
		}
		
		void shutdown() {
		    shutdown = true;
		}

		void dumpState() {
			if (disabled) {
				return;
			}
			long now = System.currentTimeMillis();
			if(lastDumpTime + MIN_PERIOD_MS > now) return;
			lastDumpTime = now;
			printStates("Progress: ");
		}

		void finalDumpState() {
			if (disabled) {
				return;
			}
			printStates("Final status: ");
		}
		
		public Map getSummary() {
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
			return summary;
		}

		void printStates(String header) {
			Map summary = getSummary();
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
