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


/** Maps the parts of a Path to parts of a data file filename. Methods
    must be implemented to map both ways between field/array indexes and
    filename fragments.
  */
public interface FileNameElementMapper {

	String mapField(String fieldName);
	String rmapField(String pathElement);
	
	String mapIndex(int index, int pos);
	String mapIndex(Object index, int pos);
	
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
