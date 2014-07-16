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

import java.util.Collection;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.InitMapper;

public class ProcessStageouts extends SwiftFunction {
    public static final Logger logger = Logger.getLogger(ProcessStageouts.class);
	
	private ArgRef<Collection<DSHandle>> stageouts;
	private ArgRef<Collection<AbsFile>> collectList;
    
    private ChannelRef<Object> cr_restartLog;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("stageouts", "collectList"),   
            returns(channel("restartLog", DYNAMIC)));
    }

    @Override
    public Object function(Stack stack) {
        Collection<DSHandle> stageouts = this.stageouts.getValue(stack);
        Collection<AbsFile> collectList = this.collectList.getValue(stack);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Collect list: " + collectList);
        }
        Channel<Object> log = cr_restartLog.get(stack);
        
        /**
         * This basically does what setDatasetValues(), mark() and log() used to do.
         * Specifically, discover all data in the collectList (similar to what
         * Root*Data nodes do when initialized), set all file objects to some 
         * dummy FILE_VALUE, deep close everything, then send everything to the
         * restart log 
         */
        process(stageouts, collectList, log);
        return null;
    }

    private void process(Collection<DSHandle> vars, Collection<AbsFile> collectList, Channel<Object> log) {
        for (DSHandle var : vars) {
            Mapper m = var.getMapper();
            if (!m.isStatic() && var.getType().hasArrayComponents()) {
                Collection<Path> found = m.existing(new FileSystemLister.FileList(collectList));
                InitMapper.addExisting(found, m, var.getRoot(), var);
                logOnly(var, log);
            }
            else {
                processStatic(var, log);
            }
        }
    }


    private void processStatic(DSHandle var, Channel<Object> log) {
        try {
            for (DSHandle leaf : var.getLeaves()) {
                leaf.setValue(AbstractDataNode.FILE_VALUE);
                leaf.closeShallow();
                LogVar.logVar(log, leaf);
            }
        }
        catch (Exception e) {
            throw new ExecutionException("Exception caught closing stageouts", e);
        }
    }
    
    private void logOnly(DSHandle var, Channel<Object> log) {
        try {
            for (DSHandle leaf : var.getLeaves()) {
                LogVar.logVar(log, leaf);
            }
        }
        catch (Exception e) {
            throw new ExecutionException("Exception caught closing stageouts", e);
        }
    }
}
