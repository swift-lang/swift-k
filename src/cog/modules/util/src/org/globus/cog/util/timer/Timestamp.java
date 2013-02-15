
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.util.timer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Timestamp
{
	/**
	   Year Month Day hour minutes seconds
	 */
	static DateFormat YMDhms =
		new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	/**
	   Current time as: Year/Month/Day hour:minutes:seconds
	 */
	public static String YMDhms()
	{
		Date now = new Date();
		String result = YMDhms.format(now);
		return result;
	}

	/**
	   Year Month Day hour minutes seconds
	 */
	static DateFormat YMDhms_dash =
		new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	/**
	   Current time as: Year_Month_Day_hour_minutes_seconds
	   Good for file names
	 */
	public static String YMDhms_dash()
	{
		Date now = new Date();
		String result = YMDhms_dash.format(now);
		return result;
	}
}
