/*
 * Lock.h
 *
 *  Created on: Aug 28, 2012
 *      Author: mike
 */

#ifndef LOCK_H_
#define LOCK_H_

#include <pthread.h>

namespace Coaster {

class Lock {
	int id;
	private:
		pthread_mutex_t l;

		/* Disable default copy constructor */
		Lock(const Lock&);
		/* Disable default assignment */
		Lock& operator=(const Lock&);
	public:
		Lock();
		virtual ~Lock();

		void lock();
		void unlock();
		pthread_mutex_t* getMutex();


		class Scoped {
			private:
				Lock& myLock;

                                /* Disable default copy constructor */
                                Scoped(const Scoped&);
                                /* Disable default assignment */
                                Scoped& operator=(const Scoped&);
			public:
				Scoped(Lock& plock);
				virtual ~Scoped();
		};
};

}

#endif /* LOCK_H_ */
