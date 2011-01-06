Installing GridUsageSensorServices:

GUSS has two components:  The service and the client.  Each can be
deployed independently, on seperate computers in fact.  Therefore, I will give
seperate installation instructions for each component.

You will need the following software on your machine for either installation.
The version numbers listed here are the versions used for development and are
readily available from the given websites.  GUSS will most likely work with
other versions of the same software, but this has not been tested.

Required software:
Java, version 1.4.2
Apache Ant, version 1.6.2
Apache Tomcat, version 5.0.28.  http://jakarta.apache.org/tomcat/index.html
Apache Axis, version 1.2 (release cantidate).  htp://ws.apache.org/axis
Javabean Activation Framework, from http://java.sun.com/products/javabeans/glasgow/jaf.html.


Installing Tomcat and Axis:

If you already have Tomcat and Axis running on your machine, skip down to step 3.  If not, here is how to install and configure them:

1.  Download Jakarta Tomcat; unpack the tarball and put the jakarta-tomcat directory in a convenient place in your file system.  The path to this directory is henceforth known as CATALINA_HOME.

2.  Download Apache Axis; after unpacking the tar file, simply copy the directory axis-1_2RC1/webapps/axis into CATALINA_HOME/webapps.

3.  Startup the Tomcat server.  You do this by going into CATALINA_HOME/bin
  and running the script startup.sh.  
  Verify that Tomcat is running by looking at http://localhost:8080.  (If you
  have configured it to run on a port other than the default, this number will
  be different.)

4.  Verify the axis installation by going to
http://localhost:8080/axis/happyaxis.jsp.  
If the happyAxis page indicates that you need any extra .jar files,
install them now.  You will probably need to download the Javabean Activation
Framework and copy the file activation.jar into CATALINA_HOME/common/lib.



Installing the GUSS Service:

1.  Edit guss.properties, found in the same directory as this file.  You will
    need to change the following lines.  If you have not changed the defaults
    of your Tomcat installation, you will be able to leave most of the
    properties alone.

Should be the directory where Tomcat has been installed:
catalina-home	=/opt/jakarta-tomcat-5.0.28

Should be the port where Tomcat has been configured to run:
axis-port	=8080

Should be the directory where the axis jars are located:
axis-library	=/opt/jakarta-tomcat-5.0.28/webapps/axis/WEB-INF/lib

#Where to deploy GUSS Service:
webappDir	=${catalina-home}/webapps/axis/WEB-INF

The URL at which to install the service.  If you are running the script from
the machine where it is to be installed, this line can be left alone.
axis-server	=http://localhost:${axis-port}/

The directory where the GUSS service will store graph images it generates.
(Directory will be automaticaly created, but should be under webapps/ROOT).
temp-file-dir	=/opt/jakarta-tomcat-5.0.28/webapps/ROOT/tempimages

The URL of the above directory, where it can be accessed publicly by HTTP.
service-url	=http://myhostname:8080/tempimages/

The URL where the gridftp server's log file can be found.  When reading log
files from multiple servers, either keep all the URLs on one line, or else escape the line breaks.  For example:
logfile-url	=file:///home/username/local_gridftp.log \
		http://hostname.school.edu/gftpserver/logfile1.log \
		http://hostname.lab.gov/gftpserver/logfile2.log 



2.  Make sure Tomcat is running, then in the cog/modules/monitor directory, run:
  ant deploy-service

  Once this is done, restart Tomcat.  Whenever changes are made to GUSS, and
  the new classes redeployed to the server, you will probably need to restart
  Tomcat before you see the changes take effect.  You can do this by running
  shutdown.sh followed by startup.sh.
  After restarting, you can verify that the service has been deployed by looking at http://localhost:8080/axis/servlet/AxisServlet.

If all goes well, you can move on to step six, but...

2.1  If you get an error message here, first make sure that Tomcat and Axis
  are running by visiting http://localhost:8080/axis
  You can check the "Axis Happiness Page" to make sure that all jars needed
  for Axis to run properly have been installed.  

2.2  If you get an http 401 error (access denied) error from Axis with the
  message "remote administration not permitted":  For some reason, Axis does
  not recognize that the administration attempt is coming from the local
  machine.  I am researching the causes and solution of this problem, but in
  the meantime, here is a workaround:  

2.3  Go to CATALINA_HOME/webapps/axis/WEB-INF and look for the file called
  server-config.wsdd.  This file does not get created until Axis has a reason
  to modify it, so you may not have a server-config.wsdd yet.

2.4  If you do not have a server-config.wsdd, you can get the default one out
  of axis.jar.  Go into CATALINA_HOME/webapps/axis/WEB-INF/lib and type
  jar xf axis.jar org/apache/axis/server/server-config.wsdd
  This will extract the default server-config.wsdd file.  Move this file to
  CATALINA_HOME/webapps/axis/WEB-INF.
 
2.5  Edit server-config.wsdd and look for a line that says:
  <parameter name="enableRemoteAdmin" value="true"/>
     Change the "false" to "true".  Save the file.

2.6  Restart Tomcat, then try ant deploy-service again.

2.7  Leaving this value set to true while Tomcat is running on a publicly
  accessible machine is a security risk.  Therefore, once the deployment is
  done, it is best to edit server-config.wsdd again and set enableRemoteAdmin
  back to false.

3.  Verify that the GUSS Service has been installed.  Point a web browser at:
  http://localhost:8080/axis/servlet/AxisServlet (Replacing localhost:8080 with
  the address and port where you have Tomcat running).  You should see GUSS
  listed here.

4.  You can test that the GUSS service works locally by running
    ant test-service
    It will create several .png files and print the filenames to standard
    output.  You can open these files manually to verify that they contain
    graphs of correct data.  Since this test invokes GUSS locally, it does not
    test the webservice functionality.  You will need to install a client in
    order to do that.


Installing the GUSS Client:

Several clients are planned which can all connect to the same GUSS service,
but at present the only one which works is a JSP page.

The same software is required as for the GUSS service; follow steps 1 and 2
above to install Tomcat and Axis.


1.  In guss.properties, edit the following lines:

    Should be set to the hostname (not full URL) of the machine where the GUSS
    service has been installed.  If the service and client are on the same
    machine, this can be "localhost".
service-hostname =localhost
    
    Should be the number of the port to connect to on the machine
    running the GUSS service.  This is whichever port Tomcat has been
    configured to use on that machine.
service-port    =8080


jsp-server      =${catalina-home}
    Should be left alone, assuming you are using Tomcat to host the jsp.

jsp-dir         =${jsp-server}/webapps/jsp-examples/
    The 
jsp-lib-dir	=${jsp-server}/common/lib/
		

2.  In this directory, run
    ant compile-client

3.  Then run
    ant deploy-jsp

4.  Test the installed page by opening GUSSPage.jsp in a web browser.  The
  location depends on the values you entered in guss.properties; the default
  location would be:
	 http://localhost:8080/jsp-examples/GUSSPage.jsp

