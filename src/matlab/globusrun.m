function result = globusrun(varargin)
%
% Matlab wrapper around the CoG kit 'globusrun' script
%
% Syntax: java GlobusRun [options] [RSL String]
%         java GlobusRun -version
%         java GlobusRun -help
 
%         Options
%         -help | -usage
%                 Display help.
%         -v | -version
%                 Display version.
%         -f <rsl filename> | -file <rsl filename>
%                 Read RSL from the local file <rsl filename>. The RSL
%                 must be a single job request.
%         -q | -quiet
%                 Quiet mode (do not print diagnostic messages)
%         -o | -output-enable
%                 Use the GASS Server library to redirect standout output
%                 and standard error to globusrun. Implies -quiet.
%         -s | -server
%                 $(GLOBUSRUN_GASS_URL) can be used to access files local
%                 to the submission machine via GASS. Implies
%                 -output-enable and -quiet.
%         -w | -write-allow
%                 Enable the GASS Server library and allow writing to
%                 GASS URLs. Implies -server and -quiet.
%         -r <resource manager> | -resource-manager <resource manager>
%                 Submit the RSL job request to the specified resource
%                 manager. A resource manager can be specified in the
%                 following ways:
%                  - host
%                  - host:port
%                  - host:port/service
%                  - host/service
%                  - host:/service
%                  - host::subject
%                  - host:port:subject
%                  - host/service:subject
%                  - host:/service:subject
%                  - host:port/service:subject
%                 For those resource manager contacts which omit the port,
%                 service or subject field the following defaults are used:
%                 port = 2119
%                 service = jobmanager
%                 subject = subject based on hostname
%                 This is a required argument when submitting a single RSL
%                 request.
%         -k | -kill <job ID>
%                 Kill a disconnected globusrun job.
%         -status <job ID>
%                 Print the current status of the specified job.
%         -b | -batch
%                 Cause globusrun to terminate after the job is successfully
%                 submitted, without waiting for its completion. Useful
%                 for batch jobs. This option cannot be used together with
%                 either -server or -interactive, and is also incompatible
%                 with multi-request jobs. The "handle" or job ID of the
%                 submitted job will be written on stdout.
%         -stop-manager <job ID>
%                 Cause globusrun to stop the job manager, without killing
%                 the job. If the save_state RSL attribute is present, then a
%                 job manager can be restarted by using the restart RSL
%                 attribute.
%         -fulldelegation
%                 Perform full delegation when submitting jobs.
 
%         Diagnostic Options
%         -p | -parse
%                 Parse and validate the RSL only. Does not submit the job
%                 to a GRAM gatekeeper. Multi-requests are not supported.
%         -a | -authenticate-only
%                 Submit a gatekeeper "ping" request only. Do not parse the
%                 RSL or submit the job request. Requires the
%                 -resource-manger argument.
%         -d | -dryrun
%                 Submit the RSL to the job manager as a "dryrun" test
%                 The request will be parsed and authenticated. The job
%                 manager will execute all of the preliminary operations,
%                 and stop just before the job request would be executed.
 
%         Not Supported Options
%         -n | -no-interrupt

% Copyright 2004
% 
% This software has been modified by:
%
% Java CoG Team
% Argonne National Laboratory
% 9700 S Cass Ave, Argonne, IL - 60439
% 

% Copyright 2002  TIAX LLC.  All rights reserved.
%
% The globuswrapper software was developed by:
%
% TIAX LLC
% Acorn Park
% Cambridge, MA 02140
% http://www.tiax.biz
% 617-498-5000
% grid.computing@tiax.biz
%
% This software is released under the terms of the Globus Toolkit Public License.
%
% All restrictions, requirements, terms and conditions applicable to the Globus Toolkit
% shall also be applicable to the this software, with TIAX LLC as the beneficiary of such terms.
% IN NO EVENT WILL TIAX LLC BE LIABLE FOR ANY DAMAGES, INCLUDING DIRECT, INCIDENTAL, SPECIAL OR 
% CONSEQUENTIAL DAMAGES RESULTING FROM THE EXERCISE OF THIS LICENSE AGREEMENT OR THE USE OF THE 
% SOFTWARE.

% Command to be executed is the globusrun batch/script file.
strCommand = 'globusrun';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
