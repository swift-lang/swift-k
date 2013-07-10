/*
 * Buffer.h
 *
 *  Created on: Aug 30, 2012
 *      Author: mike
 */

#ifndef BUFFER_H_
#define BUFFER_H_

#include <string>
#include <ostream>
#include <stdexcept>

using namespace std;

class Buffer {
	protected:
		int len;
	public:
		Buffer(int plen);
		int getLen() const;
		virtual const char* getData();
		virtual char* getModifiableData();
		virtual void setData(const char* data);
		virtual ~Buffer();

		virtual string* str();

		int getInt(int index);
		long getLong(int index);
		void putInt(int index, int value);
		void putLong(int index, long value);


		static Buffer* wrap(int i);
		static Buffer* wrap(long l);
		static Buffer* wrap(const char* buf, int len);
		static Buffer* wrap(string s);
		static Buffer* wrap(const string* s);
		static Buffer* copy(string& s);

		template<typename cls> friend cls& operator<< (cls& os, Buffer& buf);
};

class StaticBuffer: public Buffer {
	private:
		const char* data;
	public:
		StaticBuffer(int plen);
		virtual ~StaticBuffer();

		virtual const char* getData();
		virtual void setData(const char* data);
};

class DynamicBuffer: public Buffer {
	private:
		char* data;
	public:
		DynamicBuffer(int plen);
		virtual ~DynamicBuffer();

		virtual const char* getData();
		virtual char* getModifiableData();
};

#endif /* BUFFER_H_ */
