
#include "CoasterClient.h"

#ifdef SWIG
%module coaster
%{
  #include "CoasterClient.h"
  #include "CoasterSWIG.h"
%}
#endif

CoasterClient* CoasterSWIGClientCreate(char* serviceURL);

int CoasterSWIGClientSettings(CoasterClient *client, char *settings);
