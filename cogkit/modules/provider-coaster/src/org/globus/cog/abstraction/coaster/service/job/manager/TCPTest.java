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
 * Created on Mar 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.*;
import java.net.*;

public class TCPTest {
    public static void main(String[] args) {
        try {
          ServerSocket ss = new ServerSocket(40000);
          System.out.println(ss);
          while (true)
          {
            Socket socket = ss.accept();
            process(socket);
            socket.close();
          }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void process(Socket socket)
        throws Exception {
        System.out.println("connected");
        while (true) {
            BufferedReader reader =
                new BufferedReader
                (new InputStreamReader(socket.getInputStream()));
            OutputStreamWriter writer =
                new OutputStreamWriter(socket.getOutputStream());

            String line = reader.readLine();
            System.out.println("\t" + line);
            if (line == null)
                break;
            if (line.equals("quit"))
                break;
        }
    }
}
