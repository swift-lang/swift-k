/*
 * ClientHandlerFactory.cpp
 *
 *  Created on: Aug 31, 2012
 *      Author: mike
 */

#include "ClientHandlerFactory.h"
#include "JobStatusHandler.h"

ClientHandlerFactory::ClientHandlerFactory() {
	addHandler<JobStatusHandler>("JOBSTATUS");
}

ClientHandlerFactory::~ClientHandlerFactory() {
	// TODO Auto-generated destructor stub
}
