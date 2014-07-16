//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 19, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.List;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.NamedValue;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Scope.Def;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.Pair;

public class Export extends InternalFunction {
	private ChannelRef<NamedValue> cr_export;
	private String name;
	private ArgRef<Object> value;
	
	private VarRef<String> nsprefix;
	private VarRef<Object> expv;
	

	@Override
	protected Signature getSignature() {
		return new Signature(
				params(identifier("name"), "value"),
				returns(channel("export"))
		);
	}

	@Override
	public Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
		nsprefix = scope.getVarRef(Namespace.VAR_NAME);
		
		Var.Channel export = scope.lookupChannel("export");
		if (value.isStatic()) {
			Object v = value.getValue();
			if (v instanceof NamedValue) {
				NamedValue nv = (NamedValue) v;
				v = nv.value;
			}
			
			export.append(new NamedValue(nsprefix.getValue(), name, v));
			if (v instanceof Def) {
				scope.parent.addDef(nsprefix.getValue(), name, (Def) v);
			}
			else {
				scope.parent.addVar(name, v);
			}
			return null;
		}
		else {
			export.append(new NamedValue(nsprefix.getValue(), name, null));
			return this;
		}
	}

	@Override
	protected void optimizePositional(WrapperNode w, Scope scope, List<Param> params, List<Pair<Param, String>> dynamicOptimized)
			throws CompilationException {
		// do not optimize positionals
	}
}
