//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 20, 2007
 */
package org.globus.cog.abstraction.impl.common.util;

import java.io.IOException;
import java.io.Writer;

public class WriterMultiplexer extends Writer {
    private Writer w1, w2;
    
    public WriterMultiplexer(Writer w1, Writer w2) {
        this.w1 = w1;
        this.w2 = w2;
    }

    public void close() throws IOException {
        IOException ex = null;
        try {
            w1.close();
        }
        catch (IOException e) {
            ex = e;
        }
        try {
            w2.close();
        }
        catch (IOException e) {
            ex = e;
        }
    }

    public void flush() throws IOException {
        IOException ex = null;
        try {
            w1.flush();
        }
        catch (IOException e) {
            ex = e;
        }
        try {
            w2.flush();
        }
        catch (IOException e) {
            ex = e;
        }
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        IOException ex = null;
        try {
            w1.write(cbuf, off, len);
        }
        catch (IOException e) {
            ex = e;
        }
        try {
            w2.write(cbuf, off, len);
        }
        catch (IOException e) {
            ex = e;
        }
    }

    public static Writer multiplex(Writer w1, Writer w2) {
        if (w1 == null) {
            return w2;
        }
        if (w2 == null) {
            return w1;
        }
        return new WriterMultiplexer(w1, w2);
    }
}
