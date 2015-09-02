package com.blnz.fxpl.fs;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.util.ConfigProps;

/**
 *  The home interface for repository.
 */
public class FS
{
    private static FsRepository _implementation = null;
    
    /**
     * Retrieves configured XfsRepository associated with the given URI
     */
    public static final FsRepository getRepository() 
    {
        if (_implementation == null) { 
            init(); 
        } 
        return _implementation;
    }
    
    /**
     *
     */
    private static synchronized void init()
    {
        if (_implementation != null) {
            return; // another thread beat us
        }
        String implName = null;
        try {
            implName = 
                ConfigProps.getProperty("com.blnz.fxpl.FsRepository",
                                        "com.blnz.fxpl.fs.impl.FSRepositoryImpl");
            _implementation = 
                (FsRepository) Class.forName(implName).newInstance();

            _implementation.startServices();

        } catch (Throwable ex) {
            Log.getLogger().warn("FsRepositoryHome error loading class " + implName,
                                 ex);
        }
    }
}

