
package org.globus.cog.monitor.guss;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;

public class GUSSPortlet extends GenericPortlet implements GUSSConstants
{

    private int servicePort;
    private String serviceHostname;
    private GUSSClient clientStub;

    private final String linkMessage = "<p><b>Grid Usage Sensor Services</b> is part of the <a href=\"http://www.cogkit.org\"><u>Java CoGKit</u></a> and was developed with funding from the <a href=\"http://www.nsf-middleware.org\"><u>NSF Middleware Initiative</u></a> (NMI).</p>";

    private final String linkButtonText = "<form action=\"http://www.cogkit.org\" target=\"_blank\"><input type=submit name=submit value=\"About\"></form>";

    public void init(PortletConfig config) 
	throws PortletException {
	
	super.init(config);

	/*Get hostname, portnum to connect to from portlet.xml:*/
	servicePort = Integer.parseInt(
	    config.getInitParameter("service-port"));
	serviceHostname = config.getInitParameter("service-hostname");

	clientStub = new GUSSClient(serviceHostname, servicePort);

    }

    public void doView(RenderRequest request, RenderResponse response) 
	throws PortletException, IOException {
	//Generate and return html for main view here...

	int granularity, graphQuant;
	int startMonth, endMonth, startDay, endDay, graphOrTable;
	Date startDate, endDate;
	Calendar cal;
	PortletURL theActionURL;
	DateFormat df = DateFormat.getDateInstance();
	String[] arguments;

	response.setContentType(request.getResponseContentType()); //must be done before getWriter

	PrintWriter out = response.getWriter();
	response.setTitle("GUSS Portlet");

	theActionURL = response.createActionURL();
	out.write(linkMessage);

	granularity = getIntFromRequest(request, "granularity", 3);
	graphQuant = getIntFromRequest(request, "graphQuant", 1);
	/*Default start and end dates:  A week ago to now.*/
	cal = Calendar.getInstance();
	endMonth = getIntFromRequest(request, "endMonth", cal.get(Calendar.MONTH));
	endDay = getIntFromRequest(request, "endDay", cal.get(Calendar.DATE));
	startMonth = getIntFromRequest(request, "startMonth", endMonth);
	startDay = getIntFromRequest(request, "startDay", (endDay <=7)?1:endDay-7);
	graphOrTable = getIntFromRequest(request, "graphOrTable", TIMESERIES);

	makeMenus(out, theActionURL, granularity, graphQuant, startMonth, startDay, endMonth, endDay, graphOrTable);
	startDate = dateFromNumbers(startMonth, startDay);
	endDate = dateFromNumbers(endMonth, endDay);

	arguments = figureOutArguments(startDate, endDate, granularity);
	switch (graphOrTable) {
	case TIMESERIES:
	    out.write(clientStub.makeGraphImage(TIMESERIES, graphQuant, arguments));
	    break;
	case HISTOGRAM:
	    out.write(clientStub.makeGraphImage(HISTOGRAM, graphQuant, arguments));
	    break;
	case GRID_SUMMARY:
	    out.write(clientStub.makeTable(1, graphQuant, arguments));
	    break;
	}
	out.write(linkButtonText);
    }

    private Date dateFromNumbers(int month, int day) {
	Calendar cal = Calendar.getInstance();

	cal.set(Calendar.MONTH, month);
	cal.set(Calendar.DATE, day);
	
	return cal.getTime();
    }
    
    private int getIntFromRequest(RenderRequest request, String parameter, int vanilla) {
	String temp;
	temp = request.getParameter(parameter);
	if (temp != null)
	    return Integer.parseInt(temp);
	else
	    return vanilla;
    }
    
    private String[] figureOutArguments(Date startDate, Date endDate, int granularity) {
	String[] arguments = new String[4];

	arguments[0] = Long.toString(startDate.getTime());
	arguments[1] = Long.toString(endDate.getTime());
	if (granularity == 3 ) {
	    arguments[2] = "hour";
	} else if (granularity == 2) { 
	    arguments[2] = "day";
	} else if (granularity == 1) { 
	    arguments[2] = "week";
	}
	return arguments;
    }

    public void doEdit(RenderRequest request, RenderResponse response)
	throws PortletException, IOException {
	response.setContentType(request.getResponseContentType());
	PrintWriter out = response.getWriter();

	out.write("GUSS Portlet in Edit mode.");

    }

    public void doHelp(RenderRequest request, RenderResponse response) 
	throws PortletException, IOException {
	response.setContentType(request.getResponseContentType());
	PrintWriter out = response.getWriter();

	out.write("GUSS Portlet in Help mode.");
    }

    public void processAction(ActionRequest request, ActionResponse response) 
	throws PortletException, IOException {

	String granularity = request.getParameter("granularity");
	String graphQuant = request.getParameter("graphQuant");
	String startMonth = request.getParameter("startMonth");
	String startDay = request.getParameter("startDay");
	String endMonth = request.getParameter("endMonth");
	String endDay = request.getParameter("endDay");
	String graphOrTable = request.getParameter("graphOrTable");

	if (granularity != null)
	    response.setRenderParameter("granularity", granularity);
	if (graphQuant != null)
	    response.setRenderParameter("graphQuant", graphQuant);
	if (startMonth != null)
	    response.setRenderParameter("startMonth", startMonth);
	if (startDay != null)
	    response.setRenderParameter("startDay", startDay);
	if (endMonth != null)
	    response.setRenderParameter("endMonth", endMonth);
	if (endDay != null)
	    response.setRenderParameter("endDay", endDay);
	if (graphOrTable != null)
	    response.setRenderParameter("graphOrTable", graphOrTable);
    }


    private void makeMenus(PrintWriter out, PortletURL theActionURL,
			   int granularity, int graphQuant,
			   int startMonth, int startDay,
			   int endMonth, int endDay, int graphOrTable) {
	
	String[] grains = new String[] {"none", "Week", "Day", "Hour"};
	String[] quants = new String[] {"none",
					"Number of Active Hosts", 
					"Number of Transfers",
					"Total Transfer Volume",
					"Average Throughput",
					"Average File Size",
					"Average Transfer Time",
					"Number of New Hosts",
	                                "Average Number of Streams"};
	
	out.write("<p><form type=post action=" + theActionURL + ">");

	out.write("<input type=radio name=graphOrTable value=" + GRID_SUMMARY);
	if (graphOrTable == GRID_SUMMARY) out.write(" checked");
	out.write("> Grid Summary Table");
	out.write("<input type=radio name=graphOrTable value=" + TIMESERIES);
	if (graphOrTable == TIMESERIES) out.write(" checked");
	out.write("> Time Series Plot of");
	out.write("<input type=radio name=graphOrTable value=" + HISTOGRAM);
	if (graphOrTable == HISTOGRAM) out.write(" checked");
	out.write("> Histogram of<br>");
		  
	/*Make a pop-up menu for graph quantity with the current value
	  pre-selected*/
	out.write("<select name=\"graphQuant\">");
	for (int j=1; j<quants.length; j++) {
	    out.write("<option value=" + j);
	    if (graphQuant == j)
		out.write(" selected ");
	    out.write(">" + quants[j] + "\n");
	}
	out.write("</select> by the ");

	/*Make radio buttons for granularity with the current value
	  pre-selected*/
	for (int i=1; i<grains.length; i++) {
	    out.write("<input type=radio name=granularity value=" + i);
	    if (granularity == i)
		out.write(" checked");
	    out.write("> " + grains[i]);
	}

	out.write("<br>from ");
	makeDateMenus(out, "startMonth", "startDay", startMonth, startDay);
	out.write(" to ");
	makeDateMenus(out, "endMonth", "endDay", endMonth, endDay);
       
	out.write(". <input type=submit name=submit Value=\"Refresh\"></form><br>");
    }

    private void makeDateMenus(PrintWriter out, String paramName1, String paramName2,
			       int value1, int value2) {

	String[] months = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
					"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	int j;

	out.write("<select name=\"");
	out.write(paramName1);
	out.write("\">");
	for (j=0; j<months.length; j++) {
	    out.write("<option value=" + j);
	    if (value1 == j)
		out.write(" selected ");
	    out.write(">" + months[j] + "\n");
	}
	out.write("</select>");


	out.write("<select name=\"");
	out.write(paramName2);
	out.write("\">");
	for (j=1; j<32; j++) {
	    out.write("<option value=" + j);
	    if (value2 == j)
		out.write(" selected ");
	    out.write(">" + j + "\n");
	}
	out.write("</select>");
    }
}
