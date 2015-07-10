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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class ReadData extends SwiftFunction implements SwiftDeserializer {
	public static final Logger logger = Logger.getLogger(ReadData.class);
	
	public static final Map<String, Object> DEFAULT_OPTIONS;
	
	static {
	    DEFAULT_OPTIONS = new HashMap<String, Object>();
	    DEFAULT_OPTIONS.put("separator", " ");
	}
	
	@SuppressWarnings("unchecked")
    private <T> T getOption(String name, Map<String, Object> opts) {
	    Object o = opts.get(name);
	    if (o == null) {
	        o = DEFAULT_OPTIONS.get(name);
	    }
	    return (T) o;
	}
	
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
		Type st = src.getType();
		if (st.equals(Types.STRING)) {
			readData(dest, (String) src.getValue(), this, DEFAULT_OPTIONS);
		}
		else if (st.isPrimitive() || st.isComposite()) {
		    throw new ExecutionException(this, "invalid argument of type '" + st + 
		        "' passed to readData: must be a string or a mapped type");
		}
		else {
			PhysicalFormat pf = src.map();
			if (pf instanceof AbsFile) {
				AbsFile af = (AbsFile) pf;
				if (!af.getProtocol("file").equals("file")) {
					throw new ExecutionException(this, "readData only supports local files");
				}
				readData(dest, af.getPath(), this, DEFAULT_OPTIONS);
			}
			else {
				throw new ExecutionException("readData only supports reading from files");
			}
		}
		return null;
	}

	@Override
    public void checkReturnType(Type type, Node owner) {
	    // all types OK
    }

    public void readData(DSHandle dest, String path, Node owner, Map<String, Object> options) {
        options = processOptions(options);
		File f = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			try {
			    if (dest.getType().hasMappedComponents()) {
                    throw new ExecutionException(owner, "Cannot serialize file-valued data");
                }
			    if (dest.getType().isComposite()) {
    				if (dest.getType().isArray()) {
    					// each line is an item
    					readArray(dest, br, owner, options);
    				}
    				else {
                        // struct
                        readStruct(dest, br, owner, options);
                    }
			    }
                else if (dest.getType().isPrimitive()) {
                    readPrimitive(dest, br, owner);
                }
                else {
                    throw new ExecutionException(owner, "Internal error. Cannot read file-valued data");
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

	private Map<String, Object> processOptions(Map<String, Object> options) {
	    return options;
    }

    private void readPrimitive(DSHandle dest, BufferedReader br, Node owner) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			sb.append(line);
			sb.append('\n');
			line = br.readLine();
		}
		String s = sb.toString();
		setValue(dest, s, owner);
	}

	private void readArray(DSHandle dest, BufferedReader br, Node owner, Map<String, Object> opts) 
	        throws IOException {
		if (dest.getType().itemType().isPrimitive()) {
			readPrimitiveArray(dest, br, owner);
		}
		else {
			readStructArray(dest, br, owner, opts);
		}
	}

	private void readPrimitiveArray(DSHandle dest, BufferedReader br, Node owner) throws IOException {
		int index = 0;
		String line = br.readLine();
		Type itemType = dest.getType().itemType();
		try {
			while (line != null) {
				DSHandle child = dest.getField(index);
				setValue(child, line, owner);
				line = br.readLine();
				index++;
			}
		}
		catch (NoSuchFieldException e) {
			throw new ExecutionException(owner, e);
		}
	}

    private void readStructArray(DSHandle dest, BufferedReader br, Node owner, 
	        Map<String, Object> opts) throws IOException {
		String[] header = readStructHeader(dest.getType().itemType(), br, owner, opts);
		int index = 0;
		String line = br.readLine();
		try {
			while (line != null) {
				line = line.trim();
				if (!line.equals("")) {
					DSHandle child = dest.getField(index);
					readStruct(child, line, header, owner, opts);
					index++;
				}
				line = br.readLine();
			}
		}
		catch (NoSuchFieldException e) {
			throw new ExecutionException(owner, e);
		}
	}

	private String[] readStructHeader(Type type, BufferedReader br, Node owner, 
	        Map<String, Object> opts) throws IOException {
		String line = br.readLine();
		if (line == null) {
			throw new ExecutionException(owner, "Missing header");
		}
		else {
		    String sep = getOption("separator", opts);
			String[] header = split(line, sep, owner);
			Set<String> t = new HashSet<String>(type.getFieldNames());
			Set<String> h = new HashSet<String>(Arrays.asList(header));
			if (t.size() != h.size()) {
				throw new ExecutionException(owner, "File header does not match type. " + "Expected "
						+ t.size() + " whitespace separated items. Got " + h.size() + " instead.");
			}
			if (!t.equals(h)) {
				throw new ExecutionException(owner, "File header does not match type. "
						+ "Expected the following whitespace separated header items: " + t
						+ ". Instead, the header was: " + h);
			}
			return header;
		}
	}

	private void readStruct(DSHandle dest, String line, String[] header, Node owner, Map<String, Object> opts) {
	    String sep = getOption("separator", opts);
	    String[] cols = split(line, sep, owner);
		try {
			if (cols.length != header.length) {
				throw new ExecutionException(owner, "Column count for line \"" + line
						+ "\" does not match the header column count of " + header.length);
			}
			else {

				for (int i = 0; i < cols.length; i++) {
					DSHandle child = dest.getField(header[i]);
					setValue(child, cols[i], owner);
				}
			}
		}
		catch (NoSuchFieldException e) {
			throw new ExecutionException(owner, e);
		}
	}

	private String[] split(String line, String sep, Node owner) {
	    List<String> l = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        int len = line.length();
        int sepLen = sep.length();
        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);
            if (inString) {
                if (c == '"') {
                    if (i < len - 1 && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i += 2;
                    }
                    else {
                        inString = false;
                        i = line.indexOf(sep, i + 1) + 1;
                        l.add(sb.toString());
                        if (i == 0) {
                            i = len;
                            sb = null;
                        }
                        else {
                            sb = new StringBuilder();
                        }
                    }
                }
                else {
                    sb.append(c);
                }
            }
            else {
                if (line.indexOf(sep, i) == i) {
                    // separator
                    l.add(sb.toString());
                    sb = new StringBuilder();
                    i += sepLen;
                }
                else if (c == '"') {
                    inString = true;
                    if (!sb.toString().matches("\\s*")) {
                        throw new ExecutionException(owner, "Illegal extra characters before string: '" + sb.toString() + "'");
                    }
                    sb = new StringBuilder();
                }
                else {
                    sb.append(c);
                }
            }
        }
        if (sb != null) {
            l.add(sb.toString());
        }
        return l.toArray(new String[0]);
    }

    private void readStruct(DSHandle dest, BufferedReader br, Node owner, Map<String, Object> opts) 
	        throws IOException {
		String[] header = readStructHeader(dest.getType(), br, owner, opts);
		String line = br.readLine();
		if (line == null) {
			throw new ExecutionException("Missing values");
		}
		else {
			readStruct(dest, line, header, owner, opts);
		}
	}

	private void setValue(DSHandle dest, String s, Node owner) {
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
				throw new ExecutionException(owner, "Don't know how to read type " + dest.getType());
			}
		}
		catch (NumberFormatException e) {
			throw new ExecutionException(owner, "Could not convert value to number: " + s);
		}
	}
}
