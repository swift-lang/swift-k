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
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.monitors.AbstractMonitor;

public class ANSIMonitor extends AbstractMonitor implements Runnable {
    public static final Logger logger = Logger.getLogger(ANSIMonitor.class);
    
	private ServerSocket socket;
	private Set<RemoteANSIConnection> connections;
	private AbstractANSIDisplay disp;
	private int port;

	public ANSIMonitor() {
		connections = new HashSet<RemoteANSIConnection>();
		new Thread(this).start();
	}

	public void run() {
		try {
			disp = new LocalANSIDisplay(this);
			disp.start();
			if (port != 0) {
    			socket = new ServerSocket(port);
    			while (true) {
    				Socket s = socket.accept();
    				RemoteANSIConnection c = new RemoteANSIConnection(this, s);
    				synchronized (connections) {
    					connections.add(c);
    				}
    				c.start();
    			}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void itemUpdated(SystemStateListener.UpdateType updateType, StatefulItem item) {
		if (disp != null) {
			disp.itemUpdated(updateType, item);
		}
		synchronized (connections) {
		    for (RemoteANSIConnection c : connections) {
		        c.itemUpdated(updateType, item);
		    }
		}
	}

	public static void main(String[] args) {
		ANSIMonitor am = new ANSIMonitor();
		try {
			Thread.sleep(10000000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    public void remove(AbstractANSIDisplay disp) {
        if (this.disp == disp) {
            this.disp = null;
        }
    }

    public void shutdown() {
        try {
            if (disp != null) {
                disp.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setParams(String params) {
        port = Integer.parseInt(params);
    }
}
