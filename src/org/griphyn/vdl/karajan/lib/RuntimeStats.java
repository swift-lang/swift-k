/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.karajan.lib;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
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
    //formatter for timestamp against std.err lines
	public static SimpleDateFormat formatter = 
		new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
	public static final int MIN_PERIOD_MS=1000;
	public static final int MAX_PERIOD_MS=30000;

	public static final String[] preferredOutputOrder = {
		"uninitialized",
		"Initializing",
		"Selecting site",
		"Initializing site shared directory",
		"Stage in",
		"Submitting",
		"Submitted",
		"Active",
		"Checking status",
		"Stage out",
		"Failed",
		"Replicating",
		"Finished in previous run",
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
		
		// Allow user to reformat output date
		String format;
		try {
			format = VDL2Config.getDefaultConfig().getTickerDateFormat();
		} 
		catch (IOException e) {
			throw new ExecutionException(e);
		}
		if (format != null && format.length() > 0)
			formatter = 
				new SimpleDateFormat(format);
		return null;
	}

	public Object vdl_setprogress(VariableStack stack) throws ExecutionException {
		setProgress(stack, TypeUtil.toString(PA_STATE.getValue(stack)));
		return null;
	}

	static public void setProgress(VariableStack stack, String newState) throws ExecutionException {
	    RuntimeProgress p = getProgress(stack);
	    ProgressTicker t = getTicker(stack);
	    t.dec(p.status);
	    t.inc(newState);
		p.status = newState;
		t.dumpState();
	}

	public Object vdl_initprogressstate(VariableStack stack) throws ExecutionException {
		RuntimeProgress rp = new RuntimeProgress();
		ProgressTicker p = getTicker(stack);
		setProgress(stack, rp);
		rp.status = "Initializing";
		p.inc(rp.status);
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

		private Map<String, Integer> counts;

		long start;
		long lastDumpTime = 0;
		private boolean disabled;
		private boolean shutdown;

		String tickerPrefix;
		
		public ProgressTicker() {
			super("Progress ticker");
			counts = new HashMap<String, Integer>();
			try {
				if ("true".equalsIgnoreCase(VDL2Config.getConfig().getProperty("ticker.disable"))) {
					logger.info("Ticker disabled in configuration file");
					disabled = true;
				}
				tickerPrefix = 
					VDL2Config.getConfig().getTickerPrefix();
			}
			catch (IOException e) {
				logger.debug("Could not read swift properties", e);
			}
			start = System.currentTimeMillis();
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
		
		public void inc(String state) {
		    Integer crt = counts.get(state);
		    if (crt == null) {
		        counts.put(state, 1);
		    }
		    else {
		        counts.put(state, crt + 1);
		    }
		}
		
		public void dec(String state) {
            Integer crt = counts.get(state);
            if (crt != null) {
                if (crt == 1) {
                    counts.remove(state);
                }
                else {
                    counts.put(state, crt - 1);
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
			printStates(tickerPrefix);
		}

		void finalDumpState() {
			if (disabled) {
				return;
			}
			printStates("Final status:");
		}
		
		public Map getSummary() {
            return new HashMap<String, Integer>(counts);
        }

		void printStates(String prefix) {
			Map<String, Integer> summary = getSummary();
		//	SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");

			// output the results of summarization, in a relatively
			// pretty form - first the preferred order listed elements,
			// and then anything remaining
			System.err.print(prefix + " ");
			//System.err.print("  time:" + (System.currentTimeMillis() - start));
			System.err.print(formatter.format(System.currentTimeMillis()));
			
			for (int pos = 0; pos < preferredOutputOrder.length; pos++) {
				String key = preferredOutputOrder[pos];
				Integer value = summary.get(key);
				if(value != null) {
				    System.err.print("  " + key + ":" + value);
				}
				summary.remove(key);
			}

			for (Map.Entry<String, Integer> s : summary.entrySet()) {
				System.err.print(" " + s.getKey() + ":" + s.getValue());
			}

			System.err.println();
		}

	}

	class RuntimeProgress {
		String status = "uninitialized";
	}
}
