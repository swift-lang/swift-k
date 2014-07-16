/*
 * StagingSetEntry.cpp
 *
 *  Created on: Aug 11, 2012
 *      Author: mike
 */

#include "StagingSetEntry.h"

using namespace Coaster;

using std::string;

StagingSetEntry::StagingSetEntry(string psource, string pdestination, CoasterStagingMode pmode) {
	source = psource;
	destination = pdestination;
	mode = pmode;
}

string StagingSetEntry::getSource() {
	return source;
}

string StagingSetEntry::getDestination() {
	return destination;
}

CoasterStagingMode StagingSetEntry::getMode() {
	return mode;
}

StagingSetEntry::~StagingSetEntry() {
}
