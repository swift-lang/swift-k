/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.globus.transfer.reliable.client;

import org.globus.ogce.beans.filetransfer.gui.monitor.QueuePanel;
import org.globus.rft.generated.TransferType;

/**
 * Action listener of buttons in the GUI
 * @author Liu Wnatao liuwt@uchicago.edu
 */
public class RFTButtonActionListener {
	private QueuePanel queuePanel = null;
	private RFTClient rftClient = null;
	private RFTPanel panel = null;
	private static int transferID = 1;
	
	public RFTButtonActionListener(RFTPanel panel) {
		this.panel = panel;
		this.queuePanel = panel.getQueuePanel();
		this.rftClient = new RFTClient(this, queuePanel);
	}     
    
    /**
     * action for click event of Start button
     * @param options
     * @param rftParam
     */
    public void startButtonAction(RFTJob job, QueuePanel queuePanel) throws Exception {
//        int jobID = job.getJobID();
//        RFTOptions options = job.getOptions();
        RFTTransferParam param = job.getParam();
        
        TransferType transfer = param.getTransfers1()[0];
        String[] cols = {Integer.toString(job.getJobID()), Integer.toString(transferID++), 
        		Integer.toString(1), transfer.getSourceUrl(), transfer.getDestinationUrl(), 
        		"started", "0", "No errors"};        
       	queuePanel.addTransfer(cols);        	
		rftClient.startTransfer(job);		
    }
    
    /**
     * action for click event of Stop button
     * @param queuePanel
     */
    public void stopButtonAction(String jobID) throws Exception {

		int selectedRowIndex = queuePanel.getRowIndex(jobID, 1);
		String id = queuePanel.getColumnValue(selectedRowIndex, 0);
		rftClient.stopTransfer(id);
		queuePanel.setColumnValue(selectedRowIndex, 5, "Cancelled");
		queuePanel.setColumnValue(selectedRowIndex, 6, "0");
		queuePanel.setColumnValue(selectedRowIndex, 7, "No errors");
	}
    
    public void loadFileButtonAction(RFTJob job, QueuePanel queuePanel) throws Exception {
        RFTTransferParam param = job.getParam();        
        TransferType[] transfers = param.getTransfers1();
        
        for (int i = 0; i < transfers.length; i++) {
        	String[] cols = {Integer.toString(job.getJobID()), Integer.toString(transferID++), 
        			Integer.toString(i+1), transfers[i].getSourceUrl(), 
        			transfers[i].getDestinationUrl(), "started", "0", "No errors"};        
           	queuePanel.addTransfer(cols); 
        }      
          	
		rftClient.startTransfer(job);	
    }
    
    /**
	 * update Overall status panel of the GUI, this method delegate the task to
	 * RFTPanel
	 * 
	 * @param finished
	 * @param Active
	 * @param failed
	 * @param retrying
	 * @param Pending
	 * @param cancelled
	 */
    public void updateOverallStatus(int finished, int active, int failed, 
    		int retrying, int pending, int cancelled) {    	
    	panel.updateOverallStatus(finished, active, failed, retrying, pending, cancelled);
    }
}
