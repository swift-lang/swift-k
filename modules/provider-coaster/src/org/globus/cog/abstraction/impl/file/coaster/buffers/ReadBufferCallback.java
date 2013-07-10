//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 19, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

import java.nio.ByteBuffer;

public interface ReadBufferCallback {
    void dataRead(boolean last, ByteBuffer buf);
    
    void error(boolean last, Exception e);

    void queued();
}
