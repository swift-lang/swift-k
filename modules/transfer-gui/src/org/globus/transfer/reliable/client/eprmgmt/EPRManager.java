package org.globus.transfer.reliable.client.eprmgmt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import javax.xml.namespace.QName;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.xml.sax.InputSource;

public class EPRManager {
	private final static String EPR_DIR = "epr";
	private static Log logger = LogFactory.getLog(EPRManager.class);
	
	public void saveEPR(EndpointReferenceType epr) throws Exception {
		if (null == epr) {
			throw new Exception("can not save a null EPR");
		}		
		
		String resourceKey = epr.getProperties().get_any()[0].getValue();
		if (null == resourceKey || "".equals(resourceKey.trim())) {
			resourceKey = new Long(System.currentTimeMillis()).toString();
		}
		
		QName qname = new QName("", "epr");
		File file = new File(EPR_DIR, resourceKey);
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
	
	public EndpointReferenceType getEPR(String resourceKey) throws Exception {
		if (null == resourceKey || "".equals(resourceKey.trim())) {
			throw new Exception("resource key can not be null");
		}
		
		File file = new File(EPR_DIR, resourceKey);
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		
		Reader reader = null;
		try {
			reader = new FileReader(file);
			InputSource inputSource = new InputSource(reader);
			EndpointReferenceType ret = (EndpointReferenceType)ObjectDeserializer.deserialize(
					inputSource, EndpointReferenceType.class);
			
			return ret;
		} catch(Exception e) {
			logger.debug(e.getMessage(), e);
		} finally {
			if (null != reader) {
				reader.close();
			}
		}
		
		return null;
	}
	
	public boolean deleteEPR(String resourceKey) {
		if (null == resourceKey || "".equals(resourceKey.trim())) {
			return true;
		}
		
		File file = new File(EPR_DIR, resourceKey);
		return file.delete();
	}
	
	public static void main(String[] args) {
		EPRManager e = new EPRManager();
		EndpointReferenceType p;
		try {
			p = e.getEPR("22558696");
			//System.out.println(p);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}
