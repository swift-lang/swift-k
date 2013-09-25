package org.globus.cog.abstraction.coaster.service.job.manager;

import org.globus.cog.abstraction.interfaces.Status;

public interface ExtendedStatusListener {
    public void statusChanged(Status s, String out, String err);
}
