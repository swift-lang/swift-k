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


public interface IOReader extends IOHandle, Abortable {
    void start() throws IOException;

    void dataSent();

    void close();

    void resume();

    void suspend();
}
