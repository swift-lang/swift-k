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


package org.griphyn.vdl.karajan.lib.swiftscript;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

/**
    Formatted file output. <br>
    Example: fprintf("tmp.log", "\t%s\n", "hello"); <br>
    Appends to file.
    @see Tracef, Sprintf
    @author wozniak
 */
public class Fprintf extends SwiftFunction {
    private static final Logger logger = Logger.getLogger(Fprintf.class);
    
    private ArgRef<AbstractDataNode> filename;
    private ArgRef<AbstractDataNode> spec;
    private ChannelRef<AbstractDataNode> c_vargs;

    @Override
    protected Signature getSignature() {
        return new Signature(params("filename", "spec", "..."));
    }

    static ConcurrentHashMap<String, Object> openFiles = new ConcurrentHashMap<String, Object>();
    
    @Override
    public Object function(Stack stack) {
        try {
            AbstractDataNode hfilename = this.filename.getValue(stack);
            AbstractDataNode hspec = this.spec.getValue(stack);
            hfilename.waitFor(this);
            String filename = (String) hfilename.getValue();
            try {
                hspec.waitFor(this);
                Channel<AbstractDataNode> args = c_vargs.get(stack);
                waitForAll(this, args);
                String spec = (String) hspec.getValue(); 
                
                StringBuilder output = new StringBuilder();
                try {
                    Sprintf.format(spec, args, output);
                }
                catch (RuntimeException e) {
                    throw new ExecutionException(this, e.getMessage());
                }
                String msg = output.toString();
         
                if (logger.isDebugEnabled()) {
                    logger.debug("file: " + filename + " msg: " + msg);
                }
                write(filename, msg);
            }
            catch (DependentException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("file: " + filename + " msg: <exception>");
                }
                write(filename, "<exception>");
            }
        }
        catch (DependentException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("<exception>");
            }
        }
        return null;
    }
    
    private static void write(String filename, String msg) 
    throws ExecutionException {
        acquire(filename);
        
        try {
            FileWriter writer = new FileWriter(filename, true);
            writer.write(msg);
            writer.close();
        }
        catch (IOException e) {
            throw new ExecutionException
            ("write(): problem writing to: " + filename, e); 
        }
        
        openFiles.remove(filename);
    }
    
    private static void acquire(String filename) 
    throws ExecutionException {
        int count = 0;
        Object marker = new Object();
        while (openFiles.putIfAbsent(filename, marker) != null && 
                count < 10) {
            try {
                Thread.sleep(count);
            }
            catch (InterruptedException e) 
            {}
            count++;
        }
        if (count == 10)
            throw new ExecutionException
            ("write(): could not acquire: " + filename);
    }
}
