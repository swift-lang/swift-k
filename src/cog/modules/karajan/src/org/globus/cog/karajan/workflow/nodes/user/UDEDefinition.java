//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 8, 2006
 */
package org.globus.cog.karajan.workflow.nodes.user;

import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.FlowElementWrapper;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.Info;

/**
 * This class captures both a lambda and the environment in which it
 * was defined
 */
public class UDEDefinition {
	private final DefinitionEnvironment env;
	private FlowElement ude;
	
	public UDEDefinition(FlowElement ude, DefinitionEnvironment env) {
		this.ude = ude;
		this.env = env;
	}

	public DefinitionEnvironment getEnv() {
		return env;
	}

	public synchronized UserDefinedElement getUde() throws KarajanRuntimeException {
		if (ude instanceof FlowElementWrapper) {
			FlowElementWrapper few = (FlowElementWrapper) ude;
			try {
				few.resolve(env.getStack());
			}
			catch (ExecutionException e) {
				throw new KarajanRuntimeException(e);
			}
			ude = few.getPeer();
		}
		return (UserDefinedElement) ude;
	}
	
	/**
	 * Does not resolve the definition if it's a wrapper
	 */
	public FlowElement getUdeNR() {
		return ude;
	}
	
	public String toString() {
		return Info.ppDef("lambda", ude);
	}
}
