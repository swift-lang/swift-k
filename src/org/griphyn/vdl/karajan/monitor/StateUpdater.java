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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.monitor.processors.LogMessageProcessor;

public class StateUpdater {
    public static final Logger logger = Logger.getLogger(StateUpdater.class);
    
	private SystemState state;
    private Map<Level, Map<String, List<LogMessageProcessor>>> levels;
        
    public StateUpdater(SystemState state) {
        this.state = state;
        this.levels = new HashMap<Level, Map<String, List<LogMessageProcessor>>>();
    }
    
    public void addProcessor(LogMessageProcessor processor) {
        Level level = processor.getSupportedLevel();    
        Map<String, List<LogMessageProcessor>> sources;
        
        synchronized(levels) {
            sources = levels.get(level);
            if (sources == null) {
                sources = new HashMap<String, List<LogMessageProcessor>>();
                levels.put(level, sources);
            }
        }
        synchronized(sources) {
        	List<LogMessageProcessor> l = sources.get(processor.getSupportedSourceName());
        	if (l == null) {
        		l = new LinkedList<LogMessageProcessor>();
        		sources.put(processor.getSupportedSourceName(), l);
        	}
            l.add(processor);
        }
        processor.initialize(state);
    }
    
    public void logEvent(Object category, String source, Object message, Object details) {
    	Map<String, List<LogMessageProcessor>> sources = levels.get(category);
        if (sources == null) {
            return;
        }
        List<LogMessageProcessor> l = sources.get(source);
        if (l == null) {
        	return;
        }
        Iterator<LogMessageProcessor> i = l.iterator();
        while (i.hasNext()) {
        	LogMessageProcessor processor = i.next();
        	processor.processMessage(state, message, details);
        }
    }
}
