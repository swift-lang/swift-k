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

#define MAX_MSG_LEN 256

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

#endif /* COASTERERROR_H_ */
