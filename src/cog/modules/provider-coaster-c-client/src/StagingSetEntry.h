/*
 * StagingSetEntry.h
 *
 *  Created on: Aug 11, 2012
 *      Author: mike
 */

#ifndef STAGINGSETENTRY_H_
#define STAGINGSETENTRY_H_

#include <string>

namespace Coaster {

enum StagingMode { ALWAYS = 1, IF_PRESENT = 2, ON_ERROR = 4, ON_SUCCESS = 8 };

class StagingSetEntry {
	private:
		std::string source;
		std::string destination;
		StagingMode mode;
	public:
		StagingSetEntry(std::string source, std::string destination, StagingMode mode);
		std::string getSource();
		std::string getDestination();
		StagingMode getMode();
		virtual ~StagingSetEntry();
};

}

#endif /* STAGINGSETENTRY_H_ */
