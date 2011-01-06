% ----------------------------------------------------------------------
% This code is developed as part of the Java CoG Kit project
% The terms of the license can be found at http://www.cogkit.org/license
% This message may not be removed or altered.
% ----------------------------------------------------------------------

function result = cog_workflow(varargin)
%
% Matlab wrapper around the CoG kit 'karajan' script
%
%
% Command to be executed is the karajan batch/script file.
strCommand = 'cog_workflow';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
