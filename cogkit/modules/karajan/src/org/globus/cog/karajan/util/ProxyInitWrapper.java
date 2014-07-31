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
