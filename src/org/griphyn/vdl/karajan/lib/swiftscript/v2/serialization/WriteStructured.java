/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
                AbstractDataNode n = (AbstractDataNode) src;
                writeData(ps, n, Path.EMPTY_PATH, owner);
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
            throw new ExecutionException(owner, "Internal error. Cannot serialize file-valued data");
        }
    }

    private void writeStruct(PrintStream ps, AbstractDataNode src, Path path, Node owner) {
        for (String fieldName : src.getType().getFieldNames()) {
            try {
                writeData(ps, (AbstractDataNode) src.getField(fieldName), path.addLast(fieldName), owner);
            }
            catch (NoSuchFieldException e) {
                throw new ExecutionException(owner, "Internal error. Type inconsistency.");
            }
        }
    }

    private void writeArray(PrintStream ps, AbstractDataNode src, Path path, Node owner) {
        src.waitFor(owner);
        for (Map.Entry<Comparable<?>, DSHandle> e : src.getArrayValue().entrySet()) {
            writeData(ps, (AbstractDataNode) e.getValue(), path.addLast(e.getKey(), true), owner);
        }
    }

    private void writePrimitive(PrintStream ps, AbstractDataNode src, Path path, Node owner) {
        src.waitFor(owner);
        ps.print(path.stringForm());
        ps.print(" = ");
        ps.println(src.getValue());
    }

    @Override
    public void checkParamType(Type type, Node owner) {
        if (type.hasMappedComponents()) {
            throw new ExecutionException(owner, "Cannot serialize file-valued data");
        }
    }
}