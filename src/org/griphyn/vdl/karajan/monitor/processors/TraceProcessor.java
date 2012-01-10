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
package org.griphyn.vdl.karajan.monitor.processors;

import org.apache.log4j.Level;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.TraceItem;

public class TraceProcessor implements LogMessageProcessor {

    public Object getSupportedCategory() {
        return Level.INFO;
    }

    public String getSupportedSource() {
        return "org.griphyn.vdl.karajan.lib.Log";
    }

    public void processMessage(SystemState state, Object message, Object details) {
        SimpleParser p = new SimpleParser(String.valueOf(message));
        try {
            String line = null;
            boolean started = false, ended = false;
            if (p.matchAndSkip("PROCEDURE line=")) {
                line = p.word();
                started = true;
            }
            else if (p.matchAndSkip("PROCEDURE_END line=")) {
                line = p.word();
                ended = true;
            }
            else if (p.matchAndSkip("FOREACH_IT_START line=")) {
                line = p.word();
                started = true;
            }
            else if (p.matchAndSkip("FOREACH_IT_END line=")) {
                line = p.word();
                ended = true;
            }

            if (line != null) {
                TraceItem ti = (TraceItem) state.getItemByID(line,
                    StatefulItemClass.TRACE);
                if (ti == null) {
                    ti = new TraceItem(line);
                    state.addItem(ti);
                }
                if (started) {
                    ti.incStarted();
                }
                else if (ended) {
                    ti.incEnded();
                }
                state.itemUpdated(ti);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
