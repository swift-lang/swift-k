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
