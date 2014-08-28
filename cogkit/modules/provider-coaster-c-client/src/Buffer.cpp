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
 * Buffer.cpp
 *
 *  Created on: Aug 30, 2012
 *      Author: mike
 */

#include "Buffer.h"
#include <stdlib.h>
#include <string.h>

using namespace Coaster;

using std::string;
using std::out_of_range;
using std::logic_error;

Buffer::Buffer(int plen) {
	len = plen;
}

int Buffer::getLen() const {
	return len;
}

const char* Buffer::getData() {
	return NULL;
}

char* Buffer::getModifiableData() {
	throw logic_error("putInt called on static buffer");
}

void Buffer::setData(const char* pdata) {
	throw logic_error("illegal operation: Buffer::setData()");
}

Buffer::~Buffer() {
}

string* Buffer::str() {
	return new string(getData(), getLen());
}

void Buffer::str(string& str) {
	str.assign(getData(), getLen());
}

int Buffer::getInt(int index) {
	if (index + 4 > getLen()) {
		throw out_of_range("getInt index out of range");
	}
	const char* buf = getData();
	int value = 0;
	index += 3;
	for (int i = 0; i < 4; i++) {
		value <<= 8;
		value = value + (0x000000ff & buf[index--]);
	}

	return value;
}

long Buffer::getLong(int index) {
	if (index + 8 > getLen()) {
		throw out_of_range("getLong index out of range");
	}
	const char* buf = getData();
	long value = 0;
	index += 7;
	for (int i = 0; i < 8; i++) {
		value <<= 8;
		value = value + (0x00000000000000ffL & buf[index--]);
	}

	return value;
}

void Buffer::putInt(int index, int value) {
	if (index + 4 > getLen()) {
		throw out_of_range("putInt index out of range");
	}
	char* buf = getModifiableData();
	for (int i = 0; i < 4; i++) {
		buf[index++] = value & 0x000000ff;
		value >>= 8;
	}
}

void Buffer::putLong(int index, long value) {
	if (index + 8 > getLen()) {
		throw out_of_range("putLong index out of range");
	}
	char* buf = getModifiableData();
	for (int i = 0; i < 8; i++) {
		buf[index++] = value & 0x00000000000000ffL;
		value >>= 8;
	}
}

Buffer* Buffer::wrap(int i) {
	DynamicBuffer* b = new DynamicBuffer(4);
	b->putInt(0, i);
	return b;
}

Buffer* Buffer::wrap(long l) {
	DynamicBuffer* b = new DynamicBuffer(8);
	b->putLong(0, l);
	return b;
}

Buffer* Buffer::wrap(const char* data) {
	wrap(data, (int)strlen(data));
}

Buffer* Buffer::wrap(const char* data, int len) {
	StaticBuffer* b = new StaticBuffer(len);
	b->setData(data);
	return b;
}

Buffer* Buffer::wrap(string s) {
	StaticBuffer* b = new StaticBuffer(s.length());
	b->setData(s.data());
	return b;
}

Buffer* Buffer::copy(string& s) {
	DynamicBuffer* b = new DynamicBuffer(s.length());
	strncpy(b->getModifiableData(), s.data(), s.length());
	return b;
}

Buffer* Buffer::wrap(const string* s) {
	StaticBuffer* b = new StaticBuffer(s->length());
	b->setData(s->data());
	return b;
}

template<typename cls> cls& operator<< (cls& os, Buffer& buf) {
	const char* data = buf.getData();
	int len = buf.getLen();

	for (int i = 0; i < len; i++) {
		os.put(data[i]);
	}

	return os;
}

StaticBuffer::StaticBuffer(int plen): Buffer(plen) {
}

const char* StaticBuffer::getData() {
	return data;
}

void StaticBuffer::setData(const char* pdata) {
	data = pdata;
}

StaticBuffer::~StaticBuffer() {
}

DynamicBuffer::DynamicBuffer(int plen): Buffer(plen) {
	if (plen > 0) {
		data = (char *) malloc(len);
		if (data == NULL) {
			throw std::bad_alloc();
		}
	}
	else {
		data = NULL;
	}

}


const char* DynamicBuffer::getData() {
	return data;
}

char* DynamicBuffer::getModifiableData() {
	return data;
}

void DynamicBuffer::resize(int plen) {
	char *tmp = (char*)realloc(data, plen);
	if (tmp == NULL) {
		throw std::bad_alloc();
	}
	data = tmp;
	len = plen;
}

DynamicBuffer::~DynamicBuffer() {
	if (data != NULL) {
		free(data);
	}
}

