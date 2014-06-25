/*
 * JobStatusHandler.h
 *
 *  Created on: Aug 31, 2012
 *      Author: mike
 */

#ifndef JOBSTATUSHANDLER_H_
#define JOBSTATUSHANDLER_H_

#include "Handler.h"
#include "JobStatus.h"

namespace Coaster {

class JobStatusHandler: public Handler {
	public:
		JobStatusHandler();
		virtual ~JobStatusHandler();
		virtual void requestReceived();
};

}

#endif /* JOBSTATUSHANDLER_H_ */
