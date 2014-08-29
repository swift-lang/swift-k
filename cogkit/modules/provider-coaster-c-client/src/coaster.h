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
 * coaster.h
 *
 * Created: Jun 18, 2014
 *    Author: Tim Armstrong
 *
 * Pure C interface for Coaster client.  This aims to expose a subset of the
 * C++ API's functionality that is sufficient to support all common use
 * cases for submitting jobs through Coaster services.
 */

#ifndef COASTER_H__H_
#define COASTER_H__H_

#ifdef __cplusplus
#include <string>
#endif

#ifdef __cplusplus
extern "C" {
#endif

#ifdef __cplusplus
#include <cstddef>
#else
#include <stddef.h>
#endif

#include "coaster-defs.h"

// Opaque pointer types
typedef struct coaster_client coaster_client;

/*
  Treat some types as direct pointers to real classes for C++, but opaque
  pointers to dummy structs for C code to allow typechecking, but prevent
  manipulation of the rbjects.
 */
#ifdef __cplusplus
namespace Coaster {
  class Settings;
  class Job;
}

typedef class Coaster::Settings coaster_settings;
typedef std::string coaster_config_id;
typedef class Coaster::Job coaster_job;
#else
// Treat these types as opaque pointer to unimplemented struct for C
typedef struct coaster_settings_opaque_ coaster_settings;
typedef struct coaster_config_id_opaque_ coaster_config_id;
typedef struct coaster_job_opaque_ coaster_job;
#endif

/*
 * Return codes for coaster errors
 * Additional info may be available through coaster_last_err_info()
 */
typedef enum {
  COASTER_SUCCESS,
  COASTER_ERROR_INVALID,
  COASTER_ERROR_OOM,
  COASTER_ERROR_NETWORK,
  COASTER_ERROR_UNKNOWN,
} coaster_rc;

/*
 * Information about a file to be staged
 */
typedef struct {
  const char *src;
  size_t src_len;

  const char *dst;
  size_t dst_len;

  coaster_staging_mode mode;
} coaster_stage_entry;

// Set appropriate macro to specify that we shouldn't throw exceptions
#ifdef __cplusplus
#define COASTER_THROWS_NOTHING throw()
#else
#define COASTER_THROWS_NOTHING
#endif

/*
 * Start a new coaster client.
 * NOTE: don't support multiple clients per loop with this interface
 *
 * service_url[_len]: coaster service URL string with string length
 * client: output for new client
 */
coaster_rc
coaster_client_start(const char *service_url, size_t service_url_len,
                    coaster_client **client) COASTER_THROWS_NOTHING;

/*
 * Stop coaster client and free all memory.
 *
 * After calling this the client is invalid and should not be used as
 * an argument to any more Coaster C API function calls.
 */
coaster_rc
coaster_client_stop(coaster_client *client) COASTER_THROWS_NOTHING;

/*
 * Create empty settings object
 * settings: coaster settings object, should be freed with
             coaster_settings_free
 */
coaster_rc
coaster_settings_create(coaster_settings **settings)
                    COASTER_THROWS_NOTHING;

/*
 * Free memory associated with coaster settings
 */
void
coaster_settings_free(coaster_settings *settings)
                                COASTER_THROWS_NOTHING;

/*
 * Parse settings from string.
 *
 * str[_len]: String with key/value settings and length of string.
      Settings separated by separator char, key/values separated by equals sign.
      Backslash escapes the next character.
      Keys and values can be quoted with ".
   separator: character to separate keys/values
 */
coaster_rc
coaster_settings_parse(coaster_settings *settings, const char *str,
             size_t str_len, char separator) COASTER_THROWS_NOTHING;

/*
 * Set settings individually.  Will overwrite previous values.
 * key[_len]: settings key (must be non-null)
 * values[_len]: settings value (must be non-null)
 */
coaster_rc
coaster_settings_set(coaster_settings *settings,
            const char *key, size_t key_len,
            const char *value, size_t value_len) COASTER_THROWS_NOTHING;
/*
 * Erase a key/value pair in settings.
 * key[_len]: settings key (must be non-null)
 */
coaster_rc
coaster_settings_remove(coaster_settings *settings,
            const char *key, size_t key_len) COASTER_THROWS_NOTHING;

/*
 * Get settings individually.
 * value[_len]: set to value of string, null if not present in settings.
 *      Settings retains ownership of strings: any subsequent
 *      modifications to settings may invalidate the strings.
 */
coaster_rc
coaster_settings_get(coaster_settings *settings,
            const char *key, size_t key_len,
            const char **value, size_t *value_len) COASTER_THROWS_NOTHING;

/*
 * Enumerate settings.
 * keys: output array of constant strings.  Array must be freed by
 *      callee.  Settings retains ownership of strings: any subsequent
 *      modifications to settings may invalidate the strings.
 * key_lens: optional output array for string lengths.
 * count: number of keys in output array
 */
coaster_rc
coaster_settings_keys(coaster_settings *settings,
                      const char ***keys, size_t **key_lens, int *count)
                                COASTER_THROWS_NOTHING;

/*
 * Apply settings to started coaster client.
 * TODO: currently it isn't safe to free settings until client is shut
 *       down
 *
 * config: set to identifer for this service config, must be freed with
 *     coaster_free_config_id
 */
coaster_rc
coaster_apply_settings(coaster_client *client,
        coaster_settings *settings, coaster_config_id **config)
                                  COASTER_THROWS_NOTHING;

coaster_rc
coaster_config_id_free(coaster_config_id *config) COASTER_THROWS_NOTHING;

/*
 * Create a new coaster job for later submission.
 * Some standard arguments can be specified now, or left as NULL to be
 * initialized later.
 *
 * executable: must be provided, name of executable
 * argc/argv: command line arguments
 * job_manager: Name of Coaster job manager to use (can be NULL)
 * job: output, filled with pointer to new job
 */
coaster_rc
coaster_job_create(const char *executable, size_t executable_len,
                  int argc, const char **argv, const size_t *arg_lens,
                  const char *job_manager, size_t job_manager_len,
                  coaster_job **job) COASTER_THROWS_NOTHING;

/*
 * Free a coaster job
 */
coaster_rc
coaster_job_free(coaster_job *job) COASTER_THROWS_NOTHING;

/*
 * Create a human readable string describing job.
 * str: output for dynamically allocated string, to be freed by caller
 */
coaster_rc
coaster_job_to_string(const coaster_job *job, char **str, size_t *str_len)
                                  COASTER_THROWS_NOTHING;

/*
 * Set input and output stream redirections.
 * If set to NULL, don't modify.
 */
coaster_rc
coaster_job_set_redirects(coaster_job *job,
      const char *stdin_loc, size_t stdin_loc_len,
      const char *stdout_loc, size_t stdout_loc_len,
      const char *stderr_loc, size_t stderr_loc_len)
                  COASTER_THROWS_NOTHING;

/*
 * Set job directory.
 * If dir is NULL, no effect
 */
coaster_rc
coaster_job_set_directory(coaster_job *job, const char *dir, size_t dir_len)
                  COASTER_THROWS_NOTHING;

/*
 * Add environment variables for the job.  Will overwrite any
 * previous values if names match.
 * name and value strings should not be NULL.
 */
coaster_rc
coaster_job_set_envs(coaster_job *job, int nvars,
        const char **names, size_t *name_lens,
        const char **values, size_t *value_lens) COASTER_THROWS_NOTHING;

/*
 * Add attributes for the job.  Will overwrite any previous atrributes
 * if names match.
 * name and value strings should not be NULL.
 */
coaster_rc
coaster_job_set_attrs(coaster_job *job, int nattrs,
        const char **names, size_t *name_lens,
        const char **values, size_t *value_lens) COASTER_THROWS_NOTHING;

/*
 * Add cleanups for job.
 */
coaster_rc
coaster_job_add_cleanups(coaster_job *job, int ncleanups,
        const char **cleanups, size_t *cleanup_lens)
        COASTER_THROWS_NOTHING;

/*
 * Add staging set entries for job
 */
coaster_rc
coaster_job_add_stages(coaster_job *job,
    int nstageins, coaster_stage_entry *stageins,
    int nstageouts, coaster_stage_entry *stageouts)
        COASTER_THROWS_NOTHING;

/*
 * Get local job ID.  The job ID is a locally unique identifier for
 * a coaster job that is assigned when the job is created.
 */
coaster_job_id 
coaster_job_get_id(const coaster_job *job) COASTER_THROWS_NOTHING;

/*
 * Get status of a submitted job.
 * Return COASTER_ERROR_INVALID if job is invalid or has no status.
 */
coaster_rc
coaster_job_status_code(const coaster_job *job, coaster_job_status *code)
                COASTER_THROWS_NOTHING;

/*
 * Get stdin/out of completed job.
 * Pointers set to NULL and lengths to 0 if not available.  Strings
 * are owned by job: will become invalid if job is freed.
 */
coaster_rc
coaster_job_get_outstreams(const coaster_job *job,
                const char **stdout_s, size_t *stdout_len,
                const char **stderr_s, size_t *stderr_len)
                COASTER_THROWS_NOTHING;

/*
 * Submit a coaster job through a coaster client.
 * A job can only be submitted once!
 * Ownership of the job is shared between the caller and
 * the client until the job has completed, or the client
 * shuts down.
 *
 * config: required, must be non-null
 */
coaster_rc
coaster_submit(coaster_client *client, const coaster_config_id *config,
               coaster_job *job) COASTER_THROWS_NOTHING;

/*
 * Check for completion of jobs.
 *
 * wait: if true, don't return until at least one job completes
 * maxjobs: maximum number of jobs to return
 * jobs: output array large enough to hold maxjobs
 * njobs: output for number of jobs returned in jobs
 */
coaster_rc
coaster_check_jobs(coaster_client *client, bool wait, int maxjobs,
                   coaster_job **jobs, int *njobs)
                COASTER_THROWS_NOTHING;

/*
 * Get name of return code.  Returns NULL if invalid code.
 */
const char *coaster_rc_string(coaster_rc code);

/*
 * Get additional information about last returned error in current
 * thread.
 * returns: error message, NULL if no information available.  The
 *          returned string is valid until the next call by this thread
 *          to a coaster API function.
 */
const char *coaster_last_err_info(void);

coaster_rc coaster_set_log_threshold(coaster_log_level threshold);

#ifdef __cplusplus
} // extern "C"
#endif
#endif // COASTER_H__H_
