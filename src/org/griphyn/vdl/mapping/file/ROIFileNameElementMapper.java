package org.griphyn.vdl.mapping.file;

import java.util.HashMap;
import java.util.Map;

public class ROIFileNameElementMapper implements FileNameElementMapper {
	private Map basenames;
	int index;
	
	public ROIFileNameElementMapper () {
		basenames = new HashMap();
		index = 0;
	}
	
	public String mapField(String fieldName) {
        if ("roi".equals(fieldName)) {
            return "";
        }
        if ("image".equals(fieldName)) {
        	return "ROI";
        }
        if ("center".equals(fieldName)) {
        	return "ROI.center";
        }
		return fieldName;
	}

	public String rmapField(String pathElement) {
		return pathElement;
	}

	public String mapIndex(int index) {
		return (String)(basenames.get(new Integer(index)));
	}

	public int rmapIndex(String pathElement) {
		basenames.put(new Integer(index), pathElement);
		return index++;
	}

	public String getSeparator(int depth) {
        if (depth <= 1) {
            return "";
        }
		return ".";
	}
}
