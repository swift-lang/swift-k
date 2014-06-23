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
 * coasters.h
 *
 * Created: Jun 18, 2014
 *    Author: Tim Armstrong
 *
 * Pure C interface for Coasters
 */

#ifndef COASTERS_H_
#define COASTERS_H_

#ifdef __cplusplus
extern "C" {
#endif

#ifdef __cplusplus
#include <cstddef>
#else
#include <stddef.h>
#endif

#include <stdint.h>

// Opaque pointer types
typedef struct coaster_client coaster_client;
typedef struct coaster_settings coaster_settings;
typedef struct coaster_job coaster_job;

/*
 * Return codes for coaster errors
 * TODO: way to pass back error messages?
 */
typedef enum {
  COASTER_SUCCESS,
  COASTER_ERROR_INVALID,
  COASTER_ERROR_OOM,
  COASTER_ERROR_NETWORK,
  COASTER_ERROR_UNKNOWN,
} coaster_rc;

// Set appropriate macro to specify that we shouldn't throw exceptions
// Only used in this header: undefine later
#ifdef __cplusplus
#define COASTERS_THROWS_NOTHING throw()
#else
#define COASTERS_THROWS_NOTHING
#endif

/*
 * Start a new coasters client.
 * NOTE: don't support multiple clients per loop with this interface
 *
 * service_url[_len]: coasters service URL string with string length
 * client: output for new client
 */
coaster_rc
coaster_client_start(const char *service_url, size_t service_url_len,
                    coaster_client **client) COASTERS_THROWS_NOTHING;

/*
 * Stop coasters client and free memory.
 */
coaster_rc
coaster_client_stop(coaster_client *client) COASTERS_THROWS_NOTHING;

/*
 * Create empty settings object
 * settings: coaster settings object, should be freed with
             coaster_settings_free
 */
coaster_rc
coaster_settings_create(coaster_settings **settings)
                    COASTERS_THROWS_NOTHING;

/*
 * Parse settings from string.
 *
 * str[_len]: String with key/value settings and length of string.
      Settings separated by commas, key/values separated by equals sign.
      If NULL, will create empty settings object.
 */
coaster_rc
coaster_settings_parse(coaster_settings *settings, const char *str,
                       size_t str_len) COASTERS_THROWS_NOTHING;
/*
 * Set settings individually.
 */
coaster_rc
coaster_settings_set(coaster_settings *settings,
            const char *key, size_t key_len,
            const char *value, size_t value_len) COASTERS_THROWS_NOTHING;
/*
 * Get settings individually.
 * value[_len]: set to value of string, null if not present in settings.
 *      Settings retains ownership of strings: any subsequent
 *      modifications to settings may invalidate the strings.
 */
coaster_rc
coaster_settings_get(coaster_settings *settings,
            const char *key, size_t key_len,
            const char **value, size_t *value_len) COASTERS_THROWS_NOTHING;

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
                                COASTERS_THROWS_NOTHING;

void
coaster_settings_free(coaster_settings *settings)
                                COASTERS_THROWS_NOTHING;

/*
 * Apply settings to started coasters client.
 */
coaster_rc
coaster_apply_settings(coaster_client *client,
                                  coaster_settings *settings)
                                  COASTERS_THROWS_NOTHING;

/*
 * Create a new coasters job for later submission.
 * Some standard arguments can be specified now, or left as NULL to be
 * initialized later.
 *
 * executable: must be provided, name of executable
 * argc/argv: command line arguments
 * job_manager: Name of Coasters job manager to use (can be NULL)
 * job: output, filled with pointer to new job
 */
coaster_rc
coaster_job_create(const char *executable, size_t executable_len,
                  int argc, const char **argv, const size_t *arg_lens,
                  const char *job_manager, size_t job_manager_len,
                  coaster_job **job) COASTERS_THROWS_NOTHING;

/*
 * Free a coasters job
 */
coaster_rc
coaster_job_free(coaster_job *job) COASTERS_THROWS_NOTHING;

/*
 * Set input and output streams redirections.
 * If set to NULL, don't modify.
 */
coaster_rc
coaster_job_set_redirects(coaster_job *job,
      const char *stdin_loc, size_t stdin_loc_len,
      const char *stdout_loc, size_t stdout_loc_len,
      const char *stderr_loc, size_t stderr_loc_len)
                  COASTERS_THROWS_NOTHING;

/*
 * Set job directory.
 * If dir is NULL, no effect
 */
coaster_rc
coaster_job_set_directory(coaster_job *job, const char *dir, size_t dir_len)
                  COASTERS_THROWS_NOTHING;

/*
 * Add environment variables for the job.  Will overwrite any
 * previous values if names match.
 * name and value strings should not be NULL.
 */
coaster_rc
coaster_job_set_envs(coaster_job *job, int nvars,
        const char **names, size_t *name_lens,
        const char **values, size_t *value_lens) COASTERS_THROWS_NOTHING;

/*
 * Add attributes for the job.  Will overwrite any previous atrributes
 * if names match.
 * name and value strings should not be NULL.
 */
coaster_rc
coaster_job_set_attrs(coaster_job *job, int nattrs,
        const char **names, size_t *name_lens,
        const char **values, size_t *value_lens) COASTERS_THROWS_NOTHING;

// TODO: functions for setting stageins, stageouts, cleanups

/*
 * Get local job ID string.
 * Return value points to job state: will be invalid if job is freed.
 *
 * id_len: string length of id
 */
int64_t 
coaster_job_get_id(coaster_job *job) COASTERS_THROWS_NOTHING;

/*
 * Submit a coasters job
 */
coaster_rc
coaster_submit(coaster_client *client, coaster_job *job)
                COASTERS_THROWS_NOTHING;

/*
 * Get name of return code.  Returns NULL if invalid code.
 */
const char *coaster_rc_string(coaster_rc code);

#ifdef __cplusplus
} // extern "C"
#endif
#endif // COASTERS_H_
