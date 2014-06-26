/*
 * Lock.cpp
 *
 *  Created on: Aug 28, 2012
 *      Author: mike
 */

#include <stdio.h>

#include "Lock.h"

using namespace Coaster;

static int unique = 0;

#define DEBUG_LOCKS 0
#define DEBUG_LOCKS_STACKS 0

#if DEBUG_LOCKS_STACKS == 1
#include <execinfo.h>
#define __GNU_SOURCE
#include <unistd.h>
#include <sys/syscall.h> // Linux-specific
#define print_stack() \
  {							\
	printf("TID: %li\n", (long)syscall(SYS_gettid));\
        fflush(stdout);					\
	void *bt[32];					\
	int nbt = backtrace(bt, 32);			\
	backtrace_symbols_fd(bt, nbt, fileno(stdout));  \
        fflush(stdout);					\
  }
#else
#define print_stack()
#endif

#if DEBUG_LOCKS == 1
#define debug(format, args...)	   \
  {    printf("LOCK: %i " format "\n", id, ## args);	\
       fflush(stdout);				      \
       print_stack();				      \
  }
#else
#define debug(...)
#endif

Lock::Lock() {
	id= unique++;
	debug("NEW");
	pthread_mutex_init(&l, NULL);
}

Lock::~Lock() {
	debug("DELETED");
}

void Lock::lock() {
	debug("ACQUIRE");
	pthread_mutex_lock(&l);
	debug("ACQUIRED");
}

void Lock::unlock() {
	debug("RELEASE");
	pthread_mutex_unlock(&l);
}

pthread_mutex_t* Lock::getMutex() {
	return &l;
}

Lock::Scoped::Scoped(Lock& plock) : myLock(plock) {
	myLock.lock();
}

Lock::Scoped::~Scoped() {
	myLock.unlock();
}
