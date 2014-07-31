/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
