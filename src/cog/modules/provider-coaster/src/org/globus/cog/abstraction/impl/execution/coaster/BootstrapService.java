//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 17, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.execution.coaster.bootstrap.Bootstrap;
import org.globus.cog.abstraction.impl.execution.coaster.bootstrap.Digester;
import org.globus.common.CoGProperties;

public class BootstrapService implements Runnable {
    public static final Logger logger = Logger
            .getLogger(BootstrapService.class);

    private ServerSocketChannel channel;
    private ConnectionProcessor connectionProcessor;
    private String webDir;
    private Set<String> valid;
    private Map<String,String> checksums;
    private boolean started;

    public BootstrapService() {
        initWebDir();
        initList();
    }

    private void initWebDir() {
        webDir = System.getProperty("coaster.bootstrap.service.web.dir");
        if (webDir == null) {
            webDir = System.getProperty("COG_INSTALL_PATH");
            if (webDir == null) {
                throw new IllegalArgumentException(
                        "None of 'coaster.bootstrap.service.web.dir' and 'COG_INSTALL_PATH' are set");
            }
            webDir = webDir + File.separator + "lib";
        }
    }

    private void initList() {
        valid = new HashSet<String>();
        valid.add("/" + ServiceManager.BOOTSTRAP_JAR);
        valid.add("/" + Bootstrap.BOOTSTRAP_LIST);
        valid.add("/index.html");
        checksums = new HashMap<String,String>();
        loadList();
    }

    private void loadList() {
        URL url = BootstrapService.class.getClassLoader().getResource(
                Bootstrap.BOOTSTRAP_LIST);
        if (url == null) {
            throw new RuntimeException(Bootstrap.BOOTSTRAP_LIST
                    + " not found in classpath");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(url
                    .openStream()));
            String line = br.readLine();
            while (line != null) {
                String[] d = line.split("\\s+");
                if (d.length != 2) {
                    throw new RuntimeException(
                            "Invalid line in package list: " + line);
                }
                if (!exists(d[0])) {
                    throw new RuntimeException(
                            "Could not find a file in the bootstrap list: "
                                    + d[0]);
                }
                valid.add("/" + d[0]);
                checksums.put("/" + d[0], d[1]);
                line = br.readLine();
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not processs package list: "
                    + e.getMessage(), e);
        }
    }
    
    private boolean exists(String name) {
        return getFile(name).exists();
    }

    protected synchronized String getMD5(File f) {
        String name = f.getName();
        String md5 = (String) checksums.get(name);
        if (md5 == null) {
            try {
                md5 = Digester.computeMD5(f);
            }
            catch (Exception e) {
                logger.warn("Could not compute checksum of " + f, e);
                return null;
            }
            checksums.put(name, md5);
        }
        return md5;
    }

    public void start() throws IOException {
        channel = PortManager.getDefault().openServerSocketChannel();
        logger.info("Socket bound. URL is " + getURL());
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.setName("Coaster Bootstrap Service Thread");
        t.start();
        connectionProcessor = new ConnectionProcessor();
        t = new Thread(connectionProcessor);
        t.setDaemon(true);
        t.setName("Coaster Bootstrap Service Connection Processor");
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
                logger.info("Caught exception in coaster bootstrap service",
                        e);
            }
        }
    }

    public File getFile(String name) {
        return new File(webDir + File.separator + name);
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
                            s.process(key);
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

        private SocketChannel channel;
        private int state;
        private ByteBuffer rbuf, rcb;
        private FileChannel fileChannel;
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
                                sendError("400 Bad request", null);
                                System.err.println("bad request");
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
                    ByteBuffer r = (ByteBuffer) replies.next();
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
                long tr = fileChannel.transferTo(sendPos, total - sendPos,
                        channel);
                sendPos += tr;
                if (sendPos >= total) {
                    close();
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
                String coasterId = (String) cgiParams.get("serviceId");
                if (coasterId != null) {
                    ServiceManager.getDefault().serviceIsActive(coasterId);
                }
                if (page.equals("/")) {
                    page = "/index.html";
                }
                if (page.equals("/list")) {
                    page = "/" + Bootstrap.BOOTSTRAP_LIST;
                }
                if (valid.contains(page)) {
                    createFileBuffer(page);
                }
                else {
                    sendError("401 Unauthorized", ERROR_NOT_AUTHORIZED);
                }
                key.interestOps(SelectionKey.OP_WRITE);
            }
            else {
                sendError("400 Bad Request", ERROR_BAD_REQUEST);
            }
        }

        private void sendError(String error, String html) {
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
        }

        private void sendHeader(File f) {
            state = SENDING_REPLY;
            List<ByteBuffer> l = new LinkedList<ByteBuffer>();
            addReply(l, "HTTP/1.1 200 OK\n");
            addReply(l, "Date: " + new Date() + "\n");
            addReply(l, "Content-Length: " + f.length() + "\n");
            addReply(l, "Content-MD5: " + getMD5(f) + "\n");
            addReply(l, "Connection: close\n");
            if (f.getName().endsWith(".jar")) {
                addReply(l, "Content-Type: application/octet-stream;\n");
            }
            else {
                addReply(l, "Content-Type: text/html;\n");
            }
            addReply(l, "\n");
            replies = l.iterator();
        }

        private void addReply(List<ByteBuffer> l, String reply) {
            l.add(ByteBuffer.wrap(reply.getBytes()));
        }

        private void createFileBuffer(String file) {
            File f = getFile(file);
            total = f.length();
            try {
                fileChannel = new FileInputStream(f).getChannel();
                sendHeader(f);
            }
            catch (FileNotFoundException e) {
                sendError("404 Not Found", ERROR_NOTFOUND);
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
                              params[j].substring(k + 1));
                    }
                }
                return m;
            }
        }
    }

    public static void main(String[] args) {
        try {
            BootstrapService bs = new BootstrapService();
            bs.start();
            System.err.println(bs.getURL());
            Thread.sleep(200000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
