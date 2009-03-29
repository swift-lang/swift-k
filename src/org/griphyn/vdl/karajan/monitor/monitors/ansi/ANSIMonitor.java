/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.monitors.AbstractMonitor;

public class ANSIMonitor extends AbstractMonitor implements Runnable {
    public static final Logger logger = Logger.getLogger(ANSIMonitor.class);
    
	private ServerSocket socket;
	private Set connections;
	private AbstractANSIDisplay disp;

	public ANSIMonitor() {
		connections = new HashSet();
		new Thread(this).start();
	}

	public void run() {
		try {
			disp = new LocalANSIDisplay(this);
			disp.start();
			socket = new ServerSocket(11010);
			while (true) {
				Socket s = socket.accept();
				RemoteANSIConnection c = new RemoteANSIConnection(this, s);
				synchronized (connections) {
					connections.add(c);
				}
				c.start();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void itemUpdated(int updateType, StatefulItem item) {
		if (disp != null) {
			disp.itemUpdated(updateType, item);
		}
		synchronized (connections) {
			Iterator i = connections.iterator();
			while (i.hasNext()) {
				((RemoteANSIConnection) i.next()).itemUpdated(updateType, item);
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
            disp.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
