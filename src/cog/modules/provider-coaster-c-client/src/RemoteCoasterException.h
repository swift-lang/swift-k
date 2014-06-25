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
