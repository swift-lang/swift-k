//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 19, 2008
 */
package org.globus.cog.abstraction.coaster.service.local;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.Registering;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.ConnectionHandler;
import org.globus.cog.karajan.workflow.service.GSSService;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.gsi.gssapi.auth.SelfAuthorization;

public class LocalService extends GSSService implements Registering {
    public static final Logger logger = Logger.getLogger(LocalService.class);

    public static final long DEFAULT_REGISTRATION_TIMEOUT = 120 * 1000;

    private Map services;

    public LocalService() throws IOException {
        super();
    }

    public void start() {
        setAuthorization(new SelfAuthorization());
        services = new HashMap();
        this.accept = true;
        Thread t = new Thread(this);
        t.setName("Local service");
        t.setDaemon(true);
        t.start();
        logger.info("Started local service: " + getHost() + ":" + getPort());
    }

    protected void handleConnection(Socket sock) {
        logger.debug("Got connection");
        try {
            ConnectionHandler handler = new ConnectionHandler(this, sock,
                    new LocalRequestManager());
            handler.start();
        }
        catch (Exception e) {
            logger.warn("Could not start connection handler", e);
        }
    }

    public String waitForRegistration(Task t, String id)
            throws InterruptedException, IOException {
        return waitForRegistration(t, id, DEFAULT_REGISTRATION_TIMEOUT);
    }

    public String waitForRegistration(Task t, String id, long timeout)
            throws InterruptedException, IOException {
        long start = System.currentTimeMillis();
        synchronized (services) {
            while (!services.containsKey(id)) {
                services.wait(1000);
                if (timeout < System.currentTimeMillis() - start) {
                    throw new IOException(
                            "Timed out waiting for registration for " + id);
                }
                if (t.getStatus().isTerminal()) {
                    throw new IOException(
                            "Task ended before registration was received."
                                    + "\nSTDOUT: " + t.getStdOutput()
                                    + "\nSTDERR: " + t.getStdError());
                }
            }
            return (String) services.get(id);
        }
    }

    public void registrationReceived(String id, String url, KarajanChannel channel) {
        synchronized (services) {
            if (services.containsKey(id)) {
                throw new IllegalArgumentException(
                        "Another registration with the same id (" + id
                                + ") already exists");
            }
            else {
                services.put(id, url);
                services.notifyAll();
            }
        }
    }

    public static void main(String[] args) {
        try {
            LocalService ls = new LocalService();
            ls.start();
            System.out.println("Started service: " + ls);
            while (true) {
                Thread.sleep(1000);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
