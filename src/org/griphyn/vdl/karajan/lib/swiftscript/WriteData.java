package org.griphyn.vdl.karajan.lib.swiftscript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;


public class WriteData extends VDLFunction {
	public static final Logger logger = Logger.getLogger(WriteData.class);

	public static final Arg DEST = new Arg.Positional("dest");
	public static final Arg SRC = new Arg.Positional("src");
	public static boolean warning;

	static {
		setArguments(WriteData.class, new Arg[] { DEST, SRC });
	}

	protected Object function(VariableStack stack) throws ExecutionException {
		// dest needs to be mapped to a file, or a string
		DSHandle dest = (DSHandle) DEST.getValue(stack);

		// src can be any of several forms of value
		AbstractDataNode src = (AbstractDataNode) SRC.getValue(stack);

		src.waitFor();

		if (dest.getType().equals(Types.STRING)) {
			writeData((String)dest.getValue(), src);
		}
		else {
			PhysicalFormat pf = dest.getMapper().map(Path.EMPTY_PATH);
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
			throw new ExecutionException(e);
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
		Map<Comparable<?>, DSHandle> c = new TreeMap<Comparable<?>, DSHandle>(new ArrayIndexComparator());
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
		Map<Comparable<?>, DSHandle> c = new TreeMap<Comparable<?>, DSHandle>(new ArrayIndexComparator());
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

	class ArrayIndexComparator implements Comparator<Comparable<?>> {
		public int compare(Comparable<?> o1, Comparable<?> o2) {
			int i1 = Integer.parseInt(String.valueOf(o1));
			int i2 = Integer.parseInt(String.valueOf(o2));
			if(i1 < i2) return -1;
			if(i1 > i2) return 1;
			return 0;
		}
	}
}
