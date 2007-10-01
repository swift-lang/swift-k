/*
 * Created on Aug 12, 2007
 */
package org.griphyn.vdl.mapping;

public interface GeneralizedFileFormat extends PhysicalFormat {
	String getPath();
	
	String getURIAsString();
}
