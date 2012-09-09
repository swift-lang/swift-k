/*
 * HandlerFactory.cpp
 *
 *  Created on: Aug 28, 2012
 *      Author: mike
 */

#include "HandlerFactory.h"

HandlerFactory::HandlerFactory() {
}

HandlerFactory::~HandlerFactory() {
}

template<typename T> Handler * newHandler() { return new T; }

template<typename T> void HandlerFactory::addHandler(string name) {
	creators[name] = &newHandler<T>;
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
