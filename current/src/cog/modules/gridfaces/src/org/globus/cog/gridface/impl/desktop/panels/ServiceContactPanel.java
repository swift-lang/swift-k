//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.panels;

import java.util.ArrayList;

public class ServiceContactPanel extends AbstractFormPanel {

	public ServiceContactPanel() {
		super("Service Contact",HASHMODE,null,null);
		keys.add("Provider");
		keys.add("ServiceContactName");
		keys.add("ServiceContactPort");
	}
	public void load(ArrayList newKeys, Object origObject) {
		super.load(null, origObject);
	}
}



