//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.karajan.util;

import java.awt.GraphicsEnvironment;

import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.globus.cog.gui.util.UITools;
import org.globus.tools.ProxyInit;
import org.globus.tools.proxy.GridProxyInit;

/**
 * A wrapper around proxy init tools. It tries the swing version. If that
 * failes, it reverts to the console version.
 */
public class ProxyInitWrapper implements Runnable {
	private static final Logger logger = Logger.getLogger(ProxyInitWrapper.class);
	private boolean done;
	
	public ProxyInitWrapper() {
	}
	
	public void run() {
		if (GraphicsEnvironment.isHeadless()) {
			logger.info("Invoking text mode proxy init");
			ProxyInit.main(new String[0]);
		}
		else {
			logger.info("Invoking GUI proxy init");
			GridProxyInit gpi = new GridProxyInit(null, true);
			gpi.setRunAsApplication(false);
			gpi.saveProxy(true);
			gpi.pack();
			UITools.center(null, gpi);
			gpi.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			gpi.setVisible(true);
		}
	}

	public static void main(String[] args) {
		ProxyInitWrapper wrapper = new ProxyInitWrapper();
		wrapper.run();
		logger.debug("Wrapper terminated");
	}
}
