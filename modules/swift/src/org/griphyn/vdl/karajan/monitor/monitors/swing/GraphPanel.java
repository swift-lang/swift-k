/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 24, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.UIManager;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.Unit;
import org.griphyn.vdl.karajan.monitor.common.DataSampler;
import org.griphyn.vdl.karajan.monitor.common.DataSampler.Series;
import org.griphyn.vdl.karajan.monitor.common.GlobalTimer;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class GraphPanel extends JPanel implements ChartMouseListener {
    private static final Stroke DEFAULT_STROKE = new BasicStroke(1.0f);
    private static final int TOOLTIP_DISPLAY_DELAY = 1000;
    
    public static final DateFormat DATE = DateFormat.getDateInstance();
    public static final DateFormat TIME = DateFormat.getTimeInstance();
    
    
    private SystemState state;
    private DataSampler sampler;
    private JFreeChart chart;
    private final List<String> enabled;
    private final Map<Unit, Integer> datasetMapping;
    private final Map<Unit, List<String>> seriesMapping;
    private JPanel legend;
    private GraphsPanel gp;
    private ChartPanel cp;
    private ColorPalette palette;
    private TimerTask tooltipTimerTask;
    private JToolTip toolTip;
    private double toolTipValue;
    private int maxRange;

    public GraphPanel(SystemState state, GraphsPanel gp) {
        this.state = state;
        sampler = (DataSampler) state.getItemByID(DataSampler.ID, StatefulItemClass.WORKFLOW);
        this.gp = gp;
        this.setLayout(new BorderLayout());
        enabled = new ArrayList<String>();
        datasetMapping = new HashMap<Unit, Integer>();
        seriesMapping = new HashMap<Unit, List<String>>();
        palette = ColorPalette.newDefaultPalette();
        createChart();
    }
    
    private void createChart() {
        chart = ChartFactory.createTimeSeriesChart(null, "Time", null, null, false, false, false);
        cp = new ChartPanel(chart);
        // avoid stretching fonts and such
        cp.setMaximumDrawWidth(Integer.MAX_VALUE);
        cp.setMaximumDrawHeight(Integer.MAX_VALUE);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(UIManager.getColor("TextField.background"));
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(false);
        cp.addChartMouseListener(this);
        cp.setLayout(new DummyLayoutManager());
        cp.add(toolTip = new JToolTip());
        toolTip.setVisible(false);
        
        cp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                disableToolTip();
            }
        });
        chart.addChangeListener(new ChartChangeListener() {
            @Override
            public void chartChanged(ChartChangeEvent e) {
                if (e.getType() == ChartChangeEventType.DATASET_UPDATED) {
                    updateMaxRange();
                    updateToolTipLocation();
                }
            }
        });
        
        for (String s : enabled) {
            addSeries(sampler.getSeries(s));
        }
       
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(cp, BorderLayout.CENTER);
        legend = new JPanel();
        legend.setLayout(new FlowLayout());
        
        rebuildLegend();
        
        this.add(p, BorderLayout.CENTER);
        this.add(legend, BorderLayout.SOUTH);
    }
    
    @Override
    public void chartMouseClicked(ChartMouseEvent e) {
        if (isPointWithinChartArea(e.getTrigger().getX(), e.getTrigger().getY())) {
            enableTooltip(valueFromPosition(e));
        }
    }
    
    private double valueFromPosition(ChartMouseEvent e) {
        Rectangle2D chartArea = cp.getScreenDataArea();
        XYPlot plot = chart.getXYPlot();
        return plot.getDomainAxis().java2DToValue(e.getTrigger().getX(), 
            chartArea, plot.getDomainAxisEdge());
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent e) {
        disableToolTip();
        
        if (isPointWithinChartArea(e.getTrigger().getX(), e.getTrigger().getY())) {
            chart.getXYPlot().setDomainCrosshairVisible(true);
            double vx = valueFromPosition(e);
            chart.getXYPlot().setDomainCrosshairValue(vx);
            scheduleTooltipDisplay(vx);
        }
        else {
            chart.getXYPlot().setDomainCrosshairVisible(false);
            synchronized(this) {
                if (tooltipTimerTask != null) {
                    tooltipTimerTask.cancel();
                }
            }
        }
    }

    private boolean isPointWithinChartArea(int x, int y) {
        Rectangle2D chartArea = cp.getScreenDataArea();
        return chartArea.contains(x, y);
    }

    private synchronized void scheduleTooltipDisplay(final double x) {
        if (tooltipTimerTask != null) {
            tooltipTimerTask.cancel();
        }
        tooltipTimerTask = new TimerTask() {
            @Override
            public void run() {
                tooltipTimerTask = null;
                enableTooltip(x);
            }
        };
        try {
            GlobalTimer.getTimer().schedule(tooltipTimerTask, TOOLTIP_DISPLAY_DELAY);
        }
        catch (IllegalStateException e) {
            System.err.println(this + ": " + e.getMessage());
        }
    }

    protected synchronized void disableToolTip() {
        toolTip.setVisible(false);
    }

    protected synchronized void enableTooltip(double x) {
        toolTip.setTipText(getTooltipText(x));
        toolTip.setSize(toolTip.getPreferredSize());
        toolTip.setVisible(true);
        this.toolTipValue = x;
        
        updateToolTipLocation();
    }

    private String getTooltipText(double x) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>Date:</b> ");
        sb.append(DATE.format(new Date((long) x)));
        sb.append("<br>");
        sb.append("<b>Time:</b>s ");
        sb.append(TIME.format(new Date((long) x)));
        sb.append("<br>");
        
        XYPlot plot = chart.getXYPlot();
        
        for (int i = 0; i < enabled.size(); i++) {
            String key = enabled.get(i);
            Unit unit = sampler.getSeries(key).getUnit();
            int di = getDatasetIndex(unit);
            int si = getSeriesIndex(unit, key);
            Color color = (Color) chart.getXYPlot().getRenderer(di).getSeriesPaint(si);
            
            double val = binSearch(plot.getDataset(di), si, x);
            sb.append("<b style='color: ");
            sb.append(colorToHTML(color));
            sb.append("'>");
            sb.append(sampler.getSeries(key).getLabel());
            sb.append(":</b> ");
            sb.append(unit.format(val));
            sb.append("<br>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    private Object colorToHTML(Color color) {
        return "#" + String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private double binSearch(XYDataset dataset, int si, double x) {
        return binSearch(dataset, si, x, 0, dataset.getItemCount(si) - 1);
    }

    private double binSearch(XYDataset d, int si, double x, int begin, int end) {
        if (begin >= end) {
            return d.getYValue(si, end);
        }
        int mid = (begin + end) / 2;
        double vmid = d.getXValue(si, mid);
        if (x == vmid) {
            return d.getYValue(si, mid);
        }
        else if (x < vmid) {
            return binSearch(d, si, x, begin, mid - 1);
        }
        else {
            return binSearch(d, si, x, mid + 1, end);
        }
    }

    private int getSeriesIndex(Unit unit, String key) {
        List<String> series = seriesMapping.get(unit);
        for (int i = 0; i < series.size(); i++) {
            if (key.equals(series.get(i))) {
                return i;
            }
        }
        return 0;
    }

    private int getDatasetIndex(Unit unit) {
        return datasetMapping.get(unit);
    }

    private void updateToolTipLocation() {
        if (!toolTip.isVisible()) {
            return;
        }
        XYPlot plot = chart.getXYPlot();
        Rectangle2D chartArea = cp.getScreenDataArea();
        if (chartArea.getWidth() == 0) {
            // no idea why that happens
            return;
        }
        double vx = plot.getDomainAxis().valueToJava2D(this.toolTipValue, 
            chartArea, plot.getDomainAxisEdge());
        if (vx + toolTip.getWidth() > cp.getScreenDataArea().getMaxX()) {
            toolTip.setLocation((int) vx - toolTip.getWidth(), 10);
        }
        else {
            toolTip.setLocation((int) vx, 10);
        }
    }

    private void addSeries(Series<?> series) {
        Unit unit = series.getUnit();
        XYPlot plot = chart.getXYPlot();
        Integer datasetIndex = datasetMapping.get(unit);
        TimeSeriesCollection col;
        if (datasetIndex == null) {
            col = new TimeSeriesCollection();
            int nextIndex = getNextDatasetIndex(plot);
            datasetMapping.put(unit, nextIndex);
            plot.setDataset(nextIndex, col);
            plot.setRenderer(nextIndex, new XYLineAndShapeRenderer(true, false));
            
            NumberAxis axis = new AutoNumberAxis(unit);
            plot.setRangeAxis(nextIndex, axis);
            plot.mapDatasetToRangeAxis(nextIndex, nextIndex);
            
            seriesMapping.put(unit, new ArrayList<String>());
        }
        else {
            col = (TimeSeriesCollection) plot.getDataset(datasetIndex);
        }
        TimeSeries ts = new SeriesWrapper(series, sampler);
        seriesMapping.get(unit).add(series.getKey());
        col.addSeries(ts);
        setColor(series.getKey(), palette.allocate());
    }

    private int getNextDatasetIndex(XYPlot plot) {
        for (int i = 0; i < plot.getDatasetCount(); i++) {
            if (plot.getDataset(i) == null) {
                return i;
            }
        }
        return plot.getDatasetCount();
    }

    private void rebuildLegend() {
        legend.removeAll();
        for (int i = 0; i < enabled.size(); i++) {
            makeLegendEntry(legend, chart, sampler.getSeries(enabled.get(i)).getLabel(), i);
        }
        legend.add(new JLabel());
        legend.add(new JLabel());
        JButton newb = IconLoader.makeImageButton("gui/icons/plus.png", "New...");
        
        newb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayAddPopup((JButton) e.getSource());
            } 
        });
        
        legend.add(newb);         
    }

    protected void displayAddPopup(JButton src) {
        JPopupMenu p = new JPopupMenu();
        
        Map<String, List<Series<?>>> series = sampler.getCategories();
        
        for (Map.Entry<String, List<Series<?>>> e1 : series.entrySet()) {
            JMenu sm = new JMenu(e1.getKey());
            for (final Series<?> s : e1.getValue()) {
                if (!enabled.contains(s.getKey())) {
                    JMenuItem mi = new JMenuItem(s.getLabel());
                
                    mi.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            enable(s);
                        } 
                    });
                    sm.add(mi);
                }
            }
            p.add(sm);
        }
        
        p.show(src, 4, 4);
    }
    

    private void makeLegendEntry(JPanel l, final JFreeChart chart, final String label, final int series) {
        Color color = (Color) chart.getPlot().getLegendItems().get(series).getLinePaint();
        final ColorButton cb = new ColorButton(color);
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displaySeriesPopup(chart, label, series, cb);
            }
        });
        l.add(cb);
        l.add(new JLabel(label));
    }

    protected void removeSeries(JFreeChart chart, int seriesIndex) {
        Color color = (Color) chart.getPlot().getLegendItems().get(seriesIndex).getLinePaint();
        palette.release(color);
        String key = enabled.remove(seriesIndex);
        XYPlot plot = chart.getXYPlot();
        Series<?> series = sampler.getSeries(key);
        Unit unit = series.getUnit();
        
        Integer datasetIndex = datasetMapping.get(unit);
        TimeSeriesCollection col = (TimeSeriesCollection) plot.getDataset(datasetIndex);
        
        List<String> colIndices = seriesMapping.get(unit);
        int colIndex = colIndices.indexOf(key);
        colIndices.remove(key);
        
        col.removeSeries(colIndex);
        if (col.getSeriesCount() == 0) {
            plot.setDataset(datasetIndex, null);
            plot.setRangeAxis(datasetIndex, null);
            seriesMapping.remove(unit);
            datasetMapping.remove(unit);
        }
        
        rebuildLegend();
        repaint();
        gp.saveLayout();
    }
    
    protected void displaySeriesPopup(final JFreeChart chart, final String label, final int series, final ColorButton button) {
        JPopupMenu p = new JPopupMenu();
        JMenuItem color = new JMenuItem("Color...");
        p.add(color);
        color.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayColorPicker(chart, label, series, button);
            }
        });
        
        JMenuItem remove = new JMenuItem("Remove");
        p.add(remove);
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSeries(chart, series);
            } 
        });
        
        p.show(button, 4, 4);
    }

    protected void displayColorPicker(JFreeChart chart, String label, int series, ColorButton button) {
        Color color = (Color) chart.getPlot().getLegendItems().get(series).getLinePaint();
        Color newColor = JColorChooser.showDialog(this, "Pick a color for " + label, color);
        if (newColor != null) {
            palette.release(color);
            setColor(enabled.get(series), newColor);
            gp.saveLayout();
        }
    }
    
    public void setColor(String key, Color color) {
        Series<?> series = sampler.getSeries(key);
        Integer datasetIndex = datasetMapping.get(series.getUnit());
        
        List<String> l = seriesMapping.get(series.getUnit());
        int colIndex = l.indexOf(key);
        
        int seriesIndex = enabled.indexOf(key);
        
        chart.getXYPlot().getRenderer(datasetIndex).setSeriesPaint(colIndex, color);
        chart.getXYPlot().getRenderer(datasetIndex).setSeriesStroke(colIndex, DEFAULT_STROKE);
        int i = 0;
        for (Component c : legend.getComponents()) {
            if (c instanceof ColorButton) {
                i++;
                if (i == seriesIndex + 1) {
                    ((ColorButton) c).setColor(color);
                }
            }
        }
    }
    
    public void enable(Series<?> s) {
        enable(s, true);
    }

    public void enable(Series<?> s, boolean save) {        
        enabled.add(s.getKey());
        addSeries(s);
        rebuildLegend();
        repaint();
        if (save) {
            gp.saveLayout();
        }
    }
    
    public void setMaxRange(int value) {
        this.maxRange = value;
        updateMaxRange();
    }

    private void updateMaxRange() {
        ValueAxis x = chart.getXYPlot().getDomainAxis();
        if (maxRange == 0) {
            x.setAutoRange(true);
        }
        else {
            Range dr = chart.getXYPlot().getDataRange(x);
            x.setRange(Math.max(dr.getLowerBound(), dr.getUpperBound() - maxRange * 60 * 1000), dr.getUpperBound());
        }
    }

    public void store(Preferences p) {
        p.putInt("enabledCount", enabled.size());
        for (int i = 0; i < enabled.size(); i++) {
            Preferences gp = p.node("series" + i);
            gp.put("key", enabled.get(i));
            Color color = (Color) chart.getPlot().getLegendItems().get(i).getLinePaint();
            gp.putInt("color.r", color.getRed());
            gp.putInt("color.g", color.getGreen());
            gp.putInt("color.b", color.getBlue());
        }
    }
    
    public static GraphPanel load(Preferences p, SystemState state, GraphsPanel gps) {
        DataSampler sampler = (DataSampler) state.getItemByID(DataSampler.ID, StatefulItemClass.WORKFLOW);
        GraphPanel g = new GraphPanel(state, gps);
        int ec = p.getInt("enabledCount", 0);
        for (int i = 0; i < ec; i++) {
            Preferences gp = p.node("series" + i);
            String key = gp.get("key", null);
            if (key == null) {
                throw new RuntimeException("Null series key");
            }
            int cr = gp.getInt("color.r", 255);
            int cg = gp.getInt("color.g", 0);
            int cb = gp.getInt("color.b", 0);
            g.enable(sampler.getSeries(key), false);
            g.setColor(key, new Color(cr, cg, cb));
        }
        return g;
    }
    
    private static class AutoNumberAxis extends NumberAxis {
        private Unit unit;
        private double min, max;
        private String unitPrefix;
        
        public AutoNumberAxis(Unit unit) {
            super(unit.getName());
            this.unit = unit;
            this.unitPrefix = "";
            this.setTickUnit(new NumberTickUnit(unit.getMultiplier(1), new DecimalFormat("###")));
        }
        
        
        @Override
        public void setRange(Range range, boolean turnOffAutoRange, boolean notify) {
            super.setRange(range, turnOffAutoRange, notify);
            this.min = range.getLowerBound();
            this.max = range.getUpperBound();
            recalculateUnits();
        }

        private void recalculateUnits() {
            double absmax = Math.max(Math.abs(min), Math.abs(max));
            String prefix = unit.getUnitPrefix(Double.valueOf(absmax));

            this.unitPrefix = prefix;
            this.setLabel(prefix + unit.getName());
            double mult = unit.getMultiplier(absmax);
            double range = max - min;
            double m2 = mult;
            while (range / m2 > 20) {
                m2 = m2 * 10;
            }
         
            this.setTickUnit(new NumberTickUnit(m2, new ShiftingDecimalFormat(mult, "###")));
        }
    }
    
    private static class ShiftingDecimalFormat extends DecimalFormat {
        private double multiplier;
        
        public ShiftingDecimalFormat(double multiplier, String fmt) {
            super(fmt);
            this.multiplier = multiplier;
        }

        @Override
        public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
            return super.format(number / multiplier, result, fieldPosition);
        }

        @Override
        public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
            return super.format(number / multiplier, result, fieldPosition);
        }        
    }
}
