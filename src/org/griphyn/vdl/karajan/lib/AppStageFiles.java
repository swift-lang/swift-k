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
 * Created on Sep 7, 2015
 */
package org.griphyn.vdl.karajan.lib;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.mapping.AbsFile;

public abstract class AppStageFiles extends InternalFunction {
    
    protected static interface CacheKey {
        AbsFile getFile();
    }
    
    protected static abstract class AbstractCacheKey implements CacheKey {
        @Override
        public int hashCode() {
            AbsFile file = getFile();
            final int prime = 31;
            int result = 1;
            result = prime * result + ((file == null) ? 0 : file.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            AbsFile file = getFile();
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
            if (file == null) {
                if (other.getFile() != null)
                    return false;
            }
            else if (!file.equals(other.getFile()))
                return false;
            return true;
        }
    }
    
    protected static class CacheKeyTmp extends AbstractCacheKey {
        public AbsFile file;
        
        public CacheKeyTmp() {
        }
        
        public CacheKeyTmp(AbsFile file) {
            this.file = file;
        }
        
        public void set(AbsFile file) {
            this.file = file;
        }

        public AbsFile getFile() {
            return file;
        }
    }
    
    protected static class CacheKeyPerm extends AbstractCacheKey {
        private final WeakReference<AbsFile> file;
        
        public CacheKeyPerm(AbsFile file) {
            this.file = new WeakReference<AbsFile>(file);
        }

        public AbsFile getFile() {
            return file.get();
        }
    }
    
    private static Map<CacheKey, List<String>> eden = new WeakHashMap<CacheKey, List<String>>();
    private static Map<CacheKey, List<String>> survivor = new WeakHashMap<CacheKey, List<String>>();
    private static final int MAX_EDEN_SIZE = 256;
    
    protected static synchronized List<String> getFromCache(CacheKey key) {
        List<String> l = survivor.get(key);
        if (l == null) {
            l = eden.remove(key);
            if (l == null) {
                return null;
            }
            else {
                survivor.put(new CacheKeyPerm(key.getFile()), l);
                return l;
            }
        }
        else {
            return l;
        }
    }
    
    protected static synchronized void putInCache(CacheKey key, List<String> value) {
        if (eden.size() > MAX_EDEN_SIZE) {
            eden.clear();
        }
        eden.put(new CacheKeyPerm(key.getFile()), value);
    }

    protected List<String> makeList(String s1, String s2) {
        return new Pair<String>(s1, s2);
    }
}
