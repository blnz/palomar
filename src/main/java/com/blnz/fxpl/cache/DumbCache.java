// $Id: DumbCache.java 124 2005-04-11 06:29:01Z blindsey $

package com.blnz.fxpl.cache;

import java.util.HashMap;
import java.util.Properties;

/**
 * A simple cache -- Objects placed here survive for the life of the VM
 * unless explicitly removed
 */
public class DumbCache extends AbstractCache //implements IrisCache
{
    private HashMap _dumbMap = new HashMap();
    
    // contains instance specific properties 
    private Properties myProperties = null;
    
    /**
     * instantiates a new cache
     */
    public DumbCache()
    {}
    
    /**
     * @return the object identified by <code>objectKey</code> or
     *  <code>null</code>
     * @param objectKey the String key
     */
    public Object get(String objectKey)
    {
	return _dumbMap.get(objectKey);  
    }
    
    /**
     * inserts or replaces an object in the cache
     * @param objectKey the identifier we'll use to find the object
     * @param cacheObject the object we're want to store
     */
    public void put(String objectKey, Object cacheObject) 
        //        throws Exception
    {
        _dumbMap.put(objectKey, cacheObject);
    }
    
    /**
     *  Cache an object we're going to write some output to
     * @return an object for writing to
     */
    public Object putMutable(String objectKey, Object mutableObject) 
        // throws Exception
    {
	put(objectKey, mutableObject);
	return mutableObject;
    }
    
    /**
     * remover all objects in the cache
     */
    public void clear()
    {
	_dumbMap.clear();
    }
    
    /**
     * recieve notification of update event
     */
    public void invalidate(String objectKey)
    {
	_dumbMap.remove(objectKey);
    }

    /**
     * remove a single object from the cache
     */
    public void remove(String objectKey)
    {
	_dumbMap.remove(objectKey);
    }

    /** 
     * DumbCache doesn't make any use of these properties 
     */
    public void setProperties(String type, 
                              java.util.Properties myProperties)
    {
        this._type = type;
        trimProperties(myProperties);
        //this.myProperties = myProperties;
    }

    /**
     *
     */
    public Properties getProperties()
    {
        Properties temp = super.getProperties();
        temp.setProperty("currentsize",
                         Integer.toString( getCapacity() ) );
        return temp;
    }
    
    /**
     *
     */
    private int getCapacity()
    {
        return _dumbMap.size();
    }
           
    /**
     * from Refreshable 
     */
    public boolean willWantRefresh()
    {
        return false;
    }

    /**
     *
     */
    public void refresh() throws Exception
    {
        // DumbCache doesn't get refreshed
    }

    /**
     *  always returns false.
     *  NOTE: will be implemented based on requirement. 
     */
     // overrides the default verifications to return false always.
    public boolean isTimeToRefresh()
    {
        return false;
    }

    /**
     *
     */
    public Runnable getRunnable()
    {
        return null;
    }
}
