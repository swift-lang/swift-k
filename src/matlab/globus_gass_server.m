function result = globus_gass_server(varargin)
%
% Matlab wrapper around the cog kit 'globus-gass-server' script

% Syntax: java GassServer [options]
%        java GassServer -version
%        java GassServer -help
% 
%       Options
%        -help | -usage
%                Displays usage
%        -s | -silent
%                Enable silent mode (Don't output server URL)
%        -r | -read
%                Enable read access to the local file system
%        -w | -write
%                Enable write access to the local file system
%        -o
%                Enable stdout redirection
%        -e
%                Enable stderr redirection
%        -c | -client-shutdown
%                Allow client to trigger shutdown the GASS server
%                See globus-gass-server-shutdown
%        -p <port> | -port <port>
%                Start the GASS server using the specified port
%        -i | -insecure
%                Start the GASS server without security
%        -n <options>
%                Disable <options>, which is a string consisting
%                of one or many of the letters "crwoe"
% Copyright 2004 ANL
% 
%
% Java CoG Team
% Argonne National Laboratory
% 9700 S Cass Ave, Argonne, IL - 60439
% 
% This software is released under the terms of the Globus Toolkit Public License.
% Command to be executed is the globus-gass-server batch/script file.
strCommand = 'globus-gass-server';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
