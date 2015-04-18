//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 4, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2.serialization;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;

import k.rt.ExecutionException;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.karajan.lib.swiftscript.SwiftSerializer;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Type;

public class WriteStructured implements SwiftSerializer {
    @Override
    public void writeData(String path, DSHandle src, Node owner, Map<String, Object> options) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(path));
            try {
                writeData(ps, (AbstractDataNode) src, Path.EMPTY_PATH, owner);
            }
            finally {
                ps.close();
            }
        }
        catch (Exception e) {
            throw new ExecutionException(owner, e);
        }
    }

    private void writeData(PrintStream ps, AbstractDataNode src, Path path, Node owner) {
        Type t = src.getType();
        if (t.isPrimitive()) {
            writePrimitive(ps, src, path, owner);
        }
        else if (t.isArray()) {
            writeArray(ps, src, path, owner);
        }
        else if (t.isComposite()) {
            writeStruct(ps, src, path, owner);
        }
        else {
            throw new ExecutionException(owner, "Cannot handle mapped data");
        }
    }

    private void writeStruct(PrintStream ps, AbstractDataNode src, Path path, Node owner) {
    }

    private void writeArray(PrintStream ps, AbstractDataNode src, Path path, Node owner) {
        src.waitFor(owner);
    }

    private void writePrimitive(PrintStream ps, AbstractDataNode src, Path path, Node owner) {
        src.waitFor(owner);
        ps.print(path.stringForm());
        ps.print(" = ");
        ps.println(src.getValue());
    }

    @Override
    public void checkParamType(Type type, Node owner) {
        // all types OK
    }
}