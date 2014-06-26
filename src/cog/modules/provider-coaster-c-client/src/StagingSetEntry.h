/*
 * StagingSetEntry.h
 *
 *  Created on: Aug 11, 2012
 *      Author: mike
 */

#ifndef STAGINGSETENTRY_H_
#define STAGINGSETENTRY_H_

#include <string>

#include "coaster-defs.h"

namespace Coaster {

class StagingSetEntry {
	private:
		std::string source;
		std::string destination;
		CoasterStagingMode mode;
	public:
		StagingSetEntry(std::string source, std::string destination, CoasterStagingMode mode);
		std::string getSource();
		std::string getDestination();
		CoasterStagingMode getMode();
		virtual ~StagingSetEntry();

		static const CoasterStagingMode ALWAYS = COASTER_STAGE_ALWAYS;
		static const CoasterStagingMode IF_PRESENT = COASTER_STAGE_IF_PRESENT;
		static const CoasterStagingMode ON_ERROR = COASTER_STAGE_ON_ERROR;
		static const CoasterStagingMode ON_SUCCESS = COASTER_STAGE_ON_SUCCESS;
};

}

#endif /* STAGINGSETENTRY_H_ */
