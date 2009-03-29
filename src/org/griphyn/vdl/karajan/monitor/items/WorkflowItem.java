/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;


public class WorkflowItem extends AbstractStatefulItem {
	
	public WorkflowItem(String id) {
		super(id);
	}

	public StatefulItemClass getItemClass() {
		return StatefulItemClass.WORKFLOW;
	}
}
