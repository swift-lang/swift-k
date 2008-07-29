package org.globus.transfer.reliable.client;


import org.globus.rft.generated.TransferType;

public class RFTTransferParam {
	private TransferType[] transfers1 = null;
	private String serverHost = null;
	private String serverPort = null;

	public RFTTransferParam(String from, String to, String serverHost,
			String serverPort) {
		transfers1 = new TransferType[1];
		transfers1[0] = new TransferType();
		transfers1[0].setSourceUrl(from);
		transfers1[0].setDestinationUrl(to);
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}

	public RFTTransferParam(TransferType[] transferTypes, String serverHost,
			String serverPort) throws Exception {
		transfers1 = transferTypes;
		this.serverHost = serverHost;
		this.serverPort = serverPort;

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
}
