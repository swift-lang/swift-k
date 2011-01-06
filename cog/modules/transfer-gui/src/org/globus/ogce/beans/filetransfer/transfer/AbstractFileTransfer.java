package org.globus.ogce.beans.filetransfer.transfer;

import org.apache.log4j.Logger;
import org.globus.ogce.beans.filetransfer.gui.FileTransferMainPanel;
import org.globus.ogce.beans.filetransfer.gui.MainInterface;
import org.globus.ogce.beans.filetransfer.gui.monitor.MonitorPanel;
import org.globus.ogce.beans.filetransfer.gui.monitor.UrlCopyPanel;
import org.globus.ogce.beans.filetransfer.util.FileToTransfer;
import org.globus.ogce.beans.filetransfer.util.GridBrokerQueue;

import javax.swing.*;

public abstract class AbstractFileTransfer extends Thread {
    private static Logger logger =
            Logger.getLogger(AbstractFileTransfer.class.getName());

    private MainInterface theApp;

    private GridBrokerQueue mainQueue = null;
    private GridBrokerQueue saveQueue = null;

    private MonitorPanel monitorFrame = null;

    private int jobID = 0;
    private boolean active = false;
    private boolean start = true;
    private boolean previousTransfer = false;

    protected TransferInterface serviceProvider = null;
    FileToTransfer ftt = null;


    public AbstractFileTransfer(MainInterface theApp, MonitorPanel monitorFrame) {
        super("AbstractFileTransfer");
        this.theApp = theApp;
        this.monitorFrame = monitorFrame;
        mainQueue = new GridBrokerQueue();
        saveQueue = new GridBrokerQueue();
        logger.info("Constructor");
        previousTransfer = false;
    }

    public abstract void setTransferProvider();

    public AbstractFileTransfer() {
        this(null, null);
    }

    public void setControl(boolean start) {
        this.start = start;
        if (start) {
            run();
        }
    }

    public void setSaveQueue(GridBrokerQueue saveQueue) {
        this.saveQueue = saveQueue;
    }

    public GridBrokerQueue getSaveQueue() {
        return saveQueue;
    }

    public void updateQueues(GridBrokerQueue requestQueue) {
        setControl(false);
        int length = requestQueue.size();
        for (int i = 0; i < length; i++) {
            jobID = ++FileTransferMainPanel.jobID;
            FileToTransfer ftt = (FileToTransfer) requestQueue.get();

            ftt.setJobID(jobID + "");
            mainQueue.put(ftt);
            saveQueue.put(ftt);
            ((UrlCopyPanel)serviceProvider).addTransfer(jobID + "", ftt.getFrom(), ftt.getTo(), "false");

        }
        setControl(true);
    }

    public void clearAllQueues() {
        mainQueue.deleteAll();
        saveQueue.deleteAll();
        jobID = 0;
        active = false;
        previousTransfer = false;
        run();
    }

    public void run() {
        if (active) {
            return;
        }

        if (mainQueue.size() > 0) {
            while ((mainQueue.size() > 0) && (start)) {

                active = true;
                if (previousTransfer) {
                    logger.info("Process the previous request");
                    previousTransfer = false;
                } else {
                    ftt = (FileToTransfer) mainQueue.get();
                }
                serviceProvider.startTransfer(ftt.getJobID());
                String result = "Unknown";
                long time = 0;
                int index = -1;
                System.out.println("\nThe result is :" + result);
                while ((!result.equals("Finished")) && (index < 0)) {
                    try {
                        Thread.sleep(500);
                        result = serviceProvider.getFinalStatus();
                        System.out.println("\nCURRENT Status = " + result);
                        time += 500;
                        System.out.println("\nThe result is :" + result);
                        if (result != null) {
                            index = result.indexOf("Failed");
                        }

                    } catch (Exception e) {
                        logger.info("Exception sleeping.");
                    }
                }
                if (result.equals("Finished")) {
                    saveQueue.get();
                } else if (!((result.indexOf("Permission denied") > 0) || (result.indexOf("File exists") > 0)) && active) {

                    String msg = "One of the jobs failed: " +
                            result + "\nDo you wish to continue with" +
                            " other jobs ?";
                    Object options[] = {"Yes", "No"};
                    int k = JOptionPane.showOptionDialog(theApp,
                            msg, "Directory Transfer Alert", -1,
                            JOptionPane.INFORMATION_MESSAGE, null, options,
                            options[0]);
                    if (k == 1) {
                        setControl(false);
                        previousTransfer = true;
                    } else {
                        saveQueue.get();
                    }
                }
            }

        }
        active = false;
    }
}
