//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.panels;

import java.util.ArrayList;

public class JobSpecificationPanel extends AbstractFormPanel {

	public JobSpecificationPanel() {
		super("Job Specification",HASHMODE,null,null);
		keys.add("Executable");
		keys.add("TaskArguments");
		keys.add("Directory");
		keys.add("StdOutput");
		keys.add("StdError");
		keys.add("StdInput");
		keys.add("Redirected");
		keys.add("LocalExecutable");
		keys.add("BatchJob");
	}
	
	public void load(ArrayList newKeys, Object origObject) {
		super.load(null, origObject);
	}

}
