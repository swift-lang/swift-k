/*
 * ChannelCallback.cpp
 *
 *  Created on: Aug 30, 2012
 *      Author: mike
 */

#include "ChannelCallback.h"

ChannelCallback::ChannelCallback() {
}

ChannelCallback::~ChannelCallback() {
}

DeleteBufferCallback DeleteBufferCallback::CALLBACK;

void DeleteBufferCallback::dataSent(Buffer* buf) {
	delete buf;
}
