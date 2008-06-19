package org.globus.transfer.reliable.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.globus.rft.generated.TransferType;

public class RFTTransferFileParser {
	Vector requestData = new Vector();
	
	public void loadTransferFile(String filePath) throws Exception {
		File transferFile = new File(filePath);
		if (!transferFile.exists() || !transferFile.isFile()
				|| !transferFile.canRead()) {
			throw new IllegalArgumentException(filePath
					+ " is not an illegal transfer file");
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(transferFile));
			

			String line = reader.readLine();
			while (line != null) {
				if (!line.startsWith("#")) {
					if (!line.trim().equals("")) {
						requestData.add(line);
					}
				}
				line = reader.readLine();
			}

			
		} catch (IOException e) {
			throw e;
		} finally {
			reader.close();
		}
	}
	
	public TransferType[] getTransferType() throws Exception {
		int transferCount = (requestData.size() - 11) / 2;

		if (transferCount <= 0) {
			throw new Exception("Invalid transfer file format");
		}

		TransferType[] transfers1 = new TransferType[transferCount];
		int i = 11;
		for (int j = 0; j < transfers1.length; j++) {
			transfers1[j] = new TransferType();
			transfers1[j].setSourceUrl((String) requestData.elementAt(i++));
			transfers1[j].setDestinationUrl((String) requestData
					.elementAt(i++));
		}

		return transfers1;
	}
	
	public RFTOptions getRFTOptions() {
        int i = 0;
        RFTOptions options = new RFTOptions();
        
        options.setBinary(Boolean.valueOf((String) requestData.elementAt(i++)));
        options.setBlockSize(Integer.valueOf((String) requestData.elementAt(i++)));
        options.setTcpBufferSize(Integer.valueOf((String) requestData.elementAt(i++)));
        options.setNotpt(Boolean.valueOf((String) requestData.elementAt(i++)));
        options.setParallelStream(Integer.valueOf((String) requestData.elementAt(i++)));
        options.setDcau(Boolean.valueOf((String) requestData.elementAt(i++)));
        options.setConcurrent(Integer.valueOf((String) requestData.elementAt(i++)).intValue());
        options.setSourceDN((String) requestData.elementAt(i++));
        options.setDestDN((String) requestData.elementAt(i++));   
        options.setAllOrNone(Boolean.valueOf((String) requestData.elementAt(i++)).booleanValue());
        options.setMaxAttampts(Integer.valueOf((String) requestData.elementAt(i++)).intValue());
        
        return options;        
	}
}
