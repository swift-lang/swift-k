//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 4, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

public class SwingBQPMonitor extends JComponent implements ChangeListener, ActionListener {
    public static final Logger logger = Logger.getLogger(SwingBQPMonitor.class);
    
    JSpinner jtime;

    private static int xmult, xdiv, ymult, ydiv;
    private static Time starttime;

    private Time max;
    int cjobs, blockx;
    private BlockQueueProcessor bqp;

    private JButton zoomin, zoomout;
    private Settings settings;
    private List<Job> jobs;
    private Collection<Block> blocks;
    private SortedJobSet queued;
    private Set<String> blockids;

    public SwingBQPMonitor() {
        starttime = Time.now();
        max = starttime.add(TimeInterval.fromSeconds(30));
        jtime = new JSpinner();
        add(jtime);
        jtime.setLocation(4, 4);
        jtime.setSize(100, 16);
        jtime.addChangeListener(this);
        blockx = 0;
        cjobs = 100;
        JFrame f = new JFrame();
        f.setContentPane(this);
        f.setSize(800, 600);
        f.setVisible(true);

        zoomin = addButton(140, 4, "+");
        zoomout = addButton(200, 4, "-");
        blockids = new HashSet<String>();
    }

    public SwingBQPMonitor(BlockQueueProcessor bqp) {
        this();
        this.bqp = bqp;
        this.settings = bqp.getSettings();
        this.jobs = bqp.getJobs();
        this.queued = bqp.getQueued();
        this.blocks = bqp.getBlocks().values();
    }

    public void update(Settings settings, List<Job> jobs, SortedJobSet queued, Collection<Block> blocks) {
        this.settings = settings;
        this.jobs = jobs;
        this.queued = queued;
        this.blocks = blocks;
        blockx = 10;
        for (Block b : blocks) {
            blockids.add(b.getId());
            blockx += b.getWorkerCount() + 4;
        }
        repaint();
    }

    private JButton addButton(int x, int y, String t) {
        JButton b = new JButton();
        b.setText(t);
        b.setLocation(x, y);
        b.setSize(44, 24);
        this.add(b);
        b.addActionListener(this);
        return b;
    }

    static int scalex(int x) {
        return x * xmult / xdiv;
    }

    static int scaley(int y) {
        return y * ymult / ydiv;
    }

    static int scaley(Time t) {
        return scaley(t.subtract(starttime));
    }

    static int scaley(TimeInterval t) {
        return scaley((int) t.getSeconds());
    }

    private void computeMaxTime() {
        long m = max.getSeconds();
        /*
         * long now = Time.now().getSeconds(); while (i.hasNext()) { Job j =
         * (Job) i.next(); long v = BlockQueueProcessor.overallocatedSize(j,
         * settings) + now; if (m < v) { m = v; } }
         */
        for (Block b : blocks) {
            if (m < b.getEndTime().getSeconds()) {
                m = b.getEndTime().getSeconds();
            }
        }
        max = Time.fromSeconds((m / 250) * 250);
    }

    public void paint(Graphics g) {
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paint(g);
            if (jobs == null) {
                return;
            }
            cjobs = Math.max(jobs.size() + queued.size(), cjobs);
            computeMaxTime();

            int wj = getWidth() * 4 / 5;
            int hj = getHeight() * 3 / 5;

            g.setColor(Color.BLACK);
            g.drawLine(10, hj, 10 + wj, hj);

            g.drawString(String.valueOf(max.subtract(starttime).getSeconds()), 10, hj + 20);
            g.drawString("Queued: " + queued.size(), 10, hj + 30);
            g.drawString("Not queued: " + jobs.size(), 10, hj + 40);
            ymult = hj / 2 - 20;
            ydiv = (int) (Math.max(max.subtract(starttime).getSeconds(), 1L) + 10);
            xdiv = Math.max(blockx, 100);
            xmult = wj;

            g.setColor(new Color(80, 100, 255, 64));
            int i = 0;
            for (Job j : queued) {
                g.fillRect(scalex(i) + 10, hj * 7 / 5 - scaley(j.getMaxWallTime()), scalex(1),
                    scaley(j.getMaxWallTime()));
                i++;
            }

            g.setColor(new Color(255, 0, 0, 64));

            i = 0;
            for (Job j : jobs) {
                g.fillRect(scalex(i) + 10, hj * 8 / 5 - scaley(j.getMaxWallTime()), scalex(1),
                    scaley(j.getMaxWallTime()));
                i++;
            }

            g.setColor(Color.RED);
            i = 0;
            for (Job j : jobs) {
                int v = BlockQueueProcessor.overallocatedSize(j, settings);
                g.drawLine(scalex(i) + 10, hj - scaley(v), scalex(i + 1) + 10 - 1, hj - scaley(v));
                i++;
            }

            g.setColor(new Color(0, 160, 0));

            int blockx = 10;
            for (Block b : blocks) {
                max = Time.max(max, b.getEndTime());
                paintBlock(b, g, hj, blockx);
                blockx += b.getWorkerCount() + 4;
            }
            g.setColor(new Color(0, 128, 0));
            Time now = Time.now();
            g.drawLine(10, hj - scaley(now), 10 + wj, hj - scaley(now));
        }
        catch (Exception e) {
            logger.warn("Exception caught", e);
        }
    }

    static long maxplanning = 0;
    static int maxplanningstep = 0;

    private final Color DONEJOB = new Color(128, 255, 128, 170);
    private final Color RUNNINGJOB = new Color(255, 245, 128, 120);
    private final Color PLANNEDJOB = new Color(128, 128, 255, 100);

    private final Color DONEBLOCK = new Color(0, 128, 0);
    private final Color RUNNINGBLOCK = new Color(128, 120, 0);
    private final Color PLANNEDBLOCK = new Color(0, 0, 128);

    public void stateChanged(ChangeEvent e) {

    }

    public void paintBlock(Block b, Graphics g, int hj, int x) {
        if (b.isRunning()) {
            g.setColor(RUNNINGBLOCK);
        }
        else if (b.isDone()) {
            g.setColor(DONEBLOCK);
        }
        else {
            g.setColor(PLANNEDBLOCK);
        }
        Time start, dl;
        if (b.getStartTime() == null) {
            start = Time.now();
            dl = start;
        }
        else {
            start = b.getStartTime();
            dl = b.getDeadline();
        }
        g.drawRect(10 + scalex(x), hj - scaley(b.getEndTime()), scalex(b.getWorkerCount()),
            scaley(b.getWalltime()));

        g.drawLine(10 + scalex(x + b.getWorkerCount() / 2), hj - scaley(start), 10 + scalex(x
                + b.getWorkerCount() / 2), hj - scaley(b.getCreationTime()));
        g.drawString(b.getWorkerCount() + "w x " + b.getWalltime().getSeconds() + "s",
            10 + scalex(x), hj - scaley(b.getEndTime()) - 10);

        for (Cpu cpu : b.getCpus()) {
            for (Job j : cpu.getDoneJobs()) {
                paintJob(g, hj, x, cpu, j, DONEJOB);
            }
            Job running = cpu.getRunning();
            if (running != null) {
                paintJob(g, hj, x, cpu, running, RUNNINGJOB);
            }
        }

        g.setColor(Color.RED);
        g.drawLine(10 + scalex(x), hj - scaley(dl), 10 + scalex(x + b.getWorkerCount()), hj
                - scaley(dl));
    }

    private void paintJob(Graphics g, int hj, int x, Cpu cpu, Job j, Color color) {
        if (j.getStartTime() != null && j.getEndTime() != null) {
            g.setColor(color);
            g.fillRect(10 + scalex(x + cpu.getId()), hj - scaley(j.getEndTime()), Math.max(1, scalex(1)),
                scaley(j.getEndTime().subtract(j.getStartTime())));
            g.setColor(Color.BLACK);
            g.drawLine(10 + scalex(x + cpu.getId()), hj - scaley(j.getEndTime()), 10 + scalex(x
                + cpu.getId() + 1) - 1, hj - scaley(j.getEndTime()));
        }
    }

    public void update() {
        repaint();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == zoomin) {
            max =
                    Time.now().add(
                        TimeInterval.fromSeconds((int) (max.subtract(Time.now()).getSeconds() * 2 / 3)));
        }
        else if (e.getSource() == zoomout) {
            max =
                    Time.now().add(
                        TimeInterval.fromSeconds((int) (max.subtract(Time.now()).getSeconds() * 3 / 2)));
        }
        repaint();
    }
}
