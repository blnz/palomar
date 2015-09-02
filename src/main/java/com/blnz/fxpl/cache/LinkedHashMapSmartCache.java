
package com.blnz.fxpl.cache;

import java.io.Reader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.Collections;

import com.blnz.fxpl.log.Log;

public class LinkedHashMapSmartCache  extends AbstractCache {

  private Map hashMapCache = null;
  
  protected Policy[] cachePolicies = null;
  
  //
  private static boolean DEBUG = false;
  private static boolean _hadEx = false;
  
  private Class holderClass = null;   
//location where the object has to be cached
  private String defaultCacheStorageType = "memory";
  

  public LinkedHashMapSmartCache() {
    
  }

  /** 
   *  Constructor with a tag identifier.
   *  @param tag  the identifier specifying the type of this instance
   */
  public LinkedHashMapSmartCache(String tag)
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
//    set objectholder 
      String objectHolderClassProp = 
          myProperties.getProperty("holderClass",
                                   "com.blnz.fxpl.cache.ObjectHolder");
      
      try {
          holderClass = Class.forName(objectHolderClassProp);
      } catch (ClassNotFoundException cnfE2) {
          Log.getLogger().error("unable to load  " +
                                       objectHolderClassProp , cnfE2 ); 
      }
      
      int capacity = (int) Math.ceil(getMaxCacheSize() / 0.50f) + 1;
      LinkedHashMap lhp = new LinkedHashMap(capacity, 0.50f, true) {
        public static final long serialVersionUID = 1;

        protected boolean removeEldestEntry(Map.Entry eldest) {
          return size() > LinkedHashMapSmartCache.this.getMaxCacheSize();
        }
      };
      hashMapCache = (Map) Collections.synchronizedMap(lhp);

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
   *  Method for getting the cached object. Contains the entire logic of 
   *  getting a requested object. 
   *  @param objectKey    the unique key to be searched in the 
   *    specified type cache
   *  @return Object  the cached object, null if not found.
   *  @exception Exception exception raised while retreiving the object 
   */
  private Object getCachedObject(String objectKey)
  {
    Holder requestedObjectHolder = (Holder) this.hashMapCache.get(objectKey);
    if (requestedObjectHolder == null) {
        return null;
    } else {
        return requestedObjectHolder.getObject();    
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
    Holder objectHolder = (Holder) holderClass.newInstance();  
    objectHolder.setObjectKey(keyParam);
    objectHolder.setPolicies(policies);
    objectHolder.setObject(specificObject); 
    putObject(keyParam, objectHolder);
    
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
    this.hashMapCache.put(objectKey, objectHolder);

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
    
    Holder objectHolder = (Holder) holderClass.newInstance(); 
    objectHolder.setCache(this);
    
    // this could be a Reader or the actual object -we don't care  
    objectHolder.setObjectKey(irisKey);
    objectHolder.setPolicies(policies);
    Object toBeReturned = objectHolder.setObject(specificObject); 
    putObject(irisKey, objectHolder);
    return toBeReturned;
      
  } 
  
  /**
   */
  public Object putReader(String irisKey, Policy[] policies, 
                          Object specificObject, String filename) 
      throws Exception
  {
    
    Holder objectHolder = (Holder) holderClass.newInstance(); 
    objectHolder.setCache(this);
    
    // this could be a Reader or the actual object -we don't care  
    objectHolder.setObjectKey(irisKey);
    objectHolder.setPolicies(policies);
    Object toBeReturned = objectHolder.setObject(specificObject); 
    
    if (objectHolder instanceof ReaderObjectHolder) {
        ((ReaderObjectHolder)objectHolder).setFileUrl(filename);
    }
    putObject(irisKey, objectHolder);
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
      return this.hashMapCache.size();
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
      ArrayList list = new ArrayList(hashMapCache.size());
      synchronized(hashMapCache) {
      Iterator holderKeysIterator = hashMapCache.keySet().iterator();
      
      while ( holderKeysIterator.hasNext() ) {
        String nextKey = (String) holderKeysIterator.next(); 
        list.add(nextKey);
      }
      }
      int size = list.size();
      for(int k=size-1; k >=0 ; --k) {
        String nextKey = (String) list.get(k);
        Holder nextHolder = (Holder) hashMapCache.get(nextKey);
        if(nextHolder == null) continue;
        Policy[] objectPolicies = nextHolder.getPolicies(); 
        // should not be null in any case.
        
        if (objectPolicies == null) {
            // what should I do ???
            continue;
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
      Holder holder = (Holder) this.hashMapCache.remove(objectKey);
      if(holder != null)
        holder.clean();
  }


  private void sanityCheck() 
      throws Exception
  {
      
  }

  
  /**
   * removes all objects from the cache
   */
  public void clear()
  {
      Log.getLogger().info( "[SmartCache][clear]" );
      this.hashMapCache.clear();
     
          setRefreshTime(System.currentTimeMillis());
          Log.getLogger().info( "[SmartCache][clear] done" );

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
      
      Log.getLogger().debug( "\n\nCache size =  "+ hashMapCache.size() );
      Log.getLogger().debug( " Printing the key values pairs" );
      Log.getLogger().debug( " ----------------------------- " );
      //Enumeration totalKeys = typeMaps.keys();
      System.out.println(hashMapCache);
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
      temp.append(" \nsize = "+ this.hashMapCache.size() );
      temp.append(" specific cache = "+ hashMapCache );
      return temp.toString();
  }
  
  

}
