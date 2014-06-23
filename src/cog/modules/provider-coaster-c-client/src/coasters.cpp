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
 * Created: Jun 18, 2014
 *    Author: Tim Armstrong
 */

#include "coasters.h"

#include <cassert>
#include <cstdlib>

#include "CoasterClient.h"
#include "CoasterError.h"
#include "CoasterLoop.h"

using std::malloc;
using std::free;

using std::string;

/*
  Struct just wraps the object
 */
struct coaster_client {
  CoasterLoop loop;
  CoasterClient client;

  /*
    Constructor: initialize loop then client
   */
  coaster_client(string serviceURL) :
        loop(), client(serviceURL, loop) {

  };
};

static coaster_rc coaster_error_rc(const CoasterError &err);
static coaster_rc exception_rc(const std::exception &ex);

// Check for error
// TODO: store error message somewhere instead of printing?
#define COASTER_CONDITION(cond, err_rc, err_msg) { \
  if (!(cond)) { fprintf(stderr, (err_msg)); return (err_rc); }}

coaster_rc
coaster_client_start(const char *service_url, size_t service_url_len,
                    coaster_client **client) COASTERS_THROWS_NOTHING {
  try {
    *client = new coaster_client(string(service_url, service_url_len));
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
    *settings = new Settings();
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

coaster_rc
coaster_settings_set(coaster_settings *settings,
          const char *key, size_t key_len,
          const char *value, size_t value_len) COASTERS_THROWS_NOTHING {
  try {
    settings->set(key, key_len, value, value_len); 
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_settings_get(coaster_settings *settings,
            const char *key, size_t key_len,
            const char **value, size_t *value_len) COASTERS_THROWS_NOTHING {
  try {
    std::map<string, string> &map = settings->getSettings();
    std::string &str_value = map[string(key, key_len)];
    *value = str_value.c_str();
    *value_len = str_value.length();
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_settings_keys(coaster_settings *settings,
              const char ***keys, size_t **key_lens, int *count)
                                COASTERS_THROWS_NOTHING {
  try {
    std::map<string, string> &map = settings->getSettings();
    *count = map.size();

    // Use malloc so C client code can free
    *keys = (const char**)malloc(sizeof((*keys)[0]) * (*count));
    if (!(*keys)) {
      return COASTER_ERROR_OOM;
    }
    
    if (key_lens != NULL) {
      *key_lens = (size_t *)malloc(sizeof((*key_lens)[0]) * (*count));
      if (!(*key_lens)) {
        free(*keys);
        return COASTER_ERROR_OOM;
      }
    }
    
    int pos = 0;
    for(std::map<string, string>::iterator iter = map.begin();
        iter != map.end(); ++iter) {
      (*keys)[pos] = iter->first.c_str();
      if (key_lens != NULL) {
        (*key_lens)[pos] = iter->first.length();
      }
      pos++;
    }

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

void
coaster_settings_free(coaster_settings *settings)
                                COASTERS_THROWS_NOTHING {
  // Call destructor directly
  // Destructor shouldn't throw anything
  delete settings;
}

/*
 * Apply settings to started coasters client.
 */
coaster_rc
coaster_apply_settings(coaster_client *client,
                                  coaster_settings *settings)
                                  COASTERS_THROWS_NOTHING {
  if (settings == NULL || client == NULL) {
    return COASTER_ERROR_INVALID;
  }

  try {
    client->client.setOptions(*settings);
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_job_create(const char *executable, size_t executable_len,
                  int argc, const char **argv, const size_t *arg_lens,
                  const char *job_manager, size_t job_manager_len,
                  coaster_job **job) COASTERS_THROWS_NOTHING {
  try {
    assert(executable != NULL);
    Job *j = new Job(string(executable, executable_len));
   
    for (int i = 0; i < argc; i++)
    {
      assert(argv[i] != NULL);
      j->addArgument(argv[i], arg_lens[i]);
    }

    if (job_manager != NULL) {
      j->setJobManager(job_manager, job_manager_len);
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
coaster_job_set_redirects(coaster_job *job,
      const char *stdin_loc, size_t stdin_loc_len,
      const char *stdout_loc, size_t stdout_loc_len,
      const char *stderr_loc, size_t stderr_loc_len)
                  COASTERS_THROWS_NOTHING {
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    // job expects to get ownership of references, so use new
    if (stdin_loc != NULL) {
      job->setStdinLocation(*new string(stdin_loc, stdin_loc_len));
    }

    if (stdout_loc != NULL) {
      job->setStdoutLocation(*new string(stdout_loc, stdout_loc_len));
    }

    if (stderr_loc != NULL) {
      job->setStderrLocation(*new string(stderr_loc, stderr_loc_len));
    }

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_job_set_directory(coaster_job *job, const char *dir, size_t dir_len)
                  COASTERS_THROWS_NOTHING {
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    if (dir != NULL) {
      job->setDirectory(*new string(dir, dir_len));
    }

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_job_set_envs(coaster_job *job, int nvars,
      const char **names, size_t *name_lens,
      const char **values, size_t *value_lens) COASTERS_THROWS_NOTHING {
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    for (int i = 0; i < nvars; i++)
    {
      const char *name = names[i];
      size_t name_len = name_lens[i];
      const char *value = values[i];
      size_t value_len = value_lens[i];
      COASTER_CONDITION(name != NULL && value != NULL,
            COASTER_ERROR_INVALID, "Env var name or value was NULL");
      job->setEnv(name, name_len, value, value_len);
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
coaster_job_set_attrs(coaster_job *job, int nattrs,
        const char **names, size_t *name_lens,
        const char **values, size_t *value_lens) COASTERS_THROWS_NOTHING {
 
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    for (int i = 0; i < nattrs; i++)
    {
      const char *name = names[i];
      size_t name_len = name_lens[i];
      const char *value = values[i];
      size_t value_len = value_lens[i];
      COASTER_CONDITION(name != NULL && value != NULL,
            COASTER_ERROR_INVALID, "Attribute name or value was NULL");
      job->setAttribute(name, name_len, value, value_len);
    }

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_job_add_cleanups(coaster_job *job, int ncleanups,
        const char **cleanups, size_t *cleanup_lens)
        COASTERS_THROWS_NOTHING {
 
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    for (int i = 0; i < ncleanups; i++)
    {
      const char *cleanup = cleanups[i];
      size_t cleanup_len = cleanup_lens[i];
      COASTER_CONDITION(cleanup != NULL,
            COASTER_ERROR_INVALID, "Cleanup was NULL");
      job->addCleanup(cleanup, cleanup_len);
    }

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

job_id_t
coaster_job_get_id(coaster_job *job) COASTERS_THROWS_NOTHING {
  // Shouldn't throw anything from accessor method
  return job->getIdentity();
}

coaster_rc
coaster_job_status_code(coaster_job *job, coaster_job_status *code)
                                            COASTERS_THROWS_NOTHING {
  const JobStatus *status;
  if (job == NULL || (status = job->getStatus()) == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  *code = status->getStatusCode();
  return COASTER_SUCCESS;
}

coaster_rc
coaster_job_get_outstreams(coaster_job *job,
                const char **stdout_s, size_t *stdout_len,
                const char **stderr_s, size_t *stderr_len)
                COASTERS_THROWS_NOTHING {
  if (job == NULL) {
    return COASTER_ERROR_INVALID;
  }

  const string *out = job->getStdout();
  const string *err = job->getStderr();

  if (out != NULL) {
    *stderr_s = out->c_str();
    *stderr_len = out->length();
  } else {
    *stderr_s = NULL;
    *stderr_len = 0;
  }

  if (err != NULL) {
    *stderr_s = err->c_str();
    *stderr_len = err->length();
  } else {
    *stderr_s = NULL;
    *stderr_len = 0;
  }

  return COASTER_SUCCESS;
}

coaster_rc
coaster_submit(coaster_client *client, coaster_job *job)
                COASTERS_THROWS_NOTHING {
  try {
    client->client.submit(*job);
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_check_jobs(coaster_client *client, bool wait, int maxjobs,
                   coaster_job **jobs, int *njobs)
                COASTERS_THROWS_NOTHING {
  if (client == NULL) {
    return COASTER_ERROR_INVALID;
  }
  
  try {
    if (wait) {
      client->client.waitForAnyJob();
    }
    
    int n = client->client.getAndPurgeDoneJobs(maxjobs, jobs);

    *njobs = n;
    return COASTER_SUCCESS;

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
