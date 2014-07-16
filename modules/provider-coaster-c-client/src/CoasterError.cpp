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
