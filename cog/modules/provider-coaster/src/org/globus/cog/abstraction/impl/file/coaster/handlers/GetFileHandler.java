//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 26, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public class GetFileHandler extends CoasterFileRequestHandler {
    public static final Logger logger = Logger
            .getLogger(GetFileHandler.class);

    private File f;
    private long size;
    private int chunks;
    private Exception ex;

    public void requestComplete() throws ProtocolException {
        f = normalize(getInDataAsString(0));
        logger.warn(f.getAbsolutePath());
        size = f.length();
        chunks = (int) ((size - 1) / 16384) + 1;
        sendReply();
    }

    public Collection getOutData() {
        return new AbstractCollection() {

            public Iterator iterator() {
                return new Iterator() {
                    private long crt = 0;
                    private byte[] buf = new byte[16384];
                    private boolean first = true;
                    private FileInputStream is;

                    {
                        try {
                            is = new FileInputStream(f);
                        }
                        catch (Exception e) {
                            logger.info("Could not open file", e);
                            ex = e;
                        }
                    }

                    public boolean hasNext() {
                        return (first || crt < size) && ex == null;
                    }

                    public Object next() {
                        try {
                            if (first) {
                                first = false;
                                return pack(size);
                            }
                            else {
                                int l = is.read(buf);
                                crt += l;
                                if (crt == l) {
                                    is.close();
                                }
                                if (l == buf.length) {
                                    return buf;
                                }
                                else {
                                    byte[] mb = new byte[l];
                                    System.arraycopy(buf, 0, mb, 0, l);
                                    return mb;
                                }
                            }
                        }
                        catch (Exception e) {
                            ex = e;
                            return new byte[0];
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public int size() {
                return ex == null ? chunks + 1 : 0;
            }
        };
    }
    
    public void send() throws ProtocolException {
        KarajanChannel channel = getChannel();
        Collection outData = getOutData();
        if (channel == null) {
            throw new ProtocolException("Unregistered command");
        }
        if (logger.isDebugEnabled()) {
            logger.debug(ppOutData("HND"));
        }
        boolean fin = (outData == null) || (outData.size() == 0);
        if (!fin) {
            Iterator i = outData.iterator();
            while (i.hasNext()) {
                byte[] buf = (byte[]) i.next();
                channel.sendTaggedReply(getId(), buf, !i.hasNext(), getErrorFlag());
            }
        }
        if (ex != null) {
            logger.info("Transfer exception", ex);
            channel.sendTaggedReply(getId(), ex.getMessage().getBytes(), true, true);
        }
    }
}
