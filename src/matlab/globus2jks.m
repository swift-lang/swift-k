% ----------------------------------------------------------------------
% This code is developed as part of the Java CoG Kit project
% The terms of the license can be found at http://www.cogkit.org/license
% This message may not be removed or altered.
% ----------------------------------------------------------------------

function result = globus2jks(varargin)
%
% Matlab wrapper around the CoG kit 'globus2jks' script
%

%  Syntax: java KeyStoreConvert [options]
%         java KeyStoreConvert -help
 
%         Converts Globus credentials (user key and certificate) into
%         Java keystore format (JKS format supported by Sun).
 
%         Options
%         -help | -usage
%                 Displays usage.
%         -version
%                 Displays version.
%         -debug
%                 Enables extra debug output.
%         -cert     <certfile>
%                 Non-standard location of user certificate.
%         -key      <keyfile>
%                 Non-standard location of user key.
%         -alias    <alias>
%                 Keystore alias entry. Defaults to 'globus'
%         -password <password>
%                 Keystore password. Defaults to 'globus'
%         -out      <keystorefile>
%                 Location of the Java keystore file. Defaults to
%                 'globus.jks'
 

% Command to be executed is the globus2jks batch/script file.
strCommand = 'globus2jks';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
