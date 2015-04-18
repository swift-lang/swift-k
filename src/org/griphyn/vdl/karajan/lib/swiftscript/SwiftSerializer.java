//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 3, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import java.util.Map;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.type.Type;

public interface SwiftSerializer {
    void writeData(String path, DSHandle src, Node owner, Map<String, Object> options);

    void checkParamType(Type type, Node owner);
}
