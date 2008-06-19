package org.globus.transfer.reliable.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.globus.rft.generated.TransferType;

public class RFTTransferParam {
	private TransferType[] transfers1 = null;
	private String serverHost = null;
	private String serverPort = null;
	private String authType = null;
	private String authzType = null;

	public RFTTransferParam(String from, String to, String serverHost,
			String serverPort, String authType, String authzType) {
		transfers1 = new TransferType[1];
		transfers1[0] = new TransferType();
		transfers1[0].setSourceUrl(from);
		transfers1[0].setDestinationUrl(to);
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.authType = authType;
		this.authzType = authzType;
	}

	public RFTTransferParam(TransferType[] transferTypes, String serverHost,
			String serverPort, String authType, String authzType) throws Exception {
		transfers1 = transferTypes;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.authType = authType;
		this.authzType = authzType;
	}
	
	

	public TransferType[] getTransfers1() {
		return transfers1;
	}

	public void setTransfers1(TransferType[] transfers1) {
		this.transfers1 = transfers1;
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getAuthzType() {
		return authzType;
	}

	public void setAuthzType(String authzType) {
		this.authzType = authzType;
	}	
}
