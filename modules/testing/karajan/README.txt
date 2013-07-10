1. Installation

These tests require the Karajan version distributed with Java CoG Kit 4.1.4,
which can be downloaded from www.cogkit.org.

Additionally the graphs require the JFreeChart library. A copy is provided in
../lib, together with supporting libraries. Both jar files must be copied into
the lib directory of the binary CoG distribution (typically cog-4_1_4/lib).

2. Settings

The settings.k file contains a number of settings that can be edited in order
to customize certain behavioral and presentation aspects.

Constant           example value             description

COLOR:FAILED       "#ff4000"                 The background color of the table 
                                             cell of a failed test
											 
COLOR:PASSED       "#ffffff"                 The background color of the table
                                             cell of a passed test
											 
COLOR:TIMEOUT      "#ffba00"                 The background color of the table
                                             cell of a timed-out test

OUTPUT_DIR         "output"                  The local directory in which the 
                                             output files are created

PUBLISH            false                     Whether to publish the output files
                                             on a remote server after the tests
                                             are done. Copying happens using
                                             GridFTP.

PUBLISH_HOST       "wiggum.mcs.anl.gov"      If publishing is enabled, the host
                                             to which files are copied
											 
PUBLISH_DIR        "public_html/testing2"    If publishing is enabled, the remote
                                             directory in which files are copied

TEST_TIMEOUT       60*1000                   The number of miliseconds after which
                                             a test is considered to have timed-out
	
TEST_FILE          "testfile"                A file used in some of the file operation
                                             tests
											 
TEST_FILE_DIR      user.home                 The directory containing the test file
	
MAX_HISTORY_SIZE   365                       The maximum number of samples stored in
                                             the history. Older samples may be
                                             discarded in order to enforce this 
                                             setting.
	
COG_DIR            "{user.home}/cog-4_1_4"   The path to a CoG binary distribution.
                                             This is needed for indirect tests
                                             (which use command line tools rather
                                             than library calls).

3. The hosts file

The hosts.k file contains a list of machines and corresponding services on which the
tests are run. The format of the file is a flat sequence of task:host elements
(http://wiki.cogkit.org/index.php/V:4.1.4/Karajan:Task_Library#task:host). It is
possible to specify mulpile logical hosts for the same physical host for the purpose
of separating different versions of the same services (like for example transfering
files between the GT 4.0.1 and GT 4.0.2 GridFTP servers on the same machine).

4. Included tests and suites

  a) Execution (job submission)
  
    i) Direct - uses the task:execute call to submit jobs
	
	ii) Indirect - uses the cog/bin/cog-job-submit tool for the submission
	
  b) File operations
    i) Direct
	  A) Put - copies a file from the local host to the remote host
	  B) Get - copies the above file back from the remote host to the local host
	  C) List - lists files in a directory on the remote host
	  D) Rename - renames a file on a remote host
	  E) Remove - removes a file on a remote host
	  F) Exists - tests the existence of a file
	  G) Make Dir - creates a directory
	  H) Is Dir - tests the isDirectory() implementation
	  I) Remove Dir - removes a directory
	  J) Bug - Runs a sequence of operations that used to cause a problem with
         certain combinations of client/GridFTP server versions (a list on a
         nonexistant directory/file followed by a simple operation - exists())
		 
  c) Transfer
    Runs partial third-party transfers from /dev/urandom to /dev/null of 1MB of data
	between all pairs of hosts and displays the total time in a table. This test will
    only work properly with GridFTP servers.
    
4. Running the tests

The tests can be run from the current directory using the following command:
 
 cog-workflow runtests.k
 
It will loop executing all the test suites and pausing for a certain amount of
time (configurable in runtests.k line 125 - TODO: option to run only once and
some sane way of configuring the delay).

