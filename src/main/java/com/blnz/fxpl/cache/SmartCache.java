// $Id: SmartCache.java 844 2007-08-03 00:06:32Z blindsey $

package com.blnz.fxpl.cache;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.log.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.File;

/**
 * Class holds the objects for a specific type.
 * <p><pre><blockquote>
 * SmartCache - Smart enough to identify how to store the object( either 
 * memory or disk)
 * (TBD: Can spawn a thread(may be <code>CacheProxy</code> which) wakes up 
 * periodically and cleans the cache.
 */
public class SmartCache extends AbstractCache 
{

    // for finding cached objects by key
    private HashMap _specificCache = new HashMap();
    
    // first element in this specific collection
    private Holder _first = null;
    
    // last element in this specific collection
    private Holder _last = null;
    
    // if above one we have to clone the object when ever
    //   we create a specific holder
    private Class holderClass = null;    
    
    // location where the object has to be cached
    private String defaultCacheStorageType = "memory";

    private static boolean _hadEx = false;
    
    /**
     * The policies that can be assigned to a cache.
     */ 
    protected Policy[] cachePolicies = null;
        
    //
    private static boolean DEBUG = false;

    /** 
     * Default constructor 
     */
    public SmartCache()
    {
    }
    
    /** 
     *  Constructor with a tag identifier.
     *  @param tag  the identifier specifying the type of this instance
     */
    public SmartCache(String tag)
    {
        this._type = tag;
    }
    
    /**
     *  Method sets the characteristics of this particular instance.
     *  Method should be called after instantiating an object of this class.
     */
    public void setProperties(String type, Properties initialProperties)
    {
        this._type = type;
        trimProperties(initialProperties);
        initialize();
    }

    /**
     *
     */    
    public Properties getProperties()
    {
        Properties temp = super.getProperties();
        temp.setProperty("currentsize", Integer.toString( getCapacity() ) );
        return temp;
    }
    
    
    // does all initializations like size, objectholder types etc  
    private void initialize()
    {
        // set max size
        String myMaxsizeProp =  myProperties.getProperty("maxsize");
        if ( myMaxsizeProp != null) {
            setMaxCacheSize(Integer.parseInt(myMaxsizeProp));
        }

        String myRefreshProp = myProperties.getProperty("refresh");
        if ( myRefreshProp != null) {
            setRefreshFreq( Long.parseLong(myRefreshProp) );
        } 
        // set objectholder 
        String objectHolderClassProp = 
            myProperties.getProperty("holderClass",
                                     "com.blnz.fxpl.cache.ObjectHolder");
        
        try {
            holderClass = Class.forName(objectHolderClassProp);
        } catch (ClassNotFoundException cnfE2) {
            Log.getLogger().error("unable to load  " +
                                         objectHolderClassProp , cnfE2 ); 
        }

        createDefaultPolicies();
        try {
            if (DEBUG && ! _hadEx ){
                sanityCheck();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /** 
     * Sets the default policies for objects held by this cache. 
     *  Called once at the boot time. 
     */
    private void createDefaultPolicies()
    {
        // currently look for each type of policy. 
        // Will be modified by dynamic lookup and policy setting.

        long lifeTimeProp =
            Long.parseLong( myProperties.getProperty("policy_ttl",
                                                     String.valueOf( (long)24 ) ) );


        Policy[] defaultPolicies = 
            { new TTLPolicy(lifeTimeProp) };

        setDefaultPolicies(defaultPolicies);
    }    
    
    /**
     *  Method for getting an object from cache.
     *  @param  keyParam the string representation of the key
     */
    public Object get(String keyParam)
    {
        try {
            if (! _hadEx && DEBUG ) {
                sanityCheck();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return getCachedObject(keyParam);
    }
    
    /**
     *  Returns <code>Holder</code> object given a key 
     *  @param keyParam  string representation of key
     *  @return  the object's holder
     */
    protected Holder getHolder(String keyParam)
    {
        return (Holder) _specificCache.get(keyParam);
    }
    
    /**
     *  Method for getting the cached object. Contains the entire logic of 
     *  getting a requested object. 
     *  @param objectKey    the unique key to be searched in the 
     *    specified type cache
     *  @return Object  the cached object, null if not found.
     *  @exception Exception exception raised while retreiving the object 
     */
    private Object getCachedObject(String objectKey)
    {
        // lock only that particular cache

        synchronized (_specificCache) {

            // get the objectHolder, could be Objholder or
            //    ObjReaderHolder.. don't care

            Holder requestedObjectHolder = getHolder(objectKey);
            if (requestedObjectHolder == null) {
                return null;
            } else {
                // Least recently used
                moveToFront(requestedObjectHolder);
                return requestedObjectHolder.getObject();    
            }
        }
    }
    
    
    /** 
     *  Cache an object we're going to write some output to
     */
    public Object putMutable(String objectKey, 
                             Object mutableObject) 
        throws Exception
    {
        return putMutable(objectKey, 
                          getDefaultPolicies(), mutableObject);
    }
    
    /** 
     *  Cache an object we're going to write some output to
     */
    public Object putReader(String objectKey, 
                            Reader mutableObject,
                            String filename) 
        throws Exception
    {
        return putReader(objectKey, 
                         getDefaultPolicies(), mutableObject, filename);
    }
    
    /**
     *  Method for updating the cache with an object. Gets the default 
     *  policies to be attached with that particular object.
     *  @param keyParam the key identifier of an object.
     *  @param specificObject the object to be cached. 
     * 
     */
    public void put(String keyParam, Object specificObject) 
        throws Exception
    {
        // generate policy, objectIdentifier 
        Policy[] policies = getDefaultPolicies(); 
        put(keyParam, policies, specificObject);
    } 
    
    
    /**
     *  Method for updating the cache with an object. 
     *  Creates <code>ObjectHolder</code> with the input parameters.
     *
     *  @param keyParam the key identifier of an object.
     *  @param policies the <code>Policy</code> objects to be attached to an 
     *                  object that has to be cached
     *  @param specificObject the object to be cached. 
     *
     */
    public void put(String keyParam, Policy[] policies, 
                    Object specificObject) 
        throws Exception
    {
        if (getMaxCacheSize() == 0) {
            return;
        }
        Holder objectHolder = getHolder(keyParam);
        if (objectHolder != null) {
            objectHolder.setObject(specificObject);
            objectHolder.setCachedAtTime(System.currentTimeMillis());
            moveToFront(objectHolder);
        } else {
            // get the specific instance of holder
            if (holderClass == null) {

            }
            objectHolder = (Holder) holderClass.newInstance(); 
            objectHolder.setCache(this);
            // this could be a Reader or the actual object -we don't care  
            objectHolder.setObjectKey(keyParam);
            objectHolder.setPolicies(policies);
            objectHolder.setObject(specificObject); 
            
            try {
                putObject(keyParam, objectHolder);
            } catch(Exception updateEx) {
                updateEx.printStackTrace();
            }
        }
    } 
    
    
    /**
     *  Method for updating the cache. Generates the <code>CacheObject</code>, 
     *  identifies the category
     *  of storage and updates. If the cache is disk, takes the default
     *  location, generates filename based on
     *  the key and serializes the object into that file.
     *  @param objectKey        the object representation of the key
     *  @param objecHolder  specific object holder
     *  @exception Exception    the exception raised while updating
     */ 
    private  void putObject(String objectKey, 
                            Holder objectHolder) 
        throws Exception
    {
        // lock only that particular cache
        synchronized(_specificCache) {
            if (_specificCache.size() == 0) {
                setLast(objectHolder);
                setFirst(objectHolder); 
            } else {
                
                if (! _hadEx && DEBUG) {
                    sanityCheck();
                }
                
                Holder currentFirstObject = (Holder) getFirst();
                
                // set the prev and next objectholders of this objectHolder
                objectHolder.setPrev(null);
                objectHolder.setNext(currentFirstObject);
                currentFirstObject.setPrev(objectHolder);
                // set the first object of this cacheHolder to the current object
                setFirst(objectHolder);
                
                if (getMaxCacheSize() < getCapacity()) {
                    
                    Holder lastObjectHolder = (Holder) getLast();
                    removeObjectHolder(lastObjectHolder);
                    
                }
            }
            _specificCache.put(objectKey, objectHolder);
        }

        if (DEBUG && ! _hadEx) {
            sanityCheck();
        }
    }
    
    /**
     */
    public Object putMutable(String irisKey, Policy[] policies, 
                             Object specificObject) 
        throws Exception
    {
        // do nothing if no cache
        if (getMaxCacheSize() == 0) {
            return specificObject;
        }
        
        Object toBeReturned = null;
        Holder objectHolder = getHolder(irisKey);
        if (objectHolder != null) {
            toBeReturned = objectHolder.setObject(specificObject);
            moveToFront(objectHolder);
            if (DEBUG && ! _hadEx) {
                sanityCheck();
            }
        } else {
            // get the specific instance of holder
            objectHolder = (Holder) holderClass.newInstance(); 
            objectHolder.setCache(this);
            
            // this could be a Reader or the actual object -we don't care  
            objectHolder.setObjectKey(irisKey);
            objectHolder.setPolicies(policies);
            toBeReturned = objectHolder.setObject(specificObject); 
            
            try {
                putObject(irisKey, objectHolder);
            } catch(Exception updateEx) {
                updateEx.printStackTrace();
            }
        }
        return toBeReturned;
    } 
    
    /**
     */
    public Object putReader(String irisKey, Policy[] policies, 
                            Object specificObject, String filename) 
        throws Exception
    {
        // do nothing if no cache
        if (getMaxCacheSize() == 0) {
            return specificObject;
        }
        
        Object toBeReturned = null;
        Holder objectHolder = getHolder(irisKey);
        if (objectHolder != null) {
            toBeReturned = objectHolder.setObject(specificObject);
            moveToFront(objectHolder);
            if (DEBUG && ! _hadEx) {
                sanityCheck();
            }
            if (objectHolder instanceof ReaderObjectHolder) {
                ((ReaderObjectHolder)objectHolder).setFileUrl(filename);
            }
        } else {
            // get the specific instance of holder
            objectHolder = (Holder) holderClass.newInstance(); 
            objectHolder.setCache(this);
            
            // this could be a Reader or the actual object -we don't care  
            objectHolder.setObjectKey(irisKey);
            objectHolder.setPolicies(policies);
            toBeReturned = objectHolder.setObject(specificObject); 
            
            if (objectHolder instanceof ReaderObjectHolder) {
                ((ReaderObjectHolder)objectHolder).setFileUrl(filename);
            }
            try {
                putObject(irisKey, objectHolder);
            } catch(Exception updateEx) {
                updateEx.printStackTrace();
            }
        }
        return toBeReturned;
    } 
    
     
    /**
     *
     */
    public void setDefaultCacheStorageType(String storageType)
    {
        this.defaultCacheStorageType = storageType;
    }
    
    /**
     *
     */
    public String getDefaultCacheStorageType()
    {
        return defaultCacheStorageType;
    }
    
    /** 
     * @return the max number of objects the cache will store
     */
    public int getCapacity()
    {
        return _specificCache.size();
    }
    
    /**
     *
     */
    public Map getSpecificCache()
    {
        return _specificCache;
    }
    
    /**
     *  Setting the default policies for the entire objects in this cache.
     */
    public void setDefaultPolicies(Policy[] cachePolicies)
    {
        this.cachePolicies = cachePolicies;
    }
    
    /** 
     *  Method gets the default policies based on the type. Currently returns
     *  all the policies as a list.
     *  @return array of the Policies that can be attached to an object
     */
    private Policy[] getDefaultPolicies()
    {
        // default policies are set by the CacheProxy as part of initialization
        return cachePolicies; 
    } 
    
    /**
     * keep track of first element in linked list
     */
    public void setFirst(Holder first)
    {
        _first = first;
    }
    
    /**
     *  keep track of last element in linked list
     */
    public void setLast(Holder last)
    {
        _last = last;
    }
    
    /**
     * get first element in linked list
     */
    public Holder getFirst()
    {
        return _first;
    }
    
    /**
     *  get last element in linked list
     */
    public Holder getLast()
    {
        return _last;
    }
    
    /** 
     * Moves an ObjectHolder to the front of linked list
     */
    private void moveToFront(Holder theHolder)
    {
        try {
            if (DEBUG && ! _hadEx) {
                sanityCheck();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        synchronized(_specificCache) {
            Holder myPrevHolder =  theHolder.getPrev();
            Holder myNextHolder = theHolder.getNext();

            if (myPrevHolder == null) {
                return;   // we were already at the front of the list
            } else {
                theHolder.setPrev(null);
                Holder wasFirst = getFirst();
                theHolder.setNext(wasFirst); 
                setFirst(theHolder);
                myPrevHolder.setNext(myNextHolder);
                wasFirst.setPrev(theHolder);
                if (myNextHolder != null) {
                    myNextHolder.setPrev(myPrevHolder);
                } else { 
                    // we were at the end of the list
                    setLast(myPrevHolder);
                }
            }
        }
        try {
            if (DEBUG && ! _hadEx) {
                sanityCheck();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * from Refreshable 
     */
    public boolean willWantRefresh()
    {
        return true;
    }

    /**
     * <code>SmartCache</code> specific implementation of 
     *  refreshing its cache.
     */
    public void refresh() throws Exception
    {
        Vector expiredObjects = new Vector(); // will hold keys 
        
        // - loop through all the elements in _specificCache 
        // - get the policies on each holder
        // - apply policies on each holder 
        // - if expired true , store in a vector
        // - finally remove the elements of vector from specific cache

        synchronized(_specificCache) {
            Iterator holderKeysIterator = _specificCache.keySet().iterator();
            while ( holderKeysIterator.hasNext() ) {
                String nextKey = (String) holderKeysIterator.next(); 
                Holder nextHolder = (Holder) _specificCache.get(nextKey);
                Policy[] objectPolicies = nextHolder.getPolicies(); 
                // should not be null in any case.
                
                if (objectPolicies == null) {
                    // what should I do ???
                    break;
                } else {
                    boolean expired = false;
                    for (int i = 0; i < objectPolicies.length ; i++) {
                        expired = objectPolicies[i].hasExpired(nextHolder);
                        if (expired) { 
                            // if one of the policies satisfy the condition , 
                            //the obj is expired
                            expiredObjects.addElement(nextKey);
                            
                            // cannot delete the obj here as Iterator breaks
                            break; 
                            // done applying policies. the object is expired
                        }
                    }
                }
            }
        }

        removeList(expiredObjects);
        setRefreshTime(System.currentTimeMillis() );
        
        if (DEBUG && ! _hadEx ) {
            sanityCheck();
        }
    }
    
    /**
     *
     */
    public Runnable getRunnable()
    {
        return null;
    }
    
    private void removeObjectHolder(Holder holder)
        throws Exception
    {
        remove(holder.getObjectKey());
    }
    
    /**
     * Removes a list of objects from the cache, presumably because they've expired 
     * @param keys the list of objects to be removed
     */
    private void removeList(Vector keys)
        throws Exception
    {
        for( Enumeration expiredEnum = keys.elements() ;
             expiredEnum.hasMoreElements() ; 
             ) {

            String nextExpiredKey = (String) expiredEnum.nextElement();
            doRemoval(nextExpiredKey);

        }        
    }


    /**
     * receive notification of update event
     */
    public void invalidate(String objectKey)
    {
        try {
            doRemoval(objectKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * notify other servers/clients of an object invalidation
     */ 
    private void notifyOthers(String objectKey)
    {
        CacheHome.getCacheService().notifyPartners(_type, objectKey);
    }
    

    /** 
     * Remove an object from the cache. Does nothing if the object isn't found
     * @param objectKey the identifier used when storing the object
     */
    public void remove(String objectKey) throws Exception
    {
        doRemoval(objectKey);
        if (shouldNotify(_type)) {
            notifyOthers(objectKey);
        }
    }

    /** 
     * Remove an object from the cache. Does nothing if the object isn't found
     * @param objectKey the identifier used when storing the object
     */
    private void doRemoval(String objectKey) throws Exception
    {
        //- get the objectholder if exists
        //- get the prev and next objectholders of this objholder
        //- set the next and prev objectholders for the prev and next objholders
        //- check for boundary conditions for first and last holders

        if (!_hadEx && DEBUG) {
            sanityCheck();
        }
        synchronized( _specificCache) {
            if (_specificCache.containsKey(objectKey)) { 

                // BILL : will this become heavy for checking each 
                //   and every key before removing?? 
                Holder holderTBR = getHolder(objectKey);

                if (holderTBR == null) {
                    Log.getLogger().info( " No cached object found for " + 
                                                 objectKey );
                } else {
                    // synchronized(_specificCache) { //moving this lock to before containsKey() check..
                    Holder myPrevHolder = holderTBR.getPrev();
                    Holder myNextHolder = holderTBR.getNext();

                    if ( (myPrevHolder == null) && 
                         (myNextHolder != null) ) {

                        // boundary condition 1   
                        // i am the first in the list and have 
                        // siblings following, 
                        // so my nextHolder will be the first in cache
                        // and myNextHolders prev shd be null

                        myNextHolder.setPrev( null );
                        setFirst( myNextHolder );       

                    } else if ( (myNextHolder == null) && 
                                (myPrevHolder != null) ) {

                        // boundary condition 2
                        // i am the last in the list, so my prevHolder
                        //  will be the last in cache
                        // and myPrevHolders next shd be null

                        myPrevHolder.setNext( null );
                        setLast( myPrevHolder );

                    } else if ( (myNextHolder != null) && 
                                (myPrevHolder != null) ) {

                        // set the next of myprev to my next and
                        // prev of mynext to my prev

                        myPrevHolder.setNext( myNextHolder );
                        myNextHolder.setPrev( myPrevHolder );

                    } else { //  iam the only one
                        setLast(null);
                        setFirst(null);
                    }

                    _specificCache.remove(objectKey);

                    // now clean specific holder 
                    holderTBR.clean();
                }
            } else {
                // ummm ...
            }
        }
        if (!_hadEx && DEBUG) {
            sanityCheck();
        }
    }


    private void sanityCheck() 
        throws Exception
    {
        if (getFirst() == null) {
            if (getLast() != null) {
                _hadEx = true;
                throw new Exception("last non null, but first is!");
            } 
            return;
        } else if (getLast() == null) {
            _hadEx = true;
            throw new Exception("last is null, but first isn't!");
        } else if (getFirst() == getLast()) {
            Holder holder = getFirst();
            if (holder.getNext() != null) {
                _hadEx = true;
                throw new Exception( "first == last but contains a next");
            } else if (holder.getPrev() != null) { 
                _hadEx = true;
                throw new Exception( "first == last but contains a prev");
            }
            return;
        } else {
            Holder holder = getFirst();
            while (holder.getNext() != null) {
                Holder next = holder.getNext();
                if (next.getPrev() != holder) {
                    _hadEx = true;
                    throw new Exception("holder for " + 
                                        holder.getObjectKey() +
                                        " points forward to " +
                                        next.getObjectKey() +
                                        " but next object points back to " +
                                        ((next.getPrev() == null) ? 
                                         " nobody " :
                                         next.getPrev().getObjectKey()));

                }
                holder = next;
            }
            if (holder != getLast()) {
                _hadEx = true;
                throw new Exception("holder for: " + holder.getObjectKey() +
                                    " has no next, but last holds " +
                                    getLast().getObjectKey());
            }
        }
    }

    
    /**
     * removes all objects from the cache
     */
    public void clear()
    {
        Log.getLogger().info( "[SmartCache][clear]" );
        synchronized( _specificCache ) {  
            Iterator holderKeys = _specificCache.keySet().iterator();
            while(holderKeys.hasNext()){
                Holder holder = 
                    (Holder)getHolder((String) holderKeys.next());  
                holder.clean();
            }
                              
            // all the objects are cleaned ...now it's time to reset the cache
            _specificCache.clear();
            setFirst(null);
            setLast(null);
            setRefreshTime(System.currentTimeMillis());
            Log.getLogger().info( "[SmartCache][clear] done" );
        }
    }            
    
    /**
     *  Method for clearing a particular type of cache.
     *  @deprecated
     */
    public void clearCache()
    {
        clear();
    }
    
    /**
     *  Convinience method for testing.
     */
    public void printCache()
    {
        
        Log.getLogger().debug( "\n\nCache size =  "+ _specificCache.size() );
        Log.getLogger().debug( " Printing the key values pairs" );
        Log.getLogger().debug( " ----------------------------- " );
        //Enumeration totalKeys = typeMaps.keys();
        synchronized(_specificCache){
            for (Iterator keysIterator = getKeys(_specificCache); 
                 keysIterator.hasNext() ;
                 ) {
                String nextKey = ( (String)keysIterator.next() );
                Log.getLogger().debug( " TYPE = " + nextKey);
                Log.getLogger().debug( " Values are \n" + 
                                              _specificCache.get( nextKey ) );      
            }
        }
    }

    private static final void debug(String msg) 
    {
        if (DEBUG) {
            Log.getLogger().debug("SmartCache: " + msg);
        }
    }

    
    public String toString()
    {
        StringBuffer temp = new StringBuffer();
        temp.append(" \nsize = "+ _specificCache.size() );
        temp.append(" specific cache = "+ _specificCache );
        return temp.toString();
    }
    
}
