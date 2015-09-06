package com.blnz.fxpl.fs.impl;

import java.io.File;
import java.io.IOException;

import com.blnz.fxpl.util.ConfigProps;


/**
 * repository connection to a local filesystem
 */
public class FSConnection 
{
    private static File _path = null;
    
    private FSConnection() {}
    
    /** 
     * 
     */
    public static void initFSRepositoryPath()
	throws Exception
    {
        initFSRepositoryPath(null);
    }
    
    /** 
     * 
     */
    public static void initFSRepositoryPath(String path)
	throws Exception
    {
        System.out.println("FSConnection:initFSRepositoryPath( " + path + " ) entry");
        if (path == null || path.length() == 0) {
            path =  ConfigProps.getProperty("com.blnz.fxpl.fs.path" , 
                                            "");
            if (!path.startsWith("/") && path.indexOf(':') == -1) {
                String echoHome = ConfigProps.getProperty("fxpl.home");
                System.out.println("FSConnection:initFSRepositoryPath( " + echoHome + " ) echoHome");
                if (echoHome == null) {
                    echoHome = System.getProperty("user.dir");
                    System.out.println("FSConnection:initFSRepositoryPath( " + echoHome + " ) user.dir");
                }
                File echoURL = new File(echoHome);
                path = new File(echoURL, path).getAbsolutePath();
            }
    	}
        _path = new File(path).getCanonicalFile();
        if (!_path.exists()) {
            throw new IOException("filesystem repository path not found: " + _path);
        } else if (!_path.isDirectory()) {
            throw new IOException("filesystem repository is not a directory: " + _path);
        }
    }
    
    /** 
     * 
     */
    public static File getResource(String path)
    {
        return new File(_path, path);
    }
    
}
