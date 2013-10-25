
#include "CoasterClient.h"
//#include "CoasterLoop.h"

#ifdef SWIG
%module coaster
%{
  #include "CoasterClient.h"
  #include "CoasterSWIG.h"
  #include "CoasterLoop.h"
%}
#endif

CoasterLoop* CoasterSWIGLoopCreate(void);
int CoasterSWIGLoopDestroy(CoasterLoop* loop);

CoasterClient* CoasterSWIGClientCreate(CoasterLoop &loop, char* serviceURL);
int CoasterSWIGClientDestroy(CoasterClient *client);

int CoasterSWIGClientSettings(CoasterClient *client, char *settings);




