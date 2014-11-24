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
import org.griphyn.vdl.karajan.monitor.processors.AbstractFilteringProcessor;
import org.griphyn.vdl.karajan.monitor.processors.FilteringProcessorDispatcher;
import org.griphyn.vdl.karajan.monitor.processors.coasters.BlockActiveProcessor;
import org.griphyn.vdl.karajan.monitor.processors.coasters.BlockDoneProcessor;
import org.griphyn.vdl.karajan.monitor.processors.coasters.BlockFailedProcessor;
import org.griphyn.vdl.karajan.monitor.processors.coasters.BlockRequestedProcessor;
import org.griphyn.vdl.karajan.monitor.processors.coasters.RemoteLogProcessorDispatcher;
import org.griphyn.vdl.karajan.monitor.processors.coasters.WorkerActiveProcessor;
import org.griphyn.vdl.karajan.monitor.processors.coasters.WorkerLostProcessor;
import org.griphyn.vdl.karajan.monitor.processors.coasters.WorkerProbeProcessor;
import org.griphyn.vdl.karajan.monitor.processors.coasters.WorkerShutDownProcessor;
import org.griphyn.vdl.karajan.monitor.processors.karajan.ExecutionContextProcessor;
import org.griphyn.vdl.karajan.monitor.processors.karajan.SchedulerInfoProcessor;
import org.griphyn.vdl.karajan.monitor.processors.karajan.TaskProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.AppEndProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.AppFailureProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.AppInitProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.AppSelSiteProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.AppStartProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.AppTempFailureProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.AppThreadProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.CompletionProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.ConfigurationProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.ForeachItEndProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.ForeachItStartProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.JobProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.ProcedureEndProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.ProcedureStartProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.StartSomethingProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.SummaryProcessor;
import org.griphyn.vdl.karajan.monitor.processors.swift.SwiftProcessorDispatcher;

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
        updater.addProcessor(new CompletionProcessor());
    	updater.addProcessor(new TaskProcessor());
    	updater.addProcessor(new JobProcessor());
    	updater.addProcessor(new SchedulerInfoProcessor());
    	updater.addProcessor(new ExecutionContextProcessor());
    	updater.addProcessor(new ConfigurationProcessor());
    	
    	addFilteredProcessors(updater, SwiftProcessorDispatcher.class,
    	    new AppInitProcessor(),
    	    new AppSelSiteProcessor(),
    	    new AppStartProcessor(),
    	    new AppEndProcessor(),
    	    new AppThreadProcessor(),
    	    new AppFailureProcessor(),
    	    new AppTempFailureProcessor(),
    	    new ProcedureStartProcessor(),
    	    new ProcedureEndProcessor(),
    	    new ForeachItStartProcessor(),
    	    new ForeachItEndProcessor(),
    	    new StartSomethingProcessor());
    	
    	addFilteredProcessors(updater, RemoteLogProcessorDispatcher.class,
    	    new BlockRequestedProcessor(),
    	    new BlockActiveProcessor(),
    	    new BlockDoneProcessor(),
    	    new BlockFailedProcessor(),
    	    new WorkerActiveProcessor(),
    	    new WorkerLostProcessor(),
    	    new WorkerShutDownProcessor(),
    	    new WorkerProbeProcessor());
    }

	private void addFilteredProcessors(StateUpdater updater, 
	        Class<? extends FilteringProcessorDispatcher> dcls, 
	        AbstractFilteringProcessor... ps) {
	    Map<Priority, FilteringProcessorDispatcher> m = new HashMap<Priority, FilteringProcessorDispatcher>();
	    
	    for (AbstractFilteringProcessor p : ps) {
	        Level l = p.getSupportedLevel();
	        FilteringProcessorDispatcher d = m.get(l);
	        if (d == null) {
	            try {
    	            d = dcls.newInstance();
    	            d.setLevel(l);
    	            m.put(l, d);
    	            updater.addProcessor(d);
	            }
	            catch (Exception e) {
	                throw new RuntimeException("Cannot instantiate dispatcher " + dcls, e);
	            }
	        }
	        d.add(p);
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
            state.setCurrentTime(event.getTimeStamp());
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
