//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 3, 2014
 */
package k.rt;

import org.globus.cog.karajan.analyzer.CompilerSettings;

public class DebugFrame extends Frame {
	private final Object owner;
	private String[] names;

	public DebugFrame(Object owner, int count, Frame prev) {
		super(count, prev);
		if (CompilerSettings.DEBUG) {
			names = new String[count];
		}
		this.owner = owner;
	}
	
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(owner);
        sb.append(":");
        for (int i = 0; i < a.length; i++) {
            sb.append("\n\t");
            sb.append(i);
            sb.append(" - ");
            if (CompilerSettings.DEBUG) {
                if (names[i] != null) {
                    sb.append(names[i]);
                    sb.append(" = ");
                }
            }
            String s = str(a[i]);
            if (s.length() > 32) {
                sb.append(s.subSequence(0, 24));
                sb.append("... ");
                sb.append(s.subSequence(s.length() - 8, s.length()));
            }
            else {
                sb.append(s);
            }
        }
        return sb.toString();
    }
	
	public void setName(int index, String name) {
        names[index] = name;
    }
}
