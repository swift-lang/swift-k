/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.globus.transfer.reliable.client;

/**
 * Application level job object, encapsules parameters and options related to a RFT transfer,
 * each job has a unique jobID, which is an incremental integer
 * @author Liu Wantao liuwt@uchicago.edu
 */
public class RFTJob {
    private int jobID = 0;
    private RFTOptions options;
    private RFTTransferParam param;
    
    public RFTJob(int jobID, RFTOptions options, RFTTransferParam param) {
    	this.jobID = jobID;
        this.options = options;
        this.param = param;
    }

    public int getJobID() {
        return jobID;
    }

    public RFTOptions getOptions() {
        return options;
    }

    public RFTTransferParam getParam() {
        return param;
    }
    
}
