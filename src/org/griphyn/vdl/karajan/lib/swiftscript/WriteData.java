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


package org.griphyn.vdl.karajan.lib.swiftscript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;


public class WriteData extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(WriteData.class);

	private ArgRef<AbstractDataNode> dest;
    private ArgRef<AbstractDataNode> src;

    @Override
    protected Signature getSignature() {
        return new Signature(params("dest", "src"));
    }

	public static boolean warning;

	@Override
	public Object function(Stack stack) {
		// dest needs to be mapped to a file, or a string
		AbstractDataNode dest = this.dest.getValue(stack);

		// src can be any of several forms of value
		AbstractDataNode src = this.src.getValue(stack);

		try {
            src.waitFor(this);
        }
        catch (DependentException e) {
        	if (logger.isInfoEnabled()) {
        	    logger.info(this + " caught dependent exception");
        	}
        	dest.setValue(new DataDependentException(dest, e));
            return null;
        }

		if (dest.getType().equals(Types.STRING)) {
			writeData((String)dest.getValue(), src);
		}
		else {
			PhysicalFormat pf = dest.map();
			if (pf instanceof AbsFile) {
				AbsFile af = (AbsFile) pf;
				if (!af.getProtocol().equalsIgnoreCase("file")) {
					throw new ExecutionException("writeData only supports local files");
				}
				writeData(af.getPath(), src);
			}
			else {
				throw new ExecutionException("writeData only supports writing to files");
			}
			dest.closeDeep();
		}
		return null;
	}

	private void writeData(String path, DSHandle src) throws ExecutionException {
		File f = new File(path);
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(f));
			try {
				if (src.getType().isArray()) {
					// each line is an item
					writeArray(br, src);
				}
				else if (src.getType().isPrimitive()) {
					writePrimitive(br, src);
				}
				else {
					// struct
					writeStructHeader(src.getType(), br);
					writeStruct(br, src);
				}
			}
			finally {
				try {
					br.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		catch (IOException e) {
			throw new ExecutionException(this, e);
		}
	}

	private void writePrimitive(BufferedWriter br, DSHandle src) throws IOException,
			ExecutionException {
		br.write(src.getValue().toString());
	}

	private void writeArray(BufferedWriter br, DSHandle src) throws IOException, ExecutionException {
		if (src.getType().itemType().isPrimitive()) {
			writePrimitiveArray(br, src);
		}
		else {
			writeStructArray(br, src);
		}
	}

	private void writePrimitiveArray(BufferedWriter br, DSHandle src) throws IOException,
			ExecutionException {
	    // this scheme currently only works properly if the keys are strings
		Map<Comparable<?>, DSHandle> m = ((AbstractDataNode) src).getArrayValue();
		Map<Comparable<?>, DSHandle> c = new TreeMap<Comparable<?>, DSHandle>();
		c.putAll(m);
		for (DSHandle h : c.values()) {
			br.write(h.getValue().toString());
			br.newLine();
		}
	}

	private void writeStructArray(BufferedWriter br, DSHandle src) throws IOException,
			ExecutionException {
		writeStructHeader(src.getType().itemType(), br);
		Map<Comparable<?>, DSHandle> m = ((AbstractDataNode) src).getArrayValue();
		Map<Comparable<?>, DSHandle> c = new TreeMap<Comparable<?>, DSHandle>();
		c.putAll(m);
		for (DSHandle h : c.values()) {
			writeStruct(br, h);
		}
	}


	private void writeStructHeader(Type type, BufferedWriter br) throws ExecutionException,
			IOException {
		for (String name : type.getFieldNames()) {
			br.write(name);
			br.write(" ");
		}
		br.newLine();
	}

	private void writeStruct(BufferedWriter br, DSHandle struct) throws IOException, ExecutionException {
		try {
		    for (String name : struct.getType().getFieldNames()) {
				DSHandle child = struct.getField(Path.EMPTY_PATH.addLast(name));
				br.write(child.getValue().toString());
				br.write(" ");
			}
			br.newLine();
		} catch(InvalidPathException e) {
			throw new ExecutionException("Unexpectedly invalid path", e);
		}
	}
}
