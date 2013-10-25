#include <iostream>
#include <stdlib.h>
#include <string>
#include <sstream>
#include <vector>

#include <CoasterSWIG.h>
#include "CoasterLoop.h"
#include "CoasterClient.h"
#include "Settings.h"
#include "Job.h"
using namespace std;

/** CoasterSWIGLoopCreate : create, starts and returns
 *  a pointer to a CoasterLoop object.
 */
CoasterLoop* CoasterSWIGLoopCreate(void)
{
    cout << "CoasterSWIGLoopCreate()..." << endl;
    CoasterLoop *loop = new CoasterLoop();
    loop->start();
    return loop;
}

int CoasterSWIGLoopDestroy(CoasterLoop* loop)
{
    delete(loop);
    return 0;
}

CoasterClient* CoasterSWIGClientCreate(CoasterLoop &loop, char* serviceURL)
{
    cout << "CoasterSWIGClientCreate(" << serviceURL << ")..." << endl;
    CoasterClient* client = new CoasterClient(serviceURL, loop);
    return client;
}

int CoasterSWIGClientDestroy(CoasterClient *client)
{
    delete(client);
    return 0;
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
    client->setOptions(s);
    return 0;
}
