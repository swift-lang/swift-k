package com.thoughtworks.xstream.ext;

public final class IntQueue {

    private final int[] data;
    private int writePointer = 0;
    private int readPointer = 0;
    private boolean empty = true;
    private int mark;

    public IntQueue(int size) {
        data = new int[size];
    }

    public void write(int value) {
        if (!empty && writePointer == readPointer) {
            throw new OverflowException();
        }
        data[writePointer++] = value;
        if (writePointer == data.length) {
            writePointer = 0;
        }
        empty = false;
    }

    public int read() {
        if (empty) {
            throw new NothingToReadException();
        }
        int result = data[readPointer++];
        if (readPointer == data.length) {
            readPointer = 0;
        }
        if (readPointer == writePointer) {
            empty = true;
        }
        return result;
    }
    
    public void mark() {
    	mark = readPointer;
    }
    
    public void reset() {
    	readPointer = mark;
    	empty = (readPointer == writePointer);
    }

    public boolean isEmpty() {
        return empty;
    }

    public static class OverflowException extends RuntimeException {
    }

    public static class NothingToReadException extends RuntimeException {
    }

}
