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
 * CoasterError.cpp
 *
 *  Created on: Aug 26, 2012
 *      Author: mike
 */

#include "CoasterError.h"
#include <cstdlib>

using namespace Coaster;

using std::string;
using std::stringstream;

#define MAX_MSG_LEN 256

CoasterError::CoasterError(const string& msg) {
	this->message = msg;
}

CoasterError::CoasterError(const char* format, ...) {
	va_list args;
	char msg[MAX_MSG_LEN + 1];

	va_start(args, format);
	int msg_len = vsnprintf(msg, MAX_MSG_LEN, format, args);
	va_end(args);
	this->message.assign(msg, msg_len);
}

CoasterError::CoasterError(const stringstream& ss) {
	message = ss.str();
}

CoasterError::~CoasterError() throw() {
}

const char* CoasterError::what() const throw() {
	return message.c_str();
}
