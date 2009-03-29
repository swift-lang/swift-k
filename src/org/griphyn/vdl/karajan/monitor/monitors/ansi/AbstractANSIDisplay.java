/*
 * Created on Jan 30, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSIContext;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Component;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Dialog;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Key;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Label;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Menu;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.MenuBar;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.MenuItem;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Screen;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Tab;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.TabbedContainer;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Table;
import org.griphyn.vdl.karajan.monitor.monitors.swing.ApplicationTable;
import org.griphyn.vdl.karajan.monitor.monitors.swing.FilteringTaskTable.JobModel;
import org.griphyn.vdl.karajan.monitor.monitors.swing.FilteringTaskTable.TransferModel;

public class AbstractANSIDisplay extends Thread {
    public static final Logger logger = Logger
        .getLogger(AbstractANSIDisplay.class);

    private SystemState state;
    private Table appsTable, tasksTable;
    private Tab apps, schedulerInfoTab;
    private SchedulerInfoPane schedulerInfoPane;
    private InputStream in;
    private OutputStream out;
    private ANSIContext context;

    public AbstractANSIDisplay(SystemState state, InputStream in,
            OutputStream out) {
        this.state = state;
        this.in = in;
        this.out = out;
        this.setPriority(Thread.MAX_PRIORITY);
    }

    public void run() {
        try {
            context = new ANSIContext(out, in);
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) throws IOException {
                }
            }));
            Screen screen = new Screen(context);
            if (!screen.init()) {
                System.err
                    .println("Your terminal does not support ANSI escape codes");
            }
            else {
                screen.add(createMainMenu(screen));
                screen.add(createTitle(screen));
                screen.add(createMainTabs(screen));
                screen.add(createProgressBar(screen));

                apps.focus();
                screen.redraw();
                context.run();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Component createTitle(final Screen screen) {
        Label text = new Label("Swift System Monitor") {
            protected void validate() {
                setSize(screen.getWidth() - 20, 1);
                super.validate();
            }
        };
        text.setBgColor(ANSI.WHITE);
        text.setFgColor(ANSI.BLACK);
        text.setLocation(20, 0);
        // centered in screen as much as possible
        text.setJustification(20f / (screen.getWidth() - 20) * 2);
        return text;
    }

    private Component createProgressBar(final Screen screen) {
        GlobalProgress gp = new GlobalProgress(state) {
            protected void validate() {
                setLocation(0, screen.getHeight() - 1);
                setSize(screen.getWidth(), 1);
                super.validate();
            }
        };
        return gp;
    }

    private Component createMainMenu(Screen screen) {
        MenuBar menuBar = new MenuBar();
        menuBar.setSize(20, 1);
        Menu file = new Menu("&File");
        file.add(new MenuItem("&Close") {
            public void itemSelected() {
                try {
                    closeImmediately();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        file.add(new MenuItem("&Abort") {
            public void itemSelected() {
                try {
                    closeImmediately();
                    System.exit(8);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        menuBar.add(file);
        return menuBar;
    }

    private Component createMainTabs(final Screen screen) {
        TabbedContainer tl = new TabbedContainer() {
            protected void validate() {
                setSize(screen.getWidth(), screen.getHeight() - 2);
                super.validate();
            }
        };
        tl.setTabPosition(TabbedContainer.TABS_AT_BOTTOM);
        tl.setLocation(0, 1);
        tl.addTab(createSummaryTab());
        tl.addTab(createAppTab());
        tl.addTab(createJobsTab());
        tl.addTab(createTransfersTab());
        tl.addTab(createSchedulerInfoTab());
        // tl.addTab(createLogTab());
        tl.addTab(createTaskStatsTab());
        tl.addTab(createBensTab());
        return tl;
    }

    private Tab createSummaryTab() {
        Tab summary = new Tab("2 Summary");
        summary.setAcceleratorKey(new Key(Key.F2));
        summary.setContents(new SummaryPane(state));
        summary.activate();
        return summary;
    }

    private Tab createAppTab() {
        apps = new Tab("3 Apps.");
        apps.setAcceleratorKey(new Key(Key.F3));
        appsTable = new AppTable();
        appsTable.setModel(new ApplicationTable.Model(state
            .getItemClassSet(StatefulItemClass.APPLICATION)));
        appsTable.setColumnWidth(0, 9);
        appsTable.setColumnWidth(1, 16);
        appsTable.setColumnWidth(3, 16);
        appsTable.setBgColor(ANSI.CYAN);
        apps.setContents(appsTable);
        return apps;
    }

    private Tab createJobsTab() {
        schedulerInfoTab = new Tab("4 Jobs");
        schedulerInfoTab.setAcceleratorKey(new Key(Key.F4));
        tasksTable = new STable();
        tasksTable.setModel(new JobModel(state
            .getItemClassSet(StatefulItemClass.TASK)));
        tasksTable.setColumnWidth(0, 16);
        tasksTable.setColumnWidth(2, 16);
        tasksTable.setColumnWidth(3, 12);
        tasksTable.setBgColor(ANSI.CYAN);
        schedulerInfoTab.setContents(tasksTable);
        return schedulerInfoTab;
    }

    private Tab createTransfersTab() {
        schedulerInfoTab = new Tab("5 Transfers");
        schedulerInfoTab.setAcceleratorKey(new Key(Key.F5));
        tasksTable = new STable();
        tasksTable.setModel(new TransferModel(state
            .getItemClassSet(StatefulItemClass.TASK)));
        tasksTable.setBgColor(ANSI.CYAN);
        tasksTable.setColumnWidth(2, 21);
        schedulerInfoTab.setContents(tasksTable);
        return schedulerInfoTab;
    }

    private Tab createSchedulerInfoTab() {
        schedulerInfoTab = new Tab("6 Scheduler");
        schedulerInfoTab.setAcceleratorKey(new Key(Key.F6));
        schedulerInfoPane = new SchedulerInfoPane(state);
        schedulerInfoTab.setContents(schedulerInfoPane);
        return schedulerInfoTab;
    }

    private Tab createLogTab() {
        Tab log = new Tab("7 Log");
        log.setAcceleratorKey(new Key(Key.F7));
        return log;
    }

    private Tab createTaskStatsTab() {
        Tab summary = new Tab("7 Task Stat.");
        summary.setAcceleratorKey(new Key(Key.F7));
        summary.setContents(new TaskStatsPane(state));
        return summary;
    }

    private Tab createBensTab() {
        Tab ben = new Tab("8 Ben's View");
        ben.setAcceleratorKey(new Key(Key.F8));
        ben.setContents(new BensPane(state));
        return ben;
    }

    public void itemUpdated(int updateType, StatefulItem item) {
        StatefulItemClass cls = item.getItemClass();
        if (cls.equals(StatefulItemClass.APPLICATION)) {
            if (appsTable != null) {
                appsTable.dataChanged();
            }
        }
    }

    protected void cleanup() throws IOException {
        context.exit();
    }

    protected ANSIContext getContext() {
        return context;
    }
    
    public void closeImmediately() throws IOException {
        cleanup();
    }

    public void close() throws IOException {
        Dialog.displaySimpleDialog(context.getScreen(), "Finished",
            "Swift execution finished", new String[] { "&OK" });
        closeImmediately();
    }
}
