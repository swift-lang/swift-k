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


public class HostItem extends AbstractStatefulItem {
	private String name, score, jobsRunning, jobsAllowed, overload;

	public HostItem(String name, String score, String jobsRunning, String jobsAllowed, String overload) {
		super(name);
		this.name = name;
		this.score = score;
		this.jobsRunning = jobsRunning;
		this.jobsAllowed = jobsAllowed;
		this.overload = overload;
	}
	
	public HostItem(String name) {
	    super(name);
	    this.name = name;
	}
	
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getJobsRunning() {
        return jobsRunning;
    }

    public void setJobsRunning(String jobsRunning) {
        this.jobsRunning = jobsRunning;
    }

    public String getJobsAllowed() {
        return jobsAllowed;
    }

    public void setJobsAllowed(String jobsAllowed) {
        this.jobsAllowed = jobsAllowed;
    }

    public String getOverload() {
        return overload;
    }

    public void setOverload(String overload) {
        this.overload = overload;
    }

    public String toString() {
		return "HOST[" + name  + "]";
	}

    public StatefulItemClass getItemClass() {
        return StatefulItemClass.HOST;
    }
}
