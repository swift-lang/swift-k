//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 1, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers.providers;

import java.io.IOException;

public interface IOProvider {
    IOReader pull(String src, String dest, ReadIOCallback cb) throws IOException;

    IOWriter push(String src, String dest, WriteIOCallback cb) throws IOException;

    void abort(IOHandle handle) throws IOException;
    
    /**
     * Returns true if this IO provider accepts data directly. False
     * if it transfers data on its own. 
     */
    boolean isDirect();
}
