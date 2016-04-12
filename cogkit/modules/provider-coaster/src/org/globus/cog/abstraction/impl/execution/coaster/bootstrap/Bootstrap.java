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
 * Created on Jan 18, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster.bootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class Bootstrap {

    public static final boolean fork = true;
    private static final File CACHE_DIR = new File(System
            .getProperty("user.home")
            + File.separator
            + ".globus"
            + File.separator
            + "coasters"
            + File.separator + "cache");

    public static final File LOG_DIR = new File(System
            .getProperty("user.home")
            + File.separator + ".globus" + File.separator + "coasters");
    
    public static final String BOOTSTRAP_LIST = "coaster-bootstrap.list";

    public static final String SERVICE_CLASS = "org.globus.cog.abstraction.coaster.service.CoasterService";
    
    private static Logger logger;

    private String serviceURL;
    private String registrationURL;
    private String serviceId;
    private List<String[]> list;

    public Bootstrap(String serviceURL,
            String registrationURL, String serviceId) {
        this.serviceURL = serviceURL;
        this.registrationURL = registrationURL;
        this.serviceId = serviceId;
        list = new ArrayList<String[]>();
        logger = new Logger(serviceId);
    }

    public void run() throws Exception {
        processList();
        updateJars();
        start();
    }

    private void processList() throws Exception {
        logger.log("Processing file list");
        StringBuffer line = new StringBuffer();
        URL url = Bootstrap.class.getClassLoader().getResource(BOOTSTRAP_LIST);
        if (url == null) {
            error("Bootstrap list missing from bootstrap jar.");
        }
        InputStream is = url.openStream();
        int c = is.read();
        while (c != -1) {
            if (c == '\n') {
                processLine(line.toString());
                line = new StringBuffer();
            }
            else {
                line.append((char) c);
            }
            c = is.read();
        }
    }

    private void processLine(String line) {
        String[] l = line.split("\\s+");
        if (l.length != 2) {
            throw new RuntimeException("Invalid line in package list: "
                    + line);
        }
        list.add(l);
    }

    private void updateJars() throws Exception {
        logger.log("Updating jars");
        if (!CACHE_DIR.mkdirs() && !CACHE_DIR.exists()) {
            error("Could not create jar cache directory");
        }
        Iterator<String[]> i = list.iterator();
        while (i.hasNext()) {
            String[] jar = i.next();
            File f = new File(CACHE_DIR, buildName(jar));
            if (!f.exists()) {
                download(CACHE_DIR, jar[0], jar[1]);
            }
        }
    }

    private void download(File dir, String name, String checksum) throws Exception {
        try {
            logger.log("Downloading " + name);
            File dest = File.createTempFile("download-", ".jar", dir);
            URL url = new URL(serviceURL + "/" + name + "?serviceId="
                    + serviceId);
            InputStream is = url.openStream();
            FileOutputStream fos = new FileOutputStream(dest);
            byte[] buf = new byte[16384];
            int len = is.read(buf);
            while (len != -1) {
                fos.write(buf, 0, len);
                len = is.read(buf);
            }
            fos.close();
            is.close();
            String actualChecksum = Digester.computeMD5(dest);
            if (!actualChecksum.equals(checksum)) {
                throw new RuntimeException("Checksum (" + actualChecksum + ") does not match. Expected " + checksum);
            }
            dest.renameTo(new File(dir, buildName(name, checksum)));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to download " + name + ": "
                    + e.toString(), e);
        }
    }

    private void start() throws Exception {
        if (fork) {
            forkStart();
        }
        else {
            clStart();
        }
    }

    private void arrangeJars() {
        String[] coasterJar = null;
        Iterator<String[]> i = list.iterator();
        while (i.hasNext()) {
            coasterJar = i.next();
            if (coasterJar[0].indexOf("provider-coaster") != -1) {
                i.remove();
                break;
            }
        }
        if (coasterJar != null) {
            logger.log("Moved coaster jar to head of classpath");
            list.add(0, coasterJar);
        }
    }

    private void forkStart() throws Exception {
        logger.log("Forking service");
        StringBuffer sb = new StringBuffer();
        arrangeJars();
        Iterator<String[]> i = list.iterator();
        while (i.hasNext()) {
            sb.append(CACHE_DIR.getAbsolutePath());
            sb.append('/');
            sb.append(buildName(i.next()));
            if (i.hasNext()) {
                sb.append(':');
            }
        }
        String java = System.getProperty("java");
        List<String> args = new ArrayList<String>();
        args.add("nice");
        args.add("-n");
        args.add("2");
        args.add(java);
        addDebuggingOptions(args);
        String arch = System.getProperty("os.arch");
        if (arch != null && arch.indexOf("64") != -1) {
            // give the JVM more memory on 64 bit systems
            args.add("-Xmx512M");
        }
        else {
            args.add("-Xmx256M");
        }
        args.add("-Dtcp.channel.log.io.performance=true");
        //pass user home override to service
        args.add("-Duser.home=" + System.getProperty("user.home"));
        //args.add("-agentlib:hprof=file=c.hprof");
        addProperties(args);
        args.add("-cp");
        args.add(sb.toString());
        args.add(SERVICE_CLASS);
        args.add(registrationURL);
        args.add(serviceId);
        logger.log("Args: " + args);
        Process p = Runtime.getRuntime().exec(args.toArray(new String[0]));
        StringBuffer out = new StringBuffer(), err = new StringBuffer();
        logger.log("Starting stdout consumer");
        consumeOutput(p.getInputStream(), out);
        logger.log("Starting stderr consumer");
        consumeOutput(p.getErrorStream(), err);
        int ec;
        while (true) {
            try {
                ec = p.exitValue();
                break;
            }
            catch (IllegalThreadStateException e) {
                Thread.sleep(250);
            }
        }
        logger.log("Exit code: " + ec);
        logger.log("Stdout from process: " + out.toString());
        logger.log("Stderr from process: " + err.toString());
        System.out.println(out.toString());
        System.err.println(err.toString());
        if (ec != 0) {
            System.exit(ec);
        }
    }

    private void addDebuggingOptions(List<String> args) {
        //args.add("-Xdebug");
        //args.add("-Xrunjdwp:transport=dt_socket,address=8788,server=y,suspend=y");
        //args.add("-Xrunjdwp:transport=dt_socket,address=8788,server=y,suspend=n");
    }

    private void addProperties(List<String> args) {
        addProperty(args, "X509_USER_PROXY");
        addProperty(args, "GLOBUS_HOSTNAME");
        addProperty(args, "GLOBUS_TCP_PORT_RANGE");
        addProperty(args, "X509_CERT_DIR");
        args.add("-Djava.security.egd=file:///dev/urandom");
    }

    private void addProperty(List<String> args, String name) {
        String value = System.getProperty(name);
        if (value != null && !value.equals("")) {
            args.add("-D" + name + "=" + value);
        }
    }

    private void consumeOutput(final InputStream is, final StringBuffer sb) {
        new Thread() {
            public void run() {
                try {
                    int c = is.read();
                    while (c != -1) {
                        sb.append((char) c);
                        c = is.read();
                    }
                }
                catch (IOException e) {
                    sb.append("Exception caught while reading output: "
                            + e.getMessage());
                }
            }
        }.start();
    }

    private void clStart() throws Exception {
        URL[] urls = new URL[list.size()];
        for (int i = 0; i < list.size(); i++) {
            urls[i] = new URL("file://" + CACHE_DIR.getAbsolutePath() + "/"
                    + buildName(list.get(i)));
            System.err.println(urls[i]);
        }
        ClassLoader cl = new URLClassLoader(urls, Bootstrap.class
                .getClassLoader());
        Class<?> cls = cl.loadClass(SERVICE_CLASS);

        Method m = cls.getMethod("main", new Class<?>[] { String[].class });
        m.invoke(null, new Object[] { new String[] { registrationURL,
                serviceId } });
    }

    private String buildName(String[] n) {
        return buildName(n[0], n[1]);
    }

    private String buildName(String name, String checksum) {
        if (!name.endsWith(".jar")) {
            throw new RuntimeException("Unrecognized file type: " + name);
        }
        return name.substring(0, name.length() - 4) + "-" + checksum + ".jar";
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            error("Wrong number of arguments. Expected <serviceURL>, <registration service URL>, and <id>");
        }
        try {
            Bootstrap b = new Bootstrap(args[0], args[1], args[2]);
            b.run();
        }
        catch (Exception e) {
            error(e.getMessage());
        }
    }

    private static void error(String message) {
        if (logger != null) {
            logger.log(message);
        }
        //one for the log, one for reporting back to the client
        //if middleware permits
        System.out.println(message);
        System.err.println(message);
        System.exit(1);
    }

    private static class Logger {
        private PrintStream ps;
        private String id;
        private static final DateFormat DF = 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        private static final DateFormat LDF = 
            new SimpleDateFormat("yyyy-MM-dd");

        public Logger(String id) {
            this.id = " " + id + " ";
            if (!LOG_DIR.mkdirs() && !LOG_DIR.exists()) {
                error("Cannot create coaster directory (" + LOG_DIR + ")");
            }
            try {
                ps = new PrintStream(new FileOutputStream(LOG_DIR.getAbsolutePath()
                        + File.separator + "coasters-" + LDF.format(new Date()) + ".log", true));
            }
            catch (IOException e) {
                error("Cannot create coaster log file: " + e.getMessage());
            }
        }

        public void log(String message) {
            header();
            ps.println(message);
            ps.flush();
        }

        public void log(String message, Throwable t) {
            header();
            ps.println(message);
            t.printStackTrace(ps);
            ps.flush();
        }

        private void header() {
            ps.print(DF.format(new Date()));
            ps.print(id);
        }
    }
}
