package org.globus.cog.abstraction.impl.execution.condor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class LogReader implements Runnable {
    private String logFile = null;
    private Task task = null;
    private int readInterval = 5000;
    private int status = Status.UNSUBMITTED;

    public LogReader(String logFile, Task task) {
        this.logFile = logFile;
        this.task = task;

        // specify custom read interval
        String readInterval = (String) this.task.getAttribute("readInterval");
        if (readInterval != null) {
            this.readInterval = Integer.parseInt(readInterval);
        }
    }

    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(this.logFile)));
            String data = null;
            while (true) {

                data = bufferedReader.readLine();
                if (data != null) {
                    process(data);
                } else {
                    Thread.sleep(this.readInterval);
                }
                if (this.status == Status.CANCELED
                        || this.status == Status.COMPLETED
                        || this.status == Status.FAILED) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(String data) {
        String status = data.substring(0, 3);
        setStatus(status);
    }

    private void setStatus(String status) {
        // status of task set as per table 2.1 of the condor manual (v6.6.7)
        if (status.equals("000")) {
            this.task.setStatus(Status.SUBMITTED);
        } else if (status.equals("001") || status.equals("011")
                || status.equals("013")) {
            this.task.setStatus(Status.ACTIVE);
        } else if (status.equals("002") || status.equals("007")
                || status.equals("021")) {
            this.task.setStatus(Status.FAILED);
            this.status = Status.FAILED;
        } else if (status.equals("005")) {
            this.task.setStatus(Status.COMPLETED);
            this.status = Status.COMPLETED;
        } else if (status.equals("009")) {
            this.task.setStatus(Status.CANCELED);
            this.status = Status.CANCELED;
        } else if (status.equals("010") || status.equals("012")) {
            this.task.setStatus(Status.SUSPENDED);
        }
    }
}