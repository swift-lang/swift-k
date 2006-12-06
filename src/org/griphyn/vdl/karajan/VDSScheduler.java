/*
 * Created on Jun 12, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.ArrayList;
import java.util.List;

import org.globus.cog.karajan.scheduler.LateBindingScheduler;
import org.globus.cog.karajan.scheduler.NoFreeResourceException;
import org.globus.cog.karajan.scheduler.TaskConstraints;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.ContactSet;
import org.griphyn.cPlanner.classes.SubInfo;
import org.griphyn.cPlanner.selector.SiteSelector;
import org.griphyn.cPlanner.selector.site.RoundRobin;
import org.griphyn.common.catalog.transformation.Mapper;
import org.griphyn.common.catalog.transformation.mapper.Installed;

public class VDSScheduler extends LateBindingScheduler {
	private SiteSelector siteSelector;
	private List pools;
	private Mapper tcmapper;

	public VDSScheduler() {
		this.siteSelector = new RoundRobin();
		this.siteSelector.setTCMapper(tcmapper = new Installed());
		this.addTaskTransformer(new VDSTaskTransformer(tcmapper));
	}
    
	public void setResources(ContactSet grid) {
		super.setResources(grid);
		pools = new ArrayList();
		for (int i = 0; i < grid.size(); i++) {
			pools.add(grid.get(i).getHost());
		}
	}

	protected BoundContact getNextContact(TaskConstraints t) throws NoFreeResourceException {
		String host = siteSelector.mapJob2ExecPool(toSubInfo(t), pools);
		if (host.endsWith(":null")) {
			host = host.substring(0, host.length() - 5);
		}
		BoundContact bc = getResources().getContact(host);
		if (bc == null) {
			throw new RuntimeException("Could not find a valid site for transformation '"
					+ t.getConstraint("tr") + "'");
		}
		else {
			return bc;
		}
	}

	private SubInfo toSubInfo(TaskConstraints t) {
		SubInfo si = new SubInfo();
		if (isPresent("tr", t)) {
			String tr = (String) t.getConstraint("tr");
			si.setTransformation(null, tr, null);
			si.setJobClass(SubInfo.STAGED_COMPUTE_JOB);
		}
		return si;
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

	public void setProperty(String name, Object value) {
		if ("siteSelectorClassName".equals(name)) {
			this.siteSelector = SiteSelector.loadSiteSelector((String) value, null);
			this.siteSelector.setTCMapper(new Installed());
		}
		else {
			super.setProperty(name, value);
		}
	}
}
