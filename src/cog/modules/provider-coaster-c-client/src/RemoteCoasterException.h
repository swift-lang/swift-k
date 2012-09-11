/*
 * RemoteCoasterException.h
 *
 *  Created on: Sep 11, 2012
 *      Author: mike
 */

#ifndef REMOTECOASTEREXCEPTION_H_
#define REMOTECOASTEREXCEPTION_H_

#include <string>

using namespace std;

class RemoteCoasterException {
	private:
		string* className;
		string data;
	public:
		RemoteCoasterException(const char* data, int len);
		virtual ~RemoteCoasterException();
		string& str();
};

#endif /* REMOTECOASTEREXCEPTION_H_ */
