package com.blnz.fxpl.fs;

import com.blnz.fxpl.util.ConfigProps;

import java.util.logging.Logger;

/**
 *  The home interface for repository.
 */
public class FS
{
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
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
            LOGGER.severe("FsRepositoryHome error loading class " + implName + " " + ex.toString());
        }
    }
}

