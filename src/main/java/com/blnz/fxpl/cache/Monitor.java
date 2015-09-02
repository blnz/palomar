// $Id: Monitor.java 46 2004-12-11 00:39:16Z blindsey $

package com.blnz.fxpl.cache;

/**
 * Abstract class for all cache controllers. No specific abstract methods but
 * sub classes should implement the run() method.
 */
public abstract class Monitor implements Runnable
{
    /** specific cache to work on */
    protected Cache cacheToRefresh = null;
    
    protected long wakeupTime = 1000 * 60 * 60 * 24;
    
    protected long nextWakeupTime;
    
    /**
     * Set the cache to be refreshed 
     */
    public void setCache(Cache cacheToRefresh)
    {
        this.cacheToRefresh = cacheToRefresh;
    }
    
    // getter
    public Cache getCache()
    {
        return cacheToRefresh;
    }
    
    // Setter for controller wakeup time. 
    // Thumb rule - generally greater than the default object's ttl
    public void setRefreshTime(long wakeupTime)
    {
        this.wakeupTime = wakeupTime;
    }
    
    // getter 
    public long getRefreshTime()
    {
        return wakeupTime;
    }
    
    public void setNextWakeupTime(long sleepFor)
    {
    }

    public long getNextWakeupTime()
    {
        return nextWakeupTime;
    }
}

