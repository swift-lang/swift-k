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
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.griphyn.vdl.mapping.AbsFile;

public class ReadCollectList extends AbstractSingleValuedFunction {
    private ArgRef<String> file;
    
    @Override
    protected Param[] getParams() {
        return params("file");
    }

    @Override
    public Object function(Stack stack) {
        String fileName = this.file.getValue(stack);
        try {
            return readFile(fileName);
        }
        catch (IOException e) {
            throw new ExecutionException("Could not read collect file", e);
        }
    }

    private List<AbsFile> readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        List<AbsFile> l = new ArrayList<AbsFile>();
        
        String line = br.readLine();
        while (line != null) {
            AbsFile f = new AbsFile(remoteToLocal(line));
            l.add(f);
            line = br.readLine();
        }
        br.close();
        new File(fileName).delete();
        return l;
    }
    
     /**
     * Must do the reverse of PathUtils.remotePathName since the collect list
     * is built remotely by _swiftwrap
     */
    private String remoteToLocal(String s) {
        return s.replace("__root__/", "/").replace("__parent__", "..");
    }
}
