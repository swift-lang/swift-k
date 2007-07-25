package org.griphyn.vdl.mapping.file;


/** Maps the parts of a Path to parts of a data file filename. Methods
    must be implemented to map both ways between field/array indexes and
    filename fragments.
  */
public interface FileNameElementMapper {

	String mapField(String fieldName);
	String rmapField(String pathElement);
	
	String mapIndex(int index);
	int rmapIndex(String pathElement);

	/** Returns a string which will be used as a separator string between
	  * the fields of a filename mapped with this, both when constructing
	  * filenames from a Path and from constructing a Path from a filename.
	  *
	  * @param depth the depth inside the dataset hierarchy that this
	  * separator will appear. The highest requested separator is at
	  * depth 0. Each succesively level will be requested with increasing
	  * depth values.
	  */
	String getSeparator(int depth);
}
