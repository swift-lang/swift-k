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
 * Created on Sep 20, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import k.thr.LWThread;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;

public class ConcurrentMapper extends AbstractFileMapper {		
    private LWThread thread;

	public ConcurrentMapper() {
		super(new ConcurrentElementMapper());
	}
	
	@Override
    public String getName() {
        return "ConcurrentMapper";
    }

    @Override
    public void initialize(RootHandle root) {
        super.initialize(root);
        this.thread = root.getThread();
    }

	@Override
    public PhysicalFormat map(Path path) {
	    AbstractFileMapperParams cp = getParams();

	    String prefix = cp.getPrefix();
        String modifiedPrefix = "_concurrent/" + (prefix == null ? "" : prefix + "-") + 
                thread.getQualifiedName();
        return super.map(cp, path, modifiedPrefix);
    }
	
    @Override
    protected Path rmap(AbstractFileMapperParams cp, AbsFile file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBeRemapped(Path path) {
        return false;
    }

    @Override
    public boolean isPersistent(Path path) {
        return true;
    }
}