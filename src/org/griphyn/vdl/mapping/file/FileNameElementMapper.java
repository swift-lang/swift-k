/*
 * Created on Jun 8, 2006
 */
package org.griphyn.vdl.mapping.file;

public interface FileNameElementMapper {
	String mapField(String fieldName);
	String rmapField(String pathElement);
	
	String mapIndex(int index);
	int rmapIndex(String pathElement);
	
	String getSeparator(int depth);
}
