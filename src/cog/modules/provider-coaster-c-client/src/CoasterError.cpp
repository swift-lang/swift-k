/*
 * CoasterError.cpp
 *
 *  Created on: Aug 26, 2012
 *      Author: mike
 */

#include "CoasterError.h"
#include <stdlib.h>

CoasterError::CoasterError(string msg) {
	message = msg.c_str();
}

CoasterError::CoasterError(const char* format, ...) {
	va_list args;
	char* buf = (char *) malloc(MAX_MSG_LEN + 1);

	va_start(args, format);
	vsnprintf(buf, MAX_MSG_LEN, format, args);
	va_end(args);
	message = buf;
}

CoasterError::CoasterError(const stringstream* ss) {
	message = ss->str().c_str();
}

const char* CoasterError::what() const throw() {
	return message;
}
