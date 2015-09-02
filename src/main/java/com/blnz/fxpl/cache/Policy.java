package com.blnz.fxpl.cache;

/**
 *  Interface for all different types of policies. New policies should extend
 *  this interface.
 *  @see Object
 */
public interface Policy //extends IrisCloneAndSerializable
{
    /** 
     * An identifier method for each different type of policy
     */
    public String getTag();
    
    /**
     *  Method for evaluating the existance state in the cache. 
     *   Different types
     *  of policies attached to a particular cached object should be in a 
     *  position to evaluate the life period of that object.
     *  @see Object 
     */
    public boolean hasExpired(Holder cacheObject);

}



