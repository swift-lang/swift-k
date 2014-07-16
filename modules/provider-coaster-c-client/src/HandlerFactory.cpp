/*
 * HandlerFactory.cpp
 *
 *  Created on: Aug 28, 2012
 *      Author: mike
 */

#include "HandlerFactory.h"

using namespace Coaster;

using std::string;
using std::map;

HandlerFactory::HandlerFactory() {
}

HandlerFactory::~HandlerFactory() {
}

Handler* HandlerFactory::newInstance(string& name) {
	if (creators.count(name) == 0) {
		return NULL;
	}
	else {
		return creators[name]();
	}
}

Handler* HandlerFactory::newInstance(const string* name) {
	if (creators.count(*name) == 0) {
		return NULL;
	}
	else {
		return creators[*name]();
	}
}
