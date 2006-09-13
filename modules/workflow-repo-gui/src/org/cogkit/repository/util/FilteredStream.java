package org.cogkit.repository.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FilteredStream extends FilterOutputStream {
    
    public StringBuffer strBuff = new StringBuffer();
    public FilteredStream(OutputStream aStream, StringBuffer sb) {
       super(aStream);
       strBuff = strBuff.append(sb);  
     }

    public void write(byte b[]) throws IOException {
       String aString = new String(b);
       strBuff.append(aString);
     }

    public void write(byte b[], int off, int len) throws IOException {
       String aString = new String(b , off , len);
       strBuff.append(aString);
     }
    
    public String getString(){
        return strBuff.toString();
    }
    
    public void flush() {
        try {
            super.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.flush();
    }
    
}


