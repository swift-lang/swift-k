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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.common.CoGProperties;
import org.griphyn.vdl.karajan.monitor.SystemState;

public class HTTPServer implements Runnable {
    public static final Logger logger = Logger.getLogger(HTTPServer.class);

    public static final String WEB_DIR = "httpmonitor";
    
    private ServerSocketChannel channel;
    private ConnectionProcessor connectionProcessor;
    private boolean started;
    private int port;
    private ClassLoader loader = HTTPServer.class.getClassLoader();
    private String password;
    private SystemState state;
    private Map<String, StateDataBuilder> stateKeys;
    
    public HTTPServer(int port, String password, SystemState state) {
        this.port = port;
        this.password = password;
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
        channel = ServerSocketChannel.open();
        channel.socket().bind(new InetSocketAddress(port));
        logger.info("HTTPMonitor server created");
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.setName("HTTPMonitor Server Thread");
        t.start();
        connectionProcessor = new ConnectionProcessor();
        t = new Thread(connectionProcessor);
        t.setDaemon(true);
        t.setName("HTTPMonitor Server Connection Processor");
        t.start();
        synchronized(this) {
            try {
                while (!started) {
                    wait(10);
                }
            }
            catch (InterruptedException e) {
                throw new IOException("Got interrupted while starting");
            }
        }
        System.out.println("HTTPMonitor server started. URL is " + getURL());
    }

    public String getURL() {
        if (channel == null) {
            throw new IllegalThreadStateException(
                    "Server has not been started yet");
        }
        else {
            ServerSocket socket = channel.socket();
            if (CoGProperties.getDefault().getHostName() != null) {
                return "http://" + CoGProperties.getDefault().getHostName()
                    + ":" + socket.getLocalPort();
            }
            else {
            	return "http://localhost:" + socket.getLocalPort();
            }
        }
    }

    public void run() {
        while (true) {
            try {
                synchronized(this) {
                    notifyAll();
                    started = true;
                }
                SocketChannel s = channel.accept();
                s.finishConnect();
                s.configureBlocking(false);
                connectionProcessor.addChannel(s);
            }
            catch (Exception e) {
                logger.info("Caught exception in HTTP monitor service", e);
            }
        }
    }
    
    public boolean exists(String file) {
        if (stateKeys.containsKey(file)) {
            return true;
        }
        URL url = loader.getResource(WEB_DIR + file);
        return url != null;
    }

    private class ConnectionProcessor implements Runnable {
        private Map<SocketChannel,ConnectionState> channels;
        private Selector selector;
        private List<SocketChannel> newChannels;

        public ConnectionProcessor() throws IOException {
            channels = new HashMap<SocketChannel,ConnectionState>();
            newChannels = new LinkedList<SocketChannel>();
            selector = SelectorProvider.provider().openSelector();
        }

        public void addChannel(SocketChannel s) throws ClosedChannelException {
            synchronized (channels) {
                channels.put(s, new ConnectionState(this, s.socket()));
                newChannels.add(s);
                selector.wakeup();
            }
        }

        public void removeChannel(SocketChannel s) {
            synchronized (channels) {
                channels.remove(s);
                selector.wakeup();
            }
        }

        public void run() {
            List<SelectionKey> keys = new ArrayList<SelectionKey>();
            while (true) {
                try {
                    keys.clear();
                    int n = selector.select();
                    Set<SelectionKey> skeys;
                    synchronized (channels) {
                        skeys = selector.selectedKeys();
                        keys.addAll(skeys);
                    }
                    
                    for (SelectionKey key : keys) { 
                        if (key.isValid()) {
                            ConnectionState s = channels.get(key.channel());
                            boolean ok = false;
                            if (s != null) {
                                try {
                                    s.process(key);
                                    ok = true;
                                }
                                catch (Exception e) {
                                }
                            }
                            if (!ok) {
                                channels.remove(key.channel());
                                key.cancel();
                            }
                        }
                    }
                    skeys.clear();

                    synchronized(channels) {
                        if (!newChannels.isEmpty()) {
                            for (Iterator<SocketChannel> i = newChannels.iterator();
                                 i.hasNext(); ) {
                                SocketChannel s = i.next();
                                s.register(selector, SelectionKey.OP_READ);
                                i.remove();
                            }
                        }
                    }
                    if (n == 0) {
                        Thread.sleep(100);
                    }
                }
                catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
    }

    private class ConnectionState {
        public static final int IDLE = 0;
        public static final int SENDING_REPLY = 1;
        public static final int SENDING_ERROR = 2;
        public static final int SENDING_DATA = 4;

        public static final String ERROR_NOT_AUTHORIZED = "<html><head><title>Error</title></head><body>"
                + "<h1>Error: Your are not authorized to access this resource</h1></body></html>\n";
        public static final String ERROR_NOTFOUND = "<html><head><title>Error</title></head><body>"
                + "<h1>Error: The requested resource is not available</h1></body></html>\n";
        public static final String ERROR_BAD_REQUEST = "<html><head><title>Error</title></head><body>"
                + "<h1>Error: The request could not be understood by this server</h1></body></html>\n";
        public static final String ERROR_INTERNAL = "<html><head><title>Error</title></head><body>"
                + "<h1>Error: Internal server error</h1><h2>@1</h2><pre>@2</pre></body></html>\n";

        private SocketChannel channel;
        private int state;
        private ByteBuffer rbuf, rcb, bdata, sdata;
        private ReadableByteChannel fileChannel;
        private String cmd;
        private Map<String,String> headers;
        private Iterator<ByteBuffer> replies;
        private long sendPos, total;
        private int lastRead;
        private ConnectionProcessor processor;

        public ConnectionState(ConnectionProcessor processor, Socket socket) {
            this.channel = socket.getChannel();
            this.processor = processor;
            rbuf = ByteBuffer.allocate(8192);
            bdata = ByteBuffer.allocate(8192);
            rcb = rbuf.asReadOnlyBuffer();
            headers = new HashMap<String,String>();
            state = IDLE;
            lastRead = 0;
            cmd = null;
        }

        public void process(SelectionKey key) throws IOException {
            if (state == IDLE && key.isReadable()) {
                try {
                    channel.read(rbuf);
                }
                catch (BufferOverflowException e) {
                    logger.warn("Invalid request received", e);
                }
                headers.clear();
                while (rcb.position() < rbuf.position()) {
                    int c = rcb.get();
                    if (c == 13) {
                        int pos = rcb.position();
                        rcb.position(lastRead);
                        byte bytes[] = new byte[pos - lastRead];
                        rcb.get(bytes);
                        rcb.position(pos);
                        lastRead = pos;
                        String line = new String(bytes).trim();
                        if (line.equals("")) {
                            processCommand(cmd, headers, key);
                            rbuf.clear();
                            rcb.clear();
                        }
                        else if (cmd == null) {
                            cmd = line;
                        }
                        else {
                            int ix = line.indexOf(":");
                            if (ix == -1) {
                                sendError(line, "400 Bad request", null);
                                break;
                            }
                            else {
                                headers.put(line.substring(0, ix)
                                        .toLowerCase(), line
                                        .substring(ix + 1).trim());
                            }
                        }
                    }
                }
            }
            else if ((state == SENDING_REPLY || state == SENDING_ERROR)
                    && key.isWritable()) {
                if (replies != null && replies.hasNext()) {
                    ByteBuffer r = replies.next();
                    channel.write(r);
                }
                else {
                    if (state == SENDING_ERROR) {
                        close();
                    }
                    else {
                        state = SENDING_DATA;
                    }
                    replies = null;
                }
            }
            else if (state == SENDING_DATA && key.isWritable()) {
                if (fileChannel == null) {
                    sdata.rewind();
                    sdata.limit(sdata.capacity());
                    channel.write(sdata);
                    close();
                }
                else {
                    int tr = fileChannel.read(bdata);
                    bdata.rewind();
                    bdata.limit(tr);
                    int wr = channel.write(bdata);
                    bdata.rewind();
                    sendPos += tr;
                    if (sendPos >= total) {
                        close();
                    }
                }
            }
        }

        private void processCommand(String cmd, Map<String,String> headers, SelectionKey key) {
            logger.info("[" + channel.socket().getRemoteSocketAddress() + "] "
                    + cmd);
            if (logger.isDebugEnabled()) {
                logger.debug("Headers: " + headers);
            }
            String[] tokens = cmd.split("\\s+");
            if (tokens[0].equals("GET")) {
                String page = getPage(tokens[1]);
                Map<String,String> cgiParams = getCGIParams(tokens[1]);
                if (page.equals("/")) {
                    page = "/index.html";
                }
                if (exists(page)) {
                    createFileBuffer(page, cgiParams);
                }
                else {
                    sendError(cmd, "404 Not Found", ERROR_NOTFOUND);
                }
                key.interestOps(SelectionKey.OP_WRITE);
            }
            else {
                sendError(cmd, "400 Bad Request", ERROR_BAD_REQUEST);
            }
        }

        private void sendError(String request, String error, String html) {
            state = SENDING_ERROR;
            List<ByteBuffer> l = new LinkedList<ByteBuffer>();
            addReply(l, "HTTP/1.1 " + error + "\n");
            addReply(l, "Date: " + new Date() + "\n");
            if (html != null) {
                addReply(l, "Content-Length: " + html.length() + "\n");
            }
            addReply(l, "Connection: close\n");
            addReply(l, "Content-Type: text/html;\n");
            addReply(l, "\n");
            if (html != null) {
                addReply(l, html);
            }
            replies = l.iterator();
            logger.info(request + " - ERROR: " + error);
        }

        private void sendHeader(long len, String contentType) {
            state = SENDING_REPLY;
            List<ByteBuffer> l = new LinkedList<ByteBuffer>();
            addReply(l, "HTTP/1.1 200 OK\n");
            addReply(l, "Date: " + new Date() + "\n");
            addReply(l, "Content-Length: " + len + "\n");
            addReply(l, "Connection: close\n");
            addReply(l, "Content-Type: " + contentType + ";\n");
            addReply(l, "Cache-Control: no-cache, no-store, must-revalidate\n");
            addReply(l, "Pragma: no-cache\n");
            addReply(l, "Expires: 0\n");
            addReply(l, "\n");
            replies = l.iterator();
        }

        private void addReply(List<ByteBuffer> l, String reply) {
            l.add(ByteBuffer.wrap(reply.getBytes()));
        }

        private void createFileBuffer(String file, Map<String, String> params) {
            if (stateKeys.containsKey(file)) {
                try {
                    sdata = stateKeys.get(file).getData(params);
                }
                catch (Exception e) {
                    sendError("GET " + file, "500 Internal Server Error", 
                        processTemplate(ERROR_INTERNAL, e.toString(), getStackTrace(e)));
                    e.printStackTrace();
                    return;
                }
                fileChannel = null;
                sendHeader(sdata.capacity(), "text/ascii");
            }
            else {
                URL url = loader.getResource(WEB_DIR + file);
                if (url == null) {
                    sendError("GET " + file, "404 Not Found", ERROR_NOTFOUND);
                }
                else {
                    try {
                        URLConnection conn = url.openConnection();
                        total = conn.getContentLength();
                        fileChannel = Channels.newChannel(conn.getInputStream());
                        sendHeader(conn.getContentLength(), getContentType(file, conn.getContentType()));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        sendError("GET " + file, "500 Internal Server Error", 
                            processTemplate(ERROR_INTERNAL, e.toString(), getStackTrace(e)));
                    }
                }
            }
        }

        private String getContentType(String file, String dct) {
            if (file.endsWith(".js")) {
                return "text/javascript";
            }
            if (file.endsWith(".css")) {
                return "text/css";
            }
            else {
                return dct;
            }
        }

        private void close() throws IOException {
            channel.close();
            processor.removeChannel(channel);
        }
        
        private String getPage(String local) {
            int i = local.indexOf('?');
            if (i == -1) {
                return local;
            }
            else {
                return local.substring(0, i);
            }
        }
        
        private Map<String,String> getCGIParams(String local) {
            int i = local.indexOf('?');
            if (i == -1) {
                return Collections.emptyMap();
            }
            else {
                Map<String,String> m = new HashMap<String,String>();
                String[] params = local.substring(i + 1).split("&");
                for (int j = 0; j < params.length; j++) {
                    int k = params[j].indexOf('=');
                    if (k == -1) {
                        //not valid, discard parameter
                    }
                    else {
                        m.put(params[j].substring(0, k), 
                              urlDecode(params[j].substring(k + 1)));
                    }
                }
                return m;
            }
        }
    }

    public String getStackTrace(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        e.printStackTrace(pw);
        return baos.toString();
    }

    public String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "ASCII");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    }

    public String processTemplate(String s, String... ps) {
        for (int i = 0; i < ps.length; i++) {
            s = s.replace("@" + (i + 1), ps[i]);
        }
        return s;
    }
}
