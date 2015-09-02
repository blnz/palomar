package com.blnz.fxpl.cache;

/**
 *  Abstract class for all <code>Object</code>Holders.
 *  Holders 1) participate in linked list, 2) provide access to a
 *      cached object,  3) provide access to the set of cache 
 *      properties for the object, and 4) provide access to
 *      the key by which we store the object 
 */
public abstract class Holder 
{

    /**
     *  the original object that has to be cached.
     */
    protected Object _cachedObject = null;

    // We're in a linked list
    protected Holder _prev = null;
    protected Holder _next = null;
    
    // We need to know which cache we live in
    protected Cache _myCache = null;

    /**
     */
    protected String _objectKey = null;
    
    /**
     *  The policies that are attached to the object held.
     */
    protected Policy[] _objectPolicies = null;
       
    /**
     *  represents the time at which the object is cached.
     */
    protected long _cachedAtTime;
     
    /** 
     * Inform the ObjectHolder which cache owns him 
     */
    public void setCache(Cache theCache)
    { _myCache = theCache; }

    /** 
     * @return the Cache to which we belong 
     */
    public Cache getCache()
    { 
        return _myCache;
    }

    /**
     *  Method for getting the original object.
     *  @return Object	the object that is cached.
     */
    public Object getObject()
    {
        return _cachedObject;
    }

    /**
     *  Method for setting the object to be cached.
     *  @param	subject	object to be cached
     *  @return the original object, or possibly something that will act just
     *    like it
     */
    public Object setObject(Object subject)
    {
	if (_cachedObject != null) {
	    // FIXME: do something
	    // like ?? log + compare both objects or set to null if we don't care
	}
        _cachedObject = subject;
	return subject;
    }
    
    /**
     *  Method for setting policies on an <code>Object</code>. 
     *  @param objectPolicies	list of policies to be attached 
     */
    public void setPolicies(Policy[] objectPolicies)
    {
        _objectPolicies = objectPolicies;
        for (int i = 0; i < objectPolicies.length ; i++) {
            // FIXME: deprecate me
        }
    }
      
    /**
     *  Method for getting the policies attached to an <code>Object</code>
     *  @return IrisPolicy	the list of policies
     */
    public Policy[] getPolicies()
    {
        return _objectPolicies;
    }

    /**
     *  Method for getting the time at which a particular Holder is instantiated
     */
    public long getCachedAtTime()
    {
        return _cachedAtTime;
    }

    /**
     * sets the time at which the underlying object is cached.
     * @param cachedTime    time in milliseconds
     */
    public void setCachedAtTime(long cachedTime)
    {
        this._cachedAtTime = cachedTime;
    }
    
       
    /**
     *  setter for object key 
     */
    public void setObjectKey(String objectKey)
    {
        _objectKey = objectKey;
    }
  
    /**
     *  remove the object from the IrisHolder, performing any
     *  necessary cleanup
     */
    public abstract void clean();
 

    //----------------------------------------------------------------    
    // doubly Linked list maintainance

    /**
     * sets previous in linked list of IrisHolders 
     */
    public void setPrev(Holder prev)
    {
        _prev = prev;
    }

    /** 
     * gets previous in linked list of IrisHolders
     */
    public Holder getPrev()
    {
        return _prev;
    }
    
    /**
     *  sets next in linked list of IrisHolders
     */
    public void setNext(Holder next)
    {
        _next = next;
    }

    /**
     *  gets next in linked list of IrisHolders
     */
    public Holder getNext()
    {
        return _next;
    }

    /**
     *  getter for object key
     */
    public String getObjectKey()
    {
        return _objectKey;
    }
    
}

