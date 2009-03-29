/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.griphyn.vdl.karajan.monitor.monitors.Monitor;
import org.griphyn.vdl.karajan.monitor.monitors.MonitorFactory;
import org.griphyn.vdl.karajan.monitor.processors.AppProcessor;
import org.griphyn.vdl.karajan.monitor.processors.AppThreadProcessor;
import org.griphyn.vdl.karajan.monitor.processors.ExecutionContextProcessor;
import org.griphyn.vdl.karajan.monitor.processors.JobProcessor;
import org.griphyn.vdl.karajan.monitor.processors.ProcedureProcessor;
import org.griphyn.vdl.karajan.monitor.processors.SchedulerInfoProcessor;
import org.griphyn.vdl.karajan.monitor.processors.TaskProcessor;
import org.griphyn.vdl.karajan.monitor.processors.TraceProcessor;

public class MonitorAppender implements Appender {
    public static final Logger logger = Logger.getLogger(MonitorAppender.class);
    
	private SystemState state;
	private StateUpdater updater;
	private Monitor monitor;

	public MonitorAppender(String projectName) {
		state = new SystemState(projectName);
		updater = new StateUpdater(state);
        addProcessors(updater);
		try {
			monitor = MonitorFactory.newInstance("ANSI");
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
    	updater.addProcessor(new AppProcessor());
    	updater.addProcessor(new TaskProcessor());
    	updater.addProcessor(new JobProcessor());
    	updater.addProcessor(new AppThreadProcessor());
    	updater.addProcessor(new SchedulerInfoProcessor());
    	updater.addProcessor(new ProcedureProcessor());
    	updater.addProcessor(new ExecutionContextProcessor());
    	updater.addProcessor(new TraceProcessor());
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
        	updater.logEvent(event.getLevel(), event.getLocationInformation().getClassName(),
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
}
