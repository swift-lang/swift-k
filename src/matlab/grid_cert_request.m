function result = grid_cert_request(varargin)
%
% Matlab wrapper around the CoG kit 'grid-cert-request' script
%
% grid-cert-request [-help] [ options ...]
 
%   Example Usage:
 
%     Creating a user certifcate:
%       grid-cert-request
 
%     Creating a host or gatekeeper certifcate:
%       grid-cert-request -host [my.host.fqdn]
 
%     Creating a LDAP server certificate:
%       grid-cert-request -service ldap -host [my.host.fqdn]
 
%   Options:
 
%     -version           : Display version
%     -?, -h, -help,     : Display usage
%     -usage
%     -cn <name>,        : Common name of the user
%     -commonname <name>
%     -service <service> : Create certificate for a service. Requires
%                          the -host option and implies that the generated
%                          key will not be password protected (ie implies -nopw).
%     -host <FQDN>       : Create certificate for a host named <FQDN>
%     -dir <dir_name>    : Changes the directory the private key and certificate
%                          request will be placed in. By default user
%                          certificates are placed in /home/user/.globus, host
%                          certificates are placed in /etc/grid-security and
%                          service certificates are place in
%                          /etc/grid-security/<service>.
%     -prefix <prefix>   : Causes the generated files to be named
%                          <prefix>cert.pem, <prefix>key.pem and
%                          <prefix>cert_request.pem
%     -nopw,             : Create certificate without a passwd
%     -nodes,
%     -nopassphrase,
%     -verbose           : Don't clear the screen <<Not used>>
%     -force             : Overwrites preexisting certifictes


% Copyright 2004 ANL
% 
% This software has been developed by:
%
% Java CoG Team
% Argonne National Laboratory
% 9700 S Cass Ave, Argonne, IL - 60439
% 
% Command to be executed is the grid-cert-request batch/script file.
strCommand = 'grid-cert-request';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
