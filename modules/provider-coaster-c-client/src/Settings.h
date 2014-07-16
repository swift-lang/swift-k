/*
 * ServiceSettings.h
 *
 *  Created on: Sep 9, 2012
 *      Author: mike
 */

#ifndef SETTINGS_H_
#define SETTINGS_H_

#include <string>
#include <map>

namespace Coaster {

class Settings {
	private:
		/*
		 * Store settings as string values for simplicity: don't
		 * worry about managing references.
		 */
		std::map<std::string, std::string> settings;
		
		/* Disable default copy constructor */
		Settings(const Settings&);
		/* Disable default assignment */
		Settings& operator=(const Settings&);
	public:
		Settings();
		virtual ~Settings();
		void set(const std::string& key, const std::string& value);
		void set(const std::string& key, const char* value);
		void set(const char* key, const char* value);
		void set(const char* key, size_t key_len,
			 const char* value, size_t value_len);
		void remove(const std::string& key);
		
		bool contains(const std::string& key);

		/*
		 * Get key from map
		 * returns true if key exists, false if doesn't
		 * value: set to value if key exists
		 */
		bool get(const std::string& key, std::string &value);

		std::map<std::string, std::string>& getSettings();

		template<typename cls> friend cls& operator<< (cls& os, Settings& s);

		class Key {
			public:
				static std::string SLOTS;
				static std::string JOBS_PER_NODE;
				static std::string NODE_GRANULARITY;
				static std::string ALLOCATION_STEP_SIZE;
				static std::string MAX_NODES;
				static std::string LOW_OVERALLOCATION;
				static std::string HIGH_OVERALLOCATION;
				static std::string OVERALLOCATION_DECAY_FACTOR;
				static std::string SPREAD;
				static std::string RESERVE;
				static std::string MAXTIME;
				static std::string REMOTE_MONITOR_ENABLED;
				static std::string INTERNAL_HOSTNAME;
				static std::string WORKER_MANAGER;
				static std::string WORKER_LOGGING_LEVEL;
				static std::string WORKER_LOGGING_DIRECTORY;
				static std::string LD_LIBRARY_PATH;
				static std::string WORKER_COPIES;
				static std::string DIRECTORY;
				static std::string USE_HASH_BANG;
				static std::string PARALLELISM;
				static std::string CORES_PER_NODE;
		};
};

template<typename cls> cls& operator<< (cls& os, Settings& s) {
	os << "Settings(";
	std::map<std::string, std::string>& m = s.getSettings();
	std::map<std::string, std::string>::iterator i;

	for (i = m.begin(); i != m.end(); i++) {
		os << i->first << ": " << i->second;
		if (i != --m.end()) {
			os << ", ";
		}
	}
	os << ")";
	return os;
}

}
#endif /* SETTINGS_H_ */
