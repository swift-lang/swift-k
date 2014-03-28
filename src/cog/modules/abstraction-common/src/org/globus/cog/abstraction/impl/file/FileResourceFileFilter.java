//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 26, 2014
 */
package org.globus.cog.abstraction.impl.file;

import org.globus.cog.abstraction.interfaces.GridFile;

public interface FileResourceFileFilter {
    boolean accept(GridFile f);
}
