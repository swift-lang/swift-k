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

namespace Coaster {

class Handler;

class HandlerFactory {
	private:
		std::map<std::string, Handler*(*)()> creators;
	public:
		HandlerFactory();
		virtual ~HandlerFactory();
		template<typename T> void addHandler(std::string name);
		Handler* newInstance(std::string& name);
		Handler* newInstance(const std::string* name);
};

template<typename T> Handler * newHandler() { return new T; }

template<typename T> void HandlerFactory::addHandler(std::string name) {
	creators[name] = &newHandler<T>;
}

}

#endif /* HANDLERFACTORY_H_ */
