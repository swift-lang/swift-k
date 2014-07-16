//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 30, 2014
 */
package org.griphyn.vdl.mapping.nodes;

import java.util.List;

import k.rt.Future;

import org.griphyn.vdl.mapping.DSHandle;

public interface ArrayHandle extends DSHandle, Future {
    Iterable<List<?>> entryList();
    
    public int arraySize();
}
