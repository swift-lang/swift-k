StressTester README

StressTester is a simple utility that can be used to automatically and
repetitively submit jobs to grid nodes for testing purposes.

The success or failure of the jobs is graphed dynamically in a Swing window.
There is a seperate graph for each host that is participating in the test.  A
0 on this graph represents a job that failed (or could not be submitted); a 1
represents successful completion.  By default, the job used for testing is
simply /bin/date.

Once the tester module of Cog is compiled, you can run StressTester from the
command line as:
java org.globus.cog.test.stresstest.StressTest
but an easier way is to run the script called stress-tester in
cog4/modules/testing/dist/testing-0.2/bin/examples/stress-tester.

Stress-tester can be run in three ways:
1. with command-line arguments that define a single host to
test; 
2. with a configuration file that defines an arbitrary number of
hosts.
3. with the name of an old logfile created by a previous run.  It will not do
any testing, but will display graphically the results of the old tests as
recorded in the logfile.


The command-line options are as follows:


--help		 Display list of options and exit.
--load <logfile>	Loads the old logfile and displays graph of the contents.

-c     Constant scale.  The default behavior is to determine the scale of the
 graph dynamically so as to show all the data points.  If you prefer the scale
 to remain constant, and the graph to scroll to accomodate new data points,
 give StressTester this flag.

-n     No submission.  Will go through the motions, but not actually submit
 any tests.  This option was created for the purpose of testing frequency and
 user functions without risking overloading any real grid installations.

-h <hostlistfile>	The hosts, ports, and providers will be read from the
 given file (see below).

-h <hostname>		If the -h argument does not correspond to a valid
 file, StressTester will attempt to interpret it as the name of a host and
 will try to run a single-host test.

 (If no -h flag is present, StressTester will look for a default hostlist file
 called hostlist.txt.)

-p <number>		The port to connect on.  Used only if -h <hostname>
 was given.

-P <provider>		The provider to use (for example "gt2" or "gt3.2.0").
 Used only if -h <hostname> was given.

-L <logfile> Gives the name of a file to which the results of the test should
 be logged. This file can be read back in later with the --load option.

-f <constant|exponential|random> A frequency function which is used to
   determine how often to sumbit jobs.  The default is constant, which submits
   a job every ten seconds Exponential will decrease the delay between jobs
   exponentially; this should be used with caution as it might potentially
   overload a host.

-u <constant|linear|exponential|random> A number of users function.  The
 default is constant, which has just a single user.  The other options create
 multiple "virtual users" who will all submit jobs in seperate threads.
 Linear adds one user every minute; exponential doubles the number of users
 every minute; random will randomly add and remove virtual users.


The format of a host-list file:
Each line defines a host.  First should come the host name, then the port,
then the provider (i.e. the globus toolkit version).  These fields must be
separated by tabs.  A sample host-list file, called hostlist.txt, in included
in modules/testing/resources.  It looks like this:

arbat.mcs.anl.gov	5243	gt2
wiggum.mcs.anl.gov	5243	gt2
plussed.mcs.anl.gov	2119	gt3.2.0

