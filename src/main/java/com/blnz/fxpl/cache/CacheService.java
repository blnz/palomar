package com.blnz.fxpl.cache;

import java.util.Vector;
import java.util.Map;

/**
 * manages a number of named caches, each with its own policies
 */
public interface CacheService
{

    /**
     * get the named cache
     */ 
    public Cache getCache(String cacheName);

    /**
     * @return true if the named cache should notify partners in a cluster of changes
     */
    public boolean shouldNotify(String cacheName);

    /**
     * send a message to partners in a cluster that a given key in a given cache has been invalidated
     */
    public void notifyPartners(String cacheName, String objectKey);

    /**
     * Refresh one or more caches. 
     *
     * @param cacheList   list of caches to be cleaned
     */
    public void refresh(Vector cacheList);

    /**
     * A map of Cache names and Properties for each  
     */
    public Map getCacheProperties();
}
