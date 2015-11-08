
package com.blnz.fxpl.cron;

/**
 * the interface for an object, managed by the cron service.
 * objects managed by the cron service are periodically
 * refreshed
 */
public interface Refreshable extends Job
{
    /**
     * signal the object that it's time to perform
     * its refresh cycle
     */
    public void refresh() throws Exception ;
    
    /**
     * is it time to do its refresh, yet? 
     */
    public boolean isTimeToRefresh();

    /**
     * will this object ever need another refresh?
     *  if this method returns false, then the cron service
     *  can safely forget about it
     */
    public boolean willWantRefresh();
    
    /** 
     * the number of milliseconds, from now, when we next want to 
     *  visit this object with a refresh
     */
    public long getNextRefreshTime();
    
    /**
     * return an object to perform the actions at the appointed time
     */
    public Runnable getRunnable();
    
    /**
     * get a refresh interval for the job, interval in millisecs
     * after which the job should repeat 
     */
    public long getRefreshInterval();
}

	
