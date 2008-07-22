package org.globus.transfer.reliable.client;

public class RFTOptions {
	//private int blockSize;
	private int concurrent;
	private int parallelStream;
	private int tcpBufferSize;
	//private int maxAttampts;
	//private boolean dcau;
	//private boolean notpt;
	//private boolean binary;
	//private boolean allOrNone;
	private String destDN;
	private String sourceDN;

	
	public RFTOptions() {
		
	}
	
	public RFTOptions(int concurrent, int parallelStream,
			int tcpBufferSize, String destDN, String sourceDN) {
		super();
		//this.blockSize = blockSize;
		this.concurrent = concurrent;
		this.parallelStream = parallelStream;
		this.tcpBufferSize = tcpBufferSize;
//		this.maxAttampts = maxAttampts;
//		this.dcau = dcau;
//		this.notpt = notpt;
//		this.binary = binary;
//		this.allOrNone = allOrNone;
		this.destDN = destDN;
		this.sourceDN = sourceDN;
	}

//	public int getBlockSize() {
//		return blockSize;
//	}
//
//	public void setBlockSize(int blockSize) {
//		this.blockSize = blockSize;
//	}

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

//	public int getMaxAttampts() {
//		return maxAttampts;
//	}
//
//	public void setMaxAttampts(int maxAttampts) {
//		this.maxAttampts = maxAttampts;
//	}

//	public boolean isDcau() {
//		return dcau;
//	}
//
//	public void setDcau(boolean dcau) {
//		this.dcau = dcau;
//	}
//
//	public boolean isNotpt() {
//		return notpt;
//	}
//
//	public void setNotpt(boolean notpt) {
//		this.notpt = notpt;
//	}
//
//	public boolean isBinary() {
//		return binary;
//	}
//
//	public void setBinary(boolean binary) {
//		this.binary = binary;
//	}
//
//	public boolean isAllOrNone() {
//		return allOrNone;
//	}
//
//	public void setAllOrNone(boolean allOrNone) {
//		this.allOrNone = allOrNone;
//	}

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
