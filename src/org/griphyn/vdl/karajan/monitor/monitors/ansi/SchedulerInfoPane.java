/*
 * Created on Sep 2, 2008
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Component;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Frame;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Label;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Table;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.VBox;

public class SchedulerInfoPane extends VBox implements SystemStateListener {
    private SystemState state;
    
    private Label queued, running, done;
    private Table hosts;
    
	public SchedulerInfoPane(SystemState state) {
	    this.state = state;
	    state.addListener(this);
	    Frame f = new Frame();
	    f.setFilled(true);
	    f.setTitle("Tasks");
	    f.setBgColor(ANSI.CYAN);
	    setTop(f);
	    
	    f = new Frame() {
            protected void validate() {
                Component c = (Component) getComponents().get(0);
                c.setLocation(1, 1);
                c.setSize(width - 2, height - 2);
                super.validate();
            }
	    };
	    f.setFilled(true);
	    f.setTitle("Hosts");
	    hosts = new Table(new HostTableModel(state.getItemClassSet(StatefulItemClass.HOST)));
	    hosts.setColumnWidth(0, 24);
	    hosts.setColumnWidth(1, 15);
	    hosts.setColumnWidth(2, 15);
	    hosts.setColumnWidth(3, 12);
	    hosts.setBgColor(ANSI.CYAN);
	    hosts.setFgColor(ANSI.BLACK);
	    hosts.setCellRenderer(new HostCellRenderer());
	    f.add(hosts);
	    f.setBgColor(ANSI.CYAN);
	    setBottom(f);
	}

    protected void validate() {
        setSplit((float) 10 / height);
        super.validate();
    }
    
    private void update() {
        hosts.dataChanged();
    }

    public void itemUpdated(int updateType, StatefulItem item) {
        if (item.getItemClass().equals(StatefulItemClass.HOST)) {
            update();
        }
    }
}
