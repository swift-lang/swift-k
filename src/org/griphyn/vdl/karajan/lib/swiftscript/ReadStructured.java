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
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class ReadStructured extends SwiftFunction {
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
			readData(dest, (String) src.getValue());
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
					throw new ExecutionException("readData2 only supports local files");
				}
				readData(dest, af.getPath());
				dest.closeDeep();
			}
			else {
				throw new ExecutionException("readData2 only supports reading from files");
			}
		}
		return null;
	}

	private void readData(DSHandle dest, String path) throws ExecutionException {
		File f = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			try {
				readLines(dest, br, path);
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

	private void readLines(DSHandle dest, BufferedReader br, String path)
			throws ExecutionException, IOException {
		int count = 1;
		String line = br.readLine();
		while (line != null) {
			line = line.trim();
			if (!line.startsWith("#") && !line.equals("")) {
				try {
					String[] sp = line.split("=", 2);
					setValue(dest.getField(Path.parse(sp[0].trim())), sp[1].trim());
				}
				catch (Exception e) {
					throw new ExecutionException(e.getMessage() + " in " + path + ", line " + count
							+ ": " + line, e);
				}
			}
			line = br.readLine();
			count++;
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
				throw new ExecutionException("Don't know how to read type " + dest.getType()
						+ " for path " + dest.getPathFromRoot());
			}
		}
		catch (NumberFormatException e) {
			throw new ExecutionException("Could not convert value to number: " + s);
		}
	}
}
