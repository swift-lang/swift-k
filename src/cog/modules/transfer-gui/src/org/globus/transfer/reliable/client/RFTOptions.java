package org.globus.transfer.reliable.client;

public class RFTOptions {	
	private int concurrent;
	private int parallelStream;
	private int tcpBufferSize;
	private String destDN;
	private String sourceDN;

	
	public RFTOptions() {
		
	}
	
	public RFTOptions(int concurrent, int parallelStream,
			int tcpBufferSize, String destDN, String sourceDN) {
		super();		
		this.concurrent = concurrent;
		this.parallelStream = parallelStream;
		this.tcpBufferSize = tcpBufferSize;
		this.destDN = destDN;
		this.sourceDN = sourceDN;
	}

	public int getConcurrent() {
		return concurrent;
	}

	public void setConcurrent(int concurrent) {
		this.concurrent = concurrent;
	}

	public int getParallelStream() {
		return parallelStream;
	}

	public void setParallelStream(int parallelStream) {
		this.parallelStream = parallelStream;
	}

	public int getTcpBufferSize() {
		return tcpBufferSize;
	}

	public void setTcpBufferSize(int tcpBufferSize) {
		this.tcpBufferSize = tcpBufferSize;
	}

	public String getDestDN() {
		return destDN;
	}

	public void setDestDN(String destDN) {
		this.destDN = destDN;
	}

	public String getSourceDN() {
		return sourceDN;
	}

	public void setSourceDN(String sourceDN) {
		this.sourceDN = sourceDN;
	}

}
