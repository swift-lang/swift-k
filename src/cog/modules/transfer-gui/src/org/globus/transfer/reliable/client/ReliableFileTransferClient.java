/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */

package org.globus.transfer.reliable.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.gsi.GSIConstants;
import org.globus.rft.generated.RFTOptionsType;
import org.globus.rft.generated.ReliableFileTransferPortType;
import org.globus.rft.generated.TransferRequestType;
import org.globus.rft.generated.TransferType;
import org.globus.rft.generated.Start;
import org.globus.rft.generated.StartOutputType;

import org.oasis.wsrf.lifetime.SetTerminationTime;
import org.oasis.wsrf.lifetime.SetTerminationTimeResponse;

import org.globus.wsrf.encoding.ObjectSerializer;

/**
 * Command line client for RFT service
 */
public class ReliableFileTransferClient extends BaseRFTClient {
	private static Log logger = LogFactory.getLog(ReliableFileTransferClient.class);

    /**
     * Constructor
     */
    public ReliableFileTransferClient() {
        super();
    }
    
    /**
     * Gets the transfer attribute of the ReliableFileTransferClient class
     * @param epr
     * @param path
     * @return The transfer value
     */
    public static TransferRequestType getTransfer(
    String path, EndpointReferenceType epr) {
        File requestFile = new File(path);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(requestFile));
        } catch (java.io.FileNotFoundException fnfe) {
        	logger.debug(fnfe.getMessage(), fnfe);        	
            System.err.println(fnfe);
            System.exit(-1);
        }

        Vector requestData = new Vector();

        try {

            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("#")) {
                    if (!line.trim().equals("")) {
                    requestData.add(line);
                    }
                }
                line = reader.readLine();

            }

            reader.close();
        } catch (java.io.IOException ioe) {
        	logger.debug(ioe.getMessage(), ioe);
            System.err.println("IOException:" + ioe.getMessage());
            System.exit(-1);
        }

        transferCount = (requestData.size() - 11) / 2;
        
        if(transferCount <= 0) {
            System.err.println("Invalid transfer file format");
            System.exit(-1);
        }
        TransferType[] transfers1 = new TransferType[transferCount];
        RFTOptionsType multirftOptions = new RFTOptionsType();
        int i = 0;
        multirftOptions.setBinary(Boolean.valueOf(
                (String) requestData.elementAt(i++)));
        multirftOptions.setBlockSize(Integer.valueOf(
                (String) requestData.elementAt(i++)));
        multirftOptions.setTcpBufferSize(Integer.valueOf(
                (String) requestData.elementAt(i++)));
        multirftOptions.setNotpt(Boolean.valueOf(
                (String) requestData.elementAt(i++)));
        multirftOptions.setParallelStreams(Integer.valueOf(
                (String) requestData.elementAt(i++)));
        multirftOptions.setDcau(Boolean.valueOf(
                (String) requestData.elementAt(i++)));
        int concurrency = Integer.valueOf((String) requestData.elementAt(i++))
                .intValue();
        
        String sourceSubjectName = (String) requestData.elementAt(i++);
        if (sourceSubjectName != null) {
            multirftOptions.setSourceSubjectName(sourceSubjectName);
        }
        String destinationSubjectName = (String) requestData.elementAt(i++);
        if (destinationSubjectName != null) {
            multirftOptions.setDestinationSubjectName(destinationSubjectName);
        }
        boolean allOrNone = Boolean.valueOf(
                (String) requestData.elementAt(i++)).booleanValue();
        int maxAttempts = Integer.valueOf((String) requestData.elementAt(i++))
                .intValue();
        System.out.println("Number of transfers in this request: "
                + transferCount);

        for (int j = 0; j < transfers1.length; j++) {
            transfers1[j] = new TransferType();
            transfers1[j].setSourceUrl((String) requestData.elementAt(i++));
            transfers1[j]
                    .setDestinationUrl((String) requestData.elementAt(i++));
        }

        TransferRequestType transferRequest = new TransferRequestType();
        transferRequest.setTransfer(transfers1);
        transferRequest.setRftOptions(multirftOptions);
        transferRequest.setConcurrency(new Integer(concurrency));
        transferRequest.setAllOrNone(new Boolean(allOrNone));
        transferRequest.setMaxAttempts(new Integer(maxAttempts));
        transferRequest.setTransferCredentialEndpoint(epr);
        return transferRequest;
    }

   
    /**
     * @param args
     * @exception Exception
     */
    public static void main(String args[]) throws Exception {
    	
        if(args.length < 1) {
            printUsage();
            System.exit(-1);
        }
        cmd = args;
        parseArgs();
        
        TransferRequestType transferType = null;
        if (PORT == null) {
            if (authType.equals(GSIConstants.GSI_TRANSPORT)) {
                PORT = "8443";
            } else {
                PORT ="8080";
            }
        }
        if (authType.equals(GSIConstants.GSI_TRANSPORT)) {
            PROTOCOL = "https";
        }
        String rftFactoryAddress = 
            PROTOCOL + "://"+ HOST+ ":" + PORT + SERVICE_URL_ROOT 
            + "ReliableFileTransferFactoryService";
        String rftServiceAddress = PROTOCOL+ "://" + HOST + ":" + PORT + 
            SERVICE_URL_ROOT + "ReliableFileTransferService";
        
        EndpointReferenceType credEPR = delegateCredential(HOST, PORT);

        transferType = getTransfer(PATH_TO_FILE, credEPR);
                
        EndpointReferenceType rftepr = createRFT(rftFactoryAddress, 
                transferType);
        if (outFileName != null) {
            FileWriter writer = null;  
            try {                      
                writer = new FileWriter(outFileName);
                QName qName = new QName("", "RFT_EPR");
                writer.write(ObjectSerializer.toString(rftepr,
                                                       qName));
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
        rftepr.setAddress(new Address(rftServiceAddress));
        ReliableFileTransferPortType rft = rftLocator
                .getReliableFileTransferPortTypePort(rftepr);
        setSecurity((Stub)rft);

        //For secure notifications
        subscribe(rft);
        System.out.println("Subscribed for overall status");
        //End subscription code
        Calendar termTime = Calendar.getInstance();
        termTime.add(Calendar.MINUTE, TERM_TIME);
        SetTerminationTime reqTermTime = new SetTerminationTime();
        reqTermTime.setRequestedTerminationTime(termTime);
        System.out.println("Termination time to set: " + TERM_TIME
                + " minutes");
        SetTerminationTimeResponse termRes = rft
                .setTerminationTime(reqTermTime);
        StartOutputType startresp = rft.start(new Start());
        
        while (finished < transferCount && transferCount != 0) {
            if (failed == transferCount || 
                    (failed + finished == transferCount)) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }
        if ((finished == transferCount) && (finished != 0)) { 
            System.out.println( "All Transfers are completed");
            System.exit(0);
        }
        if ((failed == transferCount) && (failed != 0)) {
            System.out.println( "All Transfers failed !");
            System.exit(-1);
        }
        if ((failed + finished) == transferCount) {
            System.out.println( "Transfers Done");
            System.exit(-1);
        }

    }


}

