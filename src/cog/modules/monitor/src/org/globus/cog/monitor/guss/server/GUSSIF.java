package org.globus.cog.monitor.guss;

import java.rmi.RemoteException;

/**
 * GUSS Interface is implemented by both {@link GUSSImpl service} and
 * {@link GUSSClient client} and represents the contract between them.
 * It is used to auto-generate the WSDL using axis tools.
 * @version 1.6
 */
public interface GUSSIF {
    /**
     *Creates a chart image based on user request.  Returns an HTML link to
     *this image.  If graphType is GUSSIF.TIMESERIES, then this method expects
     *to find at least three more arguments in
     *<code>options</code>:<OL><LI>Space-separated list of source
     *hosts</LI><LI>Space-separated list of destination
     *hosts</LI><LI>Granularity (either "minute", "hour", "day", or "week").
     *Any further strings in <code>options</code> will be interpreted as
     *{@link Filter} 
     *@param graphType determines whether graph will be e.g. histogram or host-by-host category plot.  One of the constants defined below.
     *@param graphQuant determines the quantity to be graphed, e.g. total transfer volume or average transfer speed.  One of the constants defined below.
     *@param options extra arguments, passed through SOAP as an array of strings; interpretation depends on graphType.
     *@return HTML fragment containing image tag that links to URL of the image file.
     *@see GUSSImpl
     */
    public String makeGraphImage(int graphType, int graphQuant, String[] options);

    /**
     *Creates a table of numerical data based on user request and returns it as an HTML fragment.
     *@param graphType determines whether graph will be e.g. histogram or host-by-host category plot.  One of the constants defined below.
     *@param graphQuant determines the quantity to be graphed, e.g. total transfer volume or average transfer speed.  One of the constants defined below.
     *@param options extra arguments, passed through SOAP as an array of strings.  (Currently no extra arguments are used, but this is subject to change.)
     *@return HTML fragment containing the table
     *@see GUSSImpl
     */
    public String makeTable(int graphType, int graphQuant, String[] options);
    
    /**
     *Gets a list of the names of all hosts who participated in any file transfers (ie, all hosts which are mentioned in any of the logfiles.)
     *@param options an array containing extra optional arguments.  Currently none are used, but this is subject to change.
     *@return space-separated list of the host names.
     *@see GUSSImpl
     */
    public String getAllHostNames(String[] options);

    public long[] getNumbers(int graphType, int graphQuant, String[] options) throws RemoteException;

}
