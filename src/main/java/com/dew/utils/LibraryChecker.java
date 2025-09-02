package com.dew.utils;

import java.io.File;

public class LibraryChecker {

    public static boolean isLibraryLoaded(String name) {
        try {
            String path = System.getProperty("java.library.path");
            if (path != null) {
                String mapped = System.mapLibraryName(name);
                String[] parts = path.split(File.pathSeparator);
                for (String libPath : parts) {
                    File file = new File(libPath, mapped);
                    if (file.exists()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}