/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.functions.ConfigProperty;
import org.griphyn.vdl.util.TriStateBoolean;
import org.griphyn.vdl.util.VDL2ConfigProperties;

public class Kickstart extends VDLFunction {
	public static final String PROPERTY_GRIDLAUNCH = "gridlaunch";

	public static final Arg A_HOST = new Arg.Positional("host");

	static {
		setArguments(Kickstart.class, new Arg[] { A_HOST });
	}

	public static final String NOTHING = "";

	public Object function(VariableStack stack) throws ExecutionException {
		String enabled = ConfigProperty.getProperty(VDL2ConfigProperties.KICKSTART_ENABLED, stack);
		TriStateBoolean tbs = TriStateBoolean.valueOf(enabled);
		if (tbs.equals(TriStateBoolean.FALSE)) {
			return NOTHING;
		}
		else {
			BoundContact host = (BoundContact) A_HOST.getValue(stack);
			String kickstart = (String) host.getProperty(PROPERTY_GRIDLAUNCH);
			if (kickstart == null) {
				if (tbs.equals(TriStateBoolean.MAYBE)) {
					return NOTHING;
				}
				else {
					throw new ExecutionException(
							"The \"kickstart.enable\" option is set to \"true\" but Kickstart is not installed on "
									+ host);
				}
			}
			else {
				return kickstart;
			}
		}
	}
}
