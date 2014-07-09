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
#include <cstring>
#include <pthread.h>

#include "CoasterClient.h"
#include "CoasterError.h"
#include "CoasterLoop.h"
#include "Settings.h"

using namespace Coaster;

using std::malloc;
using std::free;

using std::string;
using std::memcpy;

/*
  Struct just wraps the objects
 */
struct coaster_client {
  CoasterLoop loop;
  CoasterClient client;

  /*
    Constructor: initialize loop then client
   */
  coaster_client(string serviceURL) :
        loop(), client(serviceURL, loop) { }
};

struct coaster_err_info {
  string msg;

  coaster_err_info(const string _msg): msg(_msg) {}
};

static pthread_key_t err_key;
static pthread_once_t err_key_init = PTHREAD_ONCE_INIT;

static coaster_rc coaster_return_error(coaster_rc code,
                                    const string &msg);
static coaster_rc coaster_error_rc(const CoasterError &err);
static coaster_rc exception_rc(const std::exception &ex);
static void init_err_key(void);
static void cleanup_err_key(void *errinfo);
static void clear_err_info(void);

// Check for error
#define COASTER_CONDITION(cond, err_rc, err_msg) { \
  if (!(cond)) { coaster_return_error((err_rc), (err_msg)); }}

// TODO: it's bad that this might allocate memory
#define COASTER_CHECK_MALLOC(ptr) { \
  if ((ptr) == NULL) { \
    coaster_return_error(COASTER_ERROR_OOM, "out of memory"); }}

coaster_rc
coaster_client_start(const char *service_url, size_t service_url_len,
                    coaster_client **client) COASTER_THROWS_NOTHING {
  try {
    *client = new coaster_client(string(service_url, service_url_len));
    COASTER_CHECK_MALLOC(*client);

    (*client)->loop.start();
    (*client)->client.start();
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc coaster_client_stop(coaster_client *client)
                               COASTER_THROWS_NOTHING {
  try {
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
                                COASTER_THROWS_NOTHING {
  try {
    *settings = new Settings();
    COASTER_CHECK_MALLOC(*settings);
 
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

static void
settings_emit(coaster_settings *settings, string &key, string &value);

coaster_rc
coaster_settings_parse(coaster_settings *settings,
              const char *str, size_t str_len, char separator)
                                COASTER_THROWS_NOTHING {
  COASTER_CONDITION(settings != NULL, COASTER_ERROR_INVALID,
                    "Null Coaster settings object");
  COASTER_CONDITION(str != NULL, COASTER_ERROR_INVALID,
                    "Null Coaster settings string");
  string key, value; // Storage for current key and value
  
  bool in_key = true; // Either in key or value
  bool in_quotes = false;
  bool in_escape = false;

  for (size_t pos = 0; pos < str_len; pos++) {
    char c = str[pos];
    string &curr = in_key ? key : value;

    if (in_escape) {
      // Current character is escaped
      curr.push_back(c);
      in_escape = false;

    } else if (in_quotes) {
      if (c == '\\') {
        in_escape = true;
      } else if (c == '"') {
        in_quotes = false;
      } else {
        curr.push_back(c);
      }
    } else {
      // Not in escape or quotes
      if (c == '\\') {
        in_escape = true;
      } else if (c == '"') {
        in_quotes = true;
      } else if (c == '=') {
        COASTER_CONDITION(in_key, COASTER_ERROR_INVALID,
                  "'=' not allowed in unquoted Coaster settings value");
        in_key = false;
      } else if (c == separator) {
        COASTER_CONDITION(!in_key, COASTER_ERROR_INVALID,
                  "',' not allowed in unquoted Coaster settings key");
        
        settings_emit(settings, key, value);          
        
        in_key = true;
      } else {
        curr.push_back(c);
      }
    }
  }

  // Check for invalid state
  COASTER_CONDITION(!in_escape, COASTER_ERROR_INVALID,
        "Trailing '\\' escape at end of Coaster settings");
  COASTER_CONDITION(!in_quotes, COASTER_ERROR_INVALID,
        "Unclosed '\"' quote at end of Coaster settings");
  COASTER_CONDITION(!in_key, COASTER_ERROR_INVALID,
        "Key without value at end of Coaster settings");

  settings_emit(settings, key, value);

  return COASTER_SUCCESS;
}


/*
 * Helper function to emit completed key/value setting
 */
static void
settings_emit(coaster_settings *settings, string &key, string &value) {
  if (settings->contains(key)) {
    string old_value;
    settings->get(key, old_value);
    LogWarn << "Overwrote previous Coaster settings value for "
                "key: \"" << key << "\".  Old value: \"" << 
                old_value << "\", New value: \"" <<
                value << "\"." << endl;
  }

  settings->set(key, value);
  key.clear();
  value.clear();
}

coaster_rc
coaster_settings_set(coaster_settings *settings,
          const char *key, size_t key_len,
          const char *value, size_t value_len) COASTER_THROWS_NOTHING {
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
            const char **value, size_t *value_len) COASTER_THROWS_NOTHING {
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
                                COASTER_THROWS_NOTHING {
  try {
    std::map<string, string> &map = settings->getSettings();
    *count = map.size();

    // Use malloc so C client code can free
    *keys = (const char**)malloc(sizeof((*keys)[0]) * (*count));
    COASTER_CHECK_MALLOC(*keys);
    
    if (key_lens != NULL) {
      *key_lens = (size_t *)malloc(sizeof((*key_lens)[0]) * (*count));
      if (!(*key_lens)) {
        free(*keys);
        COASTER_CHECK_MALLOC(*key_lens);
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
                                COASTER_THROWS_NOTHING {
  // Call destructor directly
  // Destructor shouldn't throw anything
  delete settings;
}

/*
 * Apply settings to started coasters client.
 */
coaster_rc
coaster_apply_settings(coaster_client *client,
        coaster_settings *settings, coaster_config_id **config)
                                  COASTER_THROWS_NOTHING {
  if (settings == NULL || client == NULL || config == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "invalid arg");
  }

  try {
    string *configId = new string;
    *configId = client->client.setOptions(*settings);
    *config = configId;
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_config_id_free(coaster_config_id *config) COASTER_THROWS_NOTHING {
  delete config;
  return COASTER_SUCCESS;
}

coaster_rc
coaster_job_create(const char *executable, size_t executable_len,
                  int argc, const char **argv, const size_t *arg_lens,
                  const char *job_manager, size_t job_manager_len,
                  coaster_job **job) COASTER_THROWS_NOTHING {
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
coaster_job_free(coaster_job *job) COASTER_THROWS_NOTHING {
  // Destructor shouldn't throw anything
  delete job;
  return COASTER_SUCCESS;
}

coaster_rc
coaster_job_to_string(const coaster_job *job, char **str, size_t *str_len)
                                   COASTER_THROWS_NOTHING {
  if (job == NULL || str == NULL || str_len == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "invalid argument");
  }

  try {
    string jobStr = job->toString();

    *str = (char*)malloc(jobStr.length() + 1);
    COASTER_CHECK_MALLOC(*str);
    memcpy(*str, jobStr.c_str(), jobStr.length() + 1);
    *str_len = jobStr.length();
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }

}

coaster_rc
coaster_job_set_redirects(coaster_job *job,
      const char *stdin_loc, size_t stdin_loc_len,
      const char *stdout_loc, size_t stdout_loc_len,
      const char *stderr_loc, size_t stderr_loc_len)
                  COASTER_THROWS_NOTHING {
  if (job == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "invalid arg");
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
                  COASTER_THROWS_NOTHING {
  if (job == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "");
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
      const char **values, size_t *value_lens) COASTER_THROWS_NOTHING {
  if (job == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "invalid arg");
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
        const char **values, size_t *value_lens) COASTER_THROWS_NOTHING {
 
  if (job == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "invalid job");
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
        COASTER_THROWS_NOTHING {
 
  if (job == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "invalid job");
  }
  
  try {
    for (int i = 0; i < ncleanups; i++) {
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

coaster_rc
coaster_job_add_stages(coaster_job *job,
    int nstageins, coaster_stage_entry *stageins,
    int nstageouts, coaster_stage_entry *stageouts)
        COASTER_THROWS_NOTHING {

  if (job == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "invalid job");
  }
  
  try {
    for (int i = 0; i < nstageins; i++) {
      coaster_stage_entry *s = &stageins[i];
      job->addStageIn(string(s->src, s->src_len),
                      string(s->dst, s->dst_len), s->mode);
    }
    
    for (int i = 0; i < nstageouts; i++) {
      coaster_stage_entry *s = &stageouts[i];
      job->addStageOut(string(s->src, s->src_len),
                       string(s->dst, s->dst_len), s->mode);
    }

    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }

}

coaster_job_id
coaster_job_get_id(const coaster_job *job) COASTER_THROWS_NOTHING {
  // Shouldn't throw anything from accessor method
  return job->getIdentity();
}

coaster_rc
coaster_job_status_code(const coaster_job *job, coaster_job_status *code)
                                            COASTER_THROWS_NOTHING {
  const JobStatus *status;
  if (job == NULL || (status = job->getStatus()) == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID,
                                "invalid or unsubmitted job");
  }
  
  *code = status->getStatusCode();
  return COASTER_SUCCESS;
}

coaster_rc
coaster_job_get_outstreams(const coaster_job *job,
                const char **stdout_s, size_t *stdout_len,
                const char **stderr_s, size_t *stderr_len)
                COASTER_THROWS_NOTHING {
  if (job == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "invalid job");
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
coaster_submit(coaster_client *client, const coaster_config_id *config,
               coaster_job *job) COASTER_THROWS_NOTHING {
  try {
    client->client.submit(*job, config);
    return COASTER_SUCCESS;
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
}

coaster_rc
coaster_check_jobs(coaster_client *client, bool wait, int maxjobs,
                   coaster_job **jobs, int *njobs)
                COASTER_THROWS_NOTHING {
  if (client == NULL) {
    return coaster_return_error(COASTER_ERROR_INVALID, "invalid client");
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

const char *coaster_last_err_info(void) {
  // Ensure initialized
  pthread_once(&err_key_init, init_err_key);

  void *val = pthread_getspecific(err_key);
  if (val == NULL) {
    return NULL;
  }

  coaster_err_info *info = static_cast<coaster_err_info *>(val);
  return info->msg.c_str();
}

static void init_err_key(void) {
  pthread_key_create(&err_key, cleanup_err_key);
}

/*
 * Called to cleanup error key
 */
static void cleanup_err_key(void *errinfo) {
  delete static_cast<coaster_err_info*>(errinfo);
}

static void set_err_info(const string& msg) {
  // Ensure key is initialized
  pthread_once(&err_key_init, init_err_key);

  void *prev = pthread_getspecific(err_key);
  if (prev != NULL) {
    cleanup_err_key(prev);
  }
  
  coaster_err_info *err_info = new coaster_err_info(msg);
  pthread_setspecific(err_key, static_cast<void*>(err_info));
}

static void clear_err_info(void) {
  void *prev = pthread_getspecific(err_key);
  if (prev != NULL) {
    cleanup_err_key(prev);
  }
  
  pthread_setspecific(err_key, NULL);
}

/*
 * Helper to set error info when returning error.
 */
static coaster_rc coaster_return_error(coaster_rc code,
                                    const string& msg) {
  set_err_info(msg);  
  return code;
}

/*
 * Set error information and return appropriate code
 */
static coaster_rc coaster_error_rc(const CoasterError &err) {
  const char *msg = err.what();
  if (msg != NULL) {
    set_err_info(string(msg));
  } else {
    clear_err_info();
  }
  // TODO: distinguish different cases?
  return COASTER_ERROR_UNKNOWN;
}

static coaster_rc exception_rc(const std::exception &ex) {
  const char *msg = ex.what();
  if (msg != NULL) {
    set_err_info(string(msg));
  } else {
    clear_err_info();
  }
  // TODO: handle specific types, e.g. bad_alloc
  return COASTER_ERROR_UNKNOWN;
}

coaster_rc coaster_set_log_threshold(coaster_log_level threshold) {
  
  Logger::singleton().setThreshold(threshold);

  return COASTER_SUCCESS;
}
