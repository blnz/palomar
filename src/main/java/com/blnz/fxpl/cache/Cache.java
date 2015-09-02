// $Id: Cache.java 123 2005-04-09 19:47:03Z blindsey $

package com.blnz.fxpl.cache;

import com.blnz.fxpl.cron.Refreshable;

import java.util.Properties;

/**
 *  Interface class for Cache implementations.
 */
public interface Cache extends Refreshable
{
    /** 
     * find a cached object
     * @param objectKey the identifier used for lookup
     * @return the cached Object, or <code>null</code> if not found
     */
    public Object get(String objectKey);
    
    /** 
     * store or replace an object in the cache
     * @param cacheObject the object we want to cache
     * @param objectKey the identifier we may use for lookup, later
     */
    public void put(String objectKey, Object cacheObject) 
	throws Exception;
    
    /**
     *  Cache an object which we're going to write some output to
     */
    public Object putMutable(String objectKey, Object mutableObject)
	throws Exception;
    
    /**
     *  remove an object from the cache. Does nothing if the object isn't found
     * @param objectKey the identifier we used when storing the object
     */
    public void remove(String objectKey) 
	throws Exception;
    

    /**
     * recieve notification from an outside party (another server)
     * that the object with the given key has been updated
     */
    public void invalidate(String key);
        
    /**
     * removes all objects from the cache
     */
    public void clear();
    
    /**
     * Set properties so that a specific cache can build its
     * characteristics on its own. 
     * @param  type the specific type
     * @param  cacheProperties  the characteristics of the specific type
     */
    public void setProperties(String type, Properties cacheProperties);
    
    /**
     * Return the characteristics of a specific implementation of IrisCache
     * @return  Properties
     */
    public Properties getProperties();
    
    /**
     * The default refresh period 
     */
    public void setRefreshFreq(long refreshFrequency);
    
    /**
     *  returns the identifier for this cache
     */
    public String getType();
    
    /**
     * returns the cache specific directory 
     *  should be overridden by the specific cache implementation
     */
    public String getCacheDir();
    
    /**
     * setter for maximum capacity this cache can hold
     */
    public void setMaxCacheSize(int maxCacheSize);
    
    /** 
     * returns the maximum holding capacity of this cache instance
     */
    public int getMaxCacheSize();
} 



