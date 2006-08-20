// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.interfaces;

import java.util.Collection;
import java.util.Enumeration;

import org.globus.cog.abstraction.impl.common.task.ActiveTaskException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Task;

public interface Broker extends Runnable {
    public boolean start();

    public boolean stop();

    public void submit(Task task);

    public boolean remove(Task task) throws ActiveTaskException;

    public void submit(Collection tasks);

    public void suspend(Task task)
            throws InvalidSecurityContextException, TaskSubmissionException;

    public void resume(Task task)
            throws InvalidSecurityContextException, TaskSubmissionException;

    public void cancel(Task task)
            throws InvalidSecurityContextException, TaskSubmissionException;

    public void addService(Service service);

    public void removeService(ServiceContact serviceContact);

    public Service getService(ServiceContact serviceContact);

    public boolean containsService(ServiceContact serviceContact);

    public Collection getAllServices();

    public Collection getServices(ClassAd classAd);

    public Collection getServices(String provider, int type);

    public Collection getServices(int type);

    public Collection getAllTasks();

    public Collection getTasks(Priority priority);

    public Collection getTasks(int status);

    public void setConcurrency(int concurrency);

    public int getConcurrency();

    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);

    public Enumeration getAllAttributes();
}