/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.Specification;

public class FileTransferSpecificationImpl implements FileTransferSpecification {

    private static final long serialVersionUID = 1L;
    
    private int type;
	private String sourceDirectory;
	private String destinationDirectory;
	private String sourceFile;
	private String destinationFile;
	private String source;
	private String destination;
	private boolean thirdparty, recursive;
	private boolean thirdPartyIfPossible;
	private long sourceFileOffset, destinationFileOffset, sourceFileLength;
	private Map<String, Object> attributes;

	public FileTransferSpecificationImpl() {
		this.type = Specification.FILE_TRANSFER;
		sourceFileLength = Long.MAX_VALUE;
	}

	public void setType(int type) {
		// By default = FILE_TRANSFER
	}

	public int getType() {
		return this.type;
	}

	public void setSpecification(String specification) {
		// N/A
	}

	public String getSpecification() {
		String spec = "Source: " + getSource() + "\nDestination: " + getDestination();
		return spec;
	}

	public void setSourceDirectory(String directory) {
	    if (directory != null) {
	        this.sourceDirectory = directory.trim();
	    }
	}

	public String getSourceDirectory() {
		return this.sourceDirectory;
	}

	public void setDestinationDirectory(String directory) {
		if (directory != null) {
		    this.destinationDirectory = directory.trim();
		}
	}

	public String getDestinationDirectory() {
		return this.destinationDirectory;
	}

	public void setSourceFile(String file) {
		this.sourceFile = file.trim();
	}

	public String getSourceFile() {
		return this.sourceFile;
	}

	public void setDestinationFile(String file) {
		this.destinationFile = file.trim();
	}

	public String getDestinationFile() {
		return this.destinationFile;
	}

	public void setSource(String source) {
		this.source = source;
		this.sourceDirectory = dirname(source);
		this.sourceFile = basename(source);
	}

	private String dirname(String path) {
		int last = path.lastIndexOf('/');
		if (last == -1) {
			return null;
		}
		else {
			return path.substring(0, last);
		}
	}

	private String basename(String path) {
		int last = path.lastIndexOf('/');
		if (last == -1) {
			return path;
		}
		else {
			return path.substring(last + 1);
		}
	}

	public String getSource() {
		if (this.source != null) {
			return this.source;
		}
		else {
			if ((sourceDirectory != null) && !sourceDirectory.equals("")) {
				if (sourceDirectory.endsWith("/")) {
					return sourceDirectory + sourceFile;
				}
				else {
				    return sourceDirectory + "/" + sourceFile;
				}
			}
			else {
				return sourceFile;
			}
		}
	}

	public void setDestination(String destination) {
		this.destination = destination;
		this.destinationDirectory = dirname(destination);
		this.destinationFile = basename(destination);
	}

	public String getDestination() {
		if (this.destination != null) {
			return this.destination;
		}
		else {
			if ((destinationDirectory != null) && !destinationDirectory.equals("")) {
				if (destinationDirectory.endsWith("/")) {
				    return destinationDirectory + destinationFile;
				}
				else {
					return destinationDirectory + "/" + destinationFile;
				}
			}
			else {
				return destinationFile;
			}
		}
	}

	public void setThirdParty(boolean bool) {
		this.thirdparty = bool;
	}

	public boolean isThirdParty() {
		return this.thirdparty;
	}

	public boolean isThirdPartyIfPossible() {
		return thirdPartyIfPossible;
	}

	public void setThirdPartyIfPossible(boolean thirdPartyIfPossible) {
		this.thirdPartyIfPossible = thirdPartyIfPossible;
	}

	public void setAttribute(String name, Object value) {
	    if (attributes == null) {
	        attributes = new HashMap<String, Object>();
	    }
		attributes.put(name, value);
	}

	public Object getAttribute(String name) {
	    if (attributes == null) {
	        return null;
	    }
	    else {
	        return attributes.get(name);
	    }
	}

	@SuppressWarnings("unchecked")
	/**
	 * @deprecated
	 */
    public Enumeration getAllAttributes() {
	    if (this.attributes == null) {
	        return Collections.enumeration(Collections.emptyList());
	    }
	    else {
	        return Collections.enumeration(this.attributes.keySet());
	    }
	}
	
	public Collection<String> getAttributeNames() {
	    if (attributes == null) {
	        return Collections.emptyList();
	    }
	    else {
	        return attributes.keySet();
	    }
	}

	public String toString() {
		return "Transfer[" + this.getSource() + " -> " + this.getDestination() + "]";
	}

	public long getDestinationOffset() {
		return destinationFileOffset;
	}

	public void setDestinationOffset(long destinationFileOffset) {
		this.destinationFileOffset = destinationFileOffset;
	}

	public long getSourceLength() {
		return sourceFileLength;
	}

	public void setSourceLength(long sourceFileLength) {
		this.sourceFileLength = sourceFileLength;
	}

	public long getSourceOffset() {
		return sourceFileOffset;
	}

	public void setSourceOffset(long sourceFileOffset) {
		this.sourceFileOffset = sourceFileOffset;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}
	
    public Object clone() {
	    FileTransferSpecificationImpl result = null;
        try {
            result = (FileTransferSpecificationImpl) super.clone();
            if (attributes != null) {
                result.attributes = new HashMap<String, Object>(attributes);
            }
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
	    return result;
	}
}
