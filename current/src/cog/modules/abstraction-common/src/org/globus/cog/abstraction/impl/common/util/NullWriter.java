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

public class NullWriter extends Writer {
    public void close() throws IOException {
    }

    public void flush() throws IOException {
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
    }
}
