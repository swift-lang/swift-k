package org.globus.transfer.reliable.client.eprmgmt;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.xml.sax.InputSource;

public class EPRManager {	
	private final static String ERP_FILE_SUFFIX = ".epr";
	private List<EndpointReferenceType> eprList;
	private static Log logger = LogFactory.getLog(EPRManager.class);
	
	public EPRManager() {
		eprList = new ArrayList<EndpointReferenceType>();
	}
	
	public boolean deleteEPRFile(String location, String resourceKey) throws IllegalArgumentException {
		if (null == location) {
			 throw new IllegalArgumentException("EPR location can not be null");
		}
		 
		if (null == resourceKey || "".equals(resourceKey.trim())) {
			throw new IllegalArgumentException("resource key can not be null");
		}
		
		String eprFileName = resourceKey + ERP_FILE_SUFFIX;
		File file = new File(location, eprFileName);
		return file.delete();
	}
	
	public void addEPR (EndpointReferenceType epr) {		
		eprList.add(epr);
	}
	
	public void removeEPR (EndpointReferenceType epr) {
		eprList.remove(epr);
	}
	
	
	public int getNumEPR () {
		return eprList.size();
	}
	
	public EndpointReferenceType getEPR (int index) throws IllegalArgumentException {
		if (index < 0 || index >= eprList.size()) {
			throw new IllegalArgumentException("index is invalid");
		}
		
		return eprList.get(index);
	}
	
	public EndpointReferenceType getEPR (String resourceKey) throws IllegalArgumentException{
		if (null == resourceKey) {
			throw new IllegalArgumentException("resource key can not be null");
		}
		
		Iterator<EndpointReferenceType> iter = eprList.iterator();
		EndpointReferenceType ret = null;
		while (iter.hasNext()) {
			ret = iter.next();
			if (resourceKey.equals(ret.getProperties().get_any()[0].getValue())) {
				return ret;
			}
		}
		
		return null;
	}
	
	public String getResourceKey (int index) throws IllegalArgumentException {
		if (index < 0 || index >= eprList.size()) {
			throw new IllegalArgumentException("index is invalid");
		}
		
		EndpointReferenceType epr = eprList.get(index);
		return epr.getProperties().get_any()[0].getValue();
	}
	
	public List<EndpointReferenceType> getEPRList () {
		return eprList;
	}
	
	public void writeEPRList (String location) throws Exception{
		 if (null == location) {
			 throw new IllegalArgumentException("EPR location can not be null");
		 }
		 
		 File eprDir = new File(location);
		 if (!eprDir.exists() || !eprDir.isDirectory()) {
			 eprDir.mkdirs();			 
		 }
		 
		 Iterator<EndpointReferenceType> iter = eprList.iterator();
		 while (iter.hasNext()) {
			 saveEPR(iter.next(), eprDir);
		 }
		 
	}
	
	public void readEPRList (String location) throws Exception {
		if (null == location) {
			 throw new IllegalArgumentException("EPR location can not be null");
		}
		
		File eprDir = new File(location);
		if (!eprDir.exists() || !eprDir.isDirectory()) {
			 throw new Exception("the directory for storing EPR does not exist, read operation failed");			 
		}
		
		List<EndpointReferenceType> savedEPRs = readAll(eprDir);
		eprList.addAll(savedEPRs);		
	}
	
	private void saveEPR(EndpointReferenceType epr, File eprDir) throws Exception {
		if (null == epr) {
			throw new Exception("can not save a null EPR");
		}		
		
		String resourceKey = epr.getProperties().get_any()[0].getValue();
		if (null == resourceKey || "".equals(resourceKey.trim())) {
			resourceKey = new Long(System.currentTimeMillis()).toString();
		}
		
		QName qname = new QName("", "epr");
		String eprFileName = resourceKey + ERP_FILE_SUFFIX;
		File file = new File(eprDir, eprFileName);
		Writer writer = null;
		try {
			writer = new FileWriter(file);
			ObjectSerializer.serialize(writer, epr, qname);
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw e;
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
		
	}
	
	private List<EndpointReferenceType> readAll(File eprDir) throws Exception {
		List<EndpointReferenceType> ret = new ArrayList<EndpointReferenceType>();
		File[] eprFiles = eprDir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if(file.isFile() && file.getName().endsWith(ERP_FILE_SUFFIX)) {
					return true;
				}
				
				return false;
			}
		});
		
		if (null != eprFiles) {
			Reader reader = null;
			for (int i = 0; i < eprFiles.length; i++) {
				try {
					reader = new FileReader(eprFiles[i]);
					InputSource inputSource = new InputSource(reader);
					EndpointReferenceType epr = (EndpointReferenceType)ObjectDeserializer.deserialize(
							inputSource, EndpointReferenceType.class);
					ret.add(epr);					
				} catch (Exception e) {
					logger.debug(e.getMessage(), e);
				} finally {
					if (null != reader) {
						reader.close();
						reader = null;
					}					
				}				
			}
		}
		
		return ret;
	}
}
