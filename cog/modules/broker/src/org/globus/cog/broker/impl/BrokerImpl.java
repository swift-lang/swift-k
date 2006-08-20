// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ActiveTaskException;
import org.globus.cog.abstraction.impl.common.task.GenericTaskHandler;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.broker.interfaces.Broker;
import org.globus.cog.broker.interfaces.ClassAd;
import org.globus.cog.broker.interfaces.Dispatcher;
import org.globus.cog.broker.interfaces.TaskToServiceMapper;
import org.globus.cog.broker.interfaces.Priority;
import org.globus.cog.broker.interfaces.Queue;
import org.globus.cog.broker.interfaces.ServiceManager;

public class BrokerImpl implements Broker, StatusListener {
    /*
     * Access to all objects in the broker must be synchronized There are two
     * threads in operation, the main thread (producer) and the BrokerThread
     * (consumer)
     */
    static Logger logger = Logger.getLogger(BrokerImpl.class.getName());
    private Queue queue = null;
    private ServiceManager serviceManager = null;
    private Dispatcher dispatcher = null;
    private TaskToServiceMapper matchMaker = null;
    private boolean updatedEnvironment = true;
    private boolean threadAlive = false;
    private Hashtable attributes = null;
    private GenericTaskHandler handler = null;
    private Thread schedulerThread = null;
    private int concurrency = 10;
    private int count = 0;

    private Object environmentLock = new Object();
    private Object threadAliveLock = new Object();

    public BrokerImpl() {
        this.queue = new QueueImpl();
        this.serviceManager = new ServiceManagerImpl();
        this.dispatcher = new DefaultDispatcher(this.queue);
        this.matchMaker = new DefaultTaskToServiceMapper(this.serviceManager);
        this.attributes = new Hashtable();
        this.handler = new GenericTaskHandler();
    }

    public BrokerImpl(GenericTaskHandler handler) {
        this.queue = new QueueImpl();
        this.serviceManager = new ServiceManagerImpl();
        this.dispatcher = new DefaultDispatcher(this.queue);
        this.matchMaker = new DefaultTaskToServiceMapper(this.serviceManager);
        this.attributes = new Hashtable();
        this.handler = handler;
    }

    public BrokerImpl(Queue queue, ServiceManager serviceManager,
            Dispatcher dispatcher, TaskToServiceMapper matchMaker) {
        this.queue = queue;
        this.serviceManager = serviceManager;
        this.dispatcher = dispatcher;
        this.matchMaker = matchMaker;
        this.attributes = new Hashtable();
        this.handler = new GenericTaskHandler();
    }

    public BrokerImpl(Queue queue, ServiceManager serviceManager,
            Dispatcher dispatcher, TaskToServiceMapper matchMaker,
            GenericTaskHandler handler) {
        this.queue = queue;
        this.serviceManager = serviceManager;
        this.dispatcher = dispatcher;
        this.matchMaker = matchMaker;
        this.attributes = new Hashtable();
        this.handler = handler;
    }

    public void submit(Task task) {
        task.setAttribute("isBrokered", "false");
        synchronized (this.queue) {
            this.queue.add(task);
        }
        synchronized (this.environmentLock) {
            this.updatedEnvironment = true;
        }
        synchronized (this) {
            this.notifyAll();
        }

    }

    public void submit(Collection taskList) {
        Iterator iterator = taskList.iterator();

        while (iterator.hasNext()) {
            Task task = (Task) iterator.next();
            task.setAttribute("isBrokered", "false");
        }
        synchronized (this.queue) {
            this.queue.addAll(taskList);
        }
        synchronized (this.environmentLock) {
            this.updatedEnvironment = true;
        }
        synchronized (this) {
            this.notifyAll();
        }
    }

    public boolean remove(Task task) throws ActiveTaskException {
        synchronized (this.handler) {
            this.handler.remove(task);
        }
        return this.queue.remove(task);
    }

    public void suspend(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        synchronized (this.handler) {
            this.handler.suspend(task);
        }
    }

    public void resume(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        synchronized (this.handler) {
            this.handler.resume(task);
        }
    }

    public void cancel(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        synchronized (this.handler) {
            this.handler.cancel(task);
        }
    }

    public void addService(Service service) {
        synchronized (this.serviceManager) {
            this.serviceManager.addService(service);
        }
        synchronized (this.environmentLock) {
            this.updatedEnvironment = true;
        }
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void removeService(ServiceContact serviceContact) {
        synchronized (this.serviceManager) {
            this.serviceManager.removeService(serviceContact);
        }
    }

    public Service getService(ServiceContact serviceContact) {
        synchronized (this.serviceManager) {
            return this.serviceManager.getService(serviceContact);
        }
    }

    public boolean containsService(ServiceContact serviceContact) {
        synchronized (this.serviceManager) {
            return this.serviceManager.containsService(serviceContact);
        }
    }

    public Collection getAllServices() {
        synchronized (this.serviceManager) {
            return this.serviceManager.getAllServices();
        }
    }

    public Collection getServices(ClassAd classAd) {

        synchronized (this.serviceManager) {
            return this.serviceManager.getServices(classAd);
        }
    }

    public Collection getServices(String provider, int type) {
        synchronized (this.serviceManager) {
            return this.serviceManager.getServices(provider, type);
        }
    }

    public Collection getServices(int type) {
        synchronized (this.serviceManager) {
            return this.serviceManager.getServices(type);
        }
    }

    public Collection getBrokeredTasks() {
        List list = new ArrayList();
        Iterator iterator;
        synchronized (this.queue) {
            iterator = this.queue.iterator();
        }
        while (iterator.hasNext()) {
            Task task = (Task) iterator.next();
            synchronized (task) {
                if (((String) task.getAttribute("isBrokered"))
                        .equalsIgnoreCase("true")) {
                    list.add(task);
                }
            }
        }
        return list;
    }

    public Collection getAllTasks() {
        synchronized (this.handler) {
            return this.handler.getAllTasks();
        }
    }

    public Collection getTasks(Priority priority) {
        Iterator iterator;
        synchronized (this.handler) {
            iterator = this.handler.getAllTasks().iterator();
        }
        Vector vector = new Vector();
        while (iterator.hasNext()) {
            Task task = (Task) iterator.next();
            if (((Priority) task.getAttribute("priority")).getValue() == priority
                    .getValue()) {
                vector.add(task);
            }
        }
        return vector;
    }

    public Collection getTasks(int status) {
        switch (status) {
        case Status.ACTIVE:
            synchronized (this.handler) {
                return this.handler.getActiveTasks();
            }
        case Status.CANCELED:
            synchronized (this.handler) {
                return this.handler.getCanceledTasks();
            }
        case Status.COMPLETED:
            synchronized (this.handler) {
                return this.handler.getCompletedTasks();
            }
        case Status.FAILED:
            synchronized (this.handler) {
                return this.handler.getFailedTasks();
            }
        case Status.RESUMED:
            synchronized (this.handler) {
                return this.handler.getResumedTasks();
            }
        case Status.SUSPENDED:
            synchronized (this.handler) {
                return this.handler.getSuspendedTasks();
            }
        default:
            return null;
        }
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public int getConcurrency() {

        return this.concurrency;
    }

    public void setAttribute(String name, Object value) {
        synchronized (this.attributes) {
            this.attributes.put(name, value);
        }
    }

    public Object getAttribute(String name) {
        synchronized (this.attributes) {
            return this.attributes.get(name);
        }
    }

    public Enumeration getAllAttributes() {
        synchronized (this.attributes) {
            return this.attributes.elements();
        }
    }

    public void run() {
        while (true) {
            synchronized (this.threadAliveLock) {
                if (!this.threadAlive) {
                    // stop this thread gracefully by exitting the run method
                    break;
                }
            }

            // if reached the concurrency then wait
            boolean maxConcurrency;
            synchronized (this.environmentLock) {
                maxConcurrency = (count == concurrency);
            }
            if (maxConcurrency) {
                try {
                    synchronized (this) {
                        logger
                                .debug("Reached maximum concurrency. BrokerThread waiting ...");
                        this.wait();
                        logger.debug("BrokerThread resumed ...");
                        continue;
                    }
                } catch (InterruptedException e) {
                    // do nothing
                }
            }

            boolean updated;
            synchronized (this.environmentLock) {
                updated = this.updatedEnvironment;
                if (updated) {
                    this.updatedEnvironment = false;
                }
            }
            Task task = null;
            // if the environnment is updated
            //i.e new tasks or services are available
            if (updated) {
                synchronized (this.dispatcher) {
                    this.dispatcher.reset();
                    task = this.dispatcher.getNext();
                }
            }
            // if environment is not updated
            else {
                synchronized (this.dispatcher) {
                    // get next task
                    task = this.dispatcher.getNext();
                }
            }

            // if no task is available, wait
            if (task == null) {
                try {
                    synchronized (this) {
                        logger
                                .debug("No task available. BrokerThread waiting ...");
                        this.wait();
                        logger.debug("BrokerThread resumed ...");
                    }
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            // if task is available then schedule the task
            else {
                execute(task);
            }
        }
    }

    private void execute(Task task) {

        logger.debug("Dispatched task " + task.getName());
        boolean matched = false;
        // match the task with the best service
        synchronized (this.matchMaker) {
            matched = this.matchMaker.match(task);
        }

        // if a desired service for the task is found, execute the task
        if (matched) {
            logger.debug("Matched task " + task.getName() + " to service "
                    + task.getService(0).getName());
            task.addStatusListener(this);
            task.setAttribute("isBrokered", "true");
            task.setProvider(task.getService(0).getProvider());
            synchronized (this.environmentLock) {
                this.count++;
            }
            try {
                synchronized (this.handler) {
                    this.handler.submit(task);
                }
            } catch (Exception e) {
                Status status = new StatusImpl();
                status.setPrevStatusCode(task.getStatus().getStatusCode());
                status.setStatusCode(Status.FAILED);
                status.setException(e);
                task.setStatus(status);
            }
        }
    }

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();

		logger.debug("Status changed by the listener to" + status.getStatusString());
		
        switch (status.getStatusCode()) {

        case Status.COMPLETED:
            synchronized (this.environmentLock) {
                this.updatedEnvironment = true;
                this.count--;
            }
			 synchronized (this) {
		            this.notifyAll();
		        }
            break;

        case Status.CANCELED:
            synchronized (this.environmentLock) {
                this.updatedEnvironment = true;
                this.count--;
            }
			 synchronized (this) {
		            this.notifyAll();
		        }
            break;

        case Status.FAILED:
            synchronized (this.environmentLock) {
                this.updatedEnvironment = true;
                this.count--;
            }
			 synchronized (this) {
		            this.notifyAll();
		        }
            break;

        default:
            break;
        }
		
		
    }

    public boolean start() {
        synchronized (this.threadAliveLock) {
            if (this.threadAlive) {
                // scheduler is already started
                logger.debug("Broker has already started");
                return false;
            } else {
                this.threadAlive = true;
                this.schedulerThread = new Thread(this, "BrokerThread");
                this.schedulerThread.start();
                logger.debug("Started BrokerThread");
                return true;
            }
        }
    }

    public boolean stop() {
        synchronized (this.threadAliveLock) {
            if (!this.threadAlive) {
                // scheduler is already stopped
              logger.debug("Broker has already stopped");
                return false;
            } else {
                this.threadAlive = false;
                logger.debug("Stopped BrokerThread");
                return true;
            }
        }
    }

}