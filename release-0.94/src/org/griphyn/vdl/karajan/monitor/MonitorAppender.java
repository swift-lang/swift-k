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


/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.griphyn.vdl.karajan.monitor.monitors.Monitor;
import org.griphyn.vdl.karajan.monitor.monitors.MonitorFactory;
import org.griphyn.vdl.karajan.monitor.processors.AbstractSwiftProcessor;
import org.griphyn.vdl.karajan.monitor.processors.AppEndProcessor;
import org.griphyn.vdl.karajan.monitor.processors.AppStartProcessor;
import org.griphyn.vdl.karajan.monitor.processors.AppThreadProcessor;
import org.griphyn.vdl.karajan.monitor.processors.ExecutionContextProcessor;
import org.griphyn.vdl.karajan.monitor.processors.ForeachItEndProcessor;
import org.griphyn.vdl.karajan.monitor.processors.ForeachItStartProcessor;
import org.griphyn.vdl.karajan.monitor.processors.JobProcessor;
import org.griphyn.vdl.karajan.monitor.processors.ProcedureEndProcessor;
import org.griphyn.vdl.karajan.monitor.processors.ProcedureStartProcessor;
import org.griphyn.vdl.karajan.monitor.processors.SchedulerInfoProcessor;
import org.griphyn.vdl.karajan.monitor.processors.SummaryProcessor;
import org.griphyn.vdl.karajan.monitor.processors.SwiftProcessorDispatcher;
import org.griphyn.vdl.karajan.monitor.processors.TaskProcessor;

public class MonitorAppender implements Appender {
    public static final Logger logger = Logger.getLogger(MonitorAppender.class);
    
	private SystemState state;
	private StateUpdater updater;
	private Monitor monitor;
	
	public MonitorAppender(String projectName) {
	    this(projectName, "ANSI");
	}

	public MonitorAppender(String projectName, String monitorType) {
		state = new SystemState(projectName);
		updater = new StateUpdater(state);
        addProcessors(updater);
		try {
			monitor = MonitorFactory.newInstance(monitorType);
            monitor.setState(state);
		}
		catch (Exception e) {
			logger.error(e);
		}
		catch (Error e) {
		    logger.error(e);
		    throw e;
		}
	}
    
    private void addProcessors(StateUpdater updater) {
        updater.addProcessor(new SummaryProcessor());
    	updater.addProcessor(new TaskProcessor());
    	updater.addProcessor(new JobProcessor());
    	updater.addProcessor(new SchedulerInfoProcessor());
    	updater.addProcessor(new ExecutionContextProcessor());
    	
    	addSwiftProcessors(updater, 
    	    new AppStartProcessor(),
    	    new AppEndProcessor(),
    	    new AppThreadProcessor(),
    	    new ProcedureStartProcessor(),
    	    new ProcedureEndProcessor(),
    	    new ForeachItStartProcessor(),
    	    new ForeachItEndProcessor());
    }

	private void addSwiftProcessors(StateUpdater updater, AbstractSwiftProcessor... ps) {
	    Map<Priority, SwiftProcessorDispatcher> m = new HashMap<Priority, SwiftProcessorDispatcher>();
	    
	    for (AbstractSwiftProcessor p : ps) {
	        Level l = p.getSupportedLevel();
	        SwiftProcessorDispatcher d = m.get(l);
	        if (d == null) {
	            d = new SwiftProcessorDispatcher(l);
	            m.put(l, d);
	        }
	        d.add(p);
	    }
	    
	    for (SwiftProcessorDispatcher d : m.values()) {
	        updater.addProcessor(d);
	    }
    }

    public void addFilter(Filter newFilter) {
	}

	public void clearFilters() {
	}

	public void close() {
	    monitor.shutdown();
	}

	public void doAppend(LoggingEvent event) {
        try {
        	updater.logEvent(event.getLevel(), event.getLogger().getName(),
        			event.getMessage(), event.getThrowableInformation());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}

	public ErrorHandler getErrorHandler() {
		return null;
	}

	public Filter getFilter() {
		return null;
	}

	public Layout getLayout() {
		return null;
	}

	public String getName() {
		return "MonitorAppender";
	}

	public boolean requiresLayout() {
		return false;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
	}

	public void setLayout(Layout layout) {
	}

	public void setName(String name) {
	}
	
	public Monitor getMonitor() {
	    return monitor;
	}
}
