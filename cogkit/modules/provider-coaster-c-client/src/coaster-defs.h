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
 * coaster-defs.h
 *
 * Created: July 9, 2014
 *    Author: mike, Tim Armstrong
 *
 * Definitions shared between C and C++ code.
 */

#ifndef __COASTER_DEFS_H
#define __COASTER_DEFS_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

/* Type to represent internal job ids */
typedef int64_t coaster_job_id;

typedef enum CoasterJobStatusCode {
  COASTER_STATUS_UNSUBMITTED = 0,
  COASTER_STATUS_SUBMITTING = 8,
  COASTER_STATUS_SUBMITTED = 1,
  COASTER_STATUS_ACTIVE = 2,
  COASTER_STATUS_SUSPENDED = 3,
  COASTER_STATUS_RESUMED = 4,
  COASTER_STATUS_FAILED = 5,
  COASTER_STATUS_CANCELED = 6,
  COASTER_STATUS_COMPLETED = 7,
  COASTER_STATUS_STAGE_IN = 16,
  COASTER_STATUS_STAGE_OUT = 17,
  COASTER_STATUS_UNKNOWN = 9999
} coaster_job_status_code;

typedef enum CoasterStagingMode {
  COASTER_STAGE_ALWAYS = 1,
  COASTER_STAGE_IF_PRESENT = 2,
  COASTER_STAGE_ON_ERROR = 4,
  COASTER_STAGE_ON_SUCCESS = 8
} coaster_staging_mode;

typedef enum CoasterLogLevel {
  COASTER_LOG_NONE = -1,
  COASTER_LOG_DEBUG = 0,
  COASTER_LOG_INFO = 1,
  COASTER_LOG_WARN = 2,
  COASTER_LOG_ERROR = 3,
  COASTER_LOG_FATAL = 4
} coaster_log_level;

#ifdef __cplusplus
} // extern "C"
#endif

#endif // __COASTER_DEFS_H
