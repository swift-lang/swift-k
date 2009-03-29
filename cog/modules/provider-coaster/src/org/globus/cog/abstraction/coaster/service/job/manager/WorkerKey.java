//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 12, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

public class WorkerKey implements Comparable {
    private Worker worker;
    private Seconds time;

    public WorkerKey(Worker worker) {
        this.worker = worker;
    }

    public WorkerKey(Seconds time) {
        this.time = time;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof WorkerKey) {
            return worker == ((WorkerKey) obj).worker;
        }
        else {
            return false;
        }
    }

    public int compareTo(Object o) {
        WorkerKey wk = (WorkerKey) o;

        if (worker == null) {
            if (wk.worker == null) {
                return sgn(time.subtract(wk.time));
            }
            else {
                return sgn(time.subtract(wk.worker
                        .getScheduledTerminationTime()));
            }
        }
        else {
            if (wk.worker == null) {
                return sgn(worker.getScheduledTerminationTime().subtract(
                        wk.time));
            }
            else {
                if (worker == wk.worker) {
                    return 0;
                }
                else {
                    int dif = sgn(worker
                            .getScheduledTerminationTime()
                            .subtract(wk.worker.getScheduledTerminationTime()));
                    if (dif != 0) {
                        return dif;
                    }
                    else {
                        return System.identityHashCode(worker)
                                - System.identityHashCode(wk.worker);
                    }
                }
            }
        }
    }

    private int sgn(Seconds s) {
        long val = s.getSeconds();
        if (val < 0) {
            return -1;
        }
        else if (val > 0) {
            return 1;
        }
        else {
            return 0;
        }
    }

    public String toString() {
        if (worker == null) {
            return "/" + time;
        }
        else {
            return worker.getId() + "/"
                    + worker.getScheduledTerminationTime();
        }
    }
}
