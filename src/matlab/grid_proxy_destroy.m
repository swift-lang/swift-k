function result = grid_proxy_destroy(varargin)
%
% Matlab wrapper around the CoG 'grid-proxy-destroy' script
%
% Syntax: java ProxyDestroy [-dryrun] [file1...]
%         java ProxyDestroy -help
 
%         Options
%         -help | -usage
%                 Displays usage
%         -dryrun
%                 Prints what files would have been destroyed
%         file1 file2 ...
%                 Destroys files listed
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
% The globuswrapper software was developed by:
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

% Command to be executed is the grid-proxy-destroy batch/script file.
strCommand = 'grid-proxy-destroy';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
