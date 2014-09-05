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
 * Logger.cpp
 *
 *  Created on: Aug 13, 2012
 *      Author: mike
 */

#include "Logger.h"
#include <string.h>
#include <sys/time.h>
#include <time.h>

using namespace Coaster;

using std::cout;
using std::ostream;
using std::string;

Logger::Logger(ostream& pout) {
	out = &pout;
	level = COASTER_LOG_NONE;
	threshold = COASTER_LOG_INFO; // Only show info and higher by default
	file = "<unknown>";
	startOfItem = true;
}

Logger::~Logger() {
}

Logger& Logger::operator<< (CoasterLogLevel plevel) {
	setLevel(plevel);
	return *this;
}

void Logger::setLevel(CoasterLogLevel plevel) {
	level = plevel;
	strLevel = levelToStr(level);
}

void Logger::setThreshold(CoasterLogLevel plevel) {
	threshold = plevel;
}


const char* Logger::levelToStr(CoasterLogLevel level) {
	switch (level) {
		case COASTER_LOG_NONE:
			return "NONE ";
		case COASTER_LOG_ERROR:
			return "ERROR";
		case COASTER_LOG_WARN:
			return "WARN ";
		case COASTER_LOG_INFO:
			return "INFO ";
		case COASTER_LOG_DEBUG:
			return "DEBUG";
		default:
			return "?????";
	}
}

Logger& Logger::operator<< (const string& str) {
	header();
	if (level >= threshold) {
		buffer << str;
	}
	return *this;
}

Logger& Logger::operator<< (const string* str) {
	header();
	if (level >= threshold) {
		buffer << *str;
	}
	return *this;
}

Logger& Logger::operator<< (const char* str) {
	header();
	if (level >= threshold) {
		buffer << str;
	}
	return *this;
}

Logger& Logger::operator<< (int i) {
	header();
	if (level >= threshold) {
		buffer << i;
	}
	return *this;
}

Logger& Logger::operator<< (long l) {
	header();
	if (level >= threshold) {
		buffer << l;
	}
	return *this;
}

Logger& Logger::operator<< (long long int ll) {
	header();
	if (level >= threshold) {
		buffer << ll;
	}
	return *this;
}

Logger& Logger::operator<< (Logger& ( *pf )(Logger&)) {
	(*pf)(*this);
	return *this;
}

Logger& Logger::setFile(const char* pfile) {
     lock.lock();
	 file = strrchr(pfile, '/');
	 if (file == NULL) {
		 file = pfile;
	 }
	 return *this;
}

void Logger::log(CoasterLogLevel level, const char* fileName, const char* msg) {
	if (level >= threshold) {
		setLevel(level);
		setFile(fileName);
		header();
		buffer << msg << std::endl;
		commitBuffer();
	}
}
void Logger::log(CoasterLogLevel level, const char* fileName, string msg) {
	if (level >= threshold) {
		setLevel(level);
		setFile(fileName);
		header();
		buffer << msg << std::endl;
		commitBuffer();
	}
}

void Logger::endItem() {
	if (level >= threshold) {
		buffer << '\n';
		commitBuffer();
	}
	startOfItem = true;
	lock.unlock();
}

void Logger::commitBuffer() {
	*out << buffer.rdbuf();
	buffer.str("");
	out->flush();
}

char* Logger::timeStamp() {
	timeval tv;
	tm *tm;
	char fmt[TS_LEN + 10];

	gettimeofday(&tv, NULL);
	if((tm = localtime(&tv.tv_sec)) != NULL) {
		strftime(fmt, sizeof(fmt), "%Y-%m-%d %H:%M:%S.%%03u%z", tm);
	    snprintf(ts, sizeof(ts), fmt, tv.tv_usec / 1000);
	}

	return ts;
}

void Logger::header() {
	if (startOfItem) {
		if (level >= threshold) {
			buffer << timeStamp() << " " << strLevel << " " << file << " ";
		}
		startOfItem = false;
	}
}

Logger& Coaster::endl(Logger& l) {
	l.endItem();
	return l;
}

StdoutLogger::StdoutLogger(): Logger(cout) {
}

StdoutLogger::~StdoutLogger() {
}

Logger& Logger::singleton() {
	return *Logger::instance;
}

Logger* Logger::instance = new StdoutLogger();
