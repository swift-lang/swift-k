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

#include <cstdlib>

extern "C" {
#include "coasters.h"
}

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
};

static coaster_rc coaster_error_rc(const CoasterError &err);
static coaster_rc exception_rc(const std::exception &ex);

coaster_rc coaster_client_start(const char *serviceURL,
                                coaster_client **client)
                                COASTERS_THROWS_NOTHING {
  try {
    *client = (coaster_client*)malloc(sizeof(coaster_client));
    if (!(*client)) {
      return COASTER_ERROR_OOM;
    }

    // Use placement new to store directly in struct
    new (&(*client)->loop) CoasterLoop();
    new (&(*client)->client) CoasterClient(serviceURL, (*client)->loop);

   (*client)->client.start();
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }
 
 return COASTER_SUCCESS;
}

coaster_rc coaster_client_stop(coaster_client *client)
                               COASTERS_THROWS_NOTHING {
  try {
    // TODO: stop things
    
    client->client.stop();
    client->loop.stop();

    // Call destructors manually before freeing memory
    client->client.~CoasterClient();
    client->loop.~CoasterLoop();
    free(client);
  } catch (const CoasterError& err) {
    return coaster_error_rc(err);
  } catch (const std::exception& ex) {
    return exception_rc(ex);
  }

  return COASTER_SUCCESS;
}

/*
 * Set error information and return appropriate code
 */
static coaster_rc coaster_error_rc(const CoasterError &err)
{
  // TODO: store detailed error info
  // TODO: distinguish different cases?
  return COASTER_ERROR_UNKNOWN;
}

static coaster_rc exception_rc(const std::exception &ex)
{
  // TODO: store error info?
  return COASTER_ERROR_UNKNOWN;
}
