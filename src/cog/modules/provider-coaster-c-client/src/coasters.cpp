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
 * TODO: exception handling?
 */

#include <cstdlib>

extern "C" {
#include "coasters.h"
}

#include "CoasterClient.h"
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

coaster_client *coaster_client_start(const char *serviceURL) {
  coaster_client *client = (coaster_client*)malloc(sizeof(coaster_client));
  if (client == NULL) {
    return NULL;
  }

  // Use placement new to store directly in struct
  new (&client->loop) CoasterLoop();
  new (&client->client) CoasterClient(serviceURL, client->loop);

  client->client.start();
  return client;
}

void coaster_client_stop(coaster_client *client) {
  // TODO: stop things
  
  client->client.stop();
  client->loop.stop();

  // Call destructors manually before freeing memory
  client->client.~CoasterClient();
  client->loop.~CoasterLoop();
  free(client);
}
