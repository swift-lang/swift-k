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
	setFile("<unknown>");
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

Logger& Logger::operator<< (string str) {
	header();
	*out << str;
	return *this;
}

Logger& Logger::operator<< (const string* str) {
	header();
	*out << *str;
	return *this;
}

Logger& Logger::operator<< (const char* str) {
	header();
	*out << str;
	return *this;
}

Logger& Logger::operator<< (Logger& ( *pf )(Logger&)) {
	(*pf)(*this);
	return *this;
}

Logger& Logger::setFile(const char* pfile) {
	 file = strrchr(pfile, '/');
	 if (file == NULL) {
		 file = pfile;
	 }
	 return *this;
}

void Logger::log(Level level, const char* fileName, const char* msg) {
	setLevel(level);
	setFile(fileName);
	header();
	*out << msg << endl;
}
void Logger::log(Level level, const char* fileName, string msg) {
	setLevel(level);
	setFile(fileName);
	header();
	*out << msg << endl;
}

void Logger::endItem() {
	out->flush();
	startOfItem = true;
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
		*out << timeStamp() << " " << strLevel << " " << file << " ";
		startOfItem = false;
	}
}

Logger& endl(Logger& l) {
	l << "\n";
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
