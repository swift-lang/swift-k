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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.translator.KarajanTranslator;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.ChainedFailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class ExecuteFile extends AbstractSequentialWithArguments {
	public static final Logger logger = Logger.getLogger(ExecuteFile.class);
	
	public static final String NAMESPACEPREFIX = "_namespaceprefix";

	public static final Arg A_FILE = new Arg.Positional("file", 0);
	public static final Arg A_NSPREFIX = new Arg.Optional("nsprefix");
	public static final Arg A_IGNOREROOT = new Arg.Optional("ignoreRoot", Boolean.TRUE);

	public static final String ARGSDONE = "##argsdone";

	static {
		setArguments(ExecuteFile.class, new Arg[] { A_FILE, A_NSPREFIX, A_IGNOREROOT });
	}

	public void post(VariableStack stack) throws ExecutionException {
		String iname = TypeUtil.toString(A_FILE.getValue(stack));
		String nsprefix = null;
		if (A_NSPREFIX.isPresent(stack)) {
			nsprefix = TypeUtil.toString(A_NSPREFIX.getValue(stack));
		}
		boolean ignoreRoot = TypeUtil.toBoolean(A_IGNOREROOT.getValue(stack));
		ArgUtil.removeNamedArguments(stack);
		ArgUtil.removeVariableArguments(stack);
		stack.setVar(ARGSDONE, true);
		Reader reader = null;
		if (iname == null) {
			throw new ExecutionException("No  file specified");
		}
		else {
			File f = new File(iname);
			if (!f.isAbsolute()) {
				boolean found = false;
				List includeDirs = stack.getExecutionContext().getProperties().getDefaultIncludeDirs();
				Iterator i = includeDirs.iterator();
				while (i.hasNext()) {
					String dir = (String) i.next();
					if (dir.startsWith("@classpath/")) {
						try {
							String path = dir.substring("@classpath/".length());
							URL url = getClass().getClassLoader().getResource(path + iname);
							reader = new InputStreamReader(url.openStream());
							found = true;
							break;
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
								reader = new FileReader(iname);
								found = true;
								break;
							}
							catch (Exception e) {
								logger.warn("Could not read file " + iname + ": " + e.toString(), e);
							}
						}
					}
				}
				if (!found) {
					throw new ExecutionException("File not found " + iname);
				}
			}
			else {
				if (!f.exists()) {
					throw new ExecutionException("File not found " + iname);
				}
				else {
					try {
						reader = new FileReader(f.getAbsolutePath());
					}
					catch (FileNotFoundException e) {
						throw new ExecutionException("File not found " + iname);
					}
				}
			}
			if (reader != null) {
				try {
					Sequential seq = new Sequential();
					if (nsprefix != null) {
						seq.setProperty(NAMESPACEPREFIX, nsprefix);
					}
					seq.setProperty(FILENAME, iname);
					seq.setParent(this);
					stack.setVar("#seq", seq);
					File finame = new File(iname);
					File parent = finame.getParentFile();
					if ((parent != null)
							&& parent.getCanonicalPath().equals(
									stack.getExecutionContext().getBasedir())) {
						iname = finame.getName();
					}
					ElementTree tree = stack.getExecutionContext().getTree();
					if (ignoreRoot) {
						if (iname.endsWith(".xml") || iname.endsWith(".kml")) {
							XMLConverter.read(seq, tree, reader, iname);
						}
						else {
							XMLConverter.read(seq, tree,
									new KarajanTranslator(reader, iname).translate(), iname, false);
						}
					}
					else {
						XMLConverter.readWithRoot(seq, tree, reader, iname);
					}
					reader.close();
					startElement(seq, stack);
				}
				catch (Exception e) {
					logger.info("Error loading " + iname, e);
					throw new ExecutionException("Could not load file " + iname + ": "
							+ e.toString(), e);
				}
			}
			else {
				throw new ExecutionException("Could not read file " + iname);
			}
		}
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		VariableStack stack = e.getStack();
		if (!stack.currentFrame().isDefined(ARGSDONE)) {
			super.notificationEvent(e);
		}
		else {
			if (NotificationEventType.EXECUTION_COMPLETED.equals(e.getType())) {
				complete(stack);
			}
			else if (NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
				failImmediately(stack, new ChainedFailureNotificationEvent(this,
						(FailureNotificationEvent) e));
			}
			else {
				super.notificationEvent(e);
			}
		}
	}
}