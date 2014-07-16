//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 7, 2013
 */
package org.griphyn.vdl.karajan.monitor.processors.coasters;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.processors.SimpleParser;

public class BlockRequestedProcessor extends AbstractRemoteLogProcessor {
    private CoasterStatusItem item;
    
    @Override
    public void initialize(SystemState state) {
        super.initialize(state);
    }

    @Override
    public String getMessageHeader() {
        return "BLOCK_REQUESTED";
    }

    @Override
    public void processMessage(SystemState state, SimpleParser p, Object details) {
        try {
            p.skip("id=");
            p.beginToken();
            p.markTo(",");
            String blockId = p.getToken();
            p.skip("cores=");
            p.beginToken();
            p.markTo(",");
            int cores = Integer.parseInt(p.getToken());
            p.skip("coresPerWorker=");
            p.beginToken();
            p.markTo(",");
            int coresPerWorker = Integer.parseInt(p.getToken());
            p.skip("walltime=");
            int walltime = Integer.parseInt(p.remaining());
            
            CoasterStatusItem item = (CoasterStatusItem) state.getItemByID(CoasterStatusItem.ID, StatefulItemClass.MISC);
            item.newBlock(blockId, cores, coresPerWorker, walltime);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
