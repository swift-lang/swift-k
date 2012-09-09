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

class ConditionVariable {
	private:
		pthread_cond_t cv;
	public:
		ConditionVariable();
		virtual ~ConditionVariable();
		void wait(Lock& lock);
		void signal();
		void broadcast();
};

#endif /* CONDITIONVARIABLE_H_ */
