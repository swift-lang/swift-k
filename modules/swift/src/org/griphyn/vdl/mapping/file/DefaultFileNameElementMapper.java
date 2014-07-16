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


/** A filename element mapper that maps according to basic rules.
  * <ul>
  *   <li>The single character "_" will be used as a separator between
  *       filename components.</li>
  *   <li>field names map directly - the filename component is used as
  *       field name and vice-versa.</li>
  *   <li>numerical values map to array indices. When mapping from an
  *       an array index to a filename element, the number will be
  *       padded to 4 digits if necessary.</li>
  * </ul>
  */

public class DefaultFileNameElementMapper extends AbstractFileNameElementMapper {

	public static final int DEFAULT_INDEX_WIDTH = 4;

	public int indexWidth;

	public DefaultFileNameElementMapper() {
		this(DEFAULT_INDEX_WIDTH);
	}

	public DefaultFileNameElementMapper(int indexWidth) {
		this.indexWidth = indexWidth;
	}

	public String mapField(String fieldName) {
		return fieldName;
	}

	public String rmapField(String pathElement) {
		return pathElement;
	}

	public String mapIndex(int index, int pos) {
		StringBuilder sb = new StringBuilder(indexWidth);
		String num = String.valueOf(index);
		for (int i = 0; i < indexWidth - num.length(); i++) {
			sb.append('0');
		}
		sb.append(num);
		return sb.toString();
	}

	public int rmapIndex(String pathElement) {
		return Integer.parseInt(pathElement);
	}

	public String getSeparator(int depth) {
		return "_";
	}
}
