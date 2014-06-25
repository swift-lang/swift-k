/*
 * ClientHandlerFactory.h
 *
 *  Created on: Aug 31, 2012
 *      Author: mike
 */

#ifndef CLIENTHANDLERFACTORY_H_
#define CLIENTHANDLERFACTORY_H_

#include "HandlerFactory.h"

namespace Coaster {

class ClientHandlerFactory: public HandlerFactory {
	public:
		ClientHandlerFactory();
		virtual ~ClientHandlerFactory();
};

}

#endif /* CLIENTHANDLERFACTORY_H_ */
