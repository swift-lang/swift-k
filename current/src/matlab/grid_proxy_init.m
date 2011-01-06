function grid_proxy_init(varargin)
%   Command-line tool to create a GLOBUS grid proxy.
%   Invokes the CoG script to create a Globus proxy for the user.  
%   Note that this script
%   echoes the user's password back to the terminal without masking.  Use
%   visual_grid_proxy_init for a GUI version that masks the password.
%
 
% Syntax: java ProxyInit [options]
%         java ProxyInit -help
%       
% Options
%    -help | -usage              Displays usage.
%    -version                    Displays version.
%
%    -debug                      Enables extra debug output.
%    -verify                     Performs proxy verification tests (default).
%    -noverify                   Disables proxy verification tests.
%    -quiet | -q                 Quiet mode, minimal output
%    -limited                    Creates a limited proxy.
%    -independent                Creates a independent globus proxy.
%    -old                        Creates a legacy globus proxy.
%    -hours <hours>              Proxy is valid for H hours (default:12).
%    -bits <bits>                Number of bits in key {512|1024|2048|4096}.
%    -globus                     Prints user identity in globus format.
%    -policy <policyfile>        File containing policy to store in the
%                                ProxyCertInfo extension
%    -pl <oid>                   OID string for the policy language.
%    -policy-language <oid>      used in the policy file.
%    -path-length <l>            Allow a chain of at most l proxies to be
%                                generated from this one
%    -cert <certfile>            Non-standard location of user certificate
%    -key <keyfile>              Non-standard location of user key
%    -out <proxyfile>            Non-standard location of new proxy cert.
%    -pkcs11                     Enables the PKCS11 support module. The
%                                -cert and -key arguments are used as labels
%                                to find the credentials on the device.
%
%
% See also visual_grid_proxy_init
%
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
% The globus wrapper software was developed by:
%
% TIAX LLC
% Acorn Park
% Cambridge, MA 02140
% http://www.tiax.biz
% 617-498-5000
% grid.computing@tiax.biz
%
% This software is released under the terms of the Globus Toolkit Public 
% License.
%
% All restrictions, requirements, terms and conditions applicable to the 
% Globus Toolkit shall also be applicable to the this software, with TIAX 
% LLC as the beneficiary of such terms.
% IN NO EVENT WILL TIAX LLC BE LIABLE FOR ANY DAMAGES, INCLUDING DIRECT, 
% INCIDENTAL, SPECIAL OR 
% CONSEQUENTIAL DAMAGES RESULTING FROM THE EXERCISE OF THIS LICENSE AGREEMENT 
% OR THE USE OF THE SOFTWARE.

% Command to be executed is the grid-proxy-init batch/script file.
strCommand = 'grid-proxy-init';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand, varargin);
