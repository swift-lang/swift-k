package com.thoughtworks.xstream.converters;

import java.util.Collections;
import java.util.Iterator;

import com.thoughtworks.xstream.core.BaseException;

/**
 * Thrown by {@link Converter} implementations when they cannot convert an object
 * to/from textual data.
 *
 * When this exception is thrown it can be passed around to things that accept an
 * {@link ErrorWriter}, allowing them to add diagnostics to the stack trace.
 *
 * @author Joe Walnes
 *
 * @see ErrorWriter
 */
public class ConversionException extends BaseException implements ErrorWriter {
    public ConversionException(String msg, Exception cause) {
        super(msg, cause);
    }

    public ConversionException(String msg) {
        super(msg);
    }

    public ConversionException(Exception cause) {
        this(cause.getMessage(), cause);
    }

    public String get(String errorKey) {
        return null;
    }

    public void add(String name, String information) {
    }

    public Iterator keys() {
        return Collections.EMPTY_SET.iterator();
    }

    public String getShortMessage() {
        return super.getMessage();
    }
}
