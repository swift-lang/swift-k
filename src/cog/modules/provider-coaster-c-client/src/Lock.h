/*
 * Lock.h
 *
 *  Created on: Aug 28, 2012
 *      Author: mike
 */

#ifndef LOCK_H_
#define LOCK_H_

#include <pthread.h>

class Lock {
	private:
		pthread_mutex_t l;
	public:
		Lock();
		virtual ~Lock();

		void lock();
		void unlock();
		pthread_mutex_t* getMutex();


		class Scoped {
			private:
				Lock* myLock;
			public:
				Scoped(Lock& plock);
				virtual ~Scoped();
		};
};

#endif /* LOCK_H_ */
