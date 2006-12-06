/*
 * Created on Jun 8, 2006
 */
package org.griphyn.vdl.mapping.file;


public class DefaultFileNameElementMapper implements FileNameElementMapper {
	public static final int INDEX_WIDTH = 4;

	public String mapField(String fieldName) {
		return fieldName;
	}

	public String rmapField(String pathElement) {
		return pathElement;
	}

	public String mapIndex(int index) {
		StringBuffer sb = new StringBuffer();
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
		return "_";
	}
}
