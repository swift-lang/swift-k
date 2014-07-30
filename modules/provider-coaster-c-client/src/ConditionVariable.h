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
 * ConditionVariable.h
 *
 *  Created on: Sep 7, 2012
 *      Author: mike
 */

#ifndef CONDITIONVARIABLE_H_
#define CONDITIONVARIABLE_H_

#include <pthread.h>
#include "Lock.h"

namespace Coaster {

class ConditionVariable {
	private:
		pthread_cond_t cv;
		
		/* Disable default copy constructor */
		ConditionVariable(const ConditionVariable&);
		/* Disable default assignment */
		ConditionVariable& operator=(const ConditionVariable&);
	public:
		ConditionVariable();
		virtual ~ConditionVariable();
		void wait(Lock& lock);
		void signal();
		void broadcast();
};

}

#endif /* CONDITIONVARIABLE_H_ */
