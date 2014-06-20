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

// Opaque pointer type
typedef struct coaster_client coaster_client;

/*
 * Start a new coasters client.
 * NOTE: don't support multiple loops per channel with this interface
 */
coaster_client *coaster_client_start(const char *serviceURL);

/*
 * Stop coasters client and free memory
 */
void coaster_client_stop(coaster_client *client);


#endif // COASTERS_H_
