package com.blnz.fxpl.fs;

import java.util.Date;
import java.util.Vector;

/**
 * interface to a (DAV) lock's information.
 */
public interface ItemLock
{
    //scopes
    public static String SCOPE_UNKNOWN = "-1";
    public static String SCOPE_EXCLUSIVE = "1";
    public static String SCOPE_SHARED = "2";
    
    //types
    public static String TYPE_UNKNOWN = "-1";
    public static String TYPE_WRITE = "1";
    
    //depth
    public static int DEPTH_INFINITY = 9999999;
    public static int DEPTH_ZERO = 0;
    
    /**
     * Get the value of _path.
     * @return value of _path.
     */
    public String getPath();
    
    /**
     * Get the value of _type.
     * @return value of _type.
     */
    public String getType();

    /**
     * Set the value of _type.
     * @param v  Value to assign to _type.
     */
    public void setType(String  v);

    /**
     * Get the value of _scope.
     * @return value of _scope.
     */
    public String getScope();
    
    /**
     * Set the value of _scope.
     * @param v  Value to assign to _scope.
     */
    public void setScope(String  v);
    
    /**
     * Get the value of _depth.
     * @return value of _depth.
     */
    public int getDepth();

    /**
     * Set the value of _depth.
     * @param v  Value to assign to _depth.
     */
    public void setDepth(int  v);
    
    /**
     * Get the value of _owner.
     * @return value of _owner.
     */
    public String getOwner();
    
    /**
     * Set the value of _owner.
     * @param v  Value to assign to _owner.
     */
    public void setOwner(String  v);
        
    /**
     * Get the value of _tokens.
     * @return value of _tokens.
     */
    public Vector getTokens();
    
    /**
     * Set the value of _tokens.
     * @param v  Value to assign to _tokens.
     */
    public void setTokens(Vector  v);
    
    /**
     * Get the value of _expiresAt.
     * @return value of _expiresAt.
     */
    public long getExpiresAt();
    
    /**
     * Set the value of _expiresAt.
     * @param v  Value to assign to _expiresAt.
     */
    public void setExpiresAt(long  v);

    /**
     * Get the value of _creationDate.
     * @return value of _creationDate.
     */
    public Date getCreationDate();
    
    /**
     * Set the value of _creationDate.
     * @param v  Value to assign to _creationDate.
     */
    public void setCreationDate(Date  v);

    /**
     * Get a String representation of this lock
     */
    public String toString() ;

    /**
     * Return true if the lock has expired.
     */
    public boolean hasExpired();

    /**
     * Return true if the lock is exclusive.
     */
    public boolean isExclusive(); 

    
    /**
     * set the repository item which is 
     * to be locked with this lock
     */
    public void setItemID(String itemID);
    

    /**
     * get the repository item id
     */
    public String getItemID();
    

    /**
     * get the repository item path
     */
    public void setPath(String path);


    /**
     * set the unique identifier for this lock
     */
    public void setLockID(String path);
    
    
    /**
     * get lock id
     */
    public String getLockID();
    
    
}
