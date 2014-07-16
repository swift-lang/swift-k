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
 * Created on Oct 8, 2007
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class ReadData extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(ReadData.class);
	
	private ArgRef<AbstractDataNode> dest;
	private ArgRef<AbstractDataNode> src;

	@Override
    protected Signature getSignature() {
        return new Signature(params("dest", "src"));
    }

	public static boolean warning;

	@Override
	public Object function(Stack stack) {
		AbstractDataNode dest = this.dest.getValue(stack);
		AbstractDataNode src = this.src.getValue(stack);
		try {
		    src.waitFor(this);
		}
		catch (DependentException e) {
		    dest.setValue(new DataDependentException(dest, e));
		    return null;
		}
		if (src.getType().equals(Types.STRING)) {
			readData(dest, (String) src.getValue());
		}
		else {
			PhysicalFormat pf = src.map();
			if (pf instanceof AbsFile) {
				AbsFile af = (AbsFile) pf;
				if (!af.getProtocol().equalsIgnoreCase("file")) {
					throw new ExecutionException("readData only supports local files");
				}
				readData(dest, af.getPath());
			}
			else {
				throw new ExecutionException("readData only supports reading from files");
			}
		}
		return null;
	}

	private void readData(DSHandle dest, String path) throws ExecutionException {
		File f = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			try {
				if (dest.getType().isArray()) {
					// each line is an item
					readArray(dest, br);
				}
				else if (dest.getType().isPrimitive()) {
					readPrimitive(dest, br);
				}
				else {
					// struct
					readStruct(dest, br);
				}
				dest.closeDeep();
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

	private void readPrimitive(DSHandle dest, BufferedReader br) throws IOException,
			ExecutionException {
		StringBuffer sb = new StringBuffer();
		String line = br.readLine();
		while (line != null) {
			sb.append(line);
			line = br.readLine();
		}
		String s = sb.toString();
		setValue(dest, s);
	}

	private void readArray(DSHandle dest, BufferedReader br) throws IOException, ExecutionException {
		if (dest.getType().itemType().isPrimitive()) {
			readPrimitiveArray(dest, br);
		}
		else {
			readStructArray(dest, br);
		}
	}

	private void readPrimitiveArray(DSHandle dest, BufferedReader br) throws IOException,
			ExecutionException {
		int index = 0;
		String line = br.readLine();
		try {
			while (line != null) {
				DSHandle child = dest.getField(index);
				setValue(child, line);
				line = br.readLine();
				index++;
			}
		}
		catch (NoSuchFieldException e) {
			throw new ExecutionException(this, e);
		}
	}

	private void readStructArray(DSHandle dest, BufferedReader br) throws IOException,
			ExecutionException {
		String[] header = readStructHeader(dest.getType().itemType(), br);
		int index = 0;
		String line = br.readLine();
		try {
			while (line != null) {
				line = line.trim();
				if (!line.equals("")) {
					DSHandle child = dest.getField(index);
					readStruct(child, line, header);
					index++;
				}
				line = br.readLine();
			}
		}
		catch (NoSuchFieldException e) {
			throw new ExecutionException(this, e);
		}
	}

	private String[] readStructHeader(Type type, BufferedReader br) throws ExecutionException,
			IOException {
		String line = br.readLine();
		if (line == null) {
			throw new ExecutionException("Missing header");
		}
		else {
			String[] header = line.split("\\s+");
			Set<String> t = new HashSet<String>(type.getFieldNames());
			Set<String> h = new HashSet<String>(Arrays.asList(header));
			if (t.size() != h.size()) {
				throw new ExecutionException("File header does not match type. " + "Expected "
						+ t.size() + " whitespace separated items. Got " + h.size() + " instead.");
			}
			if (!t.equals(h)) {
				throw new ExecutionException("File header does not match type. "
						+ "Expected the following whitespace separated header items: " + t
						+ ". Instead, the header was: " + h);
			}
			return header;
		}
	}

	private void readStruct(DSHandle dest, String line, String[] header) throws ExecutionException {
		String[] cols = line.split("\\s+");
		try {
			if (cols.length != header.length) {
				throw new ExecutionException("Column count for line \"" + line
						+ "\" does not match the header column count of " + header.length);
			}
			else {

				for (int i = 0; i < cols.length; i++) {
					DSHandle child = dest.getField(header[i]);
					setValue(child, cols[i]);
				}
			}
		}
		catch (NoSuchFieldException e) {
			throw new ExecutionException(this, e);
		}
	}

	private void readStruct(DSHandle dest, BufferedReader br) throws ExecutionException,
			IOException {
		String[] header = readStructHeader(dest.getType(), br);
		String line = br.readLine();
		if (line == null) {
			throw new ExecutionException("Missing values");
		}
		else {
			readStruct(dest, line, header);
		}
	}

	private void setValue(DSHandle dest, String s) throws ExecutionException {
		try {

			if (dest.getType().equals(Types.INT)) {
				dest.setValue(Integer.valueOf(s.trim()));
			}
			else if (dest.getType().equals(Types.FLOAT)) {
				dest.setValue(new Double(s.trim()));
			}
			else if (dest.getType().equals(Types.BOOLEAN)) {
				dest.setValue(Boolean.valueOf(s.trim()));
			}
			else if (dest.getType().equals(Types.STRING)) {
				dest.setValue(s);
			}
			else {
				throw new ExecutionException("Don't know how to read type " + dest.getType());
			}
		}
		catch (NumberFormatException e) {
			throw new ExecutionException("Could not convert value to number: " + s);
		}
	}
}
