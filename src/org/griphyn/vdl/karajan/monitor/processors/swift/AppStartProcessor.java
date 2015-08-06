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
package org.griphyn.vdl.karajan.monitor.processors.swift;

import org.apache.log4j.Level;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.processors.SimpleParser;

public class AppStartProcessor extends AbstractSwiftProcessor {

    public Level getSupportedLevel() {
        return Level.DEBUG;
    }

    @Override
    public String getMessageHeader() {
        return "JOB_START";
    }

    public void processMessage(SystemState state, SimpleParser p, Object details) {
        try {
            p.skip("jobid=");
            String id = p.word();

            p.matchAndSkip("tr=");
            String appname = p.word();
            String args = "";
            if (p.matchAndSkip("arguments=[")) {
                p.beginToken();
                p.markMatchedTo(']', '[');
                args = p.getToken();
            }
            ApplicationItem.QualifiedID qid = ApplicationItem.parseId(id);
            ApplicationItem app = (ApplicationItem) state.getItemByID(qid.id, StatefulItemClass.APPLICATION);
            app.setArguments(args);
            app.setState(ApplicationState.SUBMITTING, state.getCurrentTime());
            state.itemUpdated(app);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
