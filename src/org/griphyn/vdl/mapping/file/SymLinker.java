//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2015
 */
package org.griphyn.vdl.mapping.file;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SymLinker {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    private static Object EMPTY_FILE_ATTR_ARRAY;
    private static boolean canSymLink;
    private static Class<?> clsPaths;
    private static Class<?> clsFiles;
    private static Method methodPathsGet;
    private static Method methodFilesSymLink;
    
    static {
        if (init()) {
            canSymLink = symLinkTest();
        }
        else {
            canSymLink = false;
        }
    }
    
    private static synchronized boolean init() {
        String spec = System.getProperty("java.specification.version");
        // assume digit.digit
        try {
            if (spec.length() == 3) {
                int major = Integer.parseInt(spec.substring(0, 1));
                int minor = Integer.parseInt(spec.substring(2, 3));
                boolean canSymLink = (major > 1) || (major == 1 && minor >= 7);
                if (!canSymLink) {
                    return false;
                }
            }
        }
        catch (Exception e) {
            return false;
        }
        try {
            Class<?> clsFileAttribute = Class.forName("java.nio.file.attribute.FileAttribute");
            Class<?> clsFileAttributeArray = Class.forName("[Ljava.nio.file.attribute.FileAttribute;");
            EMPTY_FILE_ATTR_ARRAY = Array.newInstance(clsFileAttribute, 0);
            Class<?> clsPath = Class.forName("java.nio.file.Path");
            clsPaths = Class.forName("java.nio.file.Paths");
            clsFiles = Class.forName("java.nio.file.Files");
            methodPathsGet = clsPaths.getMethod("get", new Class[] {String.class, String[].class});
            methodFilesSymLink = clsFiles.getMethod("createSymbolicLink", new Class[] {clsPath, clsPath, clsFileAttributeArray});
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    public static boolean canSymLink() {
        return canSymLink;
    }

    private static boolean symLinkTest() {
        try {
            File f = File.createTempFile("symlinktest", "tmp");
            try {
                File g = File.createTempFile("symlinktest", "tmp");
                try {
                    g.delete();
                    symLink(f.getAbsolutePath(), g.getAbsolutePath());
                    return true;
                }
                catch (Exception e) {
                    return false;
                }
                finally {
                    g.delete();
                }
            }
            finally {
                f.delete();
            }
        }
        catch (Exception e) {
            return false;
        }
    }

    public static void symLink(String file, String link) throws IOException {
        Object filePath = getPath(file);
        Object linkPath = getPath(link);
        createLink(filePath, linkPath);
    }

    private static void createLink(Object file, Object link) throws IOException {
        try {
            methodFilesSymLink.invoke(null, new Object[] {link, file, EMPTY_FILE_ATTR_ARRAY});
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(e);
        }
        catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof UnsupportedOperationException) {
                throw (UnsupportedOperationException) t;
            }
            else if (t instanceof IOException) {
                throw (IOException) t;
            }
            else if (t instanceof SecurityException) {
                throw new UnsupportedOperationException(t);
            }
            else if (t instanceof RuntimeException) {
                // expect FileAlreadyExistsException
                throw new IOException(t);
            }
            else {
                throw new UnsupportedOperationException(t);
            }
        }
    }

    private static Object getPath(String file) throws IOException {
        try {
            return methodPathsGet.invoke(null, new Object[] {file, EMPTY_STRING_ARRAY});
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(e);
        }
        catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof RuntimeException) {
                // expect InvalidPathException
                throw new IOException(t);
            }
            else {
                throw new UnsupportedOperationException(t);
            }
        }
    }
}
