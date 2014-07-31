/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 *
 * Copyright 2012-2014 University of Chicago
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


/*
 * ConditionVariable.cpp
 *
 *  Created on: Sep 7, 2012
 *      Author: mike
 */

#include "ConditionVariable.h"

using namespace Coaster;

ConditionVariable::ConditionVariable() {
	pthread_cond_init(&cv, NULL);
}

ConditionVariable::~ConditionVariable() {
	pthread_cond_destroy(&cv);
}

void ConditionVariable::wait(Lock& lock) {
	pthread_cond_wait(&cv, lock.getMutex());
}

void ConditionVariable::signal() {
	pthread_cond_signal(&cv);
}

void ConditionVariable::broadcast() {
	pthread_cond_broadcast(&cv);
}
