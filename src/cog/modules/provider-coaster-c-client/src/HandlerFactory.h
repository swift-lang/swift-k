/*
 * HandlerFactory.h
 *
 *  Created on: Aug 28, 2012
 *      Author: mike
 */

#ifndef HANDLERFACTORY_H_
#define HANDLERFACTORY_H_

#include <map>
#include <string>
#include "Handler.h"

using namespace std;

class Handler;

class HandlerFactory {
	private:
		map<string, Handler*(*)()> creators;
	public:
		HandlerFactory();
		virtual ~HandlerFactory();
		template<typename T> void addHandler(string name);
		Handler* newInstance(string& name);
		Handler* newInstance(const string* name);
};

template<typename T> Handler * newHandler() { return new T; }

template<typename T> void HandlerFactory::addHandler(string name) {
	creators[name] = &newHandler<T>;
}


#endif /* HANDLERFACTORY_H_ */
