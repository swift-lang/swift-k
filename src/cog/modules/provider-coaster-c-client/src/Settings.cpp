/*
 * ServiceSettings.cpp
 *
 *  Created on: Sep 9, 2012
 *      Author: mike
 */

#include "Settings.h"

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

void Settings::set(string& key, string& value) {
	settings[&key] = value.c_str();
}

void Settings::set(string& key, const char* value) {
	settings[&key] = value;
}

void Settings::remove(string& key) {
	settings.erase(&key);
}

map<string*, const char*>* Settings::getSettings() {
	return &settings;
}
