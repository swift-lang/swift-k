package org.globus.cog.gridface.impl.util;

import java.net.NetworkInterface;
import java.util.Enumeration;



import java.net.InetAddress;

import org.globus.common.CoGProperties;

public class IPGetSetter {

  public IPGetSetter() {

  }
  public static String getAllIPAddresses() throws Exception {
  	String allIPAddresses = "";
    Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

    
    while(interfaces.hasMoreElements()) {
      NetworkInterface thisInterface = (NetworkInterface)interfaces.nextElement();
      
      allIPAddresses += "Device Name: '"+ thisInterface.getName() + " | "+thisInterface.getDisplayName()+ "'\n";
      
      Enumeration thisAddresses = thisInterface.getInetAddresses();
      while(thisAddresses.hasMoreElements()) {
        InetAddress address = (InetAddress)thisAddresses.nextElement();
        allIPAddresses += " address: '"+address.toString()+"'\n";
      }
    }
    return (allIPAddresses == "") ? "No found devices\n" : allIPAddresses;
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
