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
 * RemoteCoasterException.h
 *
 *  Created on: Sep 11, 2012
 *      Author: mike
 */

#ifndef REMOTECOASTEREXCEPTION_H_
#define REMOTECOASTEREXCEPTION_H_

#include <string>

namespace Coaster {

class RemoteCoasterException {
	private:
		std::string* className;
		std::string data;
	public:
		RemoteCoasterException(const char* data, int len);
		virtual ~RemoteCoasterException();
		std::string& str();
};

}

#endif /* REMOTECOASTEREXCEPTION_H_ */
