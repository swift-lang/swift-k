package org.griphyn.vdl.mapping.file;


/** A filename element mapper that maps paths into a fairly deep
    directory hierarchy, with the intention that there will be
    few elements at each level of the hierarchy. This code is
    intended to be used in naming 'anonymous' files and so there
    is no requirement that the way it lays files out remains
    the same from version to version.
  */

public class ConcurrentElementMapper implements FileNameElementMapper {

	/** determines how many directories and element files are permitted
	    in each directory. There will be no more than
	    DIRECTORY_LOAD_FACTOR element files and no more than
	    DIRECTORY_LOAD_FACTOR directories, so there could be up to
	    2 * DIRECTORY_LOAD_FACTOR elements. */
	public final static int DIRECTORY_LOAD_FACTOR=25;

	public String mapField(String fieldName) {
		return "-field/"+fieldName;
	}

	public String rmapField(String pathElement) {
		return pathElement;
	}

	public String mapIndex(int index) {
		StringBuffer sb = new StringBuffer();
		sb.append("-array/");
		sb.append(splitIndexByLoadFactor(index));
		sb.append("/");
		sb.append("elt-"+String.valueOf(index));
		return sb.toString();
	}


	String splitIndexByLoadFactor(int index)
	{
		if(index<=DIRECTORY_LOAD_FACTOR) {
			return "";
		} else {
			String prefix = "h"+String.valueOf(index % DIRECTORY_LOAD_FACTOR) +"/";
			String suffix = splitIndexByLoadFactor(index/DIRECTORY_LOAD_FACTOR);
			return prefix + suffix;
		}
	}


	public int rmapIndex(String pathElement) {
		return Integer.parseInt(pathElement);
	}

	public String getSeparator(int depth) {
		return "_";
	}
}
