/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.processors.LogMessageProcessor;

public class StateUpdater {
	private SystemState state;
    private Map categories;
    
    public StateUpdater(SystemState state) {
        this.state = state;
        this.categories = new HashMap();
    }
    
    public void addProcessor(LogMessageProcessor processor) {
        Object category = processor.getSupportedCategory();
        Map sources;
        synchronized(categories) {
            sources = (Map) categories.get(category);
            if (sources == null) {
                sources = new HashMap();
                categories.put(category, sources);
            }
        }
        synchronized(sources) {
        	List l = (List) sources.get(processor.getSupportedSource());
        	if (l == null) {
        		l = new LinkedList();
        		sources.put(processor.getSupportedSource(), l);
        	}
            l.add(processor);
        }
    }
    
    public void logEvent(Object category, String source, Object message, Object details) {
    	Map sources = (Map) categories.get(category);
        if (sources == null) {
            return; 
        }
        List l = (List) sources.get(source);
        if (l == null) {
        	return;
        }
        Iterator i = l.iterator();
        while (i.hasNext()) {
        	LogMessageProcessor processor = (LogMessageProcessor) i.next();
        	processor.processMessage(state, message, details);
        }
    }
}
