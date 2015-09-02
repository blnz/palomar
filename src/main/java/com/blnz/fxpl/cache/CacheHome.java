package com.blnz.fxpl.cache;

import com.blnz.fxpl.util.ConfigProps;

public class CacheHome
{

    private static CacheService _cacheService = null;

    public static CacheService getCacheService()
    {

        if (_cacheService == null) {
            init();
        }
        return _cacheService;
    }

    /**
     * configure with this runtime's implementing service
     * class as specified in the property 
     * "com.blnz.fxpl.cache.CacheService"
     */
    private static synchronized void init()
    {
        String implName = null;

        if (_cacheService != null) {
            return; // another thread beat us
        }
        try {
            implName = 
                ConfigProps.getProperty("com.blnz.fxpl.cache.CacheService",
                                        "com.blnz.fxpl.cache.CacheProxy");
           
            _cacheService = (CacheService) Class.forName(implName).newInstance();
         } catch (Exception ex) {
            // we just want to have a fallback, and continue
            ex.printStackTrace();
            _cacheService = new com.blnz.fxpl.cache.CacheProxy();
        }
    }

}
