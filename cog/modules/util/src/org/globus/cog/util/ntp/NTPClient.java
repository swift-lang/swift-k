
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
/* 
 * A NTP client based on RFC2030.  The RFC describing the protocoll
 * can be found at http://www.faqs.org/rfcs/rfc2030.html
*/

package org.globus.cog.util.ntp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * A simple <code>NTPClient</code> based on the RFC2030.
 */
public class NTPClient {

	private byte[] NTPData;

	private String server;
	private int NTPPort = 123;

	private final byte referenceOffset = 16;
	private final byte originateOffset = 24;
	private final byte receiveOffset = 32;
	private final byte transmitOffset = 40;
	private final byte refIDOffset = 12;

	private byte NTPleap;
	private byte NTPversion;
	private byte NTPmode;
	private byte NTPstratum;
	private byte NTPpoll;
	private byte NTPprecision;

	private long transmitTime;
	private long destinationTimestamp;
	private long referenceTimestamp;
	private long originateTimestamp;
	private long receiveTimestamp;
	private long transmitTimestamp;

	private long localOffset;
	private long roundTripDelay;

	private String refID;

	private long dT1900to1970; //offset (in ms) between 1900 and 1970

	/**
	 * Creates a new <code>NTPClient</code> instance.
	 *
	 * @param NTPserver is the name of an ntpserver such as
	 * <code>time.nist.gov</code>
	 */
	public NTPClient(String NTPserver) {
		server = NTPserver;
		NTPData = new byte[48];
		dT1900to1970 = 70 * 365; //days in 70 years
		dT1900to1970 += 17; //add days for leap years between 1900 and 1970
		dT1900to1970 *= 24; //hours in a day
		dT1900to1970 *= 60; //minutes in an hour
		dT1900to1970 *= 60; //seconds in a minute
		dT1900to1970 *= 1000; //milliseconds in a second
	}

	private void toBytes(long n, int offset) {
		long intPart = 0;
		long fracPart = 0;
		intPart = n / 1000;
		fracPart = ((n % 1000) / 1000) * 0X100000000L;

		NTPData[offset + 0] = (byte) ((intPart >>> 24) & 0xff);
		NTPData[offset + 1] = (byte) ((intPart >>> 16) & 0xff);
		NTPData[offset + 2] = (byte) ((intPart >>> 8) & 0xff);
		NTPData[offset + 3] = (byte) ((intPart) & 0xff);

		NTPData[offset + 4] = (byte) ((fracPart >>> 24) & 0xff);
		NTPData[offset + 5] = (byte) ((fracPart >>> 16) & 0xff);
		NTPData[offset + 6] = (byte) ((fracPart >>> 8) & 0xff);
		NTPData[offset + 7] = (byte) ((fracPart) & 0xff);

	}

	private long toLong(int offset) {

		long intPart =
			((((long) NTPData[offset + 3]) & 0xFF))
				+ ((((long) NTPData[offset + 2]) & 0xFF) << 8)
				+ ((((long) NTPData[offset + 1]) & 0xFF) << 16)
				+ ((((long) NTPData[offset + 0]) & 0xFF) << 24);

		long fracPart =
			((((long) NTPData[offset + 7]) & 0xFF))
				+ ((((long) NTPData[offset + 6]) & 0xFF) << 8)
				+ ((((long) NTPData[offset + 5]) & 0xFF) << 16)
				+ ((((long) NTPData[offset + 4]) & 0xFF) << 24);
		long millisLong = (intPart * 1000) + (fracPart * 1000) / 0X100000000L;
		
		return millisLong;
	}

	/**
	 * The mode as described in RFC2030.
	 *
	 * @return a <code>String</code> returning a readble form of the mode.
	 *
	 *    Mode     Meaning
	 *       ------------------------------------
	 *       0        reserved
	 *       1        symmetric active
	 *       2        symmetric passive
	 *       3        client
	 *       4        server
	 *       5        broadcast
	 *       6        reserved for NTP control message
	 *       7        reserved for private use
	 */
	public String getMode() {
		return convertMode(NTPmode);
	}

	/**
	 * Converts the mode as described in RFC2030.
	 *
	 * @return a <code>String</code> returning a readble form of the mode.
	 *
	 *    Mode     Meaning
	 *       ------------------------------------
	 *       0        reserved
	 *       1        symmetric active
	 *       2        symmetric passive
	 *       3        client
	 *       4        server
	 *       5        broadcast
	 *       6        reserved for NTP control message
	 *       7        reserved for private use
	 */
	public String convertMode(int mode) {

		if (mode == 0) {
			return "reserved";
		}
		if (mode == 1) {
			return "symmetric active";
		}
		if (mode == 2) {
			return "symmetric passive";
		}
		if (mode == 3) {
			return "client";
		}
		if (mode == 4) {
			return "server";
		}
		if (mode == 5) {
			return "broadcast";
		}
		if (mode == 6) {
			return "reserved for NTP control message";
		}
		if (mode == 7) {
			return "reserved for private use";
		}
		return "ERROR: mode does not exist";
	}

	private void convert() {
		boolean error = false;
		NTPleap = (byte) (NTPData[0] >> 6);
		if (NTPleap == 3) {
			// this shoulld be an exception or log4j
			System.out.println("ERROR: NTP server is unsynchronized");
			error = true;
		}

		NTPversion = (byte) ((NTPData[0] & 0X38) >> 3);
		NTPmode = (byte) (NTPData[0] & 0X7);
		if (NTPmode != 4) {
			// this shoulld be an exception
			System.out.println("ERROR: NTP server not in server mode");
			error = true;
		}

		NTPstratum = (byte) (NTPData[1]);
		if (NTPstratum != 1) {
			// this shoulld be an exception
			System.out.println("ERROR: NTP is not a primary reference");
			error = true;
		}

		if (!error) {
			NTPpoll = (byte) (NTPData[2]);
			NTPprecision = (byte) (NTPData[3]);

			refID = "";
			for (int i = 0; i <= 3; i++) {
				refID = refID.concat(String.valueOf((char) NTPData[refIDOffset + i]));
			}

			referenceTimestamp = toLong(referenceOffset);
			originateTimestamp = toLong(originateOffset);
			receiveTimestamp = toLong(receiveOffset);
			transmitTimestamp = toLong(transmitOffset);

			long T1 = originateTimestamp;
			long T2 = receiveTimestamp;
			long T3 = transmitTimestamp;
			long T4 = destinationTimestamp;
			if (T1 != transmitTime) {
				T1 = transmitTime;
			}
			roundTripDelay = ((T4 - T1) - (T2 - T3));
			localOffset = ((((T2 - T1) + (T3 - T4))) / 2);
		}
	}

	/**
	 * Returns the NTP server name.
	 *
	 * @return a <code>String</code> with the server name
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Returns the type of the serevr.
	 *
	 * @return a <code>String</code> with the type
	 */
	public String getType() {
		return refID;
	}

	/**
	 * returns the delay between the remote server and the local time.
	 *
	 * @return a <code>long</code> value representing the difference
	 * in milliseconds.
	 */
	public long getDelay() {
		return roundTripDelay;
	}

	/**
	 * Describe <code>getVariation</code> method here.
	 *
	 * @return a <code>long</code> value
	 */
	public long getVariation() {
		return localOffset;
	}

	/**
	 * returns true if the timedelay is smaller than a specified value.
	 *
	 * @param delta a <code>long</code> value representing the interval
	 * @return a <code>boolean</code> value returns true if the time
	 * difference is smaller than delta
	 */
	public boolean withinDelta(long delta) {
		return (Math.abs(localOffset - delta) <= delta);
	}

	/**
	 * queries the NTP serevr and calculates the time difference.
	 *
	 */
	public void update() throws IOException {

		// reset the data to erase previous result
		NTPData[0] = 0x1B;
		for (int i = 1; i < 48; i++) {
			NTPData[i] = 0;
		}

		InetAddress IPAddress = InetAddress.getByName(server);
		DatagramSocket NTPSocket = new DatagramSocket();
		DatagramPacket NTPPacket = new DatagramPacket(NTPData, NTPData.length, IPAddress, NTPPort);

		long startTime = System.currentTimeMillis();
		transmitTime = startTime + dT1900to1970;
		toBytes(transmitTime, transmitOffset);

		NTPSocket.send(NTPPacket);
		NTPSocket.receive(NTPPacket);

		destinationTimestamp = System.currentTimeMillis();
		destinationTimestamp += dT1900to1970;

		NTPData = NTPPacket.getData(); //get NTP data from the buffer
		convert();
		NTPSocket.close(); //close connection with NTP server 
	}
}
