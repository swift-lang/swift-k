/*
 * Created on Dec 23, 2006
 */
package org.griphyn.vdl.karajan;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;

public class VDL2ExecutionContext extends ExecutionContext {
	public static final String RUN_ID = "vdl:runid";
	public static final String SCRIPT_NAME = "vdl:scriptname";

	private final String runID;
	private final String scriptName;

	public VDL2ExecutionContext(ElementTree tree, String project) {
		super(tree);
		runID = getUUID();
		scriptName = projectName(project);
		tree.setName(scriptName + "-" + runID);
	}

	protected void printFailure(FailureNotificationEvent e) {
		String msg = e.getMessage();
		if (!"Execution completed with errors".equals(msg)) {
			if (msg == null && e.getException() != null) {
				msg = getMeaningfulMessage(e.getException());
			}
			getStderr().append("Execution failed:\n\t");
			String translation = VDL2ErrorTranslator.getDefault().translate(msg);
			if (translation != null) {
				getStderr().append(translation);
			}
			else {
				getStderr().append(e.getException().toString());
			}
			getStderr().append("\n");
		}
		else {
			// lazy errors are on and they have already been printed
		}
	}

	protected void setGlobals(VariableStack stack) {
		super.setGlobals(stack);
		stack.setGlobal(RUN_ID, runID);
		stack.setGlobal(SCRIPT_NAME, scriptName);
	}

	private static long lastTime = 0;

	protected static synchronized String getUUID() {
		long l;
		// 40 lsbits (should cover about 20 years)
		while (true) {
			l = System.currentTimeMillis() & 0x000000ffffffffffl;
			if (l != lastTime) {
				lastTime = l;
				break;
			}
			else {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
			}
		}
		// and for the msbs, some random stuff
		int rnd;
		try {
			SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
			rnd = prng.nextInt();
		}
		catch (NoSuchAlgorithmException e) {
			rnd = (int) (Math.random() * 0xffffff);
		}
		rnd &= 0x007fffff;
		l += ((long) rnd) << 40;
		return alphanum(l);
	}

	public static final String codes = "0123456789abcdefghijklmnopqrstuvxyz";

	protected static String alphanum(long val) {
		StringBuffer sb = new StringBuffer(); 
		int base = codes.length();
		for (int i = 0; i < 13; i++) {
			int c = (int) (val % base);
			sb.append(codes.charAt(c));
			val = val / base;
		}
		return sb.toString();
	}

	public String getRunID() {
		return runID;
	}

	protected String projectName(String project) {
		project = project.substring(project.lastIndexOf(File.separatorChar) + 1);
		return project.substring(0, project.lastIndexOf('.'));
	}
}
