// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class StatusEvent
{

    private Status status = null;
    private ExecutableObject source = null;

    public StatusEvent(ExecutableObject source, Status status)
    {
        this.source = source;
        this.status = status;
    }

    public void setSource(Task source)
    {
        this.source = source;
    }

    public ExecutableObject getSource()
    {
        return this.source;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public Status getStatus()
    {
        return this.status;
    }

    public String toString()
    {
        return "StatusEvent: " + source + " " + status;
    }
}
