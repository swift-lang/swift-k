function result = globus_personal_gatekeeper(varargin)
%
% Matlab wrapper around the CoG kit 'globus-personal-gatekeeper' script
%
 
% Syntax: java Gatekeeper [options]
%         java Gatekeeper -version
%         java Gatekeeper -help
 
%         Options
%         -help | -usage
%                 Displays usage
%         -p | -port
%                 Port of the Gatekeeper
%         -d | -debug
%                 Enable debug mode
%         -s | -services
%                 Specifies services configuration file.
%         -l | -log
%                 Specifies log file.
%         -gridmap
%                 Specifies gridmap file.
%         -proxy
%                 Proxy credentials to use.
%         -serverKey
%                 Specifies private key (to be used with -serverCert.
%         -serverCert
%                 Specifies certificate (to be used with -serverKey.
%         -caCertDir
%                 Specifies locations (directory or files) of trusted
%                 CA certificates.

% Copyright 2004 ANL
% 
% This software has been developed by:
%
% Java CoG Team
% Argonne National Laboratory
% 9700 S Cass Ave, Argonne, IL - 60439
% 
% This software is released under the terms of the Globus Toolkit Public License.
%
% Command to be executed is the globus-personal-gatekeeper batch/script file.
strCommand = 'globus-personal-gatekeeper';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
