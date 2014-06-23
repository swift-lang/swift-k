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

#include <cassert>
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

// Check for error
// TODO: store error message somewhere instead of printing?
#define COASTER_CONDITION(cond, err_rc, err_msg) { \
  if (!(cond)) { fprintf(stderr, (err_msg)); return (err_rc); }}

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
    std::map<string, string> &map = settings->settings.getSettings();
    std::string &str_value = map[str_key];
    *value = str_value.c_str();
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
    std::map<string, string> &map = settings->settings.getSettings();
    *count = map.size();

    // Use malloc so C client code can free
    *keys = (const char**)malloc(sizeof((*keys)[0]));
    if (!(*keys)) {
      return COASTER_ERROR_OOM;
    }
    
    int pos = 0;
    for(std::map<string, string>::iterator iter = map.begin();
        iter != map.end(); ++iter) {
      (*keys)[pos++] = iter->first.c_str();
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
  // Destructor shouldn't throw anything
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
coaster_job_create(const char *executable, int argc, const char **argv,
                  const char *job_manager, coaster_job **job)
                      COASTERS_THROWS_NOTHING
{
  try {
    assert(executable != NULL);
    coaster_job *j = new coaster_job(executable);
   
    for (int i = 0; i < argc; i++)
    {
      assert(argv[i] != NULL);
      j->job.addArgument(argv[i]);
    }

    if (job_manager != NULL)
    {
      j->job.setJobManager(job_manager);
    }

    *job = j;
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_job_free(coaster_job *job) COASTERS_THROWS_NOTHING {
  // Destructor shouldn't throw anything
  delete job;
}

coaster_rc
coaster_job_set_redirects(coaster_job *job, const char *stdin_loc,
                  const char *stdout_loc, const char *stderr_loc)
                  COASTERS_THROWS_NOTHING {
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    std::string *stdin_str = (stdin_loc == NULL) ?
                              NULL : new string(stdin_loc);
    job->job.setStdinLocation(*stdin_str);
    
    std::string *stdout_str = (stdout_loc == NULL) ?
                              NULL : new string(stdout_loc);
    job->job.setStdinLocation(*stdout_str);
    
    std::string *stderr_str = (stderr_loc == NULL) ?
                              NULL : new string(stderr_loc);
    job->job.setStdinLocation(*stderr_str);

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_job_set_directory(coaster_job *job, const char *dir)
                  COASTERS_THROWS_NOTHING {
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    std::string *dir_str = (dir == NULL) ?
                              NULL : new string(dir);
    job->job.setDirectory(*dir_str);

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_job_set_envs(coaster_job *job, int nvars, const char **names,
                    const char **values) COASTERS_THROWS_NOTHING {
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    for (int i = 0; i < nvars; i++)
    {
      const char *name = names[i];
      const char *value = values[i];
      COASTER_CONDITION(name != NULL && value != NULL,
            COASTER_ERROR_INVALID, "Env var name or value was NULL");
      job->job.setEnv(name, value);
    }

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

/*
 * Add attributes for the job.  Will overwrite any previous atrributes
 * if names match.
 */
coaster_rc
coaster_job_set_attrs(coaster_job *job, int nattrs, const char **names,
                    const char **values) COASTERS_THROWS_NOTHING {
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    for (int i = 0; i < nattrs; i++)
    {
      const char *name = names[i];
      const char *value = values[i];
      COASTER_CONDITION(name != NULL && value != NULL,
            COASTER_ERROR_INVALID, "Attribute name or value was NULL");
      job->job.setAttribute(name, value);
    }

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

const char *
coaster_job_get_id(coaster_job *job) COASTERS_THROWS_NOTHING {
  // Shouldn't throw anything from accessor method
  const string &id = job->job.getIdentity();
  return id.c_str();
}

coaster_rc
coaster_submit(coaster_client *client, coaster_job *job)
                COASTERS_THROWS_NOTHING {
  try {
    client->client.submit(job->job);
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
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
  // TODO: handle specific types, e.g. bad_alloc
  return COASTER_ERROR_UNKNOWN;
}
