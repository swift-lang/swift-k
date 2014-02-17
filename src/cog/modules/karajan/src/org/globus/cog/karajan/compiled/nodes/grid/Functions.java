//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 9, 2005
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.globus.cog.karajan.util.BoundContact;

public class Functions {

	public static class HostHasService extends AbstractSingleValuedFunction {
		private ArgRef<BoundContact> host;
		private ArgRef<String> type;
		private ArgRef<String> provider;

		@Override
		protected Param[] getParams() {
			return params("host", "type", "provider");
		}

		@Override
		public Object function(Stack stack) {
			BoundContact host = this.host.getValue(stack);
			String type = this.type.getValue(stack);
			String provider = this.provider.getValue(stack);
			if (host.hasService(BoundContact.getServiceType(type), provider)) {
				return true;
			}
			return false;
		}
		
	}
	
	public static class ServiceURI extends AbstractSingleValuedFunction {
		private ArgRef<BoundContact> host;
		private ArgRef<String> type;
		private ArgRef<String> provider;

		@Override
		protected Param[] getParams() {
			return params("host", "type", "provider");
		}

		@Override
		public Object function(Stack stack) {
			BoundContact host = this.host.getValue(stack);
			String type = this.type.getValue(stack);
			String provider = this.provider.getValue(stack);
			return host.getService(BoundContact.getServiceType(type), provider).getServiceContact().getContact();
		}
		
	}
}
