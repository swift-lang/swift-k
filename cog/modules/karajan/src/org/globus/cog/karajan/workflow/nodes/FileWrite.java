//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 9, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.functions.VariableArgumentsOperator;

public class FileWrite extends PartialArgumentsContainer {
	private static final String STREAM = "##stream";

	public static final Arg A_NAME = new Arg.Positional("name", 0);
	public static final Arg A_APPEND = new Arg.Optional("append", Boolean.FALSE);

	static {
		setArguments(FileWrite.class, new Arg[] { A_NAME, A_APPEND });
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		String fileName = TypeUtil.toString(A_NAME.getValue(stack));
		File file = new File(fileName);
		try {
			final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file,
					TypeUtil.toBoolean(A_APPEND.getValue(stack))));
			stack.setVar(STREAM, os);
			super.partialArgumentsEvaluated(stack);
			ArgUtil.setVariableArguments(stack, new VariableArgumentsOperator() {
				protected Object initialValue() {
					return null;
				}

				protected Object update(Object oldvalue, Object item) {
					if (item != null) {
						try {
							os.write(TypeUtil.toString(item).getBytes());
							os.flush();
						}
						catch (IOException e) {
							throw new KarajanRuntimeException(e);
						}
					}
					return null;
				}

				public boolean isCommutative() {
					return false;
				}
			});
		}
		catch (FileNotFoundException e) {
			throw new ExecutionException(e);
		}
		startRest(stack);
	}

	protected void _finally(VariableStack stack) throws ExecutionException {
		super._finally(stack);
		OutputStream os = (OutputStream) stack.currentFrame().getVar(STREAM);
		if (os != null) {
			try {
				os.close();
			}
			catch (IOException e) {
				STDERR.ret(stack, this.toString() + ": Warning. Failed to close file.");
			}
		}
	}
}
