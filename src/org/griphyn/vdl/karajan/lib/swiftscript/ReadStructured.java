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
import java.util.Map;

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
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class ReadStructured extends SwiftFunction implements SwiftDeserializer {
	public static final Logger logger = Logger.getLogger(ReadStructured.class);

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
			readData(dest, (String) src.getValue(), this, null);
			dest.closeDeep();
		}
		else if (st.isPrimitive() || st.isComposite()) {
            throw new ExecutionException(this, "invalid argument of type '" + st + 
                "' passed to readData2: must be a string or a mapped type");
        }
		else {
			PhysicalFormat pf = src.map();
			if (pf instanceof AbsFile) {
				AbsFile af = (AbsFile) pf;
				if (!af.getProtocol().equalsIgnoreCase("file")) {
					throw new ExecutionException("readStructured only supports local files");
				}
				readData(dest, af.getPath(), this, null);
				dest.closeDeep();
			}
			else {
				throw new ExecutionException("readStructured only supports reading from files");
			}
		}
		return null;
	}
	
	@Override
    public void checkReturnType(Type type, Node owner) {
        // all types OK
    }

	public void readData(DSHandle dest, String path, Node owner, Map<String, Object> options) {
		File f = new File(path);
		try {
		    if (dest.getType().hasMappedComponents()) {
		        throw new ExecutionException(owner, "Cannot deserialize file-valued data.");
		    }
			BufferedReader br = new BufferedReader(new FileReader(f));
			try {
				readLines(dest, br, path, owner);
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
		catch (Exception e) {
			throw new ExecutionException(this, e);
		}
	}

	private void readLines(DSHandle dest, BufferedReader br, String path, Node owner)
			throws IOException {
		int count = 1;
		String line = br.readLine();
		while (line != null) {
			line = line.trim();
			if (!line.startsWith("#") && !line.equals("")) {
				try {
					String[] sp = line.split("=", 2);
					setValue(dest.getField(Path.parse(sp[0].trim())), sp[1].trim(), owner);
				}
				catch (Exception e) {
					throw new ExecutionException(owner, e.getMessage() + " in " + path + ", line " + count
							+ ": " + line, e);
				}
			}
			line = br.readLine();
			count++;
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
			    if (!s.startsWith("\"")) {
			        throw new ExecutionException("Invalid string value: '" + s + "'. Must begin with a quote.");
			    }
			    if (!s.endsWith("\"")) {
			        throw new ExecutionException("Invalid string value: '" + s + "'. Must end with a quote.");
			    }
				dest.setValue(processEscapes(s, owner));
			}
			else {
				throw new ExecutionException(owner, "Don't know how to read type " + dest.getType()
						+ " for path " + dest.getPathFromRoot());
			}
		}
		catch (NumberFormatException e) {
			throw new ExecutionException(owner, "Could not convert value to number: " + s);
		}
	}

    private String processEscapes(String s, Node owner) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < s.length() - 1; i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                if (i == s.length()) {
                    throw new ExecutionException(owner, "Unterminated escape sequence at the end of string '" + s + "'");
                }
                c = s.charAt(i);
                switch (c) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default: 
                        throw new ExecutionException(owner, "Invalid escape sequence '\\" + c + "' in string '" + s + "'");
                }
            }
            else if (c == '"') {
                if (i != s.length() - 1) {
                    throw new ExecutionException(owner, "Unescaped quote in string literal '" + s + "'");
                }
            }
            else {
                sb.append(c);
            }
        }
        return s.toString();
    }
}
