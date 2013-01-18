function result = globus_url_copy(varargin)
%
% Matlab wrapper around the globus-url-copy CoG script
%
% Syntax: java GlobusUrlCopy [options] fromURL toURL
%        java GlobusUrlCopy -help
% 
%        Options
%        -s  <subject> | -subject <subject>
%              Use this subject to match with both the source
%              and destination servers
%        -ss <subject> | -source-subject <subject>
%              Use this subject to match with the source server
%        -ds <subject> | -dest-subject <subject>
%              Use this subject to match with the destination server
%        -notpt | -no-third-party-transfers
%              Turn third-party transfers off (on by default)
%        -nodcau | -no-data-channel-authentication
%              Turn off data channel authentication for ftp transfers
%              Applies to FTP protocols only.
% 
%        Protocols supported:
%        - gass (http and https)
%        - ftp (ftp and gsiftp)
%        - file


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
% This software was developed by:
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

% Command to be executed is the globus-url-copy batch/script file.
strCommand = 'globus-url-copy';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
