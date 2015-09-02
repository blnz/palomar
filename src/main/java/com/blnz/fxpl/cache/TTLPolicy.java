// $Id: TTLPolicy.java 46 2004-12-11 00:39:16Z blindsey $

package com.blnz.fxpl.cache;

/**
 *  Class defines the <em>Time To Live(TTL)<em> policy. Encapsulates the 
 *  details of the life time of a cache object. Given a cached object, 
 *  this can determine the objects expired state.
 */
public class TTLPolicy implements Policy
{

    private long lifeTime ;

    /** 
     * construct with time to live in seconds 
     */
    public TTLPolicy(long lifeTime)
    {
	// this.lifeTime = lifeTime;
	setLifeTimeHours(lifeTime);
    }
    
    /**
     *  Evaluates the state of a cached object. If true, the
     *  object may be refreshed/deleted from the cache.
     *
     *  @param objectHolder	the object to be analysed for its state.
     *  @see	ObjectHolder
     */
    public boolean hasExpired(Holder objectHolder) 
    {
        long currentTime = System.currentTimeMillis();
        long objectCachedAt = 
            ( (ObjectHolder)objectHolder ).getCachedAtTime();

        if ( (currentTime - objectCachedAt) > getLifeTime() ) { 
            return true; 
        }
        return false;
    }
    
    /**
     *   sets the life time for a cached object
     *  @param 	lifeTime time in seconds
     */
    public void setLifeTimeHours(long lifeTime)
    {
        this.lifeTime = lifeTime * 1000;
    }
    
    /**
     *  Method for getting the life time of a cached object
     */	
    public long getLifeTime()
    {
        return lifeTime;
    }
    
    /**
     *  Method for simple identification of a policy.
     *  @return String	the name of the policy 
     */	
    public String getTag()
    {
        return "TTLPolicy";
    }
  
    /**
     *
     */  
    public String toString()
    {
        StringBuffer temp = new StringBuffer();
        temp.append("poilicy Tag = " + getTag() );
        temp.append("policy time = " + getLifeTime() );
        return temp.toString();
    }
    
} 



