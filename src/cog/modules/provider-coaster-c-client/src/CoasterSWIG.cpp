#include <iostream>
#include <stdlib.h>
#include <string>
#include <sstream>
#include <vector>

#include <CoasterSWIG.h>

using namespace std;

CoasterLoop* CoasterSWIGLoopCreate(void)
{
    cout << "CoasterSWIGLoopCreate()..." << endl;
    CoasterLoop *loop = new CoasterLoop();
    loop->start();
    return loop;
}

CoasterClient* CoasterSWIGClientCreate(char* serviceURL)
{
  cout << "CoasterSWIGClientCreate(" << serviceURL << ")..." << endl;
  CoasterLoop loop;
  loop.start();
  CoasterClient* result = new CoasterClient(serviceURL, loop);
}

int CoasterSWIGClientSettings(CoasterClient *client, char *settings)
{
    Settings s;
    cout << "CoasterSWIGClientSettings(" << settings << ")" <<endl;
    cout << "Client : [" << (void *) client << "]"<<endl;
    // Parsing logic
    // K1=V1, K2=V2 is the format of the settings string

    std::vector<std::string> elems;
    std::stringstream ss(settings);
    std::string item, key, value;

    while (std::getline(ss, item, ',')) {
        elems.push_back(item);
        std::stringstream kv(item);
        std::string kv_item;
        std::getline(kv, kv_item, '=');
        key = kv_item;
        std::getline(kv, kv_item);
        value = kv_item;
        s.set(key, value);
        cout << "Key,Value : " << key <<", "<<value << endl;
    }

    //TODO: SegFaulting here. Why ?
    cout << "client->getURL() : " << client->getURL() << endl ;
    //client->setOptions(s);
    return 0;
}
