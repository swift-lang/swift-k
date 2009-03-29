//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 27, 2009
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.util.Collection;

import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.Bridge;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ActionListener;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Button;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Component;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Dialog;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Frame;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Screen;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Table;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.TextArea;

public class AppDialog extends Dialog implements ActionListener {
    private final ApplicationItem app;
    private final Button close;

    public AppDialog(Screen screen, ApplicationItem app) {
        this.app = app;
        setTitle("Application Details");
        setTitleAlignment(0.5f);
        setSize(screen.getWidth() * 2 / 3, screen.getHeight() * 2 / 3);
        center(screen);
        
        appProperties();
        appTasks();
        
        close = new Button("&Close");
        close.setLocation(getWidth() / 2 - 5, getHeight() - 2);
        close.setSize(9, 1);
        close.addActionListener(this);
        add(close);
        display(screen);
        close.focus();
    }

    private void appProperties() {
        Frame f = new Frame();
        f.setSize(getWidth() - 4, 6);
        f.setLocation(2, 2);
        f.setTitle("Properties");
        add(f);
        TextArea ta = new TextArea();
        ta.setText(STable.format(app));
        f.add(ta);
        add(f);
        ta.setSize(f.getWidth() - 2, f.getHeight() - 2);
        ta.setLocation(1, 1);
    }
    
    private void appTasks() {
        Frame f = new Frame();
        f.setSize(getWidth() - 4, getHeight() - 10);
        f.setLocation(2, 8);
        f.setTitle("Tasks");
        add(f);
        Table t = new Table();
        t.setModel(new AppTasksModel(app));
        t.setLocation(1, 1);
        t.setSize(getWidth() - 6, getHeight() - 12);
        t.setBgColor(ANSI.WHITE);
        t.setColumnWidth(0, 8);
        t.setColumnWidth(2, 8);
        t.setCellRenderer(new TaskCellRenderer());
        f.add(t);
    }
    
    public void actionPerformed(Component source) {
        if (source == close) {
            close();
        }
    }
}
