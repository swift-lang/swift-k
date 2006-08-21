function result = globus_gass_server_shutdown(varargin)
%
% Matlab wrapper around the cog kit 'globus-gass-server-shutdown' script
%
% Syntax: globus_url_copy [options] fromURL toURL
%         globus_url_copy -help
% 
%         Options
%         -a | -append
%                 Opens output stream in append mode
%         -notpt | -no-third-party-transfers
%                 Turn third-party transfers off (on by default)
%                 Applies to FTP protocols only.
% 
%         Protocols supported:
%         - gass (http and https)
%         - ftp (ftp and gsiftp)
%         - file
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
% Command to be executed is the globus-gass-server-shutdown batch/script file.
strCommand = 'globus-gass-server-shutdown';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
