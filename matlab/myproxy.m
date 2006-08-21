function result = myproxy(varargin)
%
% Matlab wrapper around the CoG kit 'myproxy' script
% Start a myproxy client to upload the proxy
 
% Syntax: java MyProxy [common options] command [command options]
%         java MyProxy -version
%         java MyProxy -help
 
%         Common Options:
%         -help
%                 Displays usage
%         -v | -version
%                 Displays version
 
%         -h <host> | -host <host>
%                 Hostname of the myproxy-server
%         -p <port> | -port <port>
%                 Port of the myproxy-server
%                 (default 7512)
%         -s <subject> | -subject <subject>
%                 Performs subject authorization
%         -l <username> | -username <username>
%                 Username for the delegated proxy
%         -d | -dn_as_username
%                 Use the proxy certificate subject (DN) as the default
%                 username instead of the "user.name" system property.
 
%         Commands:
%          put     - put proxy
%          get     - get proxy
%          anonget - get proxy without local credentials
%          destroy - remove proxy
%          info    - credential information
%          pwd     - change credential password
 
%         Specify -help after a command name for command-specific help.

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
% Command to be executed is the myproxy batch/script file.
strCommand = 'myproxy';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
