// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
package org.griphyn.vdl.karajan.lib;

// TODO lose these imports

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.KarajanIterator;
import org.globus.cog.karajan.util.Property;
import org.globus.cog.karajan.util.RangeIterator;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;

import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;

public class Misc extends FunctionsCollection {

	private static final Logger logger = Logger.getLogger(FunctionsCollection.class);

// TODO note that using concat here caused namespace conflict errors
// that suggest that either my understanding of how these names are
// registered is wrong or that there is something not working as
// i expect in karajan - investigate that (with mihael's help...)

	static {
		setArguments("vdl_strcat", new Arg[] { Arg.VARGS });
	}

	public String vdl_strcat(VariableStack stack) throws ExecutionException {
		Object[] args = Arg.VARGS.asArray(stack);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			buf.append(TypeUtil.toString(args[i]));
		}
		return buf.toString();
	}

}
