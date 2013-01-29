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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.util.VDL2Config;

/** this is an icky class that does too much with globals, but is for
proof of concept. */

public class RuntimeStats {

    public static final boolean TICKER_DISABLED;
    
    static{
        boolean disabled;
        try{
            disabled = "true".equalsIgnoreCase(VDL2Config.getConfig().getProperty("ticker.disable"));
        }
        catch (Exception e) {
            disabled = false;
        }
        TICKER_DISABLED = disabled;
    }
    
	public static final String TICKER = "SWIFT_TICKER";

    //formatter for timestamp against std.err lines
	public static SimpleDateFormat formatter = 
		new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
	public static final int MIN_PERIOD_MS=1000;
	public static final int MAX_PERIOD_MS=1000;

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
		
	public static class StartProgressTicker extends Node {
	    private VarRef<Context> context;

        @Override
        public Node compile(WrapperNode w, Scope scope)
                throws CompilationException {
            super.compile(w, scope);
            context = scope.getVarRef("#context");
            return TICKER_DISABLED ? null : this;
        }

        @Override
        public void run(LWThread thr) {
            ProgressTicker t = new ProgressTicker();
            t.setDaemon(true);
            t.start();
            context.getValue(thr.getStack()).setAttribute(TICKER, t);
            // Allow user to reformat output date
            String format;
            try {
                format = VDL2Config.getDefaultConfig().getTickerDateFormat();
            } 
            catch (IOException e) {
                throw new ExecutionException(this, e);
            }
            if (format != null && format.length() > 0) {
                formatter = new SimpleDateFormat(format);
            }
        }
	}
	
	public static class InitProgressState extends Node {
	    private VarRef<Context> context;
	    private ChannelRef<Object> cr_vargs;
	    
	    @Override
        public Node compile(WrapperNode w, Scope scope)
                throws CompilationException {
            super.compile(w, scope);
            context = scope.getVarRef("#context");
            Var.Channel r = scope.lookupChannel("...");
            r.appendDynamic();
            cr_vargs = scope.getChannelRef(r);
            return TICKER_DISABLED ? null : this;
        }

        @Override
        public void run(LWThread thr) {
            ProgressState ps = new ProgressState();
            ps.crt = "Initializing";
            Stack stack = thr.getStack();
            ((ProgressTicker) context.getValue(stack).getAttribute(TICKER)).addState(ps);
            cr_vargs.get(stack).add(ps);
        }
	}
	
	public static class SetProgress extends InternalFunction {
        private ArgRef<ProgressState> ps;
        private ArgRef<String> state;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("ps", "state"));
        }
        
        @Override
        protected void runBody(LWThread thr) {
            Stack stack = thr.getStack();
            ps.getValue(stack).crt = state.getValue(stack);
        }
    }


	public static class StopProgressTicker extends Node {
        private VarRef<Context> context;

        @Override
        public Node compile(WrapperNode w, Scope scope)
                throws CompilationException {
            super.compile(w, scope);
            context = scope.getVarRef("#context");
            return TICKER_DISABLED ? null : this;
        }

        @Override
        public void run(LWThread thr) {
        	ProgressTicker t = (ProgressTicker) context.getValue(thr.getStack()).getAttribute(TICKER);
            t.finalDumpState();
            t.shutdown();
        }
    }

	
	public static class ProgressState {
	    private String crt;

        public void setState(String state) {
            this.crt = state;
        }
        
        public String toString() {
            return "ProgressState: " + crt; 
        }
	}


	static public class ProgressTicker extends Thread {
	    
	    public static final Logger logger = Logger.getLogger(ProgressTicker.class);

		private Set<ProgressState> states;

		long start;
		long lastDumpTime = 0;
		private boolean disabled;
		private boolean shutdown;

		String tickerPrefix;
		
		public ProgressTicker() {
			super("Progress ticker");
			states = new HashSet<ProgressState>();
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
		
		public void addState(ProgressState ps) {
			synchronized(states) {
			    states.add(ps);
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
			printStates(tickerPrefix);
		}

		void finalDumpState() {
			if (disabled) {
				return;
			}
			printStates("Final status:");
		}
		
		private Map<String, Integer> getSummary() {
			List<ProgressState> states;
			
			synchronized(this.states) {
				states = new ArrayList<ProgressState>(this.states);
			}
		    Map<String, Integer> m = new HashMap<String, Integer>();

	        for (ProgressState s : states) {
	            inc(m, s);
	        }
		    return m;
		}
				
		private void inc(Map<String, Integer> m, ProgressState s) {
		    String v = s.crt;
		    Integer i = m.get(v);
		    if (i == null) {
		        i = 1;
		    }
		    else {
		        i = i + 1;
		    }
		    m.put(v, i);
        }

        synchronized void printStates(String prefix) {
			Map<String, Integer> summary = getSummary();
			
			StringBuilder sb = new StringBuilder();

			// output the results of summarization, in a relatively
			// pretty form - first the preferred order listed elements,
			// and then anything remaining
			System.err.print(prefix);
			System.err.print(" ");
			System.err.print(formatter.format(System.currentTimeMillis()));
						
			for (int pos = 0; pos < preferredOutputOrder.length; pos++) {
				String key = preferredOutputOrder[pos];
				Integer value = summary.get(key);
				if(value != null) {
				    sb.append("  ");
				    sb.append(key);
				    sb.append(":");
				    sb.append(value);
				}
				summary.remove(key);
			}

			for (Map.Entry<String, Integer> s : summary.entrySet()) {
			    sb.append("  ");
			    sb.append(s.getKey());
			    sb.append(":");
			    sb.append(s.getValue());
			}
		
			String msg = sb.toString();

			System.err.println(msg);
			if (logger.isInfoEnabled()) {
			    logger.info(msg);
			}
		}

	}
}
