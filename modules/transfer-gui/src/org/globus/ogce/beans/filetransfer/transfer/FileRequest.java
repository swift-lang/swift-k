package org.globus.ogce.beans.filetransfer.transfer;


import org.apache.log4j.Logger;
import org.globus.ogce.beans.filetransfer.gui.monitor.MonitorPanel;
import org.globus.ogce.beans.filetransfer.gui.monitor.RequestPanel;
import org.globus.ogce.beans.filetransfer.util.GridBrokerQueue;

public class FileRequest extends Thread {
    private static Logger logger =
            Logger.getLogger(FileRequest.class.getName());

    private GridBrokerQueue requestQueue = null;
    boolean active = false;
    boolean start = true;
    int dirJobID = 0;
    private RequestPanel requestPanel = null;
    DirTransferRequest request = null;
    private MonitorPanel monitorFrame = null;

    public FileRequest(GridBrokerQueue requestQueue, MonitorPanel monitorFrame) {
        super("FileRequest");
        this.requestQueue = requestQueue;
        this.monitorFrame = monitorFrame;
        requestPanel = monitorFrame.getRequestPanel();
    }

    public FileRequest() {
        this(null, null);
    }

    public void setControl(boolean start) {
        this.start = start;
        if (start) {
            run();
        }
    }

    public void clearRequestQueue() {
        requestQueue.deleteAll();
        dirJobID = 0;
    }

    public void updateQueue(DirTransferRequest request) {

        dirJobID++;
        DirRequestJob job = new DirRequestJob(dirJobID + "", request);
        setControl(false);
        requestQueue.put(job);

        requestPanel.addTransfer(dirJobID + "", request.getFrom(), request.getTo());
        //monitorFrame.setFocusTab(2);
        setControl(true);
    }

    public void run() {
        if (active) {
            return;
        }

        logger.info("\n In the transfer method." +
                "\nQueue size =" + requestQueue.size());

        DirRequestJob job = null;
        String jobid = null;
        if (requestQueue.size() > 0) {
            while ((requestQueue.size() > 0) && (start)) {
                active = true;
                job = (DirRequestJob) requestQueue.get();
                jobid = job.getJobID();
                request = job.getDirTransferRequest();
                requestPanel.setCurrentRequestObject(request);
                requestPanel.updateTransfer(jobid, "Active", "N/A", "No Errors");
                if (request != null) {                    
                	request.run();
                } else {
                    logger.info("The object from the queue was null");
                }
                String result = "N/A";
                String errors = "N/A";
                while (!(result.equals("Finished")
                        || result.equals("Failed")
                        || result.equals("Suspended"))) {
                	System.out.println("result:" + result);
                    try {
                        Thread.sleep(1000);
                        result = request.getStatus();
                        logger.info("Result is :" + result);
                        if (result.equals("Failed")) {
                            errors = "Thread killed";
                        }
                        requestPanel.updateTransfer(jobid, result,
                                request.getTotalFiles(),
                                errors);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                requestPanel.updateTransfer(jobid, result,
                        request.getTotalFiles(),
                        errors);
            }
            active = false;
        }
    }
}

class DirRequestJob {
    private String jobID = null;
    private DirTransferRequest dirRequest = null;

    public DirRequestJob(String jobID, DirTransferRequest dirRequest) {
        this.jobID = jobID;
        this.dirRequest = dirRequest;
    }

    public String getJobID() {
        return jobID;
    }

    public DirTransferRequest getDirTransferRequest() {
        return dirRequest;
    }
}


