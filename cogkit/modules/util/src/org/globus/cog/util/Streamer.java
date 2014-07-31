package org.globus.cog.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streamer extends Thread {
    InputStream istream;
    OutputStream ostream;
    
    int chunk = 64*1024;
    
    enum Status {
        UNSUBMITTED, 
        ACTIVE,  
        COMPLETED, 
        FAILED
    }
    
    Status status = Status.UNSUBMITTED;
    
    public Streamer(InputStream istream, OutputStream ostream) { 
        this.istream = istream;
        this.ostream = ostream;
        
        setName("Streamer");
    }
    
    public void run() {
        status = Status.ACTIVE;
        
        byte[] buffer = new byte[chunk];
        
        BufferedInputStream bis = new BufferedInputStream(istream);
        BufferedOutputStream bos = new BufferedOutputStream(ostream);
        
        int actual = 0;
        try {
            while ((actual = bis.read(buffer, 0, chunk)) != -1) 
                bos.write(buffer, 0, actual);
            bos.flush();
        }
        catch (IOException e) {
            status = Status.FAILED;
            e.printStackTrace();
        }
        status = Status.COMPLETED;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public boolean isTerminal() {
        return (status == Status.COMPLETED || 
                status == Status.FAILED);
    }
}
