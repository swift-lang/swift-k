//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 11, 2010
 */
package org.globus.cog.abstraction.impl.common;

import java.util.HashSet;

import org.globus.cog.abstraction.interfaces.CleanUpSet;

public class CleanUpSetImpl extends HashSet<String> implements CleanUpSet {

    public boolean add(String e) {
        return super.add(e);
    }

    public void remove(String e) {
        super.remove(e);
    }
}
