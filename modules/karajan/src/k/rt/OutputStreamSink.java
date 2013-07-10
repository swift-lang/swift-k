//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 2, 2013
 */
package k.rt;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamSink extends Sink<Object> {
	private OutputStream os;
	
	public OutputStreamSink(OutputStream os) {
		this.os = os;
	}

	@Override
	public boolean add(Object value) {
		try {
			os.write(String.valueOf(value).getBytes());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}
}
