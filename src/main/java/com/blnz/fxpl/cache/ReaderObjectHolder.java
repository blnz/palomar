package com.blnz.fxpl.cache;


import com.blnz.fxpl.util.ConfigProps;
import com.blnz.fxpl.util.pipe.StreamSplitter;
import com.blnz.fxpl.util.pipe.LazyBufferedReaderWriter;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.log.Logger;

import java.io.*;

/**
 * holds an object which is presented as a Reader for caching large objects to files on disk.
 */
public class ReaderObjectHolder extends ObjectHolder
{
    private String _fileUrl = null;
    
    // true, if object is stored in buffer and wrapped in a Reader object.
    private boolean isInMemObject = false;

    /**
     *
     */    
    public ReaderObjectHolder()
    {
        super();
    }
    
    /**
     *
     */
    public String getFileUrl()
    {
        return _fileUrl;
    }
    
    /**
     *
     */
    public void setFileUrl(String fileUrl)
    {
        _fileUrl = fileUrl;
    }
    
    /**
     * cache an OutputStream to disk. We return an new OutputStream to
     * the client to write to.  We split the stream to two places: 1) a
     * file on the disk which we'll read from when the client code gets
     * the object later, and 2) the original OutputStream.
     * @return an OutputStream for the client to write to
     */
    public Object setObject(Object mutable)
    {

        if (mutable instanceof OutputStream) {
            OutputStream os = null;
            String cacheDir = getCache().getCacheDir();
            LazyBufferedReaderWriter lazyBuffReaderWriter = null;
            try {
                String cacheFile = 
                    ConfigProps.getFiles().createUniqueFilename(null);
                String fileUrl = cacheDir + File.separator + cacheFile; 
                setFileUrl(fileUrl);
                //	    System.err.println( " fileUrl = "+ fileUrl);
                File f = new File(fileUrl);
                lazyBuffReaderWriter = new LazyBufferedReaderWriter(f);
                
                lazyBuffReaderWriter.setBufferSize(getVariableBuffSize());
                os = new StreamSplitter((OutputStream) mutable, 
                                        lazyBuffReaderWriter);
            } catch (Exception ex) {
                Log.getLogger().error("error setting object", ex);
            }
            super.setObject(lazyBuffReaderWriter); 
            return os;
        } else {
            super.setObject(mutable); 
            return mutable;
        }

    }

    /**
     * get the buffer size from properties
     * @return buffer size in bytes
     */
    // FIXME(Ravi): can be made intelligent in returning 
    //  variable sizes based on 
    // the runtime heap size.
    private int getBuffSizeProp()
    {
        String temp = ConfigProps.getProperty("com.snapbridge.fed.cache.MaxBuffSize","8000");
        return Integer.parseInt(temp);
    }
    
    /**
     * returns a variable buffer size based on the VM's free memory
     * @return the variable buffer size
     */    
    private int getVariableBuffSize()
    {
        Runtime vmRt = Runtime.getRuntime();
        long freeMemRatio = 
            new Float( (float)vmRt.freeMemory() / 
                       (float)vmRt.totalMemory() * 100).longValue();
        int newBuffSize = 1000;
        if ( freeMemRatio > 70) {
            return 1000 * 8;
        } else if (freeMemRatio > 50 && freeMemRatio < 70 ) {
            return 1000 * 4;
        } else if( freeMemRatio > 40 && freeMemRatio < 50 ) {
            return 1000 * 2;
        }
        return newBuffSize;
    }
    
        
    /** returns a Reader for a stream which we cached to the fileSystem */
    // FIXME(Ravi): write a meaningful exception where the clients can catch it
    // and if possible reload the object.
    // Scenario : an object is cached and while reading if say an IO Excptn has occured
    // then the client can supress it and reload it instead of propagating it to the end user.
    public Object getObject()
    {
        Object obj = super.getObject();
        if (obj instanceof String) {
            return obj;
        }

	Reader r = null;
	try {
	    r = ( (LazyBufferedReaderWriter) super.getObject() ).getReader();
	} catch( Exception readEx ) {
            Log.getLogger().error("error in getObject", readEx);
	}
	return r;
    }
    
    /** 
     * remove the cached file, and any reference to it 
     */
    public void clean()
    {
	if (_fileUrl != null) {

	    File temp = new File(_fileUrl);
	    if ( temp.exists() ) {
		temp.delete();
	    }
	}
	super.clean();
	_fileUrl = null;
    }
}



