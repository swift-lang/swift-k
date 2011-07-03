/*
 * Created on Oct 8, 2007
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.type.Types;

public class ReadStructured extends VDLFunction {
	public static final Logger logger = Logger.getLogger(ReadStructured.class);

	public static final Arg DEST = new Arg.Positional("dest");
	public static final Arg SRC = new Arg.Positional("src");
	public static boolean warning;

	static {
		setArguments(ReadStructured.class, new Arg[] { DEST, SRC });
	}

	protected Object function(VariableStack stack) throws ExecutionException {
		DSHandle dest = (DSHandle) DEST.getValue(stack);
		AbstractDataNode src = (AbstractDataNode) SRC.getValue(stack);
        src.waitFor();
		if (src.getType().equals(Types.STRING)) {
			readData(dest, (String) src.getValue());
		}
		else {
			PhysicalFormat pf = src.getMapper().map(Path.EMPTY_PATH);
			if (pf instanceof AbsFile) {
				AbsFile af = (AbsFile) pf;
				if (!af.getProtocol().equalsIgnoreCase("file")) {
					throw new ExecutionException("readData2 only supports local files");
				}
				readData(dest, af.getPath());
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
			throw new ExecutionException(e);
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
				dest.setValue(new Double(Integer.parseInt(s.trim())));
			}
			else if (dest.getType().equals(Types.FLOAT)) {
				dest.setValue(new Double(s.trim()));
			}
			else if (dest.getType().equals(Types.BOOLEAN)) {
				dest.setValue(new Boolean(s.trim()));
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
