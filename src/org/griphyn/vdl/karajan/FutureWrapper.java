//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 1, 2011
 */
package org.griphyn.vdl.karajan;

import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.Future;

public interface FutureWrapper extends Future {
    void notifyListeners();

    int listenerCount();

    EventTargetPair[] getListenerEvents();
}
