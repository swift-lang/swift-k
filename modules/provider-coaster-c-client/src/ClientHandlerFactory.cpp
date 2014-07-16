/*
 * ClientHandlerFactory.cpp
 *
 *  Created on: Aug 31, 2012
 *      Author: mike
 */

#include "ClientHandlerFactory.h"
#include "JobStatusHandler.h"

using namespace Coaster;

ClientHandlerFactory::ClientHandlerFactory() {
	addHandler<JobStatusHandler>("JOBSTATUS");
}

ClientHandlerFactory::~ClientHandlerFactory() {
}
