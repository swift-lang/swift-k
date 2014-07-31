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
 * CoasterError.h
 *
 *  Created on: Aug 26, 2012
 *      Author: mike
 */

#ifndef COASTERERROR_H_
#define COASTERERROR_H_

#include <string>
#include <stdarg.h>
#include <stdio.h>
#include <sstream>

namespace Coaster {

class CoasterError: public std::exception {
	private:
		std::string message;
	public:
		CoasterError(const std::string& msg);
		CoasterError(const char* format, ...);
		CoasterError(const std::stringstream& ss);
		virtual ~CoasterError() throw();
		virtual const char* what() const throw();
};

}

#endif /* COASTERERROR_H_ */
