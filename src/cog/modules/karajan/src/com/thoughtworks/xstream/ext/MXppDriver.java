package com.thoughtworks.xstream.ext;

import java.io.Reader;

import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public class MXppDriver implements HierarchicalStreamDriver {

    private static boolean xppLibraryPresent;

    public HierarchicalStreamReader createReader(Reader xml) {
        if (!xppLibraryPresent) {
            try {
                Class.forName("org.xmlpull.mxp1.MXParser");
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("XPP3 pull parser library not present. Specify another driver." +
                        " For example: new XStream(new DomDriver())");
            }
            xppLibraryPresent = true;
        }
        return new MXppReader(xml);
    }
}
