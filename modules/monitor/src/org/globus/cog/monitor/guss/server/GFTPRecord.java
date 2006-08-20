package org.globus.cog.monitor.guss;

import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An invariant data object representing a record of a single file
 * transfer.  Created by {@link GUSSImpl} when it parses server
 * logfiles; passed to {@link Filter}, {@link TimeSorter}, {@link
 * DataCounter} etc. for processing.
 * @version 1.4
 */
public class GFTPRecord
{   
    private static DateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static byte STOR = 0;
    public static byte RETR = 1;

    /*The following variables MUST be known.  They correspond to NOT NULL
    fields in the database.*/
    private Date startTime, endTime;
    private byte storOrRetr;
    private String loggerAddress;
    private long fileSize;

/*The following fields are available in both packets and logfiles, but are not particularly essential, so they are allowed to be zero/null in the database..:*/
    private long numStripes, numStreams, blockSize, bufferSize, ftpReturn;

//The following fields are only in packets; will be null for records from logfile:
    private long sequenceNum;
    private String gftpVersion;

//These are only in logfiles, not packets:
    private String otherSideAddress;
    //username will be replaced by hash or GUID if used at all
    private String userName; 

    /*The following fields will be derived if possible.  srcHost and destHost are
      derived from loggerIP and othersideIP.  storOrRetr tells us which is
      which.  Unknown fields are set to null.*/
    private String srcHost, destHost;
    private Integer srcID, destID;


    /**
     *This constructor is used to create GFTPRecords with specific fields for 
     *testing purposes.  In real code it is better to use 
     *{@link #GFTPRecord(String)} instead.
     *@param startTime date of start of transfer
     *@param endTime date of end of transfer
     *@param loggerAddress ip or hostname of machine logging the transfer
     *@param fileSize file size (bytes).*/
    GFTPRecord(Date startTime, Date endTime, String loggerAddress, byte storOrRetr, long fileSize) {
	this.numStripes = 0;
	this.numStreams = 0;
	this.blockSize = 0;
	this.bufferSize = 0;
	this.ftpReturn = 0;
	this.sequenceNum = 0;
	this.otherSideAddress = null;
	this.srcHost = null;
	this.destHost = null;
	this.userName = null;
	this.gftpVersion = null;

	this.startTime = startTime;
	this.endTime = endTime;
	this.loggerAddress = loggerAddress;
	this.storOrRetr = storOrRetr;
	this.fileSize = fileSize;
    }

    /*Constructor to set all fields:*/
    public GFTPRecord(Date startTime, Date endTime, String loggerAddress, byte storOrRetr, long fileSize,
	       long numStripes, long numStreams, long bufferSize, long blockSize, long ftpReturn, 
	       String gftpVersion) {

	this.startTime = startTime;
	this.endTime = endTime;
	this.storOrRetr = storOrRetr;
	this.fileSize = fileSize;
	this.numStripes = numStripes;
	this.numStreams = numStreams;
	this.blockSize = blockSize;
	this.bufferSize = bufferSize;
	this.ftpReturn = ftpReturn;
	this.gftpVersion = gftpVersion;
	this.loggerAddress = loggerAddress;

	this.sequenceNum = 0;
	this.otherSideAddress = null;
	this.srcHost = null;
	this.destHost = null;
	this.userName = null;
    }

    
    /**
     *Constructor that determines values for record fields by parsing a line from a logfile.  TODO: throw exception if the line is unparsable.
     *@param lineToParse expected to have a format like the following example:
     *DATE=20040609163425.483923 HOST=jmayor1 PROG=wuftpd NL.EVNT=FTP_INFO START=20040609163425.339678 USER=voeckler FILE=/home/voeckler/vdldemo/test.tmp BUFFER=524288 BLOCK=65536 NBYTES=47 VOLUME=/home STREAMS=1 STRIPES=1 DEST=1[128.135.152.241] TYPE=STOR CODE=227*/
    public GFTPRecord(String lineToParse) throws IOException {

	/*Split the line on spaces to get the fields; each field is
	 * "name=value" so split again on the equals sign and take the right
	 * half to get the value of the field, then parse appropriately.*/

	String[] fields = lineToParse.split(" ");
	int i;

	if (fields.length < 16) {
	    throw new IOException("Line cannot be parsed into GFTPRecord.");
	}

	for (i=0; i<fields.length; i++) {
	    //Get the part after equals sign of each field...
	    String [] temp = fields[i].split("=");
	    if (temp.length != 2) {
		throw new IOException("Line cannot be parsed into GFTPRecord.");
	    }
	    fields[i] = new String(temp[1]);
	}

	/*Todo:  Fail politely when lineToParse is not in the correct format.*/
	endTime=dateFromLogfile(fields[0]);
	startTime=dateFromLogfile(fields[4]);
	
	userName=fields[5]; /*Careful with this!!*/

	bufferSize = Long.parseLong(fields[7]);
	blockSize = Long.parseLong(fields[8]);	
	fileSize = Long.parseLong(fields[9]);
	numStreams = Long.parseLong(fields[11]);
	numStripes = Long.parseLong(fields[12]);
	ftpReturn = Long.parseLong(fields[15]);	
	
	/*STOR means that the remote host (called "DEST") is storing a file
	  onto me RETR means that the remote host is retrieving a file from
	  me.*/
	loggerAddress = fields[1];
	otherSideAddress = fields[13];
	if (fields[14].equals("STOR")) {
	    storOrRetr = STOR;
	    srcHost = otherSideAddress;
	    destHost = loggerAddress;
	} else if (fields[14].equals("RETR")) {
	    storOrRetr = RETR;
	    srcHost = loggerAddress;
	    destHost = otherSideAddress;
	}

	srcID = HostNameHash.nameToNumber(srcHost);
	destID = HostNameHash.nameToNumber(destHost);
    }

    /**GFTPRecord from a ResultSet.  Call this in a loop after a query.*/
    public GFTPRecord(ResultSet rs) throws SQLException {
	this.loggerAddress = rs.getString("ip_address");
	this.storOrRetr = rs.getByte("stor_or_retr");
	//rs.getDate() returns year/month/day with no time value! not what I want.
	this.startTime = new Date(rs.getLong("start_time"));
	this.endTime = new Date(rs.getLong("end_time"));
	this.fileSize = rs.getLong("num_bytes");
	    
	this.numStripes = rs.getLong("num_stripes");
	this.numStreams = rs.getLong("num_streams");
	this.bufferSize = rs.getLong("buffer_size");
	this.blockSize = rs.getLong("block_size");
	this.ftpReturn = rs.getLong("ftp_return_code");

	this.sequenceNum = 0;
	this.otherSideAddress = null;
	this.srcHost = null;
	this.destHost = null;
	this.userName = null;
	this.gftpVersion = null;
    }



    public boolean equals(Object object) {
	GFTPRecord other;

	if (object instanceof GFTPRecord) {
	    other = (GFTPRecord)object;
		
	    /*One or more of userName, srcHost, destHost
	      may be missing (null).  So base the comparison only on
	      the required fields.*/
	    return ((this.startTime.equals(other.startTime)) &&
		    (this.endTime.equals(other.endTime)) &&
		    (this.loggerAddress.equals(other.loggerAddress)) &&
		    (this.storOrRetr == other.storOrRetr) &&
		    (this.fileSize == other.fileSize));
	}
	else
	    return false;
    }

    public boolean optionalFieldsAreEqual(GFTPRecord other) {

//	System.err.println("Comparing optoinal fields:");
//	this.debug();
//	other.debug();
	return (this.numStripes == other.numStripes &&
		this.numStreams == other.numStreams &&
		this.blockSize == other.blockSize &&
		this.bufferSize == other.bufferSize &&
		this.ftpReturn == other.ftpReturn);
	/*also:
	  otherSideAddress, gftpVersion, sequenceNum
	  srcHost, destHost, srcID, destID, userName*/
    }

    /**We only want to put the transfer in the
       database once, but it may not be obvious that records on different
       hosts refer to the same transfer. i.e. that the sending server
       and receiving server both recorded it.  This method tries to make
       a best guess.
       If the combination succeeds, we should be able to get a new record
    that combines both of the old records... i.e. sourceHost and destHost are
    now both known.*/
    public boolean combineRecords(GFTPRecord other) {
	/*Todo.
	  Check that filesizes are identical and start and end dates
	  are within some small difference and that one is a stor and the
	  other is a retrieve.*/
	return false;
    }

    /**
     *Helper function used by constructor; takes the DEST field from the line
     *being parsed, strips off the brackets, and attempts to resolve the IP
     *address therein.
     *@param ip a string containing a hostname or IP address (possibly surrounded by brackets)
     *@return if resolution was successful, the host name; otherwise, the IP address as a string.*/
    private String resolveIP(String ip) {
	int startBracket, endBracket;
	String goodIP, hostName;

	startBracket = ip.indexOf('[');
	endBracket = ip.indexOf(']');
	
	if (startBracket == -1 || endBracket == -1)
	    return ip;
	else {
	    goodIP = ip.substring(startBracket+1, endBracket);
	    try {
		hostName = InetAddress.getByName(goodIP).getHostName();
		return hostName;
	    }
	    catch (UnknownHostException e) {
		return goodIP;
	    }
	}
    }

    /**Helper function used by {@link #GFTPRecord constructor}.  Parses the non-standard
     *date encoding used in the logfiles and returns a date.
     *@param logFileEntry the DATE field from the logfile line.
     *@return the same date as a java.util.Date object.*/
    private Date dateFromLogfile(String logfileEntry) {
	int year, month, day, hour, minute, second;
	float millis;
	Calendar temp;

	/*Date format used in log files has fixed-width fields, so
	  I can do this the easy way; this is however brittle if logfile format changes.*/
	year = 2000 + Integer.parseInt(logfileEntry.substring(3,4));
	/*Calendar uses 0-based indexing for months; i THINK logfiles used 1-based
	indexing, but TODO: make sure of this.*/
	month = Integer.parseInt(logfileEntry.substring(4,6)) - 1;
	day = Integer.parseInt(logfileEntry.substring(6,8));
	hour = Integer.parseInt(logfileEntry.substring(8,10));
	minute = Integer.parseInt(logfileEntry.substring(10,12));
	second = Integer.parseInt(logfileEntry.substring(12,14));
	//substring with one arg gets from there till end of string
	millis = 1000*Float.parseFloat("0"+logfileEntry.substring(14));
	/*In some cases the microseconds may actually be less than six digits.*/
	temp = Calendar.getInstance();
	temp.set(year, month, day, hour, minute, second);
	/*Constructor can't take milliseconds, so here's how I adjust it to milliseconds:*/
	temp.set(Calendar.MILLISECOND, (int)Math.round(millis));
	return temp.getTime();
    }
    
    /**
     *This is the ID number assigned by {@link HostNameHash}, guaranteed to be
     *unique during one execution of the proram, but will not neccessarily have
     *the same value the next time the program runs.  Useful for indexing into
     *two-dimensional arrays of source-destination data such as the one
     *returned by {@link GUSSImpl#hostByHostDataset}.
     *@return the ID number of the host that was the source of this transfer.
     */
    public int getSrcID() {
	return srcID.intValue();
    }

    /**@return the name of the host that was the source of this transfer.*/
    public String getSrcHost() {
	return srcHost;
    }
    /**@return the name of the host that was the destination of this transfer.*/
    public String getDestHost() {
	return destHost;
    }
    /**@return the ID number of the host that was the destination of this transfer.
     *@see #getSrcID*/
    public int getDestID() {
	return destID.intValue();
    }
    /**@return the name of the host that logged this transfer, whether sender or receiver.*/
    public String getLoggingHost() {
	return this.loggerAddress;
    }
    //don't use this:
    public String getUser() {
	return this.userName;
    }
    /**@return the file size in bytes*/
    public long getBytes() {
	return fileSize;
    }
    /**@return the file size in kilobytes*/
    public double getKBytes() {
	return (double)fileSize/1024;
    }
    /**@return the time the transfer started, as a number of milliseconds*/
    public long getStartTime() {
	return startTime.getTime();
    }
    /**@return the time the transfer started, as a java.util.Date*/
    public Date getStartDate() {
	return startTime;
    }
    /**@return the time the transfer ended, as a number of milliseconds*/
    public long getEndTime() {
	return endTime.getTime();
    }
    /**@return the time the transfer ended, as a java.util.Date*/
    public Date getEndDate() {
	return endTime;
    }
    public long getNumStreams() {
	return numStreams;
    }
    public void setNumStreams(long numStreams) {
	this.numStreams = numStreams;
    }
    public long getNumStripes() {
	return numStripes;
    }
    public void setNumStripes(long numStripes) {
	this.numStripes = numStripes;
    }
    public long getBufferSize() {
	return bufferSize;
    }
    public void setBufferSize(long bufferSize) {
	this.bufferSize = bufferSize;
    }
    public long getBlockSize() {
	return blockSize;
    }
    public void setBlockSize(long blockSize) {
	this.blockSize = blockSize;
    }
    public long getFTPReturn() {
	return ftpReturn;
    }
    public void setFTPReturn(long ftpReturn) {
	this.ftpReturn = ftpReturn;
    }

    public String getLoggerAddress() {
	return this.loggerAddress;
    }
/*also:		this.otherSideAddress == other.otherSideAddress &&
		this.sequenceNum == other.sequenceNum &&
		this.gftpVersion == other.gftpVersion);*/


    private void outputField(String label, Object field) {
	if (field == null) {
	    System.out.println(label + "Unknown");
	}
	else {
	    System.out.println(label + field.toString());
	}
    }

    /**Prints all fields to System.out.  Used for debugging, not recommended for use otherwise.*/
    public void debug() {
	/*TODO: this should use log4j instead of system.out.*/
	System.out.println("Start time: " +startTime);
	System.out.println("End time: "+ endTime);
	System.out.println("File size: "+ fileSize);
	System.out.println("Logged by: " + loggerAddress);
	if (storOrRetr == STOR) {
	    System.out.println("Operation type: Store");
	} else {
	    System.out.println("Operation type: Retreive");
	}
	outputField("User name: ", userName);
	outputField("Source host: ", srcHost);
	outputField("Dest host: ", destHost);
	outputField("Source ID: ", srcID);
	outputField("Dest ID: ", destID);
	System.out.println("Num stripes: "+numStripes);
	System.out.println("Num streams: "+numStreams);
	System.out.println("Block size: "+blockSize);
	System.out.println("Buffer size: "+bufferSize);
	System.out.println("FTP return code: "+ftpReturn);
    }



    public static ArrayList getRecordsFromDatabase(Connection con,
						      String tablename,
						      InetAddress serverIP,
						      Date firstDate,
						      Date lastDate)
	throws SQLException {
	return getRecordsFromDatabase(con, tablename, serverIP.getHostAddress(), firstDate, lastDate);
    }
    /**From the specified database table, returns all records that
       were made on the given server between startDate and endDate;
       returns them as an arraylist which may contain 0, 1, or n records.
    */
    public static ArrayList getRecordsFromDatabase(Connection con,
						      String tablename,
						      String hostname,
						      Date firstDate,
						      Date lastDate) 
	throws SQLException {

	ArrayList recordList = new ArrayList(10);
	ResultSet rs;

	PreparedStatement pstmt = con.prepareStatement("SELECT ip_address, stor_or_retr, start_time, end_time, num_bytes, num_stripes, num_streams, buffer_size, block_size, ftp_return_code FROM "+tablename+" WHERE (start_time BETWEEN ? and ?) AND (ip_address LIKE ?);");

	//it doesn't look like I can substitute in the tablename the way I can do with
	//the others...
	pstmt.setLong(1, firstDate.getTime());
	pstmt.setLong(2, lastDate.getTime());
	pstmt.setString(3, hostname);
	rs = pstmt.executeQuery();

	while (rs.next()) {
	    GFTPRecord tempRecord = new GFTPRecord(rs);
	    recordList.add(tempRecord);
	}
	rs.close();
	pstmt.close();

	recordList.trimToSize();
	return recordList;
    }



    /**Writes the data from this record into the specified database connection,
       table.  The deployment should have a database table called
       gftpusage.transfers with the schema defined in create_tables.sql*/
    public boolean insertIntoDatabase(Connection con, String tablename) {
	PreparedStatement pstmt;

	try {
	    pstmt = con.prepareStatement("INSERT INTO "+tablename+" (component_code, version_code, ip_address, stor_or_retr, start_time, end_time, num_bytes, num_stripes, num_streams, buffer_size, block_size, ftp_return_code) VALUES ('0', '0', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
	    
	    pstmt.setString(1, loggerAddress);
	    pstmt.setByte(2, storOrRetr);
	    /*Converting to sql DATETIME causes loss of precision.  So instead,
	      store the milliseconds from the java.util.Date as a BIGINT.*/
	    pstmt.setLong(3, startTime.getTime());
	    pstmt.setLong(4, endTime.getTime());
	    pstmt.setLong(5, fileSize);
	    pstmt.setLong(6, numStripes);
	    pstmt.setLong(7, numStreams);
	    pstmt.setLong(8, bufferSize);
	    pstmt.setLong(9, blockSize);
	    pstmt.setLong(10, ftpReturn);

	    //TODO have this get the other stuff from database too if possible
	    pstmt.executeUpdate();
			   			   
	    pstmt.close();
	}
	catch (SQLException e) {
	    System.out.println("SQL error occured:");
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

}
