//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 22, 2005
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;

public interface Purgeable {
	void purge(CoasterChannel channel) throws IOException;
}
