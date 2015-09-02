package com.blnz.fxpl.util;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.log.Logger;
import java.util.Random;
import java.io.File;

/**
 * Provides locations in the file system.
 */
public class Files
{
    // switch to turn on/off the debugging stream
    private static boolean DEBUG = false;

    private String _rootPath;

    private String _fullTextIndexPath;
    private String _xfsPath;
    private String _dtdPath;
    private String _cachePath;
    private String _tempPath;

    protected static Logger logger = null;  // a place to report exceptions

    /**
     * have a protected constructor so that no one outside the package
     * can new IrisFileProxy.  the only way to get a file proxy is thru 
     * resource broker.
     */
    protected Files()
    {

        // FIXME: new property names

        logger = Log.getLogger();
	String prop = "Files.rootPath";
	_rootPath = ConfigProps.getProperty(prop, ".");

	prop = "Files.xfsSubpath";
	String xfsSubpath = ConfigProps.getProperty(prop, "xfs");
	_xfsPath = _rootPath + File.separator + xfsSubpath;

	prop = "com.snapbridge.fed.broker.IrisFileProxy.cacheSubpath";
	String cacheSubpath = ConfigProps.getProperty(prop, "cache");
	_cachePath = _rootPath + File.separator + cacheSubpath;

	prop = "com.snapbridge.fed.broker.IrisFileProxy.tempSubpath";
	String tempSubpath = ConfigProps.getProperty(prop, "tmp");
	_tempPath = _rootPath + File.separator + tempSubpath;

	debug("_rootPath=" + _rootPath);
	debug("_xfsPath=" + _xfsPath);

	debug("_cachePath=" + _cachePath);
	debug("_tempPath=" + _tempPath);
    }
    
    /** 
     * get the root path of the server 
     */
    public String getRootPath() 
    {
	return _rootPath;
    }
    
    /**
     * @return the path of directory
     */
    public String getTextIndexDir()
    {
	return _fullTextIndexPath;
    }

    /**
     * @return the path of the XFS file system image
     */
    public String getXfsDir()
    {
        return _xfsPath;
    }

    /**
     * @return temporary directory in the file system.
     *
     * temp dir is in $rootPath/$tempSubpath.
     */
    public String getTempDir()
    {
	// create a temp dir if it does not exist
	File dir = new File(_tempPath);
	if (!dir.exists()) {
	    dir.mkdir();
	}
	return _tempPath;
    }


    /**
     * construct a new, unique File in the tmp directory
     */
    public File createTempFile()
    {
        return new File(getTempDir() + 
                        File.separator + 
                        createUniqueFilename("x"));
    }


    /**
     * create a new, uniquely named directory for temporary usage
     * by a single thread
     * @param local any string -- will become part of the dirname
     */
    public File createNewTempDir(String local)
    {
        String tmpRoot = getTempDir();
        Random rand = new Random();
        int value = rand.nextInt();
        String tmpDir = tmpRoot + File.separator + local + "_" + value;
        File tempdir = new File(tmpDir);
        if (tempdir.exists()) {
            tempdir.delete();
        }
        tempdir.mkdir(); 
        return tempdir;
    }


    /**
     * @return the path of the DTD directory. 
     *
     * the dtd base path is in $rootPath/dtds.
     */
    public String getDtdBase()
    {
	return _dtdPath;
    }

    /**
     * @return the path of the query cache directory.  it stores cached
     *         files for all canned queries.
     */
    public String getCacheDir()
    {
	// create a cache dir if it does not exist
	File dir = new File(_cachePath);
	if (!dir.exists()) {
	    dir.mkdir();
	}

	return _cachePath;
    }

    /**
     * construct a phony fiile name that is unique (almost).  the format is:
     * user id + "_" + current time in millisecond + "_" + random float number
     *
     * @param userId user id
     * @return a unique file name
     */
    public static synchronized String createUniqueFilename(String userId)
    {
	// use the name of the current thread (which is unique) to create
	// a unique seed
	int seed = Thread.currentThread().getName().hashCode();
	// debug("seed=" + seed);
	Random random = new Random(seed);
	
	if (userId == null) {
	    // if no user id is given, fake one.
	    userId = "" + random.nextInt();
	}

	String filename = userId + "_" + System.currentTimeMillis() + 
            "_" + random.nextFloat();

	return filename;
    }

    /**
     *  delete all files in the cache dir 
     */
    public void clearCacheDir()
    {
	File dir = new File(getCacheDir());
	if (!dir.exists()) {
	    return;
	}

	File[] files = dir.listFiles();
	for (int i = 0; i < files.length; i++) {
	    files[i].delete();
	}
    }

    /**
     * delete a directory and all of its included files.
     *
     * @param  path  directory path
     */
    public static void deleteDir(String path) throws Exception
    {
	File dir = new File(path);
	if (!dir.exists() || !dir.isDirectory()) {
	    return;
	}

	// remove everything in the directory.  can be recursive.
	File[] files = dir.listFiles();
	for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                deleteDir(files[i].getAbsolutePath());
            } else {
		debug("Deleting " + files[i].getAbsolutePath());
                if (!files[i].delete()) {
                    throw new Exception("Unable to delete file " + files[i].getAbsolutePath());
                }
            }
	}

	// remove the directory itself
        debug("Deleting directory " + dir.getAbsolutePath());
	if (!dir.delete()) {
	    throw new Exception("Unable to delete directory " + dir.getAbsolutePath());
	}
    }

    private static void debug(String msg)
    {
	if (DEBUG) {
            logger.debug( msg );
	}
    }
}
