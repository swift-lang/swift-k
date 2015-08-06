/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;

import java.util.LinkedList;
import java.util.List;

import org.griphyn.vdl.karajan.monitor.items.ApplicationItem.QualifiedID;


public class ApplicationItem extends AbstractStatefulItem {
    public static class QualifiedID {
        public String id;
        public String attempt;
        public String replicaId;
        
        public QualifiedID(String id, String attempt) {
            this.id = id;
            this.attempt = attempt;
        }
    }
    
	private String name, arguments, host, workerId;
	private long startTime, currentStateTime;
	/**
	 * The state of the app. Currently swift does not log app state transitions
	 * so the state is inferred from various other log events. This should
	 * probably change in that every transition should be logged.
	 * 
	 * More importantly, transitions could be logged in a prevState -> currentState
	 * format allowing a summary to be built without keeping a map of thread id states
	 */
	private ApplicationState state;
	private LinkedList<ApplicationInstance> instances;

	public ApplicationItem(String id, String name, String arguments, String host, long startTime) {
		super(id);
		this.name = name;
		this.arguments = arguments;
		this.host = host;
		this.startTime = startTime;
		this.state = ApplicationState.INITIALIZING;
		this.currentStateTime = startTime;
		this.instances = new LinkedList<ApplicationInstance>();
	}
	
	public ApplicationItem(String id) {
		this(id, null, null, null, 0);
	}

	public StatefulItemClass getItemClass() {
		return StatefulItemClass.APPLICATION;
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public void setHost(String host) {
	    getCurrentInstance().setHost(host);
		this.host = host;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public String getName() {
		return name;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public void setState(ApplicationState state, long time) {
	    ApplicationInstance inst = getCurrentInstance();
	    if (inst != null) {
	        inst.setState(state, time);
	    }
	    this.state = state;
	    this.currentStateTime = time;
	}

	public long getCurrentStateTime() {
        return currentStateTime;
    }

    public ApplicationState getState() {
        return state;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String toString() {
		return "APP[" + name + ", " + arguments + ", " + host + "]";
	}

    public static QualifiedID parseId(String id) {
        int ix = id.lastIndexOf('-');
        if (ix == 0) {
            return new QualifiedID(id, null);
        }
        else {
            return new QualifiedID(id.substring(0, ix), id.substring(ix + 1));
        }
    }

    public void addInstance(QualifiedID qid) {
        instances.add(new ApplicationInstance(qid.attempt, qid.replicaId));
    }

    public ApplicationInstance getCurrentInstance() {
        if (instances.isEmpty()) {
            return null;
        }
        else {
            return instances.getLast();
        }
    }
}
