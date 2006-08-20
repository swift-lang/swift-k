package org.globus.cog.monitor.guss;

interface GUSSConstants {

    /**
     *The constants TIMESERIES, CATEGORY_PLOT, and GRID_SUMMARY represent the types of graphs/tables
     *that GUSS can produce.  These are the valid arguments to the <code>graphType</code> parameter
     *of {@link #makeGraphImage} and {@link #makeTable}.
     *TIMESERIES tells GUSS to plot the value of a quantity over time.*/
    public static final int TIMESERIES = 1;
    /**this constant tells GUSS to plot the value of a quantity for each source/destination pair*/
    public static final int CATEGORY_PLOT = 2;
    /**GRID_SUMMARY tells GUSS to generate a high-level summary of whole-Grid status.*/
    public static final int GRID_SUMMARY = 3;
    
    public static final int HISTOGRAM = 4;


    /**
     *The constants NUM_HOSTS, NUM_USERS, NUM_TRANSFERS, TRANSFER_VOLUME, AVG_SPEED, AVG_TIME, 
     *and AVG_SIZE represent the different quantities that can be plotted in a graph or displayed
     *in a table.  These are the valid arguments to the <code>graphQuant</code> argument of
     *{@link #makeGraphImage} and {@link #makeTable}.
     *NUM_HOSTS tells GUSS  to count the number of unique hosts actively engaged in transfers.*/
    public static final int NUM_HOSTS = 1;
    /**this constant tells GUSS to count the number of transfers completed.*/
    public static final int NUM_TRANSFERS = 2;
    /**this constant tells GUSS to count the total volume, in kilobytes, of all transfers completed.*/
    public static final int TRANSFER_VOLUME = 3;
    /**this constant tells GUSS to calculate the average transfer speed, in kilobytes per second.*/
    public static final int AVG_SPEED = 4;
    /**this constant tells GUSS to caculate the average size in kilobytes of transferred files.*/
    public static final int AVG_SIZE = 5;
    /**this constant tells GUSS to calculate the average time in seconds taken to transfer a file.*/
    public static final int AVG_TIME = 6;

    public static final int NUM_NEW_HOSTS = 7;

    public static final int AVG_STREAMS = 8;
}
