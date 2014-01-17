/*
 * Lock.cpp
 *
 *  Created on: Aug 28, 2012
 *      Author: mike
 */

#include <stdio.h>

#include "Lock.h"

static int unique = 0;

#define DEBUG_LOCKS 1
#if DEBUG_LOCKS == 1
#define debug(format, args...)           \
  {    printf("LOCK: " format "\n", ## args); \
       fflush(stdout);                   \
  }
#else
#define debug(...) 0;
#endif

Lock::Lock() {
  id = unique++;
	debug("LOCK: %i NEW");
	pthread_mutex_init(&l, NULL);
}

Lock::~Lock() {
	debug("LOCK: %i DELETED");
}

void Lock::lock() {
	printf("LOCK: %i ACQUIRE", id);
	pthread_mutex_lock(&l);
}

void Lock::unlock() {
	debug("LOCK: %i RELEASE", id);
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
