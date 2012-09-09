/*
 * Logger.h
 *
 *  Created on: Aug 13, 2012
 *      Author: mike
 */

#ifndef LOGGER_H_
#define LOGGER_H_

#define TS_LEN 28

#include <iostream>
#include <fstream>
#include <string>

using namespace std;

class Logger: public ostream {
	public:
		enum Level { NONE = -1, DEBUG = 0, INFO = 1, WARN = 2, ERROR = 3, FATAL = 4 };
		static Logger* instance;
	private:
		ostream* out;
		Level level;
		const char* file;
		const char* strLevel;
		char ts[TS_LEN + 1];
		bool flushed;

	protected:
		Logger(ostream& out);
		void setLevel(Level level);
		const char* levelToStr(Level level);
		void header();
		char* timeStamp();
	public:
		virtual ~Logger();
		Logger& operator<< (Level level);
		Logger& operator<< (string str);
		Logger& operator<< (const string* str);
		Logger& operator<< (const char* str);
		Logger& operator<< (int i);
		Logger& operator<< (long l);
		Logger& operator<< (ostream& ( *pf )(ostream&));
		Logger& setFile(const char* file);
		Logger& flush();
		void log(Level level, const char* fileName, const char* msg);
		void log(Level level, const char* fileName, string msg);

		static Logger& singleton();
};

class StdoutLogger: public Logger {
	public:
		StdoutLogger();
		virtual ~StdoutLogger();
};

#define LogError Logger::singleton().setFile(__FILE__) << Logger::ERROR
#define LogWarn Logger::singleton().setFile(__FILE__) << Logger::WARN
#define LogInfo Logger::singleton().setFile(__FILE__) << Logger::INFO
#define LogDebug Logger::singleton().setFile(__FILE__) << Logger::DEBUG

#endif /* LOGGER_H_ */
