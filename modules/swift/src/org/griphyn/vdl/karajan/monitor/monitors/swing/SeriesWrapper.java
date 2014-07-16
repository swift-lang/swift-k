//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 1, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.griphyn.vdl.karajan.monitor.common.DataSampler;
import org.griphyn.vdl.karajan.monitor.common.DataSampler.Listener;
import org.griphyn.vdl.karajan.monitor.common.DataSampler.Series;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

public class SeriesWrapper extends TimeSeries implements Listener {
    private final Series<?> series;
    private final DataSampler sampler;

    public SeriesWrapper(Series<?> s, DataSampler sampler) {
        super(s.getLabel());
        this.series = s;
        this.sampler = sampler;
        sampler.addListener(this);
    }

    @Override
    public void dataItemAdded() {
        this.fireSeriesChanged();
    }

    @Override
    public void add(RegularTimePeriod period, double value, boolean notify) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(RegularTimePeriod period, double value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(RegularTimePeriod period, Number value, boolean notify) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(RegularTimePeriod period, Number value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(TimeSeriesDataItem arg0, boolean arg1) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(TimeSeriesDataItem item) {
        throw new UnsupportedOperationException();
    }


    @Override
    public TimeSeries addAndOrUpdate(TimeSeries arg0) {
        throw new UnsupportedOperationException();
    }


    @Override
    public TimeSeriesDataItem addOrUpdate(RegularTimePeriod period, double value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public TimeSeriesDataItem addOrUpdate(RegularTimePeriod period, Number value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public TimeSeriesDataItem addOrUpdate(TimeSeriesDataItem arg0) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }


    @Override
    public TimeSeries createCopy(int arg0, int arg1) throws CloneNotSupportedException {
        throw new UnsupportedOperationException();
    }


    @Override
    public TimeSeries createCopy(RegularTimePeriod arg0, RegularTimePeriod arg1) throws CloneNotSupportedException {
        throw new UnsupportedOperationException();
    }


    @Override
    public void delete(int arg0, int arg1, boolean arg2) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void delete(int start, int end) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void delete(RegularTimePeriod arg0) {
        throw new UnsupportedOperationException();
    }


    @Override
    public TimeSeriesDataItem getDataItem(int index) {
        return new TimeSeriesDataItem(getTimePeriod(index), series.getData().get(index));
    }

    @Override
    public TimeSeriesDataItem getDataItem(RegularTimePeriod period) {
        return new TimeSeriesDataItem(period, series.getData().get(getIndex(period)));
    }


    @Override
    public int getIndex(RegularTimePeriod period) {
        return (int) ((period.getMiddleMillisecond() / 1000) - sampler.getOffset());
    }


    @Override
    public int getItemCount() {
        return series.getData().size();
    }


    @Override
    public List getItems() {
        return new AbstractList<TimeSeriesDataItem>() {
            @Override
            public TimeSeriesDataItem get(int index) {
                return getDataItem(index);
            }

            @Override
            public int size() {
                return getItemCount();
            }  
        };
    }


    @Override
    public double getMaxY() {
        Number n = series.getMaxValue();
        if (n == null) {
            return 0;
        }
        else {
            return n.doubleValue();
        }
    }


    @Override
    public long getMaximumItemAge() {
        return sampler.getCapacity() * 1000;
    }


    @Override
    public int getMaximumItemCount() {
        return sampler.getCapacity();
    }


    @Override
    public double getMinY() {
        Number n = series.getMinValue();
        if (n == null) {
            return 0;
        }
        else {
            return n.doubleValue();
        }
    }


    @Override
    public RegularTimePeriod getNextTimePeriod() {
        return getTimePeriod(getItemCount());
    }


    @Override
    public RegularTimePeriod getTimePeriod(int index) {
        long time = ((long) (sampler.getOffset() + index)) * 1000;
        return new Second(new Date(time));
    }

    @Override
    public Class getTimePeriodClass() {
        return Second.class;
    }


    @Override
    public Collection getTimePeriods() {
        return new AbstractList<RegularTimePeriod>() {
            @Override
            public RegularTimePeriod get(int index) {
                return getTimePeriod(index);
            }

            @Override
            public int size() {
                return getItemCount();
            }
            
        };
    }


    @Override
    public Collection getTimePeriodsUniqueToOtherSeries(TimeSeries arg0) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Number getValue(int index) {
        return series.getData().get(index);
    }


    @Override
    public Number getValue(RegularTimePeriod period) {
        return getValue(getIndex(period));
    }


    @Override
    public void removeAgedItems(boolean arg0) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void removeAgedItems(long arg0, boolean arg1) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setMaximumItemAge(long periods) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setMaximumItemCount(int maximum) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void update(int arg0, Number arg1) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void update(RegularTimePeriod period, double value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void update(RegularTimePeriod period, Number value) {
        throw new UnsupportedOperationException();
    }
}
