/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 *
 * Copyright 2012-2014 University of Chicago
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
