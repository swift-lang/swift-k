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


/*
 * Created on Jul 26, 2007
 */
package org.griphyn.vdl.mapping;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.file.FileResourceCache;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.RemoteFile;
import org.globus.cog.abstraction.interfaces.Service;

public class AbsFile extends RemoteFile implements GeneralizedFileFormat {
    
    private static final String cwd;
    
    static {
        cwd = new File(".").getAbsolutePath();
    }
	
    public AbsFile(AbsFile dir, String name) {
        super(dir, name);
    }
    
	public AbsFile(String url) {
	    super(url);
	}
    
	public AbsFile(String protocol, String host, String path) {
	    super(protocol, host, path);
	}
	
	public AbsFile(String protocol, String host, int port, String dir, String name) {
        super(protocol, host, port, dir, name); 
    }

    public AbsFile(String protocol, String host, int port, String path) {
        super(protocol, host, port, path);    
    }

    /*@Override
    protected void parse(String str) {
        super.parse(str);
        if (getProtocol() == null) {
            setProtocol("file");
        }
        if (getHost() == null) {
            setHost("localhost");
        }
    }*/
    
    protected FileResource getFileResource() throws IOException {
		Service s = new ServiceImpl();
		s.setProvider(getProtocol("file"));
		s.setType(Service.FILE_OPERATION);
		s.setServiceContact(new ServiceContactImpl(getHost("localhost"), getPort()));
		try {
			return FileResourceCache.getDefault().getResource(s);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not instantiate file resource", e);
		}
	}

	protected void releaseResource(FileResource fr) {
		FileResourceCache.getDefault().releaseResource(fr);
	}
	
	public String getAbsolutePath() {
	    if (this.isAbsolute()) {
	        return getPath();
	    }
	    else {
	        return cwd + File.separator + getPath();
	    }
    }

	public boolean exists() {
		try {
			FileResource fr = getFileResource();
			try {
				return fr.exists(getPath());
			}
			finally {
				releaseResource(fr);
			}
		}
		catch (Exception e) {
			// TODO this should be a proper exception
			throw new RuntimeException(e);
		}
	}

	private static final AbsFile[] FILE_ARRAY = new AbsFile[0];
	
	public List<AbsFile> listDirectories(FilenameFilter filter) {
	    try {
            FileResource fr = getFileResource();
            try {
                String protocol = getProtocol();
                String host = getHost();
                int port = getPort();
                String dir = getPath();
                List<AbsFile> l = new ArrayList<AbsFile>();
                for (GridFile gf : fr.list(dir)) {
                    AbsFile f = new AbsFile(protocol, host, port, dir, gf.getName());
                    // f.getDirectory() cannot be null since dir cannot be null since getPath() returns
                    // a non-null string
                    if (gf.isDirectory() && (filter == null || filter.accept(new File(f.getDirectory()), f.getName()))) {
                        l.add(f);
                    }
                }
                return l;
            }
            finally {
                releaseResource(fr);
            }
        }
        catch (Exception e) {
            // TODO this should be a proper exception
            throw new RuntimeException(e);
        }
	}

	public List<AbsFile> listFiles(FilenameFilter filter) {
		try {
			FileResource fr = getFileResource();
			try {
			    String protocol = getProtocol();
                String host = getHost();
                int port = getPort();
                String dir = getPath();
				List<AbsFile> l = new ArrayList<AbsFile>();
				for (GridFile gf : fr.list(dir)) {
					AbsFile f = new AbsFile(protocol, host, port, dir, gf.getName());
					if (filter == null || filter.accept(new File(f.getDirectory()), f.getName())) {
						l.add(f);
					}
				}
				return l;
			}
			finally {
				releaseResource(fr);
			}
		}
		catch (Exception e) {
			// TODO this should be a proper exception
			throw new RuntimeException(e);
		}
	}
	
	public String getType() {
		return "file";
	}
	
    public void clean() {
        try {
            getFileResource().deleteFile(getPath());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
