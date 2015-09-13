/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 5, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.io.File;

import k.rt.Context;
import k.rt.Null;

import org.globus.cog.karajan.compiled.nodes.DefNode;
import org.globus.cog.karajan.compiled.nodes.Export;
import org.globus.cog.karajan.compiled.nodes.Import;
import org.globus.cog.karajan.compiled.nodes.Main;
import org.globus.cog.karajan.compiled.nodes.Namespace;
import org.globus.cog.karajan.compiled.nodes.Sequential;
import org.globus.cog.karajan.compiled.nodes.SetVarK;
import org.globus.cog.karajan.compiled.nodes.TailCall;
import org.globus.cog.karajan.compiled.nodes.functions.ChannelArg;
import org.globus.cog.karajan.compiled.nodes.functions.GetAndDeleteVariables;
import org.globus.cog.karajan.compiled.nodes.functions.List;
import org.globus.cog.karajan.compiled.nodes.functions.Named;
import org.globus.cog.karajan.compiled.nodes.functions.NumericValue;
import org.globus.cog.karajan.compiled.nodes.functions.Str;
import org.globus.cog.karajan.compiled.nodes.functions.Variable;
import org.globus.cog.karajan.compiled.nodes.user.Function;
import org.globus.cog.karajan.compiled.nodes.user.Lambda;
import org.globus.cog.karajan.util.KarajanProperties;

public class RootScope extends ContainerScope {
	private Scope importScope;
	
	public RootScope(KarajanProperties props, String file, Context context) {
		
		addDef("k", "main", new JavaDef(Main.class));
		addDef("k", "import", new JavaDef(Import.class));
		addDef("k", "def", new JavaDef(DefNode.class));
		addDef("k", "namespace", new JavaDef(Namespace.class));
		addDef("k", "export", new JavaDef(Export.class));
		addDef("k", "sequential", new JavaDef(Sequential.class));
		addDef("k", "block", new JavaDef(Sequential.class));
		addDef("k", "num", new JavaDef(NumericValue.class));
		addDef("k", "str", new JavaDef(org.globus.cog.karajan.compiled.nodes.functions.StringValue.class));
		addDef("k", "assign", new JavaDef(SetVarK.class));
		addDef("k", "named", new JavaDef(Named.class));
		addDef("k", "var", new JavaDef(Variable.class));
		addDef("k", "getAndDelete", new JavaDef(GetAndDeleteVariables.class));
		addDef("k", "slist", new JavaDef(List.Cons.class));
		addDef("k", "function", new JavaDef(Function.class));
		addDef("k", "lambda", new JavaDef(Lambda.class));
		addDef("k", "channel", new JavaDef(ChannelArg.class));
		addDef("k", "concat", new JavaDef(Str.Concat.class));
		addDef("k", "tailcall", new JavaDef(TailCall.class));
		
		addVar("#context", context);
		addVar("#properties", props);
		addVar("#filedir", new File(file).getAbsoluteFile().getParent());
		addVar("#filename", file);
		addVar("#namespaceprefix", "");
		addVar("true", Boolean.TRUE);
		addVar("false", Boolean.FALSE);
		addVar("null", Null.VALUE);
		addVar("user.name", System.getProperty("user.name"));
		addVar("user.home", System.getProperty("user.home"));
		addVar("CWD", new File(".").getAbsolutePath());
	}

	public Scope getImportScope() {
		return importScope;
	}

	public void setImportScope(Scope importScope) {
		this.importScope = importScope;
	}

	@Override
	public RootScope getRoot() {
		return this;
	}
}
