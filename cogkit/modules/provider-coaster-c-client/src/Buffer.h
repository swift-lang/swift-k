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

namespace Coaster {

/*
 * TODO: need documentation on behavior of buffers w.r.t memory lifespan.
 */
class Buffer {
	private:
		/* Disable default copy constructor for Buffer subclasses */
		Buffer(const Buffer&);
		/* Disable default assignment for Buffer subclasses */
		Buffer& operator=(const Buffer&);
	protected:
		int len;
	public:
		Buffer(int plen);
		int getLen() const;
		virtual const char* getData();
		virtual char* getModifiableData();
		virtual void setData(const char* data);
		virtual ~Buffer();

		virtual std::string* str();
		/* Set string to buffer contents */
		virtual void str(std::string &str);

		int getInt(int index);
		long getLong(int index);
		void putInt(int index, int value);
		void putLong(int index, long value);


		static Buffer* wrap(int i);
		static Buffer* wrap(long l);
		static Buffer* wrap(const char* buf);
		static Buffer* wrap(const char* buf, int len);
		static Buffer* wrap(std::string s);
		static Buffer* wrap(const std::string* s);
		static Buffer* copy(std::string& s);

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
		
		/*
		 * Resize to specified size.
		 */
		void resize(int plen);
};

}

#endif /* BUFFER_H_ */
