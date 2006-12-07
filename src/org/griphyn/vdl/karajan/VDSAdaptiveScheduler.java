//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 12, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.scheduler.AbstractScheduler;
import org.globus.cog.karajan.scheduler.ResourceConstraintChecker;
import org.globus.cog.karajan.scheduler.TaskConstraints;
import org.globus.cog.karajan.scheduler.WeightedHostScoreScheduler;
import org.globus.cog.karajan.util.BoundContact;
import org.griphyn.common.catalog.TransformationCatalog;
import org.griphyn.common.catalog.transformation.File;
import org.griphyn.common.classes.TCType;
import org.griphyn.vdl.util.FQN;

public class VDSAdaptiveScheduler extends WeightedHostScoreScheduler {
	public static final Logger logger = Logger.getLogger(VDSAdaptiveScheduler.class);
	
	private TransformationCatalog tc;

	public VDSAdaptiveScheduler() {
	}
	
	public static final String PROP_TC_FILE = "transformationCatalogFile";
	
	private static String[] propertyNames;
	
	public synchronized String[] getPropertyNames() {
		if (propertyNames == null) {
			propertyNames = AbstractScheduler.combineNames(super.getPropertyNames(),
					new String[] {PROP_TC_FILE});
		}
		return propertyNames;
	}

	public void setProperty(String name, Object value) {
		if (PROP_TC_FILE.equals(name)) {
			tc = File.getNonSingletonInstance((String) value);
			this.setConstraintChecker(new TCChecker(tc));
			this.addTaskTransformer(new VDSTaskTransformer(tc));
		}
		else {
			super.setProperty(name, value);
		}
	}

	public static class TCChecker implements ResourceConstraintChecker {
		private TransformationCatalog tc;

		public TCChecker(TransformationCatalog tc) {
			this.tc = tc;
		}

		public boolean checkConstraints(BoundContact resource, TaskConstraints tc) {
			if (isPresent("trfqn", tc)) {
				FQN tr = (FQN) tc.getConstraint("trfqn");
				try {
					List l = this.tc.getTCEntries(tr.getNamespace(), tr.getName(), tr.getVersion(),
							resource.getHost(), TCType.INSTALLED);
					if (l == null || l.isEmpty()) {
						return false;
					}
					else {
						return true;
					}
				}
				catch (Exception e) {
					logger.debug("Exception caught while querying TC", e);
					return false;
				}
			}
			else {
				return true;
			}
		}

		private boolean isPresent(String constraint, TaskConstraints t) {
			if (t == null) {
				return false;
			}
			if (t.getConstraint(constraint) == null) {
				return false;
			}
			return true;
		}

		public List checkConstraints(List resources, TaskConstraints tc) {
			LinkedList l = new LinkedList();
			Iterator i = resources.iterator();
			while (i.hasNext()) {
				BoundContact res = (BoundContact) i.next();
				if (checkConstraints(res, tc)) {
					l.add(res);
				}
			}
			return l;
		}
	}
}
