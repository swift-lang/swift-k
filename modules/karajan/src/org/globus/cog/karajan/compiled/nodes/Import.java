// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import k.rt.Context;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.NamedValue;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.RootScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Scope.Def;
import org.globus.cog.karajan.analyzer.Scope.JavaDef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.NativeParser;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.KarajanProperties;
import org.globus.cog.karajan.util.Pair;

public class Import extends InternalFunction {
	public static final Logger logger = Logger.getLogger(Import.class);
	
	private static Map<String, IncludedMain> compiled = new HashMap<String, IncludedMain>();
	
	private abstract static class Resolved {
		abstract Reader openReader() throws IOException;
		abstract String getKey();
		abstract String getDir();
	}
	
	private static class ResolvedFile extends Resolved {
		private File f;
		
		public ResolvedFile(File f) {
			this.f = f;
		}

		@Override
		Reader openReader() throws IOException {
			return new FileReader(f);
		}

		@Override
		String getKey() {
			return f.getAbsolutePath();
		}

		@Override
		String getDir() {
			return f.getAbsoluteFile().getParent();
		}
	}
	
	private static class ResolvedResource extends Resolved {
		private URL url;
		
		public ResolvedResource(URL url) {
			this.url = url;
		}

		@Override
		Reader openReader() throws IOException {
			return new InputStreamReader(url.openStream());
		}

		@Override
		String getKey() {
			return url.toString();
		}

		@Override
		String getDir() {
			return "";
		}
	}
	
	private IncludedMain m;
	
	private ChannelRef<NamedValue> cr_export;
    private ChannelRef<NamedValue> c_export;
    private String name;
    private ArgRef<Boolean> export;
    private ArgRef<String> file;
    
    private VarRef<KarajanProperties> props;
    private VarRef<String> fileDir;
    private VarRef<Context> context;
    
	
	@Override
	protected Signature getSignature() {
		return new Signature(
				params(identifier("name"), optional("file", null), optional("export", Boolean.FALSE), channel("export")),
				returns(channel("export"))
		);
	}

	protected void processIdentifierArgs(WrapperNode w, Signature sig) throws CompilationException {
		Iterator<Param> i = sig.getParams().iterator();
		boolean found = false;
		while (i.hasNext()) {
			Param p = i.next();
			if (p.type == Param.Type.IDENTIFIER) {
				if (found) {
					throw new CompilationException(w, "Only one identifier parameter allowed");
				}
				String suffix;
				WrapperNode in = w.getNode(0);
				if ("k:var".equals(in.getNodeType())) {
					name = in.getText() + ".k";
					w.removeNode(in);
					i.remove();
					return;
				}
			}
		}
	}
	
	@Override
	protected void scanNamed(WrapperNode w, Scope scope, List<Param> params, List<Pair<Param, String>> dynamicOptimized)
			throws CompilationException {
		// override to allow imports of non-identifier but static names
	}

	@Override
	protected void optimizePositional(WrapperNode w, Scope scope, List<Param> params, List<Pair<Param, String>> dynamicOptimized)
			throws CompilationException {
		// see above
	}

	@Override
	protected void scanNotSet(WrapperNode w, Scope scope, List<Param> optional)
			throws CompilationException {
	}

	@Override
	public Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
		if (name == null && file.getValue() == null) {
			throw new CompilationException(w, "Could not statically determine file name");
		}
		if (name != null && file.getValue() != null) {
			throw new CompilationException(w, "Invalid arguments");
		}
		if (name == null && file.getValue() != null) {
			name = file.getValue();
		}
		props = scope.getVarRef("#properties");
		fileDir = scope.getVarRef("#filedir");
		context = scope.getVarRef("#context");
		
		Resolved r;
		try {
			r = resolve(name, props.getValue(), fileDir.getValue());
		}
		catch (IOException ee) {
			throw new CompilationException(w, "Import of '" + name + "' failed", ee);
		}
		Boolean e = export.getValue();
		if (e == null) {
			e = Boolean.FALSE;
		}
		try {
			m = importFile(r, props.getValue(), argScope);
		}
		catch (Exception ee) {
			throw new CompilationException(w, "Import of '" + name + "' failed", ee);
		}
		catch (Error ee) {
			throw new CompilationException(w, "Import of '" + name + "' failed", ee);
		}
		
		Var.Channel eret = scope.parent.lookupChannel("export");
		List<Object> values = argScope.lookupChannel("export").getAll();
		for (Object value : values) {
			if (value instanceof NamedValue) {
				NamedValue qn = (NamedValue) value;
				if (qn.value instanceof Scope.Def) {
					//System.out.println(w + ": def - " + qn.name);
					scope.parent.addDef(qn.ns, qn.name, (Def) qn.value);
				}
				else {
					//System.out.println(w + ": " + qn.name + " -> " + qn.value);
					scope.parent.addVar(qn.name, qn.value);
				}
				if (e) {
					eret.append(qn);
				}
			}
		}
		return this;
	}

	private Resolved resolve(String fn, KarajanProperties props, String pdir) throws IOException {
		File f = new File(fn);
		if (!f.isAbsolute()) {
			boolean found = false;
			List<String> includeDirs = props.getDefaultIncludeDirs();
			for (String dir : includeDirs) {
				if (dir.equals(".")) {
					/*
					 * "." means current directory relative to the current
					 * file being executed. For example, if sys.xml is in
					 * the class path and something is included from
					 * sys.xml, then "." should also refer to the class
					 * path.
					 */
					dir = pdir;
				}
				if (dir.startsWith("@classpath/")) {
					try {
						String path = dir.substring("@classpath/".length());
						URL url = getClass().getClassLoader().getResource(path + fn);
						if (url != null) {
							// nested includes are allowed to use
							// unrestricted mode
							if (logger.isDebugEnabled()) {
								logger.debug(fn + " included from classpath");
							}
							return new ResolvedResource(url);
						}
					}
					catch (Exception e) {
						logger.debug(fn + " not found in classpath", e);
					}
				}
				else {
					File test = new File(dir, fn);
					if (test.exists()) {
						try {
							return new ResolvedFile(test);
						}
						catch (Exception e) {
							throw new IOException("Could not read file '" + fn + "': " + e.getMessage(),
									e);
						}
					}
				}
			}
			
			if (!found) {
				throw new IOException("File not found: '" + fn + "'");
			}
		}
		else {
			if (f.exists()) {
				return new ResolvedFile(f);
			}
			else {
				throw new IOException("File not found: '" + fn + "'");
			}
		}
		throw new IOException("File not found: '" + fn + "'");
	}

	@Override
	protected void runBody(LWThread thr) {
		if (m == null) {
			return;
		}
		m.run(thr);
	}
		
	private IncludedMain importFile(Resolved r, KarajanProperties props, Scope scope) 
	throws ParsingException, IOException, CompilationException {
		IncludedMain m;
		String key = r.getKey();
		synchronized(compiled) {
			m = compiled.get(key);
			if (m != null) {
				loadExports(m, scope);
				return m;
			}
		}
		m = actualImport(r, props, scope);
		if (CompilerSettings.DUMP_COMPILED_TREE) {
			m.dump(new File(new File(key + ".compiled").getName()));
		}
		storeExports(m, scope);
		synchronized(compiled) {
			compiled.put(key, m);
		}
		return m;
	}

	private void loadExports(IncludedMain m, Scope scope) {
		scope.lookupChannel("export").getChannel().appendAll(m.getExports());
	}

	private void storeExports(IncludedMain m, Scope scope) {
		m.setExports(scope.lookupChannel("export").getAll());
	}

	private IncludedMain actualImport(Resolved r, KarajanProperties props, Scope scope) 
	throws ParsingException, IOException, CompilationException {
		WrapperNode wn = new NativeParser(r.getKey(), r.openReader()).parse();
		wn.setProperty(WrapperNode.FILENAME, r.getKey());
		RootScope rs = new RootScope(props, r.getDir(), (Context) scope.lookup("#context").getValue());
		rs.setImportScope(scope);
		rs.addDef("k", "main", new JavaDef(IncludedMain.class));
		return (IncludedMain) wn.compile(null, rs);
	}
}