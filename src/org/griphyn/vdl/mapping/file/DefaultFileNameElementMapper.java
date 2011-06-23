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

public class DefaultFileNameElementMapper implements FileNameElementMapper {

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

	public String mapIndex(int index) {
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
