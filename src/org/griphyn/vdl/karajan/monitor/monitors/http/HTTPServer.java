/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 17, 2008
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.monitor.SystemState;

public class HTTPServer extends SimpleHTTPServer {
    public static final Logger logger = Logger.getLogger(HTTPServer.class);

    public static final String WEB_DIR = "httpmonitor";
    
    private SystemState state;
    private Map<String, StateDataBuilder> stateKeys;
    
    public HTTPServer(int port, String password, SystemState state) {
        super(port, password, WEB_DIR);
        this.state = state;
        buildStateKeys();
    }
    
    private void buildStateKeys() {
        stateKeys = new HashMap<String, StateDataBuilder>();
        stateKeys.put("/summary.state", new SummaryDataBuilder(state));
        stateKeys.put("/plotSeriesInfo.state", new PlotInfoBuilder(state));
        stateKeys.put("/plotData.state", new PlotDataBuilder(state));
        stateKeys.put("/browser.state", new BrowserDataBuilder(state));
    }

    public void start() throws IOException {
        super.start();
        System.out.println("HTTPMonitor server started. URL is " + getURL());
    }
    
    public boolean exists(String file) {
        if (stateKeys.containsKey(file)) {
            return true;
        }
        return super.exists(file);
    }

    @Override
    protected DataLink getDataLink(String file, Map<String, String> params) {
        if (stateKeys.containsKey(file)) {
            return new DataLink(stateKeys.get(file).getData(params), "application/json");
        }
        else {
            return super.getDataLink(file, params);
        }
    }
}
