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
