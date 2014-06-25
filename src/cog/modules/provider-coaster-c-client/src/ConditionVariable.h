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
