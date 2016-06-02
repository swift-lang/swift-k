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
 * Created on Jan 30, 2015
 */
package org.globus.cog.util.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

public abstract class AbstractHTTPServer implements Runnable {
    public static final Logger logger = Logger.getLogger(AbstractHTTPServer.class);
    
    public static final String ERROR_NOT_AUTHORIZED = "<html><head><title>Error</title></head><body>"
            + "<h1>Error: Your are not authorized to access this resource</h1></body></html>\n";
    public static final String ERROR_NOTFOUND = "<html><head><title>Error</title></head><body>"
            + "<h1>Error: The requested resource is not available</h1></body></html>\n";
    public static final String ERROR_BAD_REQUEST = "<html><head><title>Error</title></head><body>"
            + "<h1>Error: The request could not be understood by this server</h1></body></html>\n";
    public static final String ERROR_INTERNAL = "<html><head><title>Error</title></head><body>"
            + "<h1>Error: Internal server error</h1><h2>@1</h2><pre>@2</pre></body></html>\n";


    private String name;
    protected ServerSocketChannel channel;
    private ConnectionProcessor connectionProcessor;
    private boolean started;
    private final int port;
    private ClassLoader loader = AbstractHTTPServer.class.getClassLoader();
    private final String password;
    
    public AbstractHTTPServer(String name, int port, String password) {
        this.name = name;
        this.port = port;
        this.password = password;
    }
    
    public void start() throws IOException {
        channel = openChannel();
        bind(channel.socket(), port);
        logger.info("HTTP server created");
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.setName(name + " Service Thread");
        t.start();
        connectionProcessor = new ConnectionProcessor(this);
        t = new Thread(connectionProcessor);
        t.setDaemon(true);
        t.setName(name + " Connection Processor");
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
    
    protected ServerSocketChannel openChannel() throws IOException {
        return ServerSocketChannel.open();
    }

    protected void bind(ServerSocket socket, int port) throws IOException {
        if (port == 0) {
            socket.bind(null);
        }
        else {
            socket.bind(new InetSocketAddress(port));
        }
    }

    public String getURL() {
        if (channel == null) {
            throw new IllegalThreadStateException(
                    "Server has not been started yet");
        }
        else {
            ServerSocket socket = channel.socket();
            return "http://localhost:" + socket.getLocalPort();
        }
    }

    @Override
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
                logger.info("Caught exception in " + name + " service", e);
            }
        }
    }
    
    protected abstract boolean exists(String url);
    
    protected abstract DataLink getDataLink(String url, Map<String, String> params) throws IOException;
    
    protected String translateURL(String url) {
        if (url.equals("/")) {
            return "/index.html";
        }
        else {
            return url;
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
    
    protected String getAllowedOrigin() {
        return null;
    }

    public String processTemplate(String s, String... ps) {
        for (int i = 0; i < ps.length; i++) {
            s = s.replace("@" + (i + 1), ps[i]);
        }
        return s;
    }
    
    protected String getContentType(String file, String dct) {
        if (file.endsWith(".js")) {
            return "text/javascript";
        }
        if (file.endsWith(".html")) {
            return "text/html";
        }
        if (file.endsWith(".css")) {
            return "text/css";
        }
        else {
            return dct;
        }
    }

    
    protected class ConnectionProcessor implements Runnable {
        private Map<SocketChannel, ConnectionState> channels;
        private Selector selector;
        private List<SocketChannel> newChannels;
        private AbstractHTTPServer server;

        public ConnectionProcessor(AbstractHTTPServer server) throws IOException {
            this.server = server;
            channels = new HashMap<SocketChannel, ConnectionState>();
            newChannels = new LinkedList<SocketChannel>();
            selector = SelectorProvider.provider().openSelector();
        }

        public void addChannel(SocketChannel s) throws ClosedChannelException {
            synchronized (channels) {
                channels.put(s, new ConnectionState(this, s.socket(), server));
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
                                    ok = s.process(key);
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
    
    protected static class DataLink {
        public static final int TYPE_BYTE_BUFFER = 0;
        public static final int TYPE_URL = 1;
        public static final int TYPE_FILE = 2;
        
        private final int type;
        private final ByteBuffer buf;
        private final URL url;
        private final File file;
        private final String contentType;
        
        public DataLink(ByteBuffer buf) {
            this(buf, "text/plain");
        }
        
        public DataLink(ByteBuffer buf, String contentType) {
            this.type = TYPE_BYTE_BUFFER;
            this.buf = buf;
            this.url = null;
            this.file = null;
            this.contentType = contentType;
        }
        
        public DataLink(URL url) {
            this.type = TYPE_URL;
            this.url = url;
            this.buf = null;
            this.file = null;
            this.contentType = null;
        }
        
        public DataLink(File file) {
            this(file, null);
        }
                
        public DataLink(File file, String contentType) {
            this.type = TYPE_FILE;
            this.file = file;
            this.url = null;
            this.buf = null;
            this.contentType = contentType;
        }
        
        public int getType() {
            return type;
        }
        
        public boolean isByteBuffer() {
            return type == TYPE_BYTE_BUFFER;
        }
        
        public boolean isURL() {
            return type == TYPE_URL;
        }
        
        public boolean isFile() {
            return type == TYPE_FILE;
        }
        
        public URL getURL() {
            if (!isURL()) {
                throw new IllegalStateException("Not a URL");
            }
            return url;
        }
        
        public ByteBuffer getByteBuffer() {
            if (!isByteBuffer()) {
                throw new IllegalStateException("Not a byte buffer");
            }
            return buf;
        }
        
        public File getFile() {
            if (!isFile()) {
                throw new IllegalStateException("Not a file");
            }
            return file;
        }

        public String getContentType() {
            return contentType;
        }
    }

    protected class ConnectionState {
        public static final int IDLE = 0;
        public static final int SENDING_REPLY = 1;
        public static final int SENDING_ERROR = 2;
        public static final int SENDING_DATA = 4;

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
        private AbstractHTTPServer server;

        public ConnectionState(ConnectionProcessor processor, Socket socket, AbstractHTTPServer server) {
            this.channel = socket.getChannel();
            this.processor = processor;
            this.server = server;
            rbuf = ByteBuffer.allocate(8192);
            bdata = ByteBuffer.allocate(8192);
            rcb = rbuf.asReadOnlyBuffer();
            headers = new HashMap<String,String>();
            state = IDLE;
            lastRead = 0;
            cmd = null;
        }

        public boolean process(SelectionKey key) throws IOException {
            if (state == IDLE && key.isReadable()) {
                try {
                    int r = channel.read(rbuf);
                    if (r == 0) {
                        return false;
                    }
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
                        initialize(bdata);
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
                	if (shouldRead(bdata)) {
                		bdata.rewind();
                	    int tr = fileChannel.read(bdata);
                	    switch(tr) {
                	    	case -1:
                	    		fileChannel.close();
                	    		close();
                	    		// avoide write
                	    		initialize(bdata);
                	    		break;
                	    	case 0:
                	    		// reset pointer to mark as empty
                	    		initialize(bdata);
                	    		break;
                	    	default:
                	    		setWriteable(bdata);
                	    		break;
                	    }
                	}
                	if (shouldWrite(bdata)) {
                		int wr = channel.write(bdata);
                		if (allWritten(bdata)) {
                			initialize(bdata);
                		}
                		sendPos += wr;
                		if (sendPos >= total) {
                            close();
                        }
                	}
                }
            }
            return true;
        }

        private boolean allWritten(ByteBuffer b) {
            return b.position() == b.limit();
        }

        private boolean shouldWrite(ByteBuffer b) {
            return b.position() != b.limit();
        }

        private void setWriteable(ByteBuffer b) {
        	b.limit(b.position());
        	b.rewind();
        }

        private boolean shouldRead(ByteBuffer b) {
            return b.position() == b.capacity();
        }

        private void initialize(ByteBuffer b) {
        	b.limit(b.capacity());
        	b.position(b.capacity());
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
                page = translateURL(page);
                if (exists(page)) {
                    createPageBuffer(page, cgiParams);
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
            addReply(l, "HTTP/1.1 " + error + "\r\n");
            addReply(l, "Date: " + new Date() + "\r\n");
            if (html != null) {
                addReply(l, "Content-Length: " + html.length() + "\r\n");
            }
            addReply(l, "Connection: close\r\n");
            addReply(l, "Content-Type: text/html;\r\n");
            addReply(l, "\r\n");
            if (html != null) {
                addReply(l, html);
            }
            replies = l.iterator();
            logger.info(request + " - ERROR: " + error);
        }

        private void sendHeader(long len, String contentType) {
            state = SENDING_REPLY;
            List<ByteBuffer> l = new LinkedList<ByteBuffer>();
            addReply(l, "HTTP/1.1 200 OK\r\n");
            addReply(l, "Content-type: " + contentType + "; charset=utf-8\r\n");
            addReply(l, "Date: " + new Date() + "\r\n");
            addReply(l, "Content-Length: " + len + "\r\n");
            addReply(l, "Connection: close\r\n");
            String allowedOrigin = getAllowedOrigin();
            if (allowedOrigin != null) {
                addReply(l, "Access-Control-Allow-Origin: " + allowedOrigin + "\r\n");
            }
            addReply(l, "Cache-Control: no-cache, no-store, must-revalidate\r\n");
            addReply(l, "Pragma: no-cache\r\n");
            addReply(l, "Expires: 0\r\n");
            addReply(l, "\r\n");
            replies = l.iterator();
        }

        private void addReply(List<ByteBuffer> l, String reply) {
            l.add(ByteBuffer.wrap(reply.getBytes()));
        }

        private void createPageBuffer(String file, Map<String, String> params) {
            DataLink dl = null;
            try {
                dl = server.getDataLink(file, params);
                
                if (dl == null) {
                    sendError("GET " + file, "404 Not Found", ERROR_NOTFOUND);
                }
                else if (dl.isByteBuffer()) {
                    fileChannel = null;
                    sdata = dl.getByteBuffer();
                    sendHeader(sdata.capacity(), dl.getContentType());
                }
                else if (dl.isFile()) {
                    File f = dl.getFile();
                    total = f.length();
                    fileChannel = new FileInputStream(f).getChannel();
                    sendHeader(total, getContentType(f.getName(), "text/ascii"));
                }
                else {
                    URLConnection conn = dl.getURL().openConnection();
                    total = conn.getContentLength();
                    fileChannel = Channels.newChannel(conn.getInputStream());
                    sendHeader(total, getContentType(file, conn.getContentType()));
                }
            }
            catch (Exception e) {
                sendError("GET " + file, "500 Internal Server Error", 
                    processTemplate(ERROR_INTERNAL, e.toString(), getStackTrace(e)));
                e.printStackTrace();
                return;
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
}
