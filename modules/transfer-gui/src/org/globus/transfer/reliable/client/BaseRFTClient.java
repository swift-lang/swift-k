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

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import javax.xml.rpc.Stub;

import org.globus.axis.util.Util;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.delegation.DelegationConstants;
import org.globus.delegation.DelegationUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.ogsa.impl.security.authorization.SelfAuthorization;
import org.globus.rft.generated.BaseRequestType;
import org.globus.rft.generated.CreateReliableFileTransferInputType;
import org.globus.rft.generated.CreateReliableFileTransferOutputType;
import org.globus.rft.generated.DeleteRequestType;
import org.globus.rft.generated.OverallStatus;
import org.globus.rft.generated.RFTFaultResourcePropertyType;
import org.globus.rft.generated.ReliableFileTransferFactoryPortType;
import org.globus.rft.generated.ReliableFileTransferPortType;
import org.globus.rft.generated.TransferRequestType;
import org.globus.rft.generated.service.ReliableFileTransferFactoryServiceAddressingLocator;
import org.globus.rft.generated.service.ReliableFileTransferServiceAddressingLocator;
import org.globus.transfer.reliable.client.utils.LogFileUtils;
import org.globus.transfer.reliable.service.RFTConstants;

import org.globus.wsrf.container.ServiceContainer;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.client.BaseClient;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.ResourcePDPConfig;
import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;
import org.globus.wsrf.impl.security.descriptor.GSISecureMsgAuthMethod;
import org.globus.wsrf.impl.security.descriptor.GSISecureConvAuthMethod;
import org.globus.wsrf.impl.security.descriptor.GSITransportAuthMethod;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.security.Constants;
import org.globus.wsrf.utils.AddressingUtils;
import org.oasis.wsn.Subscribe;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.faults.BaseFaultType;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

/**
 * BaseClient for RFT service
 */
public class BaseRFTClient extends BaseClient implements NotifyCallback {
    private static Log logger = LogFactory.getLog(BaseRFTClient.class);
    public static ReliableFileTransferServiceAddressingLocator rftLocator = 
        new ReliableFileTransferServiceAddressingLocator();

    static final String SERVICE_URL_ROOT = "/wsrf/services/";
    
    public static ReliableFileTransferFactoryServiceAddressingLocator 
         rftFactoryLocator = 
             new ReliableFileTransferFactoryServiceAddressingLocator();

    private static ReliableFileTransferFactoryPortType factoryPort;

    private static NotificationConsumerManager consumer = null;

    public static int transferCount = 0;

    public static int finished = 0;

    public static int failed = 0;

    
    public static URL endpoint = null; 
    
    public static String HOST = "127.0.0.1";
    
    public static String PORT = null;

    public static String PROTOCOL = "http";
    
    public static int TERM_TIME = 60;
    
    public static String PATH_TO_FILE = null;
    
    public static String AUTHZ = "host";
    
    public static String cmd[];

    public static String outFileName = null;

    public static Object authType = Constants.GSI_TRANSPORT;

    public static Object authVal = Constants.SIGNATURE;
    
    public static Authorization authzVal = HostAuthorization.getInstance();
    
    public static String optionString = 
            "rft [options] -f <path to transfers file>\n"
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
        + " -file filename to write EPR of created Reliable"
        + " File Transfer Resource\n";
    
    static {
        Util.registerTransport();
    }

    
    public BaseRFTClient() {
        super();
    }
    
    public static void parseArgs() {
        for(int i =0; i< cmd.length; i++) {
            if ((cmd[i].equals("--help")) || (cmd[i].equals("-help"))) {
                System.out.println( optionString );
                System.exit(0);
            } else if(cmd[i].equals("-h")) {
                HOST = getValue(i);
                i++;
            } else if(cmd[i].equals("-r")) {
                PORT = getValue(i);
                i++;
            } else if(cmd[i].equals("-l")) {
                TERM_TIME = Integer.parseInt(getValue(i));
                i++;
            } else if(cmd[i].equals("-z")) {
                AUTHZ = getValue(i);
                authzVal = AuthUtil.getClientAuthorization(AUTHZ);
                i++;
            } else if(cmd[i].equals("-m")) {
                String secType = getValue(i);
                if (secType.equals("msg")) {
                    authType = Constants.GSI_SEC_MSG;
                } else if (secType.equals("conv")) {
                    authType = Constants.GSI_SEC_CONV;
                } else if(secType.equals("trans")) {
                    authType = Constants.GSI_TRANSPORT;
                }
            } else if (cmd[i].equals("-p")) {
                String prot = getValue(i);
                if (prot.equals("sig")) {
                    authVal = Constants.SIGNATURE;
                } else if(prot.equals("enc")) {
                    authVal = Constants.ENCRYPTION;
                }
            } else if (cmd[i].equals("-file")) {
                outFileName = getValue(i);
            } else if (cmd[i].equals("-f")) {
                PATH_TO_FILE = getValue(i);
            } 
        }
        if (PATH_TO_FILE == null) {
            printUsage();
            System.exit(-1);
        }
    }
    protected static String getValue(int i) {
        if (i + 1 > cmd.length) {
            System.err.println(cmd[i] + " needs a argument");
            System.exit(-1);
        }
        return cmd[i+1];
    }

    /**
     * @param request
     * @return rft epr
     * @exception Exception
     */
    public static EndpointReferenceType createRFT(String rftFactoryAddress, 
            BaseRequestType request) 
    throws Exception {
        endpoint = new URL(rftFactoryAddress);
        factoryPort = rftFactoryLocator
                .getReliableFileTransferFactoryPortTypePort(endpoint);
        CreateReliableFileTransferInputType input = 
            new CreateReliableFileTransferInputType();
        //input.setTransferJob(transferType);
        if(request instanceof TransferRequestType) {
            input.setTransferRequest((TransferRequestType)request);
        } else {
            input.setDeleteRequest((DeleteRequestType)request);
        }
        Calendar termTime = Calendar.getInstance();
        termTime.add(Calendar.HOUR, 1);
        input.setInitialTerminationTime(termTime);
        setSecurity((Stub)factoryPort); 
        CreateReliableFileTransferOutputType response = factoryPort
                .createReliableFileTransfer(input);

        return response.getReliableTransferEPR();
    }
    /**
     * Prints the usage
     */
    public static void printUsage() {
        System.out.println(optionString);
    }
    /**
     * 
     * @param topicPath
     * @param producer
     * @param message
     */
    public void deliver(List topicPath, EndpointReferenceType producer,
            Object message) {
        ResourcePropertyValueChangeNotificationType changeMessage =
            ((ResourcePropertyValueChangeNotificationElementType) message)
                .getResourcePropertyValueChangeNotification();
        BaseFaultType fault = null;
        try {
        	
            if (changeMessage != null) {

                OverallStatus overallStatus = (OverallStatus) changeMessage
                        .getNewValue().get_any()[0].getValueAsType(
                        RFTConstants.OVERALL_STATUS_RESOURCE,
                        OverallStatus.class);
                System.out.println("\n Overall status of transfer:");
                System.out.println("Finished/Active/Failed/Retrying/Pending");
                System.out.print(overallStatus.getTransfersFinished() + "/");
                System.out.print(overallStatus.getTransfersActive() + "/");
                System.out.print(overallStatus.getTransfersFailed() + "/");
                System.out.print(overallStatus.getTransfersRestarted() + "/");
                System.out.println(overallStatus.getTransfersPending());
                if ( finished < overallStatus.getTransfersFinished()) {
                    finished = overallStatus.getTransfersFinished();
                }
                if (failed < overallStatus.getTransfersFailed()) {
                    failed = overallStatus.getTransfersFailed();
                }
                transferCount = overallStatus.getTransfersFinished() + overallStatus.getTransfersActive() 
                    + overallStatus.getTransfersFailed() + overallStatus.getTransfersRestarted() 
                    + overallStatus.getTransfersPending();
                RFTFaultResourcePropertyType faultRP = overallStatus.getFault();
                if(faultRP != null) {
                    fault = getFaultFromRP(faultRP);
                }
                if (fault != null) {
                    System.err.println("Error:" + fault.getDescription(0));
                }

            }
        } catch (Exception e) {
        	logger.debug(e.getMessage(), e);
            System.err.println(e.getMessage());
        }
    }

    /**
     * @param host
     * @param port
     * @return
     * @throws Exception
     */
    public static EndpointReferenceType
        delegateCredential(String host, String port) throws Exception {
        ClientSecurityDescriptor desc = getClientSecDesc();
        // Credential to sign with, assuming default credential
        GlobusCredential credential = GlobusCredential.getDefaultCredential();
        //desc.setGSITransport(Constants.GSI_TRANSPORT);
        //desc.setAuthz(AuthUtil.getClientAuthorization("self"));
        
        
        String factoryUrl = PROTOCOL + "://" + host + ":" 
                        + port + SERVICE_URL_ROOT
                        + DelegationConstants.FACTORY_PATH;

        // lifetime in seconds
        int lifetime = TERM_TIME * 60;

        // Get the public key to delegate on.
        EndpointReferenceType delegEpr = AddressingUtils
                .createEndpointReference(factoryUrl, null);
        X509Certificate[] certsToDelegateOn = DelegationUtil
                .getCertificateChainRP(delegEpr, desc);
        X509Certificate certToSign = certsToDelegateOn[0];

        // send to delegation service and get epr.
        // Authz set to null, so HostAuthz will be done.
        return DelegationUtil.delegate(factoryUrl,
                    credential, certToSign, lifetime, false, 
                    desc);   
    }
   
   public static void setSecurity(Stub stub) {
       stub._setProperty(Constants.CLIENT_DESCRIPTOR,
               getClientSecDesc());
   }

    public static void 
        subscribe(ReliableFileTransferPortType rft)
        throws Exception {
        Subscribe request = new Subscribe();
        request.setUseNotify(Boolean.TRUE);
        if(PROTOCOL.equals("http")) {
            consumer = NotificationConsumerManager.getInstance();
        } else if (PROTOCOL.equals("https")) {
            Map properties = new HashMap();
            properties.put(ServiceContainer.CLASS,
            "org.globus.wsrf.container.GSIServiceContainer");
            consumer = NotificationConsumerManager.getInstance(properties);
        }
        consumer.startListening();
        EndpointReferenceType consumerEPR = null;
        ResourceSecurityDescriptor resDesc = new ResourceSecurityDescriptor();
        Vector authMethod = new Vector();
        if(AUTHZ.equalsIgnoreCase("host")) {
            ResourcePDPConfig pdpConfig = new ResourcePDPConfig("host");
            pdpConfig.setProperty(Authorization.HOST_PREFIX,
                HostAuthorization.URL_PROPERTY,endpoint);
            ServiceAuthorizationChain authz = new ServiceAuthorizationChain();
            authz.initialize(pdpConfig, "chainName", "someId");
            resDesc.setAuthzChain(authz);
        } else if(AUTHZ.equalsIgnoreCase("self")) {
            resDesc.setAuthz("self");
        }
        if (PROTOCOL.equals("http")) {
            if (authType.equals(Constants.GSI_SEC_MSG)) {
                authMethod.add(GSISecureMsgAuthMethod.BOTH);
            } else if (authType.equals(Constants.GSI_SEC_CONV)) {
                authMethod.add(GSISecureConvAuthMethod.BOTH);
            }
        } else if (PROTOCOL.equals("https")) {
            authMethod.add(GSITransportAuthMethod.BOTH);
        }
        resDesc.setAuthMethods(authMethod);
        consumerEPR = consumer.createNotificationConsumer(
                new BaseRFTClient(), resDesc);
        request.setConsumerReference(consumerEPR);
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect(WSNConstants.SIMPLE_TOPIC_DIALECT);
        topicExpression.setValue(RFTConstants.OVERALL_STATUS_RESOURCE);
        request.setTopicExpression(topicExpression);

        rft.subscribe(request);
    }
    
    private BaseFaultType getFaultFromRP(RFTFaultResourcePropertyType faultRP) {
        if(faultRP.getRftAuthenticationFaultType() != null) {
            return faultRP.getRftAuthenticationFaultType();
        } else if(faultRP.getRftAuthorizationFaultType() != null) {
            return faultRP.getRftAuthorizationFaultType();
        } else if(faultRP.getRftDatabaseFaultType() != null) {
            return faultRP.getRftDatabaseFaultType();
        } else if(faultRP.getRftRepeatedlyStartedFaultType() != null) {
            return faultRP.getRftRepeatedlyStartedFaultType();
        } else if(faultRP.getRftTransferFaultType() != null) {
            return faultRP.getRftTransferFaultType();
        } else if(faultRP.getTransferTransientFaultType() != null) {
            return faultRP.getTransferTransientFaultType();
        } else {
            return null;
        }
    }
    
   public static void setAuthzValue(String authz) {
	   if ("SELF".equals(authz)) {
		   authzVal = HostAuthorization.getInstance();
	   } else if ("HOST".equals(authz)) {
		   authzVal = HostAuthorization.getInstance();
	   }
	   
   }
   
   public static ClientSecurityDescriptor getClientSecDesc() {
       ClientSecurityDescriptor desc = new ClientSecurityDescriptor();
       if (authType.equals(Constants.GSI_SEC_MSG)) {
           desc.setGSISecureMsg((Integer)authVal);
       } else if (authType.equals(Constants.GSI_SEC_CONV)) {
           desc.setGSISecureConv((Integer)authVal);
       } else if (authType.equals(Constants.GSI_TRANSPORT)) {
           desc.setGSITransport((Integer)authVal);
           Util.registerTransport();
       }
       desc.setAuthz(authzVal);       
       return desc;                   
   }

   public static ReliableFileTransferFactoryPortType 
   getFactoryPort(String rftFactoryAddress) throws Exception {
       endpoint = new URL(rftFactoryAddress);
       return rftFactoryLocator.getReliableFileTransferFactoryPortTypePort(endpoint);
   }

}
