/*
 * StagingSetEntry.cpp
 *
 *  Created on: Aug 11, 2012
 *      Author: mike
 */

#include "StagingSetEntry.h"

using std::string;

StagingSetEntry::StagingSetEntry(string psource, string pdestination, StagingMode pmode) {
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

StagingMode StagingSetEntry::getMode() {
	return mode;
}

StagingSetEntry::~StagingSetEntry() {
}
