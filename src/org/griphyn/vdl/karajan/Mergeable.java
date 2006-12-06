/*
 * Created on Jun 13, 2006
 */
package org.griphyn.vdl.karajan;

import org.globus.cog.karajan.workflow.futures.Future;

public interface Mergeable {
	void mergeListeners(Future f);
}
