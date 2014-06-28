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


/** A filename element mapper that maps paths into a fairly deep
    directory hierarchy, with the intention that there will be
    few elements at each level of the hierarchy. This code is
    intended to be used in naming 'anonymous' files and so there
    is no requirement that the way it lays files out remains
    the same from version to version.
  */

public class ConcurrentElementMapper extends AbstractFileNameElementMapper {

	public String mapField(String fieldName) {
		return fieldName;
	}

	public String rmapField(String pathElement) {
		return pathElement;
	}

	public String mapIndex(int index, int pos) {
		return indexToPath(index);
	}

	/**
     * Indices 0 to 9 -> [0] ... [9]
     * Indices 10 to 99 -> [d0-d9]/[d0]...[d9]
     * Indices 100 to 999 -> [d00-d99]/[de0-de9]/...
     * ...
     * etc.
     */
	public static String indexToPath(int index) {
	    boolean neg = false;
	    if (index < 0) {
	        neg = true;
	        index = -index;
	    }
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("/");
	    indexToPath(sb, neg, 0, index);
	    return sb.toString();
	}


	public static void indexToPath(StringBuilder sb, boolean negative, int prefix, int index) {
	    if (index < 10) {
	        sb.append('[');
            if (negative) {
                sb.append('-');
            }
            if (prefix != 0) {
                sb.append(prefix / 10);
            }
            sb.append(index);
            sb.append(']');
	    }
	    else {
            int t = getFirstDigit(index);
            int o = getOrder(index);
            sb.append("[");
            if (negative) {
                sb.append('-');
            }
            if (prefix != 0) {
                sb.append(prefix / o / 10);
            }
            sb.append(t);
            sb.append('-');
            if (prefix != 0) {
                sb.append(prefix / o / 10);
            }
            sb.append(t + o - o / 10);
            sb.append("]/");
            indexToPath(sb, negative, prefix + t, index - t);
	    }
    }

	/**
	 * Assume n >= 10
	 * Return n with all but the most significant digit
	 * set to 0
	 */
    private static int getFirstDigit(int n) {
        int order = 1;
        while (n >= 10) {
            n = n / 10;
            order = order * 10;
        }
        return n * order;
    }
    
    /**
     * Assume n >= 10
     * Return 1 followed by the number of digits in n minus one
     */
    private static int getOrder(int n) {
        int order = 1;
        while (n >= 10) {
            n = n / 10;
            order = order * 10;
        }
        return order;
    }

    // TODO: this should be but is not the inverse of mapIndex
	public int rmapIndex(String pathElement) {
		return Integer.parseInt(pathElement);
	}

	public String getSeparator(int depth) {
		return "_";
	}	
}
