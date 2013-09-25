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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;


public class ExtractInt extends VDLFunction {
	static {
		setArguments(ExtractInt.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		AbstractDataNode handle = null;
		try {
			handle = (AbstractDataNode) PA_VAR.getValue(stack);
			handle.waitFor();
			
			String fn = argList(filename(handle), true);
			Reader freader = new FileReader(fn);
			BufferedReader breader = new BufferedReader(freader);
			String str = breader.readLine();
			freader.close();
			DSHandle result = new RootDataNode(Types.INT, Integer.parseInt(str));
			int provid = VDLFunction.nextProvenanceID();
			VDLFunction.logProvenanceResult(provid, result, "extractint");
			VDLFunction.logProvenanceParameter(provid, handle, "filename");
			return result;
		}
		catch (IOException ioe) {
			throw new ExecutionException("Reading integer content of file", ioe);
		}
	}
}
