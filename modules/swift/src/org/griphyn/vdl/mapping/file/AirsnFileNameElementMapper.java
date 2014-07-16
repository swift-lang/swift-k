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


/*
 * Created on Jun 8, 2006
 */
package org.griphyn.vdl.mapping.file;


public class AirsnFileNameElementMapper extends AbstractFileNameElementMapper {
	public static final int INDEX_WIDTH = 4;

	public String mapField(String fieldName) {
        if ("v".equals(fieldName)) {
            return "";
        }
		return fieldName;
	}

	public String rmapField(String pathElement) {
		return pathElement;
	}

	public String mapIndex(int index, int pos) {
		StringBuffer sb = new StringBuffer();
        sb.append('i');
		String num = String.valueOf(index);
		for (int i = 0; i < INDEX_WIDTH - num.length(); i++) {
			sb.append('0');
		}
		sb.append(num);
		return sb.toString();
	}

	public int rmapIndex(String pathElement) {
		return Integer.parseInt(pathElement);
	}

	public String getSeparator(int depth) {
        if (depth == 1) {
            return "";
        }
		return "_";
	}
}
