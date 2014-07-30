/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 *
 * Copyright 2013-2014 University of Chicago
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#include <iostream>
#include <string>
#include <sstream>
#include <vector>
#include <typeinfo>

//#include <list>
//#include <iostream>

//#include <algorithm>
//#include <iterator>


using namespace std;


std::vector<std::string> split(const std::string &s, char delim) {
    std::vector<std::string> elems;
    std::stringstream ss(s);
    std::string item;
    while (std::getline(ss, item, delim)) {
        elems.push_back(item);
        std::stringstream kv(item);
        std::string kv_item;
        std::getline(kv, kv_item, '=');
        cout << "Key : " << kv_item << endl;
        std::getline(kv, kv_item);
        cout << "Value : " << kv_item << endl;
    }
    return elems;
}

int main (void)
{
    std::vector<std::string> settings = split("SLOTS=1,JOBS_PER_NODE=2", ',');
    return 0;
}


/*
Instead of copying the extracted tokens to an output stream, one could insert them into a container, using the same generic copy algorithm.

 vector<string> tokens;
copy(istream_iterator<string>(iss),
     istream_iterator<string>(),
     back_inserter<vector<string> >(tokens));

*/

/*The settings string will be of the format
 * key1=v1,key2=v2
    std::vector<std::string> pair;
    for(std::vector<std::string>::iterator it = elems.begin(); it != elems.end(); ++it) {
        pair << *it;
        cout << *it <<" Type : " << typeid(pair).name() << endl;

    }
    return elems;

 */
