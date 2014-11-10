//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 9, 2014
 */
package org.globus.cog.coaster.channels;

public class ChannelOptions {
    public enum Type {
        COMPRESSION, UP_COMPRESSION, DOWN_COMPRESSION;
    }
    
    public enum CompressionType {
        NONE, DEFLATE;
    }
}
