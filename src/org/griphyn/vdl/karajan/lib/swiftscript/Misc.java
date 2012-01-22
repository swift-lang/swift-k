package org.griphyn.vdl.karajan.lib.swiftscript;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.griphyn.vdl.karajan.lib.SwiftArg;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.util.VDL2Config;

public class Misc extends FunctionsCollection {

	private static final Logger logger = Logger.getLogger(Misc.class);

	public static final SwiftArg PA_INPUT = new SwiftArg.Positional("input");
	public static final SwiftArg PA_PATTERN = new SwiftArg.Positional("regexp");
	public static final SwiftArg PA_TRANSFORM = new SwiftArg.Positional("transform");
	public static final SwiftArg PA_FILE = new SwiftArg.Positional("file");
	public static final SwiftArg PA_ARRAY = new SwiftArg.Positional("array");

	static {
		setArguments("swiftscript_trace", new Arg[] { Arg.VARGS });
		setArguments("swiftscript_strcat",  new Arg[] { Arg.VARGS });
		setArguments("swiftscript_exists", new Arg[] { Arg.VARGS });
		setArguments("swiftscript_strcut", new Arg[] { PA_INPUT, PA_PATTERN });
		setArguments("swiftscript_strstr", new Arg[] { PA_INPUT, PA_PATTERN });
		setArguments("swiftscript_strsplit", new Arg[] { PA_INPUT, PA_PATTERN });
		setArguments("swiftscript_regexp", new Arg[] { PA_INPUT, PA_PATTERN, PA_TRANSFORM });
		setArguments("swiftscript_toint", new Arg[] { PA_INPUT });
		setArguments("swiftscript_tofloat", new Arg[] { PA_INPUT });
		setArguments("swiftscript_format", new Arg[] { PA_INPUT, PA_TRANSFORM });
		setArguments("swiftscript_pad", new Arg[] { PA_INPUT, PA_TRANSFORM });
		setArguments("swiftscript_tostring", new Arg[] { PA_INPUT });
		setArguments("swiftscript_dirname", new Arg[] { PA_FILE });
		setArguments("swiftscript_length", new Arg[] { PA_ARRAY });
		setArguments("swiftscript_existsfile", new Arg[] { PA_FILE });
	}

	private static final Logger traceLogger =
	    Logger.getLogger("org.globus.swift.trace");
	public DSHandle swiftscript_trace(VariableStack stack)
	throws ExecutionException {

		AbstractDataNode[] args = VDLFunction.waitForAllVargs(stack);

		StringBuffer buf = new StringBuffer();
		buf.append("SwiftScript trace: ");
		for (int i = 0; i < args.length; i++) {
			DSHandle handle = args[i];
			if (i != 0) {
			    buf.append(", ");
			}
			Object v = args[i].getValue();
			//buf.append(v == null ? args[i] : v);
			prettyPrint(buf, args[i]);
		}
		traceLogger.warn(buf);
		return null;
	}

	private void prettyPrint(StringBuffer buf, DSHandle h) {
	    Object o = h.getValue();
	    if (o == null) {
	        buf.append(h);
	    }
	    else {
    	    if (h.getType().isPrimitive()) {
    	        buf.append(o);
    	    }
    	    else if (h.getType().isArray()) {
    	        try {
    	            Iterator<DSHandle> i = h.getFields(Path.CHILDREN).iterator();
    	            buf.append('[');
    	            while (i.hasNext()) {
    	                prettyPrint(buf, i.next());
    	                if (i.hasNext()) {
    	                    buf.append(", ");
    	                }
    	            }
    	            buf.append(']');
    	        }
    	        catch (HandleOpenException e) {
    	        }
                catch (InvalidPathException e) {
                }
    	    }
	    }
    }

    public DSHandle swiftscript_strcat(VariableStack stack) throws ExecutionException {
	    if (logger.isDebugEnabled()) {
	        logger.debug(stack);
	    }
		Object[] args = SwiftArg.VARGS.asArray(stack);
		int provid = VDLFunction.nextProvenanceID();
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < args.length; i++) {
			buf.append(TypeUtil.toString(args[i]));
		}
		
		DSHandle handle = new RootDataNode(Types.STRING);
		handle.setValue(buf.toString());
		handle.closeShallow();
		try {
			if(VDL2Config.getConfig().getProvenanceLog()) {
				DSHandle[] provArgs = SwiftArg.VARGS.asDSHandleArray(stack);
				for (int i = 0; i < provArgs.length; i++) {
					VDLFunction.logProvenanceParameter(provid, provArgs[i], ""+i);
				}
				VDLFunction.logProvenanceResult(provid, handle, "strcat");
			}
		} catch(IOException ioe) {
			throw new ExecutionException("When logging provenance for strcat", ioe);
		}
		return handle;
	}

	public DSHandle swiftscript_exists(VariableStack stack)
	throws ExecutionException {
		logger.debug(stack);
		Object[] args = SwiftArg.VARGS.asArray(stack);
		int provid = VDLFunction.nextProvenanceID();

		if (args.length != 1)
		    throw new ExecutionException
			("Wrong number of arguments to @exists()");

		String filename = TypeUtil.toString(args[0]);

		DSHandle handle = new RootDataNode(Types.BOOLEAN);
		AbsFile file = new AbsFile(filename);
		logger.debug("exists: " + file);
		handle.setValue(file.exists());
		handle.closeShallow();

		try {
			if(VDL2Config.getConfig().getProvenanceLog()) {
				DSHandle[] provArgs =
				    SwiftArg.VARGS.asDSHandleArray(stack);
				for (int i = 0; i < provArgs.length; i++) {
					VDLFunction.logProvenanceParameter
					    (provid, provArgs[i], ""+i);
				}
				VDLFunction.logProvenanceResult
				    (provid, handle, "exists");
			}
		} catch (IOException ioe) {
			throw new ExecutionException
			    ("When logging provenance for exists",
			     ioe);
		}

		return handle;
	}

	public DSHandle swiftscript_strcut(VariableStack stack)
	throws ExecutionException {
		int provid = VDLFunction.nextProvenanceID();
		String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
		String pattern = TypeUtil.toString(PA_PATTERN.getValue(stack));
		if (logger.isDebugEnabled()) {
			logger.debug("strcut will match '" + inputString + "' with pattern '" + pattern + "'");
		}

		String group;
		try {
			Pattern p = Pattern.compile(pattern);
			// TODO probably should memoize this?

			Matcher m = p.matcher(inputString);
			m.find();
			group = m.group(1);
		}
		catch (IllegalStateException e) {
			throw new ExecutionException("@strcut could not match pattern " + pattern
					+ " against string " + inputString, e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("strcut matched '" + group + "'");
		}
		DSHandle handle = new RootDataNode(Types.STRING);
		handle.setValue(group);
		handle.closeShallow();
		VDLFunction.logProvenanceResult(provid, handle, "strcut");
		VDLFunction.logProvenanceParameter(provid, PA_INPUT.getRawValue(stack), "input");
		VDLFunction.logProvenanceParameter(provid, PA_PATTERN.getRawValue(stack), "pattern");
		return handle;
	}

    public DSHandle swiftscript_strstr(VariableStack stack)
    throws ExecutionException {
        String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
        String pattern = TypeUtil.toString(PA_PATTERN.getValue(stack));
        if (logger.isDebugEnabled()) {
            logger.debug("strstr will search '" + inputString +
                "' for pattern '" + pattern + "'");
        }
        int result = inputString.indexOf(pattern);
        DSHandle handle = new RootDataNode(Types.INT);
        handle.setValue(new Double(result));
        handle.closeShallow();
        return handle;
    }

	public DSHandle swiftscript_strsplit(VariableStack stack)
	throws ExecutionException, InvalidPathException {

		String str = TypeUtil.toString(PA_INPUT.getValue(stack));
		String pattern = TypeUtil.toString(PA_PATTERN.getValue(stack));

		String[] split = str.split(pattern);

		DSHandle handle = new RootArrayDataNode(Types.STRING.arrayType());
		for (int i = 0; i < split.length; i++) {
			DSHandle el = handle.getField(Path.EMPTY_PATH.addFirst(String.valueOf(i), true));
			el.setValue(split[i]);
		}
		handle.closeDeep();
		int provid=VDLFunction.nextProvenanceID();
		VDLFunction.logProvenanceResult(provid, handle, "strsplit");
		VDLFunction.logProvenanceParameter(provid, PA_INPUT.getRawValue(stack), "input");
		VDLFunction.logProvenanceParameter(provid, PA_PATTERN.getRawValue(stack), "pattern");
		return handle;
	}

	public DSHandle swiftscript_regexp(VariableStack stack)
	throws ExecutionException {
		String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
		String pattern = TypeUtil.toString(PA_PATTERN.getValue(stack));
		String transform = TypeUtil.toString(PA_TRANSFORM.getValue(stack));
		if (logger.isDebugEnabled()) {
			logger.debug("regexp will match '" + inputString + "' with pattern '" + pattern + "'");
		}

		String group;
		try {
			Pattern p = Pattern.compile(pattern);
			// TODO probably should memoize this?

			Matcher m = p.matcher(inputString);
			m.find();
			group = m.replaceFirst(transform);
		}
		catch (IllegalStateException e) {
			throw new ExecutionException("@regexp could not match pattern " + pattern
					+ " against string " + inputString, e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("regexp replacement produced '" + group + "'");
		}
		DSHandle handle = new RootDataNode(Types.STRING);
		handle.setValue(group);
		handle.closeShallow();

		int provid=VDLFunction.nextProvenanceID();
		VDLFunction.logProvenanceResult(provid, handle, "regexp");
		VDLFunction.logProvenanceParameter(provid, PA_INPUT.getRawValue(stack), "input");
		VDLFunction.logProvenanceParameter(provid, PA_PATTERN.getRawValue(stack), "pattern");
		VDLFunction.logProvenanceParameter(provid, PA_TRANSFORM.getRawValue(stack), "transform");
		return handle;
	}

	public DSHandle swiftscript_toint(VariableStack stack)
	throws ExecutionException {
		String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
		int i = inputString.indexOf(".");
		if( i >= 0 )
		{
			inputString = inputString.substring(0, i);
		}
		DSHandle handle = new RootDataNode(Types.INT);

		try
		{
		    handle.setValue(new Double(inputString));
		}
		catch(NumberFormatException e)
		{
		    throw new ExecutionException(stack, "Could not convert value \""+inputString+"\" to type int");
		}
		handle.closeShallow();

		int provid=VDLFunction.nextProvenanceID();
		VDLFunction.logProvenanceResult(provid, handle, "toint");
		VDLFunction.logProvenanceParameter(provid, PA_INPUT.getRawValue(stack), "string");
		return handle;
	}

	public DSHandle swiftscript_tofloat(VariableStack stack)
	throws ExecutionException {
		String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
		DSHandle handle = new RootDataNode(Types.FLOAT);

		try
		{
		    handle.setValue(new Double(inputString));
		}
		catch(NumberFormatException e)
		{
		    throw new ExecutionException(stack, "Could not convert value \""+inputString+"\" to type float");
		}
		handle.closeShallow();
		int provid=VDLFunction.nextProvenanceID();
		VDLFunction.logProvenanceResult(provid, handle, "tofloat");
		VDLFunction.logProvenanceParameter(provid, PA_INPUT.getRawValue(stack), "string");
		return handle;
	}

	/*
	 * Takes in a float and formats to desired precision and returns a string
	 */
	public DSHandle swiftscript_format(VariableStack stack)
	throws ExecutionException {
	    String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
	    String inputFormat = TypeUtil.toString(PA_TRANSFORM.getValue(stack));
	    DSHandle handle = new RootDataNode(Types.STRING);

	    String output = String.format("%."+inputFormat+"f", Double.parseDouble(inputString));
	    handle.setValue(output);
	    handle.closeShallow();

	    int provid=VDLFunction.nextProvenanceID();
	    VDLFunction.logProvenanceResult(provid, handle, "format");
	    VDLFunction.logProvenanceParameter(provid, PA_INPUT.getRawValue(stack), "float");
	    VDLFunction.logProvenanceParameter(provid, PA_TRANSFORM.getRawValue(stack), "float");
	    return handle;
	}

	/*
	 * Takes in an int and pads zeros to the left and returns a string
	 */
	public DSHandle swiftscript_pad(VariableStack stack)
	throws ExecutionException {
	    String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
	    String inputFormat = TypeUtil.toString(PA_TRANSFORM.getValue(stack));
	    DSHandle handle = new RootDataNode(Types.STRING);

	    int num_length = inputString.length();
	    int zeros_to_pad = Integer.parseInt(inputFormat);
	    zeros_to_pad += num_length;

	    String output = String.format("%0"+zeros_to_pad+"d",
	                                  Integer.parseInt(inputString));
	    handle.setValue(output);
	    handle.closeShallow();

	    int provid=VDLFunction.nextProvenanceID();
	    VDLFunction.logProvenanceResult(provid, handle, "pad");
	    VDLFunction.logProvenanceParameter(provid, PA_INPUT.getRawValue(stack), "int");
	    VDLFunction.logProvenanceParameter(provid, PA_TRANSFORM.getRawValue(stack), "int");
	    return handle;
	}

	public DSHandle swiftscript_tostring(VariableStack stack)
	throws ExecutionException {
	    Object input = PA_INPUT.getValue(stack);
	    DSHandle handle = new RootDataNode(Types.STRING);
	    handle.setValue(String.valueOf(input));
	    handle.closeShallow();
	    return handle;
	}

	public DSHandle swiftscript_dirname(VariableStack stack)
	throws ExecutionException {
	    AbstractDataNode n = (AbstractDataNode) PA_FILE.getRawValue(stack);
	    n.waitFor();
        String name = VDLFunction.filename(n)[0];
        String result = new AbsFile(name).getDir();
        return RootDataNode.newNode(Types.STRING, result);
	}

	/*
	 * This is copied from swiftscript_dirname.
	 * Both the functions could be changed to be more readable.
	 * Returns length of an array.
	 * Good for debugging because array needs to be closed
	 *   before the length is determined
	 */
	public DSHandle swiftscript_length(VariableStack stack)
	throws ExecutionException {
	    Map<?, ?> n = (Map<?, ?>) PA_ARRAY.getValue(stack);
	    return RootDataNode.newNode(Types.INT, Integer.valueOf(n.size()));
	}

	public DSHandle swiftscript_existsfile(VariableStack stack)
    throws ExecutionException {
	    logger.debug(stack);
	    DSHandle result = null;
	    Object[] args = SwiftArg.VARGS.asArray(stack);
	    String arg = (String) args[0];
	    AbsFile file = new AbsFile(arg);
	    boolean b = file.exists();
	    result = new RootDataNode(Types.BOOLEAN);
	    result.setValue(b);
	    result.closeShallow();

        return result;
	}
}

/*
 * Local Variables:
 *  c-basic-offset: 4
 * End:
 *
 * vim: ft=c ts=8 sts=4 sw=4 expandtab
 */
