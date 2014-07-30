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
 * ServiceSettings.cpp
 *
 *  Created on: Sep 9, 2012
 *      Author: mike
 */

#include "Settings.h"

using namespace Coaster;

using std::string;
using std::map;

string Settings::Key::SLOTS = "slots";
string Settings::Key::JOBS_PER_NODE = "jobsPerNode";
string Settings::Key::NODE_GRANULARITY = "nodeGranularity";
string Settings::Key::ALLOCATION_STEP_SIZE = "allocationStepSize";
string Settings::Key::MAX_NODES = "maxNodes";
string Settings::Key::LOW_OVERALLOCATION = "lowOverallocation";
string Settings::Key::HIGH_OVERALLOCATION = "highOverallocation";
string Settings::Key::OVERALLOCATION_DECAY_FACTOR = "overallocationDecayFactor";
string Settings::Key::SPREAD = "spread";
string Settings::Key::RESERVE = "reserve";
string Settings::Key::MAXTIME = "maxtime";
string Settings::Key::REMOTE_MONITOR_ENABLED = "remoteMonitorEnabled";
string Settings::Key::INTERNAL_HOSTNAME = "internalHostname";
string Settings::Key::WORKER_MANAGER = "workerManager";
string Settings::Key::WORKER_LOGGING_LEVEL = "workerLoggingLevel";
string Settings::Key::WORKER_LOGGING_DIRECTORY = "workerLoggingDirectory";
string Settings::Key::LD_LIBRARY_PATH = "ldLibraryPath";
string Settings::Key::WORKER_COPIES = "workerCopies";
string Settings::Key::DIRECTORY = "directory";
string Settings::Key::USE_HASH_BANG = "useHashBang";
string Settings::Key::PARALLELISM = "parallelism";
string Settings::Key::CORES_PER_NODE = "coresPerNode";

Settings::Settings() {
}

Settings::~Settings() {
}

void Settings::set(const string& key, const string& value) {
	settings[key] = value;
}

void Settings::set(const string& key, const char* value) {
	settings[key] = value;
}
void Settings::set(const char* key, const char* value) {
	settings[key] = value;
}
void Settings::set(const char* key, size_t key_len,
	 const char* value, size_t value_len) {
	settings[string(key, key_len)] = string(value, value_len);
}

void Settings::remove(const string& key) {
	settings.erase(key);
}

bool Settings::contains(const string& key) {
	return settings.find(key) != settings.end();
}

bool Settings::get(const string& key, string& value) {
	map<string, string>::iterator it = settings.find(key);
	if (settings.find(key) == settings.end()) {
		return false;
	} else {
		value = it->second;
		return true;
	}
}

map<string, string>& Settings::getSettings() {
	return settings;
}
