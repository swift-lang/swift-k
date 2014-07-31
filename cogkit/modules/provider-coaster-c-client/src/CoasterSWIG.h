/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 *
 * Copyright 2013-2014 University of Chicago
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



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
