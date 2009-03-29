/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.processors;

import java.util.Date;

import org.apache.log4j.Level;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;

public class AppProcessor implements LogMessageProcessor {

    public Object getSupportedCategory() {
        return Level.DEBUG;
    }

    public String getSupportedSource() {
        return "org.griphyn.vdl.karajan.lib.Log";
    }

    public void processMessage(SystemState state, Object message, Object details) {
        SimpleParser p = new SimpleParser(String.valueOf(message));
        try {
            String appname;
            boolean add;
            if (p.matchAndSkip("JOB_START ")) {
                add = true;
            }
            else if (p.matchAndSkip("JOB_END ")) {
                add = false;
            }
            else {
                return;
            }
            p.matchAndSkip("jobid=");
            String id = p.word();

            if (add) {
                p.matchAndSkip("tr=");
                appname = p.word();
                String args = "";
                if (p.matchAndSkip("arguments=[")) {
                    p.beginToken();
                    p.markTo("]");
                    args = p.getToken();
                }
                p.skip("host=");
                String host = p.word();
                
                ApplicationItem app = (ApplicationItem) state.getItemByID(id,
                    StatefulItemClass.APPLICATION);
                boolean newapp = app == null;
                if (newapp) {
                    app = new ApplicationItem(id);
                }
                app.setArguments(args);
                app.setHost(host);
                app.setName(appname);
                app.setStartTime(new Date());
                if (newapp) {
                    state.addItem(app);
                }
                else {
                    state.itemUpdated(app);
                }
                state.getStats("apps").add();
            }
            else {
                StatefulItem app = state.getItemByID(id,
                    StatefulItemClass.APPLICATION);
                state.removeItem(app);
                state.getStats("apps").remove();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
