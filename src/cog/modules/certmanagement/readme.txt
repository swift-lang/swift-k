			Certificate Management Module
			-----------------------------


The certificate management module has a main page called certmanagement.html. This
page contains links to the various html, jsp and jnlp launchers. It also contains
some information regarding the use of the various applets.

Remember to set the set the codebase property in the cog's main webstart.properties.
This should be set to the location where you will deploy your applets.

codebase=http://localhost:8080/certmanagement


How to sign using your own certificate:

The webstart.properties also contains all the fields required to change the signer
of the applets. Simply edit this file to your liking.

keystore=${user.home}/.globus/javacogkit.jks
storetype=JKS
storepass=password
signalias=javacogkit


How to use and install a different CA:

The applets will install the CA certificates files listed in the
conf/*.properties files. Currently they are configured to install the
Grid Canada (5f54f417.0) and Globus (42864e48.0) CA certificates, like so:

    CACertFiles=5f54f417.0,42864e48.0

The cacerts directory must contain the actual CA certificates you want the
certmanagement applets to install on the clients machine. These files get copied
into the cog-certmanagement.jar. The applets will extract the certificates and
install them in the clients ~/.globus/certificates directory.

So if you want to install your own CA certificate file simply add it to the cacerts
directory and add an entry for it in the conf/*.properties files.

You may also want to specify your own email address to send certificate request to.
The default is ca@globus.org. There is also a default myproxy server address that
should be changed. See the launchers.xml file:

    <!-- CA email address. -->
    <property name="CA_EMAIL_ADDRESS" value="ca@globus.org"/>
    <!-- default MyProxy server to use. -->
    <property name="MYPROXY_ADDRESS" value="myproxy@x.y.z"/>
        

    
How to build:

ant dist (copies jars required by certmanagement module into the dist directory)
ant compile
ant deploy.webstart (build jars and generates the jnlp files)


Things remaingin:

Still making use of my original GridCertRequest class. Make use of
GridCertRequest.java located in modules/certrequest.
Finish internalization, framework is in place but not all strings are
internazonalized.


Jean-Claude Cote
High Performance Computing / Calcul de haute performance
National Research Council Canada / Conseil national de recherches Canada
www.grid.nrc.ca



 

 


