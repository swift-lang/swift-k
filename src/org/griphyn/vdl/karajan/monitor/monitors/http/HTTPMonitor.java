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
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.common.DataSampler;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.monitors.AbstractMonitor;

public class HTTPMonitor extends AbstractMonitor {
    public static final Logger logger = Logger.getLogger(HTTPMonitor.class);
    
    public static final int DEFAULT_PORT = 3030;
    
    private int port = DEFAULT_PORT;
    private String password;
    private HTTPServer server;

	public HTTPMonitor() {
	}
	
	@Override
    public void start() {
	    Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    // sleep 2 seconds before shutting down to allow monitor to get
                    // latest data
                    Thread.sleep(2000);
                }
                catch (Exception e) {
                    //ignored
                }
            } 
	    });
    }

    @Override
    public void setState(SystemState state) {
        DataSampler.install(state);
        super.setState(state);
        server = new HTTPServer(port, password, getState());
        try {
            server.start();
            System.out.println("HTTP montior URL: " + server.getURL());
        }
        catch (IOException e) {
            logger.warn("Failed to start HTTP monitor server", e);
        }
    }

    public void itemUpdated(SystemStateListener.UpdateType updateType, StatefulItem item) {
	}

    public void shutdown() {
    }

    @Override
    public void setParams(String params) {
        if (params.contains("@")) {
            int index = params.lastIndexOf('@');
            password = params.substring(0, index);
            port = Integer.parseInt(params.substring(index + 1));
        }
        else {
            port = Integer.parseInt(params);
        }
        if (port < 0) {
            throw new IllegalArgumentException("Negative port number!");
        }
        else if (port > 65535) {
            throw new IllegalArgumentException("Port number larger than 65535");
        }
    }
}
