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
