package org.globus.ogce.beans.filetransfer.gui.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.IdentityAuthorization;
import org.globus.io.urlcopy.UrlCopy;
import org.globus.io.urlcopy.UrlCopyListener;
import org.globus.ogce.beans.filetransfer.gui.MainInterface;
import org.globus.ogce.beans.filetransfer.gui.remote.gridftp.GridClient;
import org.globus.ogce.beans.filetransfer.transfer.TransferInterface;
import org.globus.tools.ui.util.UITools;
import org.globus.transfer.reliable.client.utils.Utils;
import org.globus.util.GlobusURL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;

public class UrlCopyPanel extends JPanel implements TransferInterface, UrlCopyListener//, ActionListener
{
    static Log logger =
            LogFactory.getLog(UrlCopyPanel.class.getName());

    QueuePanel urlcopyQueuePanel = null;
    UrlCopyOptions urlcopyOptions = null;
    Hashtable jobs = null;
    String currentJob = null;
    String errorMsg = null;
    MainInterface theApp = null;
    String finalStatus = "Unknown";
    boolean active = false;

    public UrlCopyPanel() {
        this(null);
    }

    public UrlCopyPanel(MainInterface theApp) {
        setLayout(new BorderLayout());
        this.theApp = theApp;
        urlcopyQueuePanel = new QueuePanel();
        urlcopyQueuePanel.createHeader(new String[]{"Jobid", "From", "To",
                                                    "Status", "Current", "%", "Errors", "RFT"});
        add(urlcopyQueuePanel, BorderLayout.CENTER);
        urlcopyOptions = new UrlCopyOptions();
        //	add(urlcopyOptions.getPanel(), BorderLayout.SOUTH);
        jobs = new Hashtable();
        //urlcopyQueuePanel.createButtonsPanel(new String[]{"Start", "Stop", "Load", "Save", "Clear"}, new ButtonActionListener());
        urlcopyQueuePanel.addPopupItems(new String[]{"Info", "Cancel", "Restart",
                                                     "Delete"}, new ButtonActionListener());
        //	createCheckBoxPanel();

    }

    public void addTransfer(String jobid, String from, String to) {

        urlcopyQueuePanel.addTransfer(new String[]{jobid, from, to, "Submitted",
                                                   "0", "0", "N/A"});

    }
    
    public void addTransfer(String jobid, String from, String to, String rft) {

        urlcopyQueuePanel.addTransfer(new String[]{jobid, from, to, "Submitted",
                                                   "0", "0", "N/A", rft});

    }

    public void startTransfer(String jobid) {
        currentJob = jobid;

        if (!((isJob("Cancelled", jobid, false) || (isJob("Finished", jobid, false))))) {
            active = true;
            String from = urlcopyQueuePanel.getColumnValue(
                    urlcopyQueuePanel.getRowIndex(jobid), 1);
            String to = urlcopyQueuePanel.getColumnValue(
                    urlcopyQueuePanel.getRowIndex(jobid), 2);
            callUrlCopyTransfer(jobid, from, to);
            active = false;
        } else {
            return;
        }

    }

    public void updateTransfer(String jobid, String status,
                               String current, String percent, String errorMsg) {

        urlcopyQueuePanel.updateTransfer(new String[]{jobid, null, null, status,
                                                      current, percent
                                                      , errorMsg});

    }

    public void deleteTransfer(String jobid) {
        cancelTransfer(jobid);
        urlcopyQueuePanel.deleteTransfer(jobid);
    }

    public void cancelTransfer(String jobid) {
        if (isJob("Finished", jobid)) {
            logger.info("Job Finished id = " + jobid);
        } else {
            logger.info("Job Finished id = " + jobid);
            updateTransfer(jobid, "Cancelled", null, null, null);
            UrlCopy urlcopy = (UrlCopy) jobs.get(jobid);
            urlcopy.cancel();
        }
    }

    public boolean callUrlCopyTransfer(String jobid, String from, String to) {
    	System.out.println("from:" + from);
    	System.out.println("to:" + to);
    	
        GlobusURL froms = null;
        GlobusURL tos = null;
        UrlCopy c = null;
        if (from.equals(to)) {
            updateTransfer(jobid, "Cancelled",
                    null, null, "Destination and Source are same.");
        }
        logger.info("\nJOB :: \n ID = " + jobid);
        logger.info("\nFrom = " + from);
        logger.info("\nTo = " + to);
        try {
        	
            froms = new GlobusURL(from);
            tos = new GlobusURL(to);
            c = new UrlCopy();
            c.setSourceUrl(froms);
            c.setDestinationUrl(tos);
            Authorization auth = null;
            if (null != GridClient.subject1 && !"".equals(GridClient.subject1.trim())) {
            	auth = new IdentityAuthorization(GridClient.subject1);
            }
            
            c.setSourceAuthorization(auth);
            c.setDestinationAuthorization(auth);
            if (from.startsWith("gsiftp") && to.startsWith("gsiftp")) {
                c.setUseThirdPartyCopy(true);                
            } else {
                c.setUseThirdPartyCopy(false);
            }   
            int bufferSize;
            try {
            	bufferSize = Integer.parseInt(Utils.getProperty("tcpbuffersize", "rft.properties"));
            } catch (Exception e) {
            	bufferSize = 16000;
            }
             c.setBufferSize(bufferSize);             
             c.setAppendMode(urlcopyOptions.getAppendMode());
            c.setDCAU(urlcopyOptions.getDCAU());
            c.addUrlCopyListener(this);
            jobs.put(jobid, c);
            
            long st = System.currentTimeMillis();
            if (!(c.isCanceled())) {
                printOutput("\n\n------------------------------\n");
                printOutput("Started Job: " + jobid);
                printOutput("\n------------------------------\n");
                updateTransfer(currentJob, "Active",
                        null, null, "No errors");
                
                c.copy();
               
                finalStatus = "Finished";
                updateTransfer(currentJob, "Finished",
                        null, null, "No errors");

                urlcopyQueuePanel.setFocus(currentJob);
                long ft = System.currentTimeMillis();
                long time = ft - st;
                printOutput("\nFrom :" + c.getSourceUrl() +
                        "\nTo :" + c.getDestinationUrl() +
                        "\nTotal time in millisec = " + time);


            }
            return true;

        } catch (Exception te) {

            String error = getStackTrace(te);
            String errorMsg = null;

            te.printStackTrace();
            errorMsg = processErrorMessage(error);
            if (errorMsg == null) {
                errorMsg = te.getMessage();
            }
            if (errorMsg.indexOf("Root error message: null") >= 0) {
                errorMsg = "File exists";
            }
            finalStatus = "Failed : " + errorMsg;
            updateTransfer(currentJob, "Failed",
                    null, null, errorMsg);
            urlcopyQueuePanel.setFocus(currentJob);
            printError("\n\n\nFrom :" + c.getSourceUrl() +
                    "\nTo :" + c.getDestinationUrl() +
                    "Error during actual transfer:\n" +
                    error);
            return false;

        }


    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public QueuePanel getQueuePanel() {
    	return urlcopyQueuePanel;
    }
    
    public String processErrorMessage(String error) {
        String errorMsg = null;
        if ((error.indexOf("Permission denied")) > 0) {
            errorMsg = "Permission denied";
        } else if ((error.indexOf("Expired credentials"))
                > 0) {
            errorMsg = "Credentials expired";
        } else if (((error.indexOf("FileNotFoundException")) > 0)
                || ((error.indexOf("No such file or directory")) > 0)) {
            errorMsg = "Destination Directory is not present. Please create one.";

        } else if ((error.indexOf("ClassCastException"))
                > 0) {
            errorMsg = "Destination rejected third party transfer";
        } else if (((error.indexOf("not enough space on the disk")) > 0) || ((error.indexOf("Disc quota exceeded")) > 0)) {
            errorMsg = "No space in Destination Disk. ";
        } else if ((error.indexOf("Connection reset"))
                > 0) {
            errorMsg = "Network Connection lost.";

        } else if (((error.indexOf("Timeout")) > 0) ||
                ((error.indexOf("timed out")) > 0) ||
                (((error.indexOf("timeout"))) > 0)) {
            errorMsg = "Connection Timed out. Check for firewalls. ";
        } else if (((error.indexOf("close failed")) > 0) ||
                (((error.indexOf("closing"))) > 0)) {
            errorMsg = "Transfer was cancelled.";
        }
        return errorMsg;
    }

    Exception _exception;

    public void transfer(long current, long total) {
        long progress = 0;
        if (total == -1) {
            if (current == -1) {
                printOutput("This is a third party transfer.");
            } else {
                logger.info("\nJOB :: \n ID = " + currentJob + "   " + current);
            }
        } else {
            double fraction = (double) current / (double) total;
            float fvalue = Math.round(Math.round(fraction * 100));
            progress = (long) fvalue;
            logger.info("\n" + current + " out of " + total + " Percent =" + progress);
        }
        String currValue = "N/A";
        String progressValue = "N/A";
        if (progress != 0) {
            progressValue = progress + "";
        }

        if (current > 0) {
            currValue = current + "";
        }
        updateTransfer(currentJob, "Active",
                currValue, progressValue, "No errors");
    }

    public void transferError(Exception e) {
        _exception = e;

    }

    public void transferCompleted() {
        if (_exception == null) {
            printOutput("Transfer completed successfully");

        } else {
            printError("Error during transfer : " + _exception.getMessage());
            logger.debug("Error during transfer : " + _exception.getMessage());

            _exception.printStackTrace(System.out);

        }
    }

    public JPanel getOptionsPanel() {
        return urlcopyOptions.getPanel();
    }

    public void createCheckBoxPanel() {
        JCheckBox allButton = new JCheckBox("All", true);
        allButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    urlcopyQueuePanel.showRows("All", 3);
                }
            }
        });
        JCheckBox activeButton = new JCheckBox("Active");
        JCheckBox finishedButton = new JCheckBox("Finished");
        JCheckBox failedButton = new JCheckBox("Failed");
        JCheckBox othersButton = new JCheckBox("Others");
        urlcopyQueuePanel.createCheckBoxPanel(new JCheckBox[]{allButton, activeButton, finishedButton, failedButton, othersButton});
    }

    public void clear() {
        if (urlcopyQueuePanel.tableSize() > 0) {
            Object aobj[] = {"Yes", "No"};
            int k = JOptionPane.showOptionDialog(null, " Do you wish to clear all the jobs and stop the unfinished jobs ?", "Cancellation Alert", -1, 2, null, aobj, aobj[0]);
            if (k == 1) {
                return;
            } else {
                if (!isJob("Finished", currentJob, false)) {
                    cancelTransfer(currentJob);
                }
                theApp.clearQueue("urlcopy");
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                urlcopyQueuePanel.clear();
                return;

            }
        } else {
            return;
        }
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public void printOutput(String msg) {
        theApp.msgOut(msg);
    }

    public void printError(String msg) {
        theApp.msgOut(msg);
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame("Java CoG Kit -UrlCopy File Transfer");

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });


        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        UrlCopyPanel urlcopyPanel = new UrlCopyPanel();
        //	frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(urlcopyPanel);
        //	frame.getContentPane().add(urlcopyPanel.getOptionsPanel().getPanel(), BorderLayout.SOUTH);
        frame.pack();
        frame.setSize(d.width / 2, d.height / 2);
        frame.setVisible(true);
        UITools.center(null, frame);
        for (int i = 1; i < 10; i++) {
            urlcopyPanel.addTransfer("" + i, "gsiftp://arbat.mcs.anl.gov:6223/homes/alunkal/dead.letter", "gsiftp://arbat.mcs.anl.gov:6223/homes/alunkal/test" + i);
        }
        int j = 1;
        while (j < 9) {
            urlcopyPanel.startTransfer(j + "");
            j++;
        }

    }

    public boolean isJob(String status, String job) {
        return isJob(status, job, true);
    }

    public boolean isJob(String status, String job, boolean alert) {
        int row = urlcopyQueuePanel.getRowIndex(job);
        if (row >= 0) {
            logger.info("VALUE OF THE CURRENT JOB =" + urlcopyQueuePanel.getColumnValue(row, 3));
            if (urlcopyQueuePanel.getColumnValue(row, 3).equals(status)) {
                if (alert) {
                    JOptionPane.showMessageDialog(null,
                            "The job is already " + status,
                            "URLCOPY Job Information",
                            JOptionPane.PLAIN_MESSAGE);
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    class ButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            String actionCommand = ae.getActionCommand();
            if (actionCommand.equals("Save")) {
                Thread saveThread = new Thread() {
                    public void run() {
                        if (urlcopyQueuePanel.tableSize() > 0) {
                            theApp.saveQueueToFile("urlcopy");
                        }
                    }
                };
                saveThread.start();
            } else if (actionCommand.equals("Load")) {
                Thread loadThread = new Thread() {
                    public void run() {
                        theApp.loadQueueFromFile("urlcopy");
                    }
                };
                loadThread.start();
            } else if (actionCommand.equals("Stop")) {
                Thread controlThread = new Thread() {
                    public void run() {
                        if (urlcopyQueuePanel.tableSize() > 0) {
                            theApp.controlExecutionQueue(false, "urlcopy");
                        }
                    }
                };
                controlThread.start();
            } else if (actionCommand.equals("Start")) {
                Thread controlThread = new Thread() {
                    public void run() {
                        if (urlcopyQueuePanel.tableSize() > 0) {
                            theApp.controlExecutionQueue(true, "urlcopy");
                        }
                    }
                };
                controlThread.start();
            } else if (actionCommand.equals("Clear")) {
                Thread controlThread = new Thread() {
                    public void run() {
                        if (urlcopyQueuePanel.tableSize() > 0) {
                            clear();
                        }
                    }
                };
                controlThread.start();
            } else if (actionCommand.equals("Info")) {
                String job = urlcopyQueuePanel.getSelectedJob();
                
                int row = urlcopyQueuePanel.getRowIndex(job);                
                String msg = "   Job ID : " +
                        urlcopyQueuePanel.getColumnValue(row, 0)
                        + "\n   From : "
                        + urlcopyQueuePanel.getColumnValue(row, 1) +
                        "\n   To : " +
                        urlcopyQueuePanel.getColumnValue(row, 2) +
                        "\n   Status : " +
                        urlcopyQueuePanel.getColumnValue(row, 3)
                        + "\n   Errors : " +
                        urlcopyQueuePanel.getColumnValue(row, 6) +
                        "\n";

                JOptionPane.showMessageDialog(null,
                        msg,
                        "URLCOPY Job Information",
                        JOptionPane.PLAIN_MESSAGE);

            } else if (actionCommand.equals("Cancel")) {
                Thread controlThread = new Thread() {
                    public void run() {
                        String job = urlcopyQueuePanel.getSelectedJob();
                        cancelTransfer(job);

                    }
                };
                controlThread.start();

            } else if (actionCommand.equals("Restart")) {
                Thread controlThread = new Thread() {
                    public void run() {
                        String job = urlcopyQueuePanel.getSelectedJob();
                        if ((isJob("Cancelled", job) || (isJob("Finished", job)))) {
                            return;
                        }
                        while (active) {
                            try {
                                Thread.sleep(200);
                                logger.info("Waiting for the previous job");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        startTransfer(job);
                    }
                };
                controlThread.start();

            } else if (actionCommand.equals("Delete")) {
                String job = urlcopyQueuePanel.getSelectedJob();
                int row = urlcopyQueuePanel.getRowIndex(job);
                if (!urlcopyQueuePanel.getColumnValue(row, 3).equals("Finished")
                		&& !urlcopyQueuePanel.getColumnValue(row, 3).equals("Expanding_Done")) {
                    Object aobj[] = {"Yes", "No"};
                    int k = JOptionPane.showOptionDialog(null, " This job is not Finished yet. Do you wish to cancel the job and delete it?", "Deletion Alert", -1, 2, null, aobj, aobj[0]);
                    if (k == 1) {
                        return;
                    } else {
                        deleteTransfer(job);
                    }
                } else {
                    deleteTransfer(job);
                }

            }
        }

    }


}// end of class


