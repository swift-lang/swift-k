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
