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

Logger::Logger(ostream& pout) {
	out = &pout;
	level = NONE;
	file = "<unknown>";
	startOfItem = true;
}

Logger::~Logger() {
}

Logger& Logger::operator<< (Level plevel) {
	setLevel(plevel);
	return *this;
}

void Logger::setLevel(Level plevel) {
	level = plevel;
	strLevel = levelToStr(level);
}

void Logger::setThreshold(Level plevel) {
	threshold = plevel;
}


const char* Logger::levelToStr(Level level) {
	switch (level) {
		case NONE:
			return "NONE ";
		case ERROR:
			return "ERROR";
		case WARN:
			return "WARN ";
		case INFO:
			return "INFO ";
		case DEBUG:
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

void Logger::log(Level level, const char* fileName, const char* msg) {
	if (level >= threshold) {
		setLevel(level);
		setFile(fileName);
		header();
		buffer << msg << endl;
		commitBuffer();
	}
}
void Logger::log(Level level, const char* fileName, string msg) {
	if (level >= threshold) {
		setLevel(level);
		setFile(fileName);
		header();
		buffer << msg << endl;
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

Logger& endl(Logger& l) {
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
