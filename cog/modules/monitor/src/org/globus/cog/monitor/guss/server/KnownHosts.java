package org.globus.cog.monitor.guss;

import java.util.HashMap;
import java.util.Date;
import java.util.Iterator;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

class KnownHosts {  /*meant to be a singleton, everything is static*/

    /*Keys are hostnames, values are dates:*/
    private static HashMap firstDateMap = new HashMap();
    private static HashMap lastDateMap = new HashMap();
    //the table to write this in when saving to database:
    private static final String TABLENAME = "known_hosts";

    public static boolean contains(String hostName) {
	return firstDateMap.containsKey(hostName);
    }
    public static void setLastDate(String hostName, Date lastDate) {
	lastDateMap.put(hostName, lastDate);
    }
    public static void setFirstDate(String hostName, Date firstDate) {
	firstDateMap.put(hostName, firstDate);
	lastDateMap.put(hostName, firstDate);
	//the first date is also the only date so far, so it is also the
	//last date so far.
    }

    public static void writeToDatabase(Connection con) throws
	SQLException {
	Statement stmt = con.createStatement();
	Iterator it;

	final String sqlBase = "INSERT INTO known_hosts (host_ip, host_name, first_seen_gftp, last_seen_gftp) VALUES ";

	final String updateBase = "UPDATE known_hosts SET last_seen_gftp=";
	it = firstDateMap.keySet().iterator();
	while (it.hasNext()) {

	    Object key = it.next();
	    Date first, last;
	    String hostname;
	    String[] hostnameParts;
	    StringBuffer sqlText = new StringBuffer();

	    hostname = (String)key;
	    hostnameParts = hostname.split("/");
	    first = (Date)firstDateMap.get(key);
	    last = (Date)lastDateMap.get(key);
		
	    sqlText.append(sqlBase);
	    sqlText.append("('");
	    sqlText.append(hostnameParts[1]);
	    sqlText.append("', '");
	    sqlText.append(hostnameParts[0]);
	    sqlText.append("', '");
	    sqlText.append((new Timestamp(first.getTime())).toString());
	    sqlText.append("', '");
	    sqlText.append((new Timestamp(last.getTime())).toString());
	    sqlText.append("');");


	    try {
		stmt.executeUpdate(sqlText.toString());
	    }
	    catch (SQLException e) { //If that didn't work, try this:
		sqlText = new StringBuffer();
		sqlText.append(updateBase);
		sqlText.append("'");
		sqlText.append((new Timestamp(last.getTime())).toString());
		sqlText.append("' WHERE host_ip='");
		sqlText.append(hostnameParts[1]);
		sqlText.append("';");
		stmt.executeUpdate(sqlText.toString());
	    }
	}
	stmt.close();
    }

    public static void readFromDatabase(Connection con) throws
	SQLException {
	PreparedStatement stmt;
	ResultSet rs;

	/*Clear out anything that was in the hashes:*/
	firstDateMap = new HashMap();
	lastDateMap = new HashMap();

	stmt = con.prepareStatement("SELECT host_ip, host_name, first_seen_gftp, last_seen_gftp FROM known_hosts;");
	rs = stmt.executeQuery();

	while (rs.next()) {
	    StringBuffer keyString = new StringBuffer();
	    keyString.append(rs.getString(2));
	    keyString.append("/");
	    keyString.append(rs.getString(1));
	    Date firstSeen = new Date((rs.getTimestamp(3)).getTime());
	    Date lastSeen = new Date((rs.getTimestamp(4)).getTime());
	    String key = keyString.toString();

	    firstDateMap.put(key, firstSeen);
	    lastDateMap.put(key, lastSeen);
	}

	rs.close();
	stmt.close();
    }
}
