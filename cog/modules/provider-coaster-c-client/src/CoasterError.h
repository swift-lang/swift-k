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

using namespace std;

class CoasterError: public exception {
	private:
		const char* message;
	public:
		CoasterError(string msg);
		CoasterError(const char* format, ...);
		CoasterError(const stringstream* ss);
		virtual const char* what() const throw();
};

#endif /* COASTERERROR_H_ */
