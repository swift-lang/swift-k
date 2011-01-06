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
import org.globus.rft.generated.DeleteOptionsType;
import org.globus.rft.generated.DeleteRequestType;
import org.globus.rft.generated.DeleteType;
import org.globus.rft.generated.ReliableFileTransferPortType;
import org.globus.rft.generated.StartOutputType;
import org.globus.rft.generated.Start;
import org.oasis.wsrf.lifetime.SetTerminationTime;
import org.oasis.wsrf.lifetime.SetTerminationTimeResponse;

import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.impl.security.authentication.Constants;
/**
 */
public class ReliableFileDeleteClient extends BaseRFTClient {
   
     public static String optionString = 
            "rft-delete [options] -f <path to transfers file>\n"
        +  "Where options can be:\n"
        +  "-h <hostname or ip-address of container> Defaults to 'localhost'.\n"
        +  "-r <port on which container is listening> Defaults to TCP port 8443.\n"
        +  "-l lifetime of the created resource in minutes Defaults to 60 mins.\n"
        +  "-m security mechanism Allowed values: 'msg' for secure messages, 'conv' for\n"
        +  " secure conversation and 'trans' for secure transport. Defaults to \n"
        +  " 'trans'.\n"
        + " -p protection type Allowed values: 'sig' for signature and 'enc' for encryption.\n"
        + " Defaults to 'sig'.\n"
        + " -z authorization Defaults to 'host' authorization. Allowed values: 'self' for\n"
        + " self authorization and 'host' for host authorization.\n"
        + " -file file to write EPR of created Reliable"
        + " File Transfer Resource\n";

    public static DeleteRequestType 
        getDeleteRequest(String path, EndpointReferenceType epr) {
        DeleteRequestType deleteRequest = new DeleteRequestType();
        File requestFile = new File(path);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(requestFile));
        } catch (java.io.FileNotFoundException fnfe) {
            System.err.println(fnfe);
            System.exit(-1);
        }

        Vector requestData = new Vector();

        try {

            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("#")) {
                    requestData.add(line);
                }
                line = reader.readLine();

            }

            reader.close();
        } catch (java.io.IOException ioe) {
            System.err.println("IOException:" + ioe.getMessage());
            System.exit(-1);
        }
        DeleteType deleteArray[] = new DeleteType[requestData.size() - 1];
        String subjectName = (String) requestData.elementAt(0);
        for (int i = 0; i < deleteArray.length; i++) {
            deleteArray[i] = new DeleteType();
            deleteArray[i].setFile((String) requestData.elementAt(i + 1));
        }
        transferCount = (requestData.size() - 1) / 2;
        if (transferCount <=0 ) {
            System.err.println("Invalid transfer file format");
            System.exit(-1);
        }
        DeleteOptionsType deleteOptions = new DeleteOptionsType();
        deleteOptions.setSubjectName(subjectName);
        deleteRequest.setDeleteOptions(deleteOptions);
        deleteRequest.setDeletion(deleteArray);
        deleteRequest.setTransferCredentialEndpoint(epr);
        deleteRequest.setConcurrency(new Integer(1));
        deleteRequest.setMaxAttempts(new Integer(10));
        return deleteRequest;
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        DeleteRequestType deleteRequestType = null;
        if (args.length < 1) {
            printUsage();
            System.exit(-1);
        }
        cmd = args;
        parseArgs();

        if (PORT == null) {
            if (authType.equals(Constants.GSI_TRANSPORT)) {
                PORT = "8443";
            } else {
                PORT ="8080";
            }
        }
        if (authType.equals(Constants.GSI_TRANSPORT)) {
            PROTOCOL = "https";
        }
        String rftFactoryAddress =
            PROTOCOL + "://"+ HOST+ ":" + PORT + SERVICE_URL_ROOT
            + "ReliableFileTransferFactoryService";
        String rftServiceAddress = PROTOCOL+ "://" + HOST + ":" + PORT +
            SERVICE_URL_ROOT + "ReliableFileTransferService";

        System.out.println("creating a rft resource");
        // do delegation
        EndpointReferenceType credEPR = delegateCredential(HOST, PORT);

        deleteRequestType = getDeleteRequest(PATH_TO_FILE, credEPR);

        EndpointReferenceType rftepr = createRFT(rftFactoryAddress, 
                deleteRequestType);

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
        //For secure notifications
        setSecurity((Stub)rft);
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
        while (finished < transferCount && transferCount !=0) {
            if (failed == transferCount ||
                    (failed + finished == transferCount)
                    || finished == transferCount) {
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
