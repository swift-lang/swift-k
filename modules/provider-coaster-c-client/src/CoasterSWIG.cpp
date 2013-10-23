
#include <iostream>

#include <CoasterSWIG.h>

using namespace std;

CoasterClient* CoasterSWIGClientCreate(char* serviceURL)
{
  cout << "CoasterSWIGClientCreate(" << serviceURL << ")..." << endl;
  CoasterLoop loop;
  loop.start();
  CoasterClient* result = new CoasterClient(serviceURL, loop);
}

