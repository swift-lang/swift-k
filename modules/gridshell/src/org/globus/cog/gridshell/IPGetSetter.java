package org.globus.cog.gridshell;

import java.net.NetworkInterface;
import java.util.Enumeration;



import java.net.InetAddress;

import org.globus.common.CoGProperties;

public class IPGetSetter {
  private static final String EOL = System.getProperty("line.separator");

  public static String getAllIPAddresses() throws Exception {
  	StringBuffer allIPAddresses = new StringBuffer();  	
    Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

    
    while(interfaces.hasMoreElements()) {
      NetworkInterface thisInterface = (NetworkInterface)interfaces.nextElement();
      
      allIPAddresses.append("Device Name: '");
      allIPAddresses.append(thisInterface.getName());
      allIPAddresses.append(" | ");
      allIPAddresses.append(thisInterface.getDisplayName());
      allIPAddresses.append(EOL);
      
      Enumeration thisAddresses = thisInterface.getInetAddresses();
      while(thisAddresses.hasMoreElements()) {
        InetAddress address = (InetAddress)thisAddresses.nextElement();
        allIPAddresses.append(" address: '");
        allIPAddresses.append(address.toString());
        allIPAddresses.append(EOL);
      }
    }
    return ("".equals(allIPAddresses.toString())) ? "No found devices\n" : allIPAddresses.toString();
  }
  
  public static void setRedirectIPAddressDev(String devName) throws Exception {
  	String address = getRedirectIPAddress(devName);
  	setRedirectIPAddress(address);
  }
  
  public static void setRedirectIPAddress(String value) throws Exception {
  	CoGProperties.getDefault().setIPAddress(value);
  	CoGProperties.getDefault().save();
  }  
  public static String getRedirectIPAddress(String devName) throws Exception {
    NetworkInterface redirectNetworkInterface = NetworkInterface.getByName(devName);
    if(redirectNetworkInterface == null) {
    	throw new RuntimeException("Error: Device name '"+devName+"' is not found.");
    }
         
    return ((InetAddress)redirectNetworkInterface.getInetAddresses().nextElement()).getHostAddress();
  }

  public static void main(String[] args) throws Exception {
    IPGetSetter.getAllIPAddresses();
    //System.out.println(IPGetSetter.getRedirectIPAddress("eth2"));
    //IPGetSetter.setRedirectIPAddress(getRedirectIPAddress("eth2"));
  }
}
