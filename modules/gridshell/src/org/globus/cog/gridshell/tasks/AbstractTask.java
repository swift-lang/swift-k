/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeException;

/**
 * 
 */
public abstract class AbstractTask extends TaskImpl {
    private static final Logger logger = Logger.getLogger(AbstractTask.class);
    
    private Boolean lock = Boolean.FALSE;
    
	private final StatusListener LOCK_LISTENER = new StatusListener() {
		public void statusChanged(StatusEvent event) {
			logger.debug("statusChanged "+event);
			int statusCode = event.getStatus().getStatusCode();
			if( statusCode == Status.COMPLETED || statusCode == Status.FAILED) {
				unlock();
				notifyAllLock();
			}
		}		
	};

    public AbstractTask(String id, int type) {
        super(id, type);
    }
    
	public abstract TaskHandler getTaskHandler();
	public abstract String getProvider();
	public abstract String getServiceContact();
	public abstract Object getResult();
	
	public void initTask() throws InvalidProviderException, ProviderMethodException {
	    unlock();
        this.setProvider(getProvider());
	}
	
    public final void submitTask() throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        logger.info("submitTask()");
        getTaskHandler().submit(this);
    }

    public final Object submitAndWait() throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        logger.info("submitAndWait()");

        synchronized (lock) {
            if (!isLocked()) {
                // put a lock
                lock();

                // add a listener
                this.addStatusListener(LOCK_LISTENER);

                try {
                    // submit
                    this.submitTask();
                } catch (IllegalSpecException exception) {
                    unlock();
                    throw exception;
                } catch (InvalidSecurityContextException exception) {
                    unlock();
                    throw exception;
                } catch (InvalidServiceContactException exception) {
                    unlock();
                    throw exception;
                } catch (TaskSubmissionException exception) {
                    unlock();
                    throw exception;
                } catch (Exception e) {
                    unlock();
                    throw new RuntimeException(e);
                }

                logger.info("before while");
                // while locked wait
                while (isLocked()) {
                    logger.info("start wait while");
                    try {
                        logger.info("start wait");
                        Thread.sleep(1000);
                        logger.info("done wait");
                    } catch (InterruptedException e) {
                        logger.debug("Got intrupted while waiting", e);
                    }
                    logger.info("end wait while");
                }
                logger.info("after while");
            }
        }
        return getResult();
    }
    
    public void addScopeStatusListener(final Scope scope,final String varName) {
        if(scope==null) {
            throw new IllegalArgumentException("Scope must be non-null");
        }
        
        StatusListener scopeStatusListener = new StatusListener() {
            public void statusChanged(StatusEvent sEvent) {
                logger.debug("Status="+sEvent.getStatus().getStatusString());
                int statusCode = sEvent.getStatus().getStatusCode();
                if(statusCode == Status.COMPLETED) {                    
                    AbstractTask source = (AbstractTask)sEvent.getSource();
                    Object result = source.getResult();
                    try {
                        scope.setVariableTo(varName,result);
                    } catch (ScopeException e) {
                        logger.debug("Couldn't set variable",e);
                        throw new RuntimeException("Error",e);
                    }
                }else if(statusCode == Status.FAILED) {
                    AbstractTask t = (AbstractTask)sEvent.getSource();
                    logger.error("stdError="+t.getStdError());
                    logger.error("exception="+sEvent.getStatus().getException());
                }
            }
        };
        
        this.addStatusListener(scopeStatusListener);
    }
     
    private void unlock() {
        logger.info("unlock()");
        synchronized (lock) {
            lock = Boolean.FALSE;
        }
    }

    private void lock() {
        logger.info("lock()");
        synchronized (lock) {
            lock = Boolean.TRUE;
        }
    }

    public boolean isLocked() {
        synchronized (lock) {
            return lock.booleanValue();
        }
    }

    public final void notifyAllLock() {
        logger.info("notifyAllLock()");
        lock.notifyAll();
    }

}