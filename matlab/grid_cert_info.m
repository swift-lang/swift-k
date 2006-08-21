function result = grid_cert_info(varargin)
%
% Matlab wrapper around the CoG kit 'grid-cert-info' script
%
% Syntax: java CertInfo [-help] [-file certfile] [-all] [-subject] [...]
 
%         Displays certificate information. Unless the optional
%         file argument is given, the default location of the file
%         containing the certficate is assumed:
 
%           -- /home/nvijayak/.globus/usercert.pem
 
%         Options
%         -help | -usage
%                 Display usage.
%         -version
%                 Display version.
%         -file certfile
%                 Use 'certfile' at non-default location.
%         -globus
%                 Prints information in globus format.
 
%         Options determining what to print from certificate
 
%         -all
%                 Whole certificate.
%         -subject
%                 Subject string of the cert.
%         -issuer
%                 Issuer.
%         -startdate
%                 Validity of cert: start date.
%         -enddate
%                 Validity of cert: end date.
 
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

% Command to be executed is the grid-cert-info batch/script file.
strCommand = 'grid-cert-info';

% Run the globuswrapper script using the given command and user-supplied arguments
globuswrapper(strCommand,varargin);
