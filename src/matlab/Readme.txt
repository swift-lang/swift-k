Matlab Wrappers for the Java CoG

Copyright 2004
This software has been modified by:
Java CoG Team
Argonne National Laboratory
9700 S Cass Ave, Argonne, IL - 60439
 
TIAX LLC
Copyright 2002

1. Introduction

To facilitate interaction between the Matlab family of products and
the Globus Toolkit, we have developed a set of scripts that allow
one to manage grid resources from the Matlab command line.  These
scripts are simple wrappers around the corresponding batch files provided
with the Java Commodity Grid Kit (CoG).

2. Installation

To use these scripts, place the containing folder at any convenient
location on your hard drive.  Within Matlab, add that folder to the
Matlab search path.

The matlab code can be found in the CoG Kit CVS directory at

cvs co :pserver:anonymous@cvs.cogkit.org/cvs/cogkit/src/matlab

You natutrally must also have the CoG Kit

cvs co :pserver:anonymous@cvs.cogkit.org/cvs/cogkit/src/cog


3. Requirements

The Java CoG kit must be installed on your machine, and the environment
variable COG_INSTALL_PATH must be defined to point to the root directory
of the CoG installation.  In addition, the java runtime executable must
be accessible through the path environment variable.

These wrappers have been tested with Matlab 6.1 (or R12.1) under Windows 2000.
Under Windows 98 it may be necessary to deactivate Virus protection software
to get output from globus commands to appear in the Matlab console.  It may
also be necessary to modify globuswrapper.m to use the directory delimiter that
is appropriate to your operating system.

4. Usage

Each of the provided Matlab scripts is invoked using the exact same
syntax as you would the corresponding command-line batch file.  Use
them exactly as you would those commands.  Wrappers are provided for the
following CoG batch files:

cog_setup
globus_url_copy
globusrun
globuswrapper
globus2jks
globus_gass_server
globus_gass_server_shutdown
globus_personal_gatekeeper
graph_editor
grid_cert_info
grid_change_pass_phrase
grid_proxy_destroy
grid_proxy_info
grid_proxy_init
grid_cert_request
grid_info_search
cog_workflow
cog_workflow_gui
myproxy
visual_grid_proxy_init


Note the use of the underscore rather than the dash.  This was done because
dashes are not allowed in Matlab function names.

5. Contact Information and Licensing Information

The license is contained in the document License.txt
