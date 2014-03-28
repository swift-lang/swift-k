/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.mapping.file;

import java.util.HashMap;
import java.util.Map;

public class ROIFileNameElementMapper extends AbstractFileNameElementMapper {
	private Map<Integer, String> basenames;
	int index;
	
	public ROIFileNameElementMapper () {
		basenames = new HashMap<Integer, String>();
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

	public String mapIndex(int index, int pos) {
		return basenames.get(index);
	}

	public int rmapIndex(String pathElement) {
		basenames.put(index, pathElement);
		return index++;
	}

	public String getSeparator(int depth) {
        if (depth <= 1) {
            return "";
        }
		return ".";
	}
}
