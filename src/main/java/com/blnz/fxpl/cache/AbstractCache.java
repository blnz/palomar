// $Id: AbstractCache.java 828 2007-03-19 05:56:45Z blindsey $

package com.blnz.fxpl.cache;

import com.blnz.fxpl.util.ConfigProps;

import java.util.Properties;
import java.io.File;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;


/**
 * Abstract class for common implementations of IrisCache.
 */
public abstract class AbstractCache implements Cache
{
    
    /** Refresh frequency. 
     *  Will be set as part of initialization.
     */
    // will be in milliseconds
    protected long refreshFrequency ;


    // MAX size that this cache can hold.
    private int _maxCacheSize; 
    
    /** identifier for this cache */
    protected String _type = null;
    
    
    // Last refreshed time 
    protected long lastRefreshTime = System.currentTimeMillis();
    
    // if this caches files / streams, we need a place to put 'em 
    private String _cacheDir = null;
    
    // instance specific properties holder
    protected Properties myProperties = new Properties();

    private CacheService _cacheService = null;
    private boolean _shouldNotify = true;

    /** 
     *  Checks to see if it is the right time to refresh
     *  @return boolean true, if it is time to refresh 
     */
    public boolean isTimeToRefresh()
    {
        String mySelf = this.getClass().getName();
        
        long currentTime = System.currentTimeMillis();
        if ( (lastRefreshTime + refreshFrequency) < currentTime ) {
            return true;
        }
        return false;
    }
    
    /** 
     * Get the object type this cache is holding 
     */
    public String getType()
    {
        return _type;
    }
    
    /**
     *  Set the object type this cache has to hold
     * @param  type can only be set by subclasses
     */
    protected void setType(String type)
    {
        this._type = type;
    }
    
    /**
     * Returns the next refreshing time in milliseconds from now
     */
    public long getNextRefreshTime()
    {
        return (lastRefreshTime + refreshFrequency);
    }
    

    /**
     *
     */
    protected void setRefreshTime(long refreshedAt)
    {
        lastRefreshTime = refreshedAt;
    }

    /**
     *
     */    
    public void setRefreshFreq(long refreshFrequency)
    {
        this.refreshFrequency = refreshFrequency * 1000; 
    }

    /**
     *
     */
    public long getRefreshFreq()
    { // check for 0 or null(?) and return
        return refreshFrequency / 1000;
    }

    /**
     *
     */
    public void setMaxCacheSize(int maxCacheSize)
    {
        this._maxCacheSize = maxCacheSize;
    }
    
    /**
     *
     */
    public int getMaxCacheSize()
    {
        return _maxCacheSize;
    }   
        
    /** 
     * returns the directory to be used for caching 
     */
    public String getCacheDir()
    {
        if (_cacheDir == null) {
            _cacheDir = ConfigProps.getFiles().getCacheDir();
        }

        String temp = myProperties.getProperty("subpath", this.getType() );

        String myCacheDir =
            _cacheDir + File.separator + 
            myProperties.getProperty("subpath",
                                     this.getType());

        if ( myCacheDir == null) { 
            myCacheDir = "temp";
        }

        File cacheDir = new File(myCacheDir);
        if (cacheDir.exists() ) {
            if (cacheDir.isDirectory() ) {
                return cacheDir.toString();
            } else {
                cacheDir.delete();
            }
        }
        cacheDir.mkdir(); 

        return cacheDir.toString();
    }

    // Method trims the key element and extracts the property variable 
    // properties key(s) look like : "IrisCacheProxy.cparams.<type>.<variable> = value"
    protected void trimProperties(Properties initialProperties)
    {
        for (Enumeration propEnum = initialProperties.keys();
             propEnum.hasMoreElements() ; 
             ) {

            String nextProp = (String) propEnum.nextElement();
            String trimmedProp = 
                nextProp.substring( nextProp.lastIndexOf(".") + 1);

            myProperties.put(trimmedProp, 
                             (String) initialProperties.get(nextProp));
        }
    }

    /**
     *
     */
    public Properties getProperties()
    {
        return myProperties;
    }
    
    
    /**
     *  Method for testing the capacity of a passed Map.
     *  @param typeMap  a Collection object
     *  @return boolean flag on the capacity of a particular Map.
     */ 
    public boolean isEmpty(Map typeMap)
    {
        return typeMap.isEmpty();
    }
    
    /**
     *  Method for getting an Iterator on keys for a particular Collection 
     *  object
     */ 
    public Iterator getKeys(Map typeMap)
    {
        return typeMap.keySet().iterator();
    }
    
    /**
     *  Method for getting an Iterator on values for a particular Collection
     *  object
     */ 
    public Iterator getObjects(Map typeMap)
    {
        return typeMap.entrySet().iterator();
    }
    

    protected boolean shouldNotify(String cacheName)
    {
        if (_cacheService == null) {
            _cacheService = CacheHome.getCacheService();
            _shouldNotify = _cacheService.shouldNotify(cacheName);
        }
        return _shouldNotify;
    }

    /**
     * get refresh interval for this cache
     */
    public long getRefreshInterval()
    {
        return refreshFrequency;
    }
    
    /**
     * run this cron job
     */
    public void runJob()
        throws Exception
    {
        refresh();
    }
    
    /**
     * get cron expression for this job, returns a empty string
     * as all cache jobs are type refreshable i.e.
     * scheduled to repeat after specified refresh interval
     */
    public String getCronExpr()
    {
        return "";
    }
    
    /**
     * get name for this cron job
     */
    public String getJobName()
    {
        return getType();
    }
        
    /**
     * get group name for this cron job, several jobs can belong to a group
     */
    public String getJobGroupName()
    {
        return "cache";
    }
}
