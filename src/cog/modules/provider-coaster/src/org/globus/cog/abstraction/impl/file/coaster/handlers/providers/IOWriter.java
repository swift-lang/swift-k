//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 4, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers.providers;

import java.io.IOException;

public interface IOWriter extends IOHandle, Abortable {
    void setLength(long len) throws IOException;

    void close() throws IOException;

    void write(boolean last, byte[] data) throws IOException;

    void suspend();

    void resume();

    void setUpThrottling();

    void cancelThrottling();
}
