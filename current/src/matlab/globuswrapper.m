function globuswrapper(strCommand, varargin)
%
% function globuswrapper(strCommand, varargin)
%
% Internal wrapper function used around individual batch files that
% are part of the Java CoG for Globus.  This function is not meant
% to be used by users; it is implemented as a programming convenience for
% implementation of all Matlab versions of CoG batch files.
%
% Fix: globuswrapper.m was using a 'break' statement outside of loop
% which caused many warning statements to be printed. This warning has
% been suppressed by using a 'return' statement instead of 'break'.
%
% Copyright 2004 ANL
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

% Retrieve the environment variable that defines the CoG installation path
cogPathVar = 'COG_INSTALL_PATH';
persistent cogPath;

if (isempty(cogPath))
    cogPath = getenv(cogPathVar);
    % If it is still empty the environment variable doesn't exist
    if (isempty(cogPath))
        fprintf('\nUnable to invoke %s.', strCommand);
        fprintf('Environment variable %s is undefined\n\n', cogPathVar);
        % Halt Matlab execution
        return;
    end
    
    % Use the same directory delimiter as whatever is used in the path string
    % Assume that it's '/' or '\'.  Append it just once.
    
    if (isempty(findstr(cogPath, '/')))
        cogPath = [cogPath '\bin\'];
    else
        cogPath = [cogPath '/bin/'];
    end
    
    % Ensure that cogPath is a directory (should use more generic "dir" command in the future)
    if (isdir(cogPath))
        cogPath = ['!' cogPath];
    else
        fprintf('\nUnable to find directory %s.  Halting execution.\n', cogPath);
        cogPath = '';
        return;
    end
end

% Extract the argument list that was passed to whatever function invoked globuswrapper
arglist = varargin{1};
argListSize = size(arglist);
nArgs = argListSize(2);

% Construct the command that will be passed onto the system
strCommand = [cogPath strCommand ' '];
for i=1:nArgs,
    strCommand = [strCommand arglist{i} '  '];
end
% Execute it
eval(strCommand);
