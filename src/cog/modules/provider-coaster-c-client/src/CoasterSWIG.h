
#include "CoasterClient.h"
//#include "CoasterLoop.h"

#ifdef SWIG
%module coaster
%{
  #include "CoasterClient.h"
  #include "CoasterSWIG.h"
  #include "CoasterLoop.h"
  #include "Settings.h"
%}
#endif

namespace Coaster {

CoasterLoop* CoasterSWIGLoopCreate(void);
int CoasterSWIGLoopDestroy(CoasterLoop* loop);

CoasterClient* CoasterSWIGClientCreate(CoasterLoop *loop, char* serviceURL);
int CoasterSWIGClientDestroy(CoasterClient *client);

int CoasterSWIGClientSettings(CoasterClient *client, char *settings);

Job* CoasterSWIGJobCreate(char *cmd_string, char *jobmanager);

int CoasterSWIGJobSettings(Job* j, char* dir, char* args, char* attr,
                           char* envs, char* stdoutLoc, char* stderrLoc);
int CoasterSWIGSubmitJob(CoasterClient *client, Job* job);

int CoasterSWIGWaitForJob(CoasterClient *client, Job* job);

int CoasterSWIGGetJobStatus(CoasterClient *client, Job* job);

int CoasterSWIGTest(CoasterLoop *loop, char *service_URL, CoasterClient *c);

//int CoasterSWIGTest(CoasterLoop *loop, char *service_URL);
//int CoasterSWIGTest(CoasterClient *client);

}
