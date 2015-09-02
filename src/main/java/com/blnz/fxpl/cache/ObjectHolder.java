// $Id: ObjectHolder.java 46 2004-12-11 00:39:16Z blindsey $

package com.blnz.fxpl.cache;

/**
 *  Object representation of  object to be cached. 
 *  Behaves as a holder for the actual object that has to be cached.
 *  @see Object
 */
public class ObjectHolder extends Holder
{

   /** 
    * Default constructor 
    */
    public ObjectHolder()
    {
        setCachedAtTime(System.currentTimeMillis() );    
    }
    
    /**
     *
     */
    public void clean()
    {
        // what to clean.
        _cachedObject = null;
        _prev = null;
        _next = null;
        _objectKey = null;
        _objectPolicies = null;
    }
}





