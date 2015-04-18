//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 4, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2.serialization;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map;

import k.rt.ExecutionException;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.karajan.lib.swiftscript.SwiftSerializer;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.type.Type;

public class WritePrimitive implements SwiftSerializer {

    @Override
    public void writeData(String path, DSHandle src, Node owner, Map<String, Object> options) {
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(path));
            try {
                br.write(src.getValue().toString());
            }
            finally {
                br.close();
            }
        }
        catch (Exception e) {
            throw new ExecutionException(owner, e);
        }
    }

    @Override
    public void checkParamType(Type type, Node owner) {
        if (!type.isPrimitive()) {
            throw new ExecutionException(owner, "This format only supports primitive data");
        }
    }
}