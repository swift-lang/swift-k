/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.globus.transfer.reliable.client;


import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.globus.gsi.GSIConstants;
import org.globus.ogce.beans.filetransfer.gui.monitor.QueuePanel;
import org.globus.rft.generated.BaseRequestType;
import org.globus.rft.generated.Cancel;
import org.globus.rft.generated.GetStatusSet;
import org.globus.rft.generated.GetStatusSetResponse;
import org.globus.rft.generated.OverallStatus;
import org.globus.rft.generated.RFTDatabaseFaultType;
import org.globus.rft.generated.RFTFaultResourcePropertyType;
import org.globus.rft.generated.RFTOptionsType;
import org.globus.rft.generated.ReliableFileTransferPortType;
import org.globus.rft.generated.RequestStatusType;
import org.globus.rft.generated.Start;
import org.globus.rft.generated.StartOutputType;
import org.globus.rft.generated.TransferRequestType;
import org.globus.rft.generated.TransferStatusType;
import org.globus.rft.generated.TransferStatusTypeEnumeration;
import org.globus.rft.generated.TransferType;
import org.globus.transfer.reliable.client.BaseRFTClient;
import org.globus.transfer.reliable.client.utils.UIConstants;
import org.globus.transfer.reliable.service.RFTConstants;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.container.ServiceContainer;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.ResourcePDPConfig;
import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;
import org.globus.wsrf.impl.security.descriptor.GSISecureConvAuthMethod;
import org.globus.wsrf.impl.security.descriptor.GSISecureMsgAuthMethod;
import org.globus.wsrf.impl.security.descriptor.GSITransportAuthMethod;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.security.Constants;
import org.oasis.wsn.Subscribe;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.faults.BaseFaultType;
import org.oasis.wsrf.lifetime.SetTerminationTime;
import org.oasis.wsrf.lifetime.SetTerminationTimeResponse;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

/**
 * A  Client for RFT, extends BaseRFTClient
 * @author Liu Wantao liuwt@uchicago.edu
 */
public class RFTClient extends BaseRFTClient {
	private static NotificationConsumerManager consumer = null;
	private Map epr2ID = new HashMap();
	private Map ID2Stub = new HashMap();
	private QueuePanel queuePanel = null;
	private RFTButtonActionListener listener = null;
	private TransferStatusReporter reporter = null;
    public RFTClient(RFTButtonActionListener listener, QueuePanel queuePanel) {    	
    	super();
    	this.listener = listener;
    	this.queuePanel = queuePanel;
    	
    	new TransferStatusReporter().start();
    }
    
    /**
     * Generate RFT transfer request
     * @param options       RFT options
     * @param rftParam      RFT Transfer Parameters
     * @param epr           
     * @return
     */
    public TransferRequestType getTransferRequestType(RFTOptions options, RFTTransferParam rftParam, EndpointReferenceType epr) {
               
        //set RFT options
        RFTOptionsType rftOptions = new RFTOptionsType();
        rftOptions.setBinary(options.isBinary());
        rftOptions.setBlockSize(options.getBlockSize());
        rftOptions.setDcau(options.isDcau());        
        rftOptions.setNotpt(options.isNotpt());
        rftOptions.setParallelStreams(options.getParallelStream());        
        rftOptions.setTcpBufferSize(options.getTcpBufferSize());
        
        String sourceSubjectName = options.getSourceDN();
        if (null != sourceSubjectName) {
            rftOptions.setSourceSubjectName(sourceSubjectName);
        }
        String destSubjectName = options.getDestDN();
        if (null != destSubjectName) {
            rftOptions.setDestinationSubjectName(destSubjectName);
        }
        
        TransferRequestType request = new TransferRequestType();
        request.setRftOptions(rftOptions);
        request.setTransfer(rftParam.getTransfers1());
        request.setAllOrNone(options.isAllOrNone());
        request.setConcurrency(options.getConcurrent());
        request.setMaxAttempts(options.getMaxAttampts());
        request.setTransferCredentialEndpoint(epr);
        
        return request;
    }
    
    /**
     * Respond to click event of Start button
     * @param job     
     * @throws java.lang.IllegalArgumentException
     */
    public void startTransfer(RFTJob job) throws Exception {
    	RFTOptions options = job.getOptions();
    	RFTTransferParam rftParam = job.getParam();
    	int jobID = job.getJobID();
    	
        String host = rftParam.getServerHost();
        if (null == host) {
            throw new IllegalArgumentException(UIConstants.ILLEGAL_HOST);
        }
        String port = rftParam.getServerPort();
        String authType = rftParam.getAuthType();
        String authzType = rftParam.getAuthzType();
        if (null == port) {
            if (authType.equals(GSIConstants.GSI_TRANSPORT)) {
                port = "8443";
            } else {
                port ="8080";
            }
        }
        
        if (authType.equals(GSIConstants.GSI_TRANSPORT)) {
            PROTOCOL = UIConstants.HTTPS_PROTOCOL;
        }
        
        String rftFactoryAddress = PROTOCOL + "://"+ host + ":" + port + "/wsrf/services/" 
            + "ReliableFileTransferFactoryService";
        String rftServiceAddress = PROTOCOL+ "://" + host + ":" + port + "/wsrf/services/" 
                + "ReliableFileTransferService";
        
        EndpointReferenceType credEPR = delegateCredential(host, port);
        TransferRequestType transferType = getTransferRequestType(options, rftParam, credEPR);                
        EndpointReferenceType rftepr = createRFT(rftFactoryAddress, transferType);
        rftepr.setAddress(new Address(rftServiceAddress));
        ReliableFileTransferPortType rft = rftLocator
                .getReliableFileTransferPortTypePort(rftepr);
        setAuthzValue(authzType);
        setSecurity((Stub)rft);
        
        //For secure notifications
        epr2ID.put(createMapKey(rftepr), Integer.toString(jobID));
        ID2Stub.put(Integer.toString(jobID), rft);
        subscribeRFT(rft);
        System.out.println("Subscribed for overall status");
        //End subscription code
        Calendar termTime = Calendar.getInstance();
        termTime.add(Calendar.MINUTE, TERM_TIME);
        SetTerminationTime reqTermTime = new SetTerminationTime();
        reqTermTime.setRequestedTerminationTime(termTime);
        System.out.println("Termination time to set: " + TERM_TIME + " minutes");
        SetTerminationTimeResponse termRes = rft.setTerminationTime(reqTermTime);
        StartOutputType startresp = rft.start(new Start());
    }  
    
    /**
     * Cancel a specified transfer
     * @param jobID
     * @throws Exception
     */
    public void stopTransfer(String jobID) throws Exception {
    	ReliableFileTransferPortType rft = (ReliableFileTransferPortType)ID2Stub.get(jobID);
    	Cancel cancel = new Cancel();
    	rft.cancel(cancel);
    }
    
    /**
     * Subscribe resource properties of RFT service
     * @param rft
     * @throws Exception
     */
    public void subscribeRFT(ReliableFileTransferPortType rft) throws Exception {
		Subscribe request = new Subscribe();
		request.setUseNotify(Boolean.TRUE);
		if (PROTOCOL.equals("http")) {
			consumer = NotificationConsumerManager.getInstance();
		} else if (PROTOCOL.equals("https")) {
			Map properties = new HashMap();
			properties.put(ServiceContainer.CLASS, "org.globus.wsrf.container.GSIServiceContainer");
			consumer = NotificationConsumerManager.getInstance(properties);
		}
		consumer.startListening();
		EndpointReferenceType consumerEPR = null;
		ResourceSecurityDescriptor resDesc = new ResourceSecurityDescriptor();
		Vector authMethod = new Vector();
		if (AUTHZ.equalsIgnoreCase("host")) {
			ResourcePDPConfig pdpConfig = new ResourcePDPConfig("host");
			pdpConfig.setProperty(Authorization.HOST_PREFIX,
					HostAuthorization.URL_PROPERTY, endpoint);
			ServiceAuthorizationChain authz = new ServiceAuthorizationChain();
			authz.initialize(pdpConfig, "chainName", "someId");
			resDesc.setAuthzChain(authz);
		} else if (AUTHZ.equalsIgnoreCase("self")) {
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
		consumerEPR = consumer.createNotificationConsumer(this, resDesc);
		request.setConsumerReference(consumerEPR);
		TopicExpressionType topicExpression = new TopicExpressionType();
		topicExpression.setDialect(WSNConstants.SIMPLE_TOPIC_DIALECT);
		topicExpression.setValue(RFTConstants.REQUEST_STATUS_RESOURCE);
		request.setTopicExpression(topicExpression);		
		rft.subscribe(request);
		
		topicExpression.setValue(RFTConstants.OVERALL_STATUS_RESOURCE);
		request.setTopicExpression(topicExpression);
		rft.subscribe(request);
	}
    
    /**
	 * Call back method for subscription
	 */
    public void deliver(List topicPath, EndpointReferenceType producer, Object message) {
        ResourcePropertyValueChangeNotificationType changeMessage =
            ((ResourcePropertyValueChangeNotificationElementType) message)
                .getResourcePropertyValueChangeNotification();
        BaseFaultType fault = null;
        //System.out.println(((QName)topicPath.get(0)).toString());
        try {        	
            if (changeMessage != null) {
				QName topicQName = (QName) topicPath.get(0);
				if (RFTConstants.REQUEST_STATUS_RESOURCE.equals(topicQName)) {
					RequestStatusType requestStatus = (RequestStatusType) changeMessage
							.getNewValue().get_any()[0].getValueAsType(
							RFTConstants.REQUEST_STATUS_RESOURCE,
							RequestStatusType.class);
					System.out.println("\n Transfer Request Status:"
							+ requestStatus.getRequestStatus().getValue());

					String jobID = (String) epr2ID.get(createMapKey(producer));
					int rowIndex = queuePanel.getRowIndex(jobID);
					queuePanel.setColumnValue(rowIndex, 5, requestStatus
							.getRequestStatus().getValue());

					RFTFaultResourcePropertyType faultRP = requestStatus
							.getFault();
					if (faultRP != null) {
						fault = getFaultFromRP(faultRP);
					}
					if (fault != null) {
						System.err.println("Error:" + fault.getDescription(0));
						queuePanel.setColumnValue(rowIndex, 7, fault
								.getDescription(0).get_value());
					}
				} else if (RFTConstants.OVERALL_STATUS_RESOURCE.equals(topicQName)) {
					OverallStatus overallStatus = (OverallStatus) changeMessage
							.getNewValue().get_any()[0].getValueAsType(
							RFTConstants.OVERALL_STATUS_RESOURCE,
							OverallStatus.class);
					int finished = overallStatus.getTransfersFinished();
					int active = overallStatus.getTransfersActive();
					int failed = overallStatus.getTransfersFailed();
					int retrying = overallStatus.getTransfersRestarted();
					int pending = overallStatus.getTransfersPending();
					int cancelled = overallStatus.getTransfersCancelled();
					System.out.println("\n Overall status of transfer:");
	                System.out.println("Finished/Active/Failed/Retrying/Pending/Cancelled");
	                System.out.print(overallStatus.getTransfersFinished() + "/");
	                System.out.print(overallStatus.getTransfersActive() + "/");
	                System.out.print(overallStatus.getTransfersFailed() + "/");
	                System.out.print(overallStatus.getTransfersRestarted() + "/");
	                System.out.print(overallStatus.getTransfersPending() + "/");
	                System.out.print(overallStatus.getTransfersCancelled());                
	                listener.updateOverallStatus(finished, active, failed, retrying, pending, cancelled);
				}


            }
        } catch (Exception e) {
        	e.printStackTrace();
            System.err.println(e.getMessage());
        }
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
    
    private String createMapKey(EndpointReferenceType epr) {
    	String ret = null;
    	if (null != epr) {
    		String address = epr.getAddress().toString();
    		QName qname = new QName("http://www.globus.org/namespaces/2004/10/rft", "TransferKey");
    		String resourceKey = epr.getProperties().get(qname).getValue();
    		ret = new String(address + ":" + resourceKey);
    	}
    	
    	return ret;
    }
    
    class TransferStatusReporter extends Thread {
    	public void run() {
    		while (true) {
    			//int length = queuePanel.tableSize();
        		int i = 0;
        		while (i < queuePanel.tableSize()) {
        			int jobID = Integer.parseInt(queuePanel.getColumnValue(i, 0));
        			int j = 1;
        			while (j + i < queuePanel.tableSize()) {
        				int nextJobID = Integer.parseInt(queuePanel.getColumnValue(j + i, 0));
        				if (jobID == nextJobID) {
        					//System.out.println("jobid=" + jobID + ", nextjobid=" + nextJobID + ", j=" + j);
        					j++;
        				} else {
        					break;
        				}
        			}
        			
        			ReliableFileTransferPortType rft = (ReliableFileTransferPortType)ID2Stub.get(Integer.toString(jobID));
        			if (null != rft) {
            			try {
        					GetStatusSetResponse response = rft.getStatusSet(new GetStatusSet(1, j));        					
        					for (int k = 0; k < j; k++) {
        						TransferStatusType statusType = response.getTransferStatusSet(k);
        						String status = statusType.getStatus().getValue();
        						if (i + k < queuePanel.tableSize()) {
        							queuePanel.setColumnValue(i + k, 5, status);
        						}        						
        					}
        				} catch (Exception e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				} 
        				
        				i+=j;
        			}

        		}
        		
        		try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		
    	}
    }
}
