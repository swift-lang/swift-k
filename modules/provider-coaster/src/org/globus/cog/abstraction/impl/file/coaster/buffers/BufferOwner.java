//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 23, 2012
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

public interface BufferOwner {
    String getName();
    
    boolean isAlive();
}
