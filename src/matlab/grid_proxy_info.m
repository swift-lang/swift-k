function grid_proxy_info(varargin)
%
%   Matlab wrapper around the CoG kit 'grid-proxy-info' script

% 
% Syntax: java ProxyInfo [options]
%         java ProxyInfo -help
 
%         Options:
%         -help | usage
%                 Displays usage.
%         -file <proxyfile>  (-f)
%                 Non-standard location of proxy.
%         [printoptions]
%                 Prints information about proxy.
%         -exists [options]  (-e)
%                 Returns 0 if valid proxy exists, 1 otherwise.
%         -globus
%                 Prints information in globus format
 
%         [printoptions]
%         -subject
%                 Distinguished name (DN) of subject.
%         -issuer
%                 DN of issuer (certificate signer).
%         -identity
%                 DN of the identity represented by the proxy.
%         -type
%                 Type of proxy.
%         -timeleft
%                 Time (in seconds) until proxy expires.
%         -strength
%                 Key size (in bits)
%         -all
%                 All above options in a human readable format.
%         -text
%                 All of the certificate.
%         -path
%                 Pathname of proxy file.
 
%         [options to -exists] (if none are given, H = B = 0 are assumed)
%         -hours H     (-h)
%                 time requirement for proxy to be valid.
%         -bits  B     (-b)
%                 strength requirement for proxy to be valid


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
% This software is released under the terms of the Globus Toolkit Public License.
%
% All restrictions, requirements, terms and conditions applicable to the Globus Toolkit
% shall also be applicable to the this software, with TIAX LLC as the beneficiary of such terms.
% IN NO EVENT WILL TIAX LLC BE LIABLE FOR ANY DAMAGES, INCLUDING DIRECT, INCIDENTAL, SPECIAL OR 
% CONSEQUENTIAL DAMAGES RESULTING FROM THE EXERCISE OF THIS LICENSE AGREEMENT OR THE USE OF THE 
% SOFTWARE.

% Command to be executed is the grid-proxy-info batch/script file.
strCommand = 'grid-proxy-info';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand, varargin);
