//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 6, 2005
 */
package org.globus.cog.coaster.test;

import java.io.IOException;

import org.globus.cog.coaster.GSSService;
import org.globus.cog.coaster.Service;
import org.globus.gsi.gssapi.auth.SelfAuthorization;

public class Services {

	public static void main(String[] args) {
		Service s1, s2, s3;
		try {
			s1 = newService(50001);
			s2 = newService(50002);
			s3 = newService(50003);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Service newService(int port) throws IOException {
		GSSService service = new GSSService(port);
		service.setAuthorization(new SelfAuthorization());
		service.initialize();
		System.out.println("port: " + service.getPort());
		return service;
	}
}
