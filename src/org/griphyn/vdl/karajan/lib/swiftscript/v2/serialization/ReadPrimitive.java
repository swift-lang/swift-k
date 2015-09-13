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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import k.rt.ExecutionException;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.karajan.lib.swiftscript.SwiftDeserializer;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class ReadPrimitive implements SwiftDeserializer {
    @Override
    public void readData(DSHandle dest, String path, Node owner, Map<String, Object> options) {
        Type t = dest.getType();
        try {
            String s = readFully(path);
            if (t.equals(Types.STRING)) {
                dest.setValue(s);
            }
            else if (t.equals(Types.INT)) {
                dest.setValue(Integer.parseInt(s.trim()));
            }
            else if (t.equals(Types.FLOAT)) {
                dest.setValue(Double.parseDouble(s.trim()));
            }
            else if (t.equals(Types.BOOLEAN)) {
                dest.setValue(Boolean.parseBoolean(s.trim()));
            }
            else {
                throw new ExecutionException(owner, "Wrong type: " + t);
            }
        }
        catch (Exception e) {
            throw new ExecutionException(owner, e);
        }
    }

    private String readFully(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
                if (line != null) {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
        finally {
            br.close();
        }
    }

    @Override
    public void checkReturnType(Type type, Node owner) {
        if (!type.isPrimitive()) {
            throw new ExecutionException(owner, "This format only supports primitive data");
        }
    }
}