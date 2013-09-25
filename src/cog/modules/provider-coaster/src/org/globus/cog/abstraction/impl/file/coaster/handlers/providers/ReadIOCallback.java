//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 4, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers.providers;

import java.nio.ByteBuffer;

public interface ReadIOCallback extends IOCallback {
    void length(long len);
    
    void data(IOHandle handle, ByteBuffer data, boolean last);

    void info(String msg);

    void queued();
}
