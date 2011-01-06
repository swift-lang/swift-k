
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.gcm;

import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.apache.log4j.Logger;

/**
 * Handles command submission of GCM in a separate thread for
 * background jobs
 */
public class CommandRunner extends Thread {

	static Logger logger = Logger.getLogger(CommandRunner.class.getName());

	private GridCommandManagerImpl gcm;
	private ExecutableObject executable;


	/** constructor accepts executable object and gcm*/
	public CommandRunner(
		ExecutableObject executable,
		GridCommandManagerImpl gcm) {
		this.gcm = gcm;
		this.executable = executable;
	}

	/** run gcm submission in a separate thread */
	public void run() {
		try {
			logger.debug("Run in background");
			gcm.submit(executable);
		} catch (Exception e) {
			logger.error("Background submission failed",e);
		}
	}

}
