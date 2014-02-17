/*
 * Lock.cpp
 *
 *  Created on: Aug 28, 2012
 *      Author: mike
 */

#include "Lock.h"

Lock::Lock() {
	pthread_mutex_init(&l, NULL);
}

Lock::~Lock() {
}

void Lock::lock() {
	pthread_mutex_lock(&l);
}

void Lock::unlock() {
	pthread_mutex_unlock(&l);
}

pthread_mutex_t* Lock::getMutex() {
	return &l;
}

Lock::Scoped::Scoped(Lock& plock) {
	myLock = &plock;
	myLock->lock();
}

Lock::Scoped::~Scoped() {
	myLock->unlock();
}
