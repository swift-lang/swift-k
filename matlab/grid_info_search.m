function result = grid_info_search(varargin)
%
% Matlab wrapper around the CoG kit 'grid-info-search' script
%
% grid-info-search [ options ] <search filter> [attributes]
 
%     Searches the MDS server based on the search filter, where some
%     options are:
%        -help
%                Displays this message
 
%        -version
%                Displays the current version number
 
%        -mdshost host (-h)
%                The host name on which the MDS server is running
%                The default is vijayaku.mcs.anl.gov.
 
%        -mdsport port (-p)
%                The port number on which the MDS server is running
%                The default is 2135
 
%        -mdsbasedn branch-point (-b)
%                Location in DIT from which to start the search
%                The default is 'mds-vo-name=local, o=grid'
 
%        -mdstimeout seconds (-T)
%                The amount of time (in seconds) one should allow to
%                wait on an MDS request. The default is 30
 
%        -anonymous (-x)
%                Use anonymous binding instead of GSSAPI.
 
%      grid-info-search also supports some of the flags that are
%      defined in the LDAP v3 standard.
%      Supported flags:
 
%       -s scope   one of base, one, or sub (search scope)
%       -P version protocol version (default: 3)
%       -l limit   time limit (in seconds) for search
%       -z limit   size limit (in entries) for search
%       -Y mech    SASL mechanism
%       -D binddn  bind DN
%       -v         run in verbose mode (diagnostics to standard output)
%       -O props   SASL security properties (auth, auth-conf, auth-int)
%       -w passwd  bind password (for simple authentication)

%
% Copyright 2004 ANL
% 
% This software has been developed by:
%
% Java CoG Team
% Argonne National Laboratory
% 9700 S Cass Ave, Argonne, IL - 60439
% 
%
% This software is released under the terms of the Globus Toolkit Public License.
% Command to be executed is the grid-info-search batch/script file.
strCommand = 'grid-info-search';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
