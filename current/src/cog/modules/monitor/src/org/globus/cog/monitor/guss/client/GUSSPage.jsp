<%@ page import="org.globus.cog.monitor.guss.GUSSClient" %>
<%@ page import="org.globus.cog.monitor.guss.subcreated.GUSSIFServiceLocator" %>
<%@ page import="org.globus.cog.monitor.guss.subcreated.GUSSIF" %>
<%@ page import="org.globus.cog.monitor.guss.subcreated.GUSSSoapBindingStub" %>

<!-- this test page goes in tomcat/webapps/jsp-examples -->
<!-- to find it, GUSSClient.jar must be in tomcat/common/lib. -->
<!-- To use webservice client, axis jars must be there too. -->

<html>
<head><title>Grid Usage Sensor Services</title></head>
<body bgcolor="white">

<h2>Java CoG Kit -- Grid File Transfer Monitor</h2>
<%String temp[];
int chartType = 3;
int quant = 4;
GUSSClient theGussClient;
String GUSSOutput = "";
String granularity = "";
String[] hostNames;
String selectedSources = "";
String selectedDests = "";
boolean[] srcCheckBoxes;
boolean[] destCheckBoxes;
boolean totalCheckBox = false;

    theGussClient = new GUSSClient();

    //Store list of hostnames in the session:
    if (session.isNew()) {
        String hostNameBlob = theGussClient.getAllHostNames();
        hostNames = hostNameBlob.split(" ");
	session.setAttribute("hostNameList", hostNameBlob);
    }
    else {
	String hostNameBlob = (String)session.getAttribute("hostNameList");
	hostNames = hostNameBlob.split(" ");
    } 

    srcCheckBoxes = new boolean[hostNames.length];
    destCheckBoxes = new boolean[hostNames.length];

    temp = request.getParameterValues("srcHost");
    if (temp!= null && temp.length > 0) {
         selectedSources = "";
	 int z;
	 int index;
         for (z = 0; z<temp.length; z++) {
	     if (temp[z].equals("total")) {
		selectedSources = selectedSources + "total ";
		totalCheckBox = true;
             }
	     else {
	         index = Integer.parseInt(temp[z]);
	         srcCheckBoxes[index] = true;
	         selectedSources = selectedSources + hostNames[index]+" ";
             }
	 }
    } else {
	 totalCheckBox = true;
	 selectedSources = "total";
    }

   temp = request.getParameterValues("destHost");
    if (temp!= null && temp.length > 0) {
         selectedDests = "";
	 int z;
	 int index;
         for (z = 0; z<temp.length; z++) {
	     index = Integer.parseInt(temp[z]);
	     destCheckBoxes[index] = true;
	     selectedDests = selectedDests + hostNames[index]+" ";
         }
    }


    temp = request.getParameterValues("chartType");	
    if (temp != null && temp.length > 0)
        chartType = Integer.parseInt(temp[0]);
    else
	chartType = 3;

    temp = request.getParameterValues("granularity");
    if (temp != null && temp.length > 0)
       granularity = temp[0];
    else
       granularity = "day";

    temp = request.getParameterValues("graphQuant");
    if (temp != null && temp.length > 0) {
       if (temp[0].equals("hosts"))
           quant = 1;
       if (temp[0].equals("users"))
           quant = 2;
       if (temp[0].equals("transfers"))
	   quant = 3;
       if (temp[0].equals("volume"))
	   quant = 4;
       if (temp[0].equals("avgspeed"))
	   quant = 5;
       if (temp[0].equals("avgsize"))
	   quant = 6;
       if (temp[0].equals("avgtime"))
	   quant = 7;

   }

   if (chartType == 1) {
	GUSSOutput = theGussClient.makeGraphImage(chartType, quant, new String[] {selectedSources, selectedDests, granularity});
   }
   else if (chartType == 2 || chartType == 3){
        GUSSOutput = theGussClient.makeTable(chartType, quant, new String[]{selectedSources, selectedDests, granularity});
   }
    
%>

<form type=post>
<br>
<input type=radio name=chartType value=1
<%= chartType==1?"checked":""%>> Histogram
<input type=radio name=chartType value=2
<%= chartType==2?"checked":""%>> Table 
<input type=radio name=chartType value=3
<%= chartType==3?"checked":""%>> Summary
&nbsp;&nbsp;
<% if (chartType != 3) { %>
of <select name="graphQuant">
<option value="hosts" <%= quant==1?"selected":""%>>Number of Active Hosts
<option value="users" <%= quant==2?"selected":""%>>Number of Active Users
<option value="transfers" <%= quant==3?"selected":""%>>Number of Transfers
<option value="volume" <%= quant==4?"selected":""%>>Total Transfer Volume
<option value="avgspeed" <%= quant==5?"selected":""%>>Average Throughput
<option value="avgsize" <%= quant==6?"selected":""%>>Average File Size
<option value="avgtime" <%= quant==7?"selected":""%>>Average Transfer Time
</select>
<% } %>
&nbsp;&nbsp;
<% if (chartType == 1) {
      String dropDownMenu = "<select name=\"granularity\">";
      dropDownMenu += "<option value=\"minute\" ";
      if (granularity.equals("minute"))
	  dropDownMenu += "selected";
      dropDownMenu += ">Minute";
      dropDownMenu += "<option value=\"hour\" ";
      if (granularity.equals("hour"))
	  dropDownMenu += "selected";
      dropDownMenu += ">Hour";
      dropDownMenu += "<option value=\"day\" ";
      if (granularity.equals("day"))
	  dropDownMenu += "selected";
      dropDownMenu += ">Day\n";
      dropDownMenu += "<option value=\"week\" ";
      if (granularity.equals("week"))
	  dropDownMenu += "selected";
      dropDownMenu += ">Week\n";
     
      dropDownMenu += "</select>";
      out.println(" by " + dropDownMenu);
}%>&nbsp;&nbsp;
<INPUT TYPE=submit name=submit Value="Refresh"><BR>
<HR>

<%= GUSSOutput %><BR>

<% if (chartType != 3)
   switch (quant)  { 
   case 5:%>
<hr><p><b>About Average Throughput calculations:</b><br>
The size of each file transferred is divided by the time needed to transfer it
to find the throughput for that transfer.  The point that is plotted
represents the mean of this value over all transfers that occurred
during the time period.<br> 
Note that the logging done by the GridFTP server is only accurate to about a second.
For very small files that transfer in less than a second, the calculated
transfer time (and thus the throughput computed from it) are highly
suspect.  Therefore only transfers that took longer than one second are counted
in this graph.<BR>Also, the average throughput may be misleadingly low on days
when there were many very small transfers, because the elapsed time for these
transfers is mostly the overhead of starting the connection.</P><hr>

<% break; case 1: %>

<hr><p><b>About this data:</b><br>
Each data point represents the number of unique hosts who were participating in
transfers during the time period.
This option is meant to be used with a graph of "totals", as plotting it
host-by-host doesn't show anything interesting.
</p>

<% break; case 2: %>

<hr><p><b>About this data:</b><br>
Each data point represents the number of unique users who were participating in
transfers between the given pair of machines during the time period in
question.
</p>

<% break; case 3: %>
<hr><p><b>About this data:</b><br>
Each data point represents the number of transfers that were made between the
given pair of machines during the time period in question.</p>

<% break; case 4: %>
<hr><p><b>About this data:</b><br>
Each data point represents the sum of the file sizes of all transfers that
were made between the given pair of machines during the time period in question.</p>


<% break; case 6: %>
<hr><p><b>About this data:</b><br> Each data point
represents the mean file size calculated over all transfers that were made
between the given pair of machines during the time period in question.</p>

<% break; case 7: %>
<hr><p><b>About this data:</b><br> For each transfer that occurred between the
given pair of machines during the time period in question, the time that
transfer took to complete is calculated.  The number that is plotted is the
mean of this value over all tranfers in that time period.</p>

<% break; } %>

<% if (chartType == 1) { %>
  <p><b>About the histogram:</b> Each data point is calculated only from the
  transfers that occured during that (week/day/hour/minute).  Totals and
  averages are for that time period only and are not cumulative.  If no point is plotted on the histogram for a certain time interval, it
  means that no transfers were made between those hosts during that time
  interval.</p>
  <hr>
 <P><i>Select the source and destination hosts for which you want data
  displayed (each pair will be plotted separately).  If the "totals" check box
  is selected, an additional plot will be displayed, computed from all
  transfers made during the time interval, regardless of host.</i></p>
 <table>
	<tr><td colspan=2><input type=checkbox name=srcHost value="total" <%=totalCheckBox?"checked":""%>>Totals</td></tr> <tr><th>Sources</th><th>Destinations</th></tr><%
      for (int i = 0; i<hostNames.length; i++) { %>
         <tr><td>
         <input type=checkbox name=srcHost value=<%=i%> <%=srcCheckBoxes[i]?"checked":""%>> <%=hostNames[i]%>
         </td><td>
         <input type=checkbox name=destHost value=<%=i%> <%=destCheckBoxes[i]?"checked":""%>> <%=hostNames[i]%>
         </td></tr>
    <% } %>

    </table>
<% }  %>
</form>
</body>
</html>
