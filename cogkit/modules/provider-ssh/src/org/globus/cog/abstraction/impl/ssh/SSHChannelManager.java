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
 * Created on Nov 19, 2007
 */
package org.globus.cog.abstraction.impl.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.PasswordAuthentication;
import org.globus.cog.abstraction.impl.common.PublicKeyAuthentication;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;

public class SSHChannelManager {
    public static final Logger logger = Logger
        .getLogger(SSHChannelManager.class);

    public static final long REAP_INTERVAL = 10 * 1000;

    private static SSHChannelManager defaultManager;

    static {
        defaultManager = new SSHChannelManager();
    }

    public static SSHChannelManager getDefault() {
        return defaultManager;
    }

    private Map bundles;
    private Reaper reaper;

    public SSHChannelManager() {
        bundles = new HashMap();
        reaper = new Reaper();
        reaper.start();
    }

    public SSHChannel getChannel(String host, int port, Object credentials)
            throws InvalidSecurityContextException, IllegalSpecException,
            InvalidServiceContactException, TaskSubmissionException {
        Object pcred = credentials;
        if (port == -1) {
            port = 22;
        }
        try {
            ConnectionID i = new ConnectionID(host, port, getCredentials(
                credentials, host));
            SSHConnectionBundle bundle = null;
            synchronized (bundles) {
                bundle = (SSHConnectionBundle) bundles.get(i);
                if (bundle == null) {
                    bundle = new SSHConnectionBundle(i);
                    bundles.put(i, bundle);
                }
            }
            return bundle.allocateChannel();
        }
        catch (InvalidSecurityContextException e) {
            if (pcred == null) {
                invalidateCredentials(credentials, host);
            }
            throw e;
        }
    }

    private static final char[] NO_PASSPHRASE = new char[0];

    public static void invalidateCredentials(Object credentials, String host) {
        Object def = getDefaultCredentials(host);
        if (def instanceof InteractiveAuthentication) {
            ((InteractiveAuthentication) def).reset();
        }
    }

    public static Object getCredentials(Object credentials, String host)
            throws InvalidSecurityContextException {
        if (credentials == null) {
            credentials = getDefaultCredentials(host);
        }
        if (credentials instanceof InteractiveAuthentication) {
            return credentials;
        }
        else if (credentials instanceof PasswordAuthentication) {
            return credentials;
        }
        else if (credentials instanceof PublicKeyAuthentication) {
            return credentials;
        }
        else if (credentials == null) {
            return null;
            /*return new InteractiveAuthentication(System
                .getProperty("user.name"), null, null, null);*/
        }
        else {
            throw new InvalidSecurityContextException(
                "Unsupported credentials: " + credentials);
        }
    }

    public void releaseChannel(SSHChannel s) {
        s.getBundle().releaseChannel(s);
    }

    private static Map credentials;
    private static long lastLoad;
    public static final String CREDENTIALS_FILE = "auth.defaults";

    static {
        credentials = new HashMap();
    }

    private static String getAuthFilePath() {
        return System.getProperty("user.home") + File.separator + ".ssh"
                + File.separator + CREDENTIALS_FILE;
    }

    public static Object getDefaultCredentials(String host) {
        File f = new File(getAuthFilePath());
        if (f.exists()) {
            if (lastLoad < f.lastModified()) {
                try {
                    loadDefaultCredentials(f);
                }
                catch (IOException e) {
                    logger.warn("Failed to load default credentials file", e);
                }
            }
            synchronized (credentials) {
                return credentials.get(host);
            }
        }
        else {
            return null;
        }
    }

    public static void loadDefaultCredentials(File f) throws IOException {
        synchronized (credentials) {
            credentials.clear();
            Properties p = new Properties();
            p.load(new FileInputStream(f));
            Iterator i = p.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                String key = (String) e.getKey();
                String val = (String) e.getValue();
                if (key.endsWith(".type")) {
                    String host = key.substring(0, key.length() - 5);
                    String username = p.getProperty(host + ".username");
                    if (username == null) {
                        username = System.getProperty("user.name");
                    }
                    Object auth = null;
                    if ("password".equals(val)) {
                        String password = p.getProperty(host + ".password");
                        auth = new PasswordAuthentication(username,
                            password == null ? null : password.toCharArray());
                    }
                    else if ("key".equals(val)) {
                        String pkey = p.getProperty(host + ".key");
                        String passphrase = p.getProperty(host + ".passphrase");
                        auth = new PublicKeyAuthentication(username, pkey,
                            toCharArray(passphrase));
                    }
                    else if ("interactive".equals(val)) {
                        auth = new InteractiveAuthentication(p.getProperty(host
                                + ".username"), toCharArray(p.getProperty(host
                                + ".password")), p.getProperty(host + ".key"),
                            toCharArray(p.getProperty(host + ".passphrase")));
                    }
                    else {
                        logger.warn("Unknown authentication type for \"" + host
                                + "\": " + val);
                    }
                    credentials.put(host, auth);
                }
            }
            lastLoad = System.currentTimeMillis();
        }
    }

    private static char[] toCharArray(String v) {
        if (v == null) {
            return null;
        }
        else {
            return v.toCharArray();
        }
    }

    private class Reaper extends Thread {
        public Reaper() {
            super("SSH Channel Reaper");
            setDaemon(true);
        }

        public void run() {
            try {
                List shutdownList = new ArrayList();
                while (true) {
                    Thread.sleep(REAP_INTERVAL);
                    synchronized (bundles) {
                        Iterator i = bundles.entrySet().iterator();
                        while (i.hasNext()) {
                            Map.Entry e = (Entry) i.next();
                            ConnectionID ix = (ConnectionID) e.getKey();
                            SSHConnectionBundle bundle = (SSHConnectionBundle) e
                                .getValue();
                            if (!bundle.shutdownIdleConnections(shutdownList)) {
                                i.remove();
                            }
                        }
                    }
                    Iterator i = shutdownList.iterator();
                    while (i.hasNext()) {
                        try {
                            ((Runnable) i.next()).run();
                        }
                        catch (Exception e) {
                            logger
                                .warn("Failed to shut down SSH connection", e);
                        }
                    }
                    shutdownList.clear();
                }
            }
            catch (InterruptedException e) {

            }
        }
    }
}
