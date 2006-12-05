// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.translator.KarajanTranslator;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.ElementDefinition;
import org.globus.cog.karajan.util.LoadListener;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.JavaElement;

public class Include extends AbstractSequentialWithArguments implements LoadListener {
	public static final Logger logger = Logger.getLogger(Include.class);
	
	public static final Arg A_FILE = new Arg.Positional("file", 0);
	public static final Arg A_EXPORT = new Arg.Positional("export", 1);
	
	
	public static final String VTRUSTED = "#trusted";
	public static final String EXPORT = "##export";
	public static final String INCLUDED = "##included";

	static {
		setArguments(Include.class, new Arg[] { A_FILE, A_EXPORT });
	}

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		Export.DEF_CHANNEL.create(stack);
		if (hasProperty("__trusted")) {
			stack.setVar(VTRUSTED, true);
		}
	}

	protected void checkArgs(VariableStack stack) throws ExecutionException {
		if (A_FILE.isPresent(stack)) {
			includeFile(TypeUtil.toString(A_FILE.getValue(stack)), stack);
		}
		else if (elementCount() == 0) {
			throw new ExecutionException("Missing file/name argument");
		}
		if (A_EXPORT.isPresent(stack)) {
			stack.setVar(EXPORT, A_EXPORT.getValue(stack));
		}
	}

	public void post(VariableStack stack) throws ExecutionException {
		if (!stack.currentFrame().isDefined(INCLUDED)) {
			checkArgs(stack);
			stack.setVar(INCLUDED, Boolean.TRUE);
            stack.setBarrier();
			startNext(stack);
		}
		else {
			// TODO should bogus backward compatibility be kept?
			Iterator i = stack.currentFrame().names().iterator();
			LinkedList l = new LinkedList();
			while (i.hasNext()) {
				String name = (String) i.next();
				if ((name.length() > 0 && name.charAt(0) != '#')) {
					l.add(name);
				}
			}
			while (!l.isEmpty()) {
				stack.exportVar((String) l.removeFirst());
			}
			defineElements(stack);
			super.post(stack);
		}
	}

	private static final JavaElement DISALLOWED = new JavaElement(Disallowed.class);

	protected void defineElements(VariableStack stack) throws ExecutionException {
		VariableArguments vargs = Export.DEF_CHANNEL.get(stack);
		VariableArguments chain = null;
		try {
			chain = Export.DEF_CHANNEL.getReturn(stack);
		}
		catch (VariableNotFoundException e) {
			// It's ok. We don't chain definitions in that case
		}
		boolean export = false;
		if (stack.currentFrame().isDefined(EXPORT)) {
			export = chain != null;
		}
		if (stack.currentFrame().isDefined(VTRUSTED)) {
			stack.currentFrame().deleteVar(VTRUSTED);
		}
		boolean trusted;
		try {
			trusted = stack.getBooleanVar(VTRUSTED);
		}
		catch (VariableNotFoundException e) {
			trusted = true;
		}
        stack.leave();
        stack.enter();
		Iterator i = vargs.iterator();
		while (i.hasNext()) {
			ElementDefinition def = (ElementDefinition) this.checkClass(i.next(),
					ElementDefinition.class, "element definition");
			if (!trusted && def.isRestricted()) {
				DefUtil.addDef(stack, stack.parentFrame(), def.getNsprefix(), def.getName(), DISALLOWED);
			}
			else {
				DefUtil.addDef(stack, stack.parentFrame(), def.getNsprefix(), def.getName(), def.getDef());
			}
			if (export) {
				chain.append(def);
			}
		}
		DefUtil.updateEnvCache(stack, stack.parentFrame());
	}

	public synchronized void includeFile(String iname, VariableStack stack)
			throws ExecutionException {
		if (hasProperty("_processed")) {
			return;
		}
		setProperty("_processed", true);
		A_FILE.setStatic(this, iname);
		Reader reader = null;
		if (iname == null) {
			throw new ExecutionException("No include file specified");
		}
		else {
			File f = new File(iname);
			if (!f.isAbsolute()) {
				boolean found = false;
				List includeDirs = stack.getExecutionContext().getProperties().getDefaultIncludeDirs();
				Iterator i = includeDirs.iterator();
				while (i.hasNext()) {
					String dir = (String) i.next();
					if (dir.equals(".")) {
						/*
						 * "." means current directory relative to the current
						 * file being executed. For example, if sys.xml is in
						 * the class path and something is included from
						 * sys.xml, then "." should also refer to the class
						 * path.
						 */
						dir = (String) getTreeProperty("_path", getParent());
						if (dir == null) {
							dir = stack.getExecutionContext().getBasedir();
						}
					}
					if (dir.startsWith("@classpath/")) {
						try {
							if (iname.indexOf("..") != -1) {
								continue;
								/*
								 * Although getResource() seems to not resolve
								 * those, which is good, but I'm not betting on
								 * it
								 */
							}
							String path = dir.substring("@classpath/".length());
							URL url = getClass().getClassLoader().getResource(path + iname);
							if (url != null) {
								// nested includes are allowed to use
								// unrestricted mode
								setProperty("__trusted", true);
								stack.setVar(VTRUSTED, true);
								if (logger.isDebugEnabled()) {
									logger.debug(iname + " included from classpath");
								}
								reader = new InputStreamReader(url.openStream());
								found = true;
								setProperty("__no_child_serialization", true);
								Set imports;
								if (stack.isDefined("#imports")) {
									imports = new HashSet((Set) stack.getVar("#imports"));
								}
								else {
									imports = new HashSet();
								}
								imports.add(iname);
								stack.setVar("#path", "@classpath");
								stack.parentFrame().setVar("#imports", imports);
								break;
							}
						}
						catch (Exception e) {
							logger.debug(iname + " not found in classpath", e);
						}
					}
					else {
						File test = new File(dir, iname);
						if (test.exists()) {
							try {
								iname = dir + File.separator + iname;
								if (logger.isDebugEnabled()) {
									logger.debug(iname + " included from " + dir);
								}
								reader = new FileReader(iname);
								setProperty("_path", dir);
								found = true;
								break;
							}
							catch (Exception e) {
								logger.warn("Could not read file " + iname + ": " + e.getMessage(),
										e);
							}
						}
					}
				}
				if (!found) {
					throw new ExecutionException("File not found " + iname);
				}
			}
			else {
				try {
					reader = new FileReader(iname);
				}
				catch (FileNotFoundException e) {
					throw new ExecutionException("Error reading file: " + iname, e);
				}
			}
			if (reader != null) {
				try {
					File finame = new File(iname);
					File parent = finame.getParentFile();
					if ((parent != null)
							&& parent.getCanonicalPath().equals(
									stack.getExecutionContext().getBasedir())) {
						iname = finame.getName();
					}
					setProperty(FILENAME, iname);
					ElementTree tree = stack.getExecutionContext().getTree();
					if (iname.endsWith(".xml") || iname.endsWith(".kml")) {
						XMLConverter.read(this, tree, reader, iname);
					}
					else {
						XMLConverter.read(this, tree,
								new KarajanTranslator(reader, iname).translate(), iname, false);
					}
					reader.close();
				}
				catch (Exception e) {
					logger.debug("Exception caught while reading file " + iname, e);
					throw new ExecutionException("Could not read file " + iname + ": "
							+ e.getMessage() + "\n\t" + this, e);
				}
			}
			else {
				throw new ExecutionException("Internal error. No reader set");
			}
		}
	}

	public void loadStarted() {
	}

	/*public String toString() {
		String tmp = getElementType();
		if (hasProperty(ANNOTATION)) {
			tmp = tmp + " (" + getStringProperty(ANNOTATION) + ") ";
		}
		if (this.getParent() != null) {
			Object fileName = getTreeProperty(FILENAME, this.getParent());
			if (fileName instanceof String) {
				tmp = tmp + " @ " + fileName;
			}
		}
		if (hasProperty(LINE)) {
			tmp = tmp + ", line: " + getProperty(LINE);
		}
		return tmp;
	}*/
}