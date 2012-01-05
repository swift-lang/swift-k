% ----------------------------------------------------------------------
% This code is developed as part of the Java CoG Kit project
% The terms of the license can be found at http://www.cogkit.org/license
% This message may not be removed or altered.
% ----------------------------------------------------------------------

function result = cog_setup(varargin)
%
% Matlab wrapper for the CoG kit 'cog-setup' script
%
% Syntax: cog-setup

% Command to be executed is the cog-setup batch/script file.
strCommand = 'cog-setup';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
