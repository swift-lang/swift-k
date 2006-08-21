% ----------------------------------------------------------------------
% This code is developed as part of the Java CoG Kit project
% The terms of the license can be found at http://www.cogkit.org/license
% This message may not be removed or altered.
% ----------------------------------------------------------------------

function result = cog_workflow_gui(varargin)
%
% Matlab wrapper around the CoG kit 'karajan-gui' script
%
% Usage:
%   karajan-gui [options]
%     -load <file>
%       Loads the specified file
%     -run
%       If a workflow specification was loaded, it starts executing it;
%       otherwise, it does nothing.
%     -help
%       Displays a usage summary

% Command to be executed is the karajan-gui batch/script file.
strCommand = 'cog_workflow_gui';

% Run the globuswrapper script using the given command and user-supplied 
% arguments

globuswrapper(strCommand,varargin);
