/*
 * Copyright 2014 University of Chicago and Argonne National Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

/*
 * Implementation of pure C functions.
 *
 */

#include "coasters.h"

#include <cstdlib>

#include "CoasterClient.h"
#include "CoasterError.h"
#include "CoasterLoop.h"

using std::malloc;
using std::free;

/*
  Struct just wraps the object
 */
struct coaster_client {
  CoasterLoop loop;
  CoasterClient client;

  /*
    Constructor: initialize loop then client
   */
  coaster_client(const char *serviceURL) :
        loop(), client(serviceURL, loop) {

  };
};

struct coaster_settings {
  Settings settings;

  coaster_settings() : settings() {};
};

struct coaster_job {
  Job job;

  coaster_job(const string &executable) : job(executable) {};
};

static coaster_rc coaster_error_rc(const CoasterError &err);
static coaster_rc exception_rc(const std::exception &ex);

coaster_rc coaster_client_start(const char *serviceURL,
                                coaster_client **client)
                                COASTERS_THROWS_NOTHING {
  try {
    *client = new coaster_client(serviceURL);
    if (!(*client)) {
      return COASTER_ERROR_OOM;
    }

    (*client)->client.start();
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc coaster_client_stop(coaster_client *client)
                               COASTERS_THROWS_NOTHING {
  try {
    // TODO: stop things
    
    client->client.stop();
    client->loop.stop();
    
    delete client;
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc coaster_settings_create(coaster_settings **settings)
                                COASTERS_THROWS_NOTHING {
  try {
    *settings = new coaster_settings();
    if (!(*settings)) {
      return COASTER_ERROR_OOM;
    }
 
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc coaster_settings_parse(coaster_settings *settings,
                                  const char *str)
                                COASTERS_THROWS_NOTHING {

  // TODO: parsing using code currently in CoasterSwig
  return COASTER_ERROR_UNKNOWN;
}

coaster_rc coaster_settings_set(coaster_settings *settings,
                      const char *key, const char *value)
                                COASTERS_THROWS_NOTHING {
  try {
    std::string str_key(key);
    settings->settings.set(str_key, value); 
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc coaster_settings_get(coaster_settings *settings,
                      const char *key, const char **value)
                                COASTERS_THROWS_NOTHING {
  try {
    std::string str_key(key);
    std::map<string*, const char*> *map;
    map = settings->settings.getSettings();
    *value = (*map)[&str_key]; 
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}
coaster_rc coaster_settings_keys(coaster_settings *settings,
                      const char ***keys, int *count)
                                COASTERS_THROWS_NOTHING {
  try {
    std::map<string*, const char*> *map;
    map = settings->settings.getSettings();
    *count = map->size();

    // Use malloc so C client code can free
    *keys = (const char**)malloc(sizeof((*keys)[0]));
    if (!(*keys)) {
      return COASTER_ERROR_OOM;
    }
    
    int pos = 0;
    for(std::map<string*, const char*>::iterator iter = map->begin();
        iter != map->end(); ++iter) {
      (*keys)[pos++] = iter->first->c_str();
    }

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

void coaster_settings_free(coaster_settings *settings)
                                COASTERS_THROWS_NOTHING {
  // Call destructor directly
  delete settings;
}

/*
 * Apply settings to started coasters client.
 */
coaster_rc coaster_apply_settings(coaster_client *client,
                                  coaster_settings *settings)
                                  COASTERS_THROWS_NOTHING {
  try {
    client->client.setOptions(settings->settings);
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_job_create(const char *executable, int argc, const char *argv,
                  const char *job_manager, coaster_job **job)
                      COASTERS_THROWS_NOTHING
{
  coaster_job *j = new coaster_job(executable);
  

  *job = j;
  return COASTER_SUCCESS;
}

const char *coaster_rc_string(coaster_rc code)
{
  switch (code)
  {
    case COASTER_SUCCESS:
      return "COASTER_SUCCESS";
    case COASTER_ERROR_OOM:
      return "COASTER_ERROR_OOM";
    case COASTER_ERROR_NETWORK:
      return "COASTER_ERROR_NETWORK";
    case COASTER_ERROR_UNKNOWN:
      return "COASTER_ERROR_UNKNOWN";
    default:
      return (const char*)0;
  }
}

/*
 * Set error information and return appropriate code
 */
static coaster_rc coaster_error_rc(const CoasterError &err) {
  // TODO: store detailed error info
  // TODO: distinguish different cases?
  return COASTER_ERROR_UNKNOWN;
}

static coaster_rc exception_rc(const std::exception &ex) {
  // TODO: store error info?
  return COASTER_ERROR_UNKNOWN;
}
