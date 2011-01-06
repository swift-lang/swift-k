function result = grid_change_pass_phrase(varargin)
%
% Matlab wrapper around the CoG kit 'grid-change-pass-phrase' script
%
 
%    Changes the passphrase that protects the private key. Note that
%    this command will work even if the original key is not password
%    protected. If the -file argument is not given, the default location
%    of the file containing the private key is assumed:
 
%      -- The location pointed to by X509_USER_KEY
%      -- If X509_USER_KEY not set, /home/nvijayak/.globus/userkey.pem
 
%    Options
%       -help, -usage    Displays usage
%       -version         Displays version
%       -file location   Change passphrase on key stored in the file at
%                        the non-standard location 'location'.

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

% Command to be executed is the grid-change-pass-phrase batch/script file.
strCommand = 'grid-change-pass-phrase';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
