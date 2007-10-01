/*
 * Created on Jul 26, 2007
 */
package org.griphyn.vdl.mapping;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.file.FileResourceCache;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Service;

public class AbsFile implements GeneralizedFileFormat {
	private final String protocol;
	private final String host;
	private final String path;
	private String dir, name;
	
	public AbsFile(String url) {
		int pi = url.indexOf("://");
		if (pi == -1) {
			protocol = "file";
			host = "localhost";
			path = url;
		}
		else {
			protocol = url.substring(0, pi);
			if (protocol.equals("file")) {
				host = "localhost";
				String rp = url.substring(pi + 3);
				if (rp.startsWith("localhost/")) {
					rp = rp.substring("localhost/".length());
				}
                path = rp;
			}
			else {
				int si = url.indexOf('/', pi + 3);
				if (si == -1) {
					host = url.substring(pi + 3);
					path = "";
				}
				else {
					host = url.substring(pi + 3, si);
					path = url.substring(si + 1);
				}
			}
		}
		initDirAndName();
	}
    
    private void initDirAndName() {
    	int di = path.lastIndexOf('/');
        if (di == 0) {
            dir = "/";
        }
        else if (di > 0) {
            dir = path.substring(0, di);
        }
        else {
            dir = "";
        }
        name = path.substring(di + 1);        
    }

	public AbsFile(String protocol, String host, String path) {
		this.protocol = protocol;
		this.host = host;
		this.path = path;
		initDirAndName();
	}

	protected FileResource getFileResource() throws IOException {
		Service s = new ServiceImpl();
		s.setProvider(protocol);
		s.setType(Service.FILE_OPERATION);
		s.setServiceContact(new ServiceContactImpl(host));
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

	public boolean exists() {
		try {
			FileResource fr = getFileResource();
			try {
				return fr.exists(path);
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

	public AbsFile[] listFiles(FilenameFilter filter) {
		try {
			FileResource fr = getFileResource();
			try {
				List l = new ArrayList();
				Collection c = fr.list(path);
				Iterator i = c.iterator();
				while (i.hasNext()) {
					GridFile gf = (GridFile) i.next();
					AbsFile f = new AbsFile(protocol, host, gf.getAbsolutePathName());
					if (filter == null || filter.accept(new File(f.getDir()), f.getName())) {
						l.add(f);
					}
				}
				return (AbsFile[]) l.toArray(FILE_ARRAY);
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

	public String getName() {
		return name;
	}

	public String getDir() {
		return dir;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return host;
	}

	public String getPath() {
		return path;
	}
	
	public String getType() {
		return "file";
	}
	
	public String getURIAsString() {
		return protocol + "://" + host + '/' + path;
	}
	
	public String toString() {
		return getURIAsString();
	}
}
