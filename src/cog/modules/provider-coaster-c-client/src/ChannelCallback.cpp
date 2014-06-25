/*
 * ChannelCallback.cpp
 *
 *  Created on: Aug 30, 2012
 *      Author: mike
 */

#include "ChannelCallback.h"

using namespace Coaster;

ChannelCallback::ChannelCallback() {
}

ChannelCallback::~ChannelCallback() {
}

DeleteBufferCallback DeleteBufferCallback::CALLBACK;

void DeleteBufferCallback::dataSent(Buffer* buf) {
	delete buf;
}
