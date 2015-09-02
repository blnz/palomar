
package com.blnz.fxpl.fs.impl;

import com.blnz.fxpl.fs.helpers.FSRepositoryUtil;
import org.xml.sax.ContentHandler;

import com.blnz.fxpl.security.User;

import com.blnz.fxpl.fs.ItemLock;
import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.RepositoryUtil;
import com.blnz.fxpl.fs.Transaction;
import com.blnz.fxpl.fs.FsException;
import com.blnz.fxpl.fs.FsRepository;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.util.ConfigProps;


import com.blnz.fxpl.cache.Cache;
import com.blnz.fxpl.cache.CacheHome;
import java.util.Iterator;
import java.util.Date;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;

/**
 *  Webdav implementation respository
 */
public class FSRepositoryImpl implements FsRepository
{

    protected Cache _cache = null;
    protected boolean isCreatingMeta = false;
    
    /**
     *
     */
    public FSRepositoryImpl() 
    {
        try {
            FSConnection.initFSRepositoryPath();
        
            //System.out.println("FSRepositoryImpl : initializing FSRepositoryImpl done.");
            _cache = CacheHome.getCacheService().getCache("fsItems");
            this.isCreatingMeta = ConfigProps.getProperty("com.blnz.fxpl.fs.fs.meta", "false").equals("true");

        } catch (Exception ex) {
            // FIXME: handle properly
            System.out.println("Error: FSRepositoryImpl : constructor : initFSRepositoryPath : " 
                               + ex.getMessage());
            //ex.printStackTrace();
        }
    }

    public void startServices()
    {}
    
    public void addLock(Transaction xact, ItemLock newLock, User user) 
        throws FsException
    {
        //commits the (tentative) lock
        //FIXME: not implemented
    }
    
    public boolean canAccessCheckedoutItem(RepositoryItem item, User user)
    {
        //return true if the checked out item can be accessed by a user
        //FIXME: not implemented
        return true;
    }
    
    public boolean canAccessLock(User user, ItemLock lock)
    {//returns true if the user can have access to resource lock
    
        //FIXME: not implemented
        return true;
    
    }
    
    public void deleteAllExpiredLocks()
    {//delete all expired locks from the ItemLocks table
        //FIXME: not implemented
    }
    
    public void deleteLock(Transaction xact, ItemLock newLock, User user)
    {//deletes a lock
        //FIXME: not implemented
    }
    
    public void deleteRepositoryItem(Transaction xact, User user, 
                                     java.lang.String recurse, RepositoryItem item) throws FsException 
    {
	//deletes the given item on behalf of the given user as part of a larger transaction.
        FSRepositoryItem repositoryItem = (FSRepositoryItem)item;
        Timestamp deleteTime = new Timestamp((new java.util.Date()).getTime() );
        try {
            item.deleteSelf( deleteTime, true );
        } catch (Exception e) {
            e.printStackTrace();
            throw new FsException(e);
        }
    }
    
    public void doBranchIndex(java.lang.String mimeType, java.lang.String path,
                              int skip, int count, boolean renew)
	throws FsException
    {
	throw new FsException("operation not supported");
    }
    
    //     public java.lang.String getBaseXfsDir()
    //     {//get the repository base path on the file system
    //         //FIXME: not implemented
    //         return "";
    //     }
    
    public java.util.Iterator getBranchWalker(java.lang.String path, 
                                              java.lang.String mimeType)
    {//returns an interator over the items in a given branch with a given mimetype
    
        //FIXME: not implemented
        return null;
    
    }
    
    public ItemLock[] getCollectionLocks(Transaction xact) 
    {
        //FIXME: not implemented
        return null;
    }
    
    
    public ItemLock getItemLock(Transaction xact, RepositoryItem item)
	throws FsException
    {
	//return first lock on the given item
        //FIXME: not implemented
        return null;
    }
        
    public ItemLock[] getItemLocks(Transaction xact, RepositoryItem item)
	throws FsException
    {
	//return all the locks on the given item
        //FIXME: not implemented
        return null;
    }
    
    public java.util.Hashtable  getNullResourceLocks()
    {
        //FIXME: not implemented
        return null;
    }
    
    public RepositoryItem getOrCreateFolder(Transaction xact, 
                                            User user, java.lang.String path)
	throws FsException
    {
	//gets the named folder on behalf of the given user for updating,
        //create the folder if it doesn't exist
    
	//         System.out.println("FSRepositoryImpl : getOrCreateFolder : calling getRepositoryItemByPath = " 
	//                            + path);
	
        RepositoryItem item =  getRepositoryItemByPath(user, path);
	
        if (item == null) {
	    
            // couldn't get item, let's see if we can find its parent and create
            System.out.println("path " + path + 
                               "not found, gonna try to create");
            
            if (path.length() < 2) {
                return null;
            }
            
            String[] parts = RepositoryUtil.splitPath(path);
            RepositoryItem parent = null;
            try {
                // perhaps we'll recurse
                parent = getOrCreateFolder(xact, user, parts[0]);
                if (parent == null) {
                    return null;
                }
                
                return parent.createChildDirItem(user, parts[1]);
            } catch (Exception ex2) {
                return null;
            }
	    
        }
	
        return item;
    }
    
    /**
     * returns the identified item, writeable under the given transaction
     */
    public RepositoryItem getRepositoryItem(Transaction xact, User user, String itemID)
	throws FsException
    {
        return getRepositoryItem(user, itemID);
    }
    
    /**
     * gets the identified item on behalf of the given user (Note: item's itemID is assumed == item's path)
     */
    public RepositoryItem getRepositoryItem(User user, String itemID)
	throws FsException
    {
        return getRepositoryItemByPath(user, itemID);
    }
    
    /**
     * gets the named item on behalf of the given user for updating
     */
    public RepositoryItem getRepositoryItemByPath(Transaction xact, User user, 
                                                  java.lang.String path)
	throws FsException
    {
        //System.out.println("FSRepositoryImpl : getRepositoryItemByPath(xact,user,itemID) : callin getRepositoryItemByPath (user, path) : path= " + path);
        return getRepositoryItemByPath(user, path);
    }
    
    
    /**
     * gets the named item on behalf of the given user
     */
    public RepositoryItem getRepositoryItemByPath( User user, String path )
	throws FsException
    { 
        FSRepositoryItem repositoryItem = getFromCaches(user, path);
        if (repositoryItem == null || repositoryItem.hasChanged()) {    
            try {
                //System.out.println("FSRepositoryImpl : getRepositoryItemByPath { " + path + " }");
    
                repositoryItem = new FSRepositoryItem(this, user, path);
                if (repositoryItem.exists()) {
                    addToCaches(user, repositoryItem);
                } else {
                    repositoryItem = null;
                }
            
            } catch (Exception e) {
                System.out.println("Error: Cannot find repository item {"+ path +"}" + e.getMessage());
                //e.printStackTrace();
            }
        }
        return repositoryItem;
    }
    
    // 
    private FSRepositoryItem getFromCaches(User user, String path)
    {
        // just one cache, for now
        FSRepositoryItem item =  null;
        if (_cache != null) {
            item = (FSRepositoryItem) _cache.get(path);
        }
        return item;
    }


    //
    private void addToCaches(User user, FSRepositoryItem repositoryItem) 
    {
        // just one cache, for now
        if (_cache != null) {
            String key = repositoryItem.getItemID();
            try {
                _cache.put(key, repositoryItem);
            } catch (Exception ex) {
                Log.getLogger().warn(ex.getMessage(), ex);
            }
        }
    }

    public ItemLock[] getResourceLocks() 
    { //???
        //FIXME: not implemented
        return null;
    }
    
    
    public boolean isItemCheckedout(RepositoryItem item, User user)
    {//return true if item is already checked out
    
        //FIXME: not implemented
        return false;
	
    }
    
    public boolean isItemLocked(RepositoryItem item, String ifHeader, User user)
    {//Check to see if a resource is currently write locked.
    
        //FIXME: not implemented
        return false;
    
    }
    
    public boolean isReadOnly()
    {//gets the read-only status of the repository
    
        //FIXME: not implemented
        return false;
    
    }
    
    public void listDeletedRepositoryItems(User user, ContentHandler dest)
    {//retrieves the list of repository items and writes it to the given content handler
    
        //FIXME: not implemented
    }
    
    /**
     * gets the files identified by paths to a jarfile, on behalf of the given user
     */
    public void loadJar(InputStream jarStream, User user, String basePath)
        throws FsException
    {
        FSRepositoryUtil.loadJar(this, jarStream, user, basePath);
    }
    
    public ItemLock newLock(java.lang.String path)
    {//create a new (tentative) lock on an item in the repository
    
        //FIXME: not implemented
        return null;
    }
        

    public void purgeItemAndVersions(Transaction xact, User user,
                                     java.lang.String recurse, 
				     RepositoryItem item)
	throws FsException
    {
	//permanently deletes the given item and its versions.
	FSRepositoryItem repositoryItem = (FSRepositoryItem)item;
        try {
            repositoryItem.purgeSelf();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FsException(e);
        }
    }
    

    public void purgeItemVersions(Transaction xact, User user,
                                  int keepRecent,
                                  java.lang.String recurse, RepositoryItem item)
	throws FsException
    {
	//permanantly deletes the versions of the given item.
	//FIXME: not implemented
	
    }
    
    public void purgeRepositoryItem(Transaction xact, User user, 
				    java.lang.String recurse, RepositoryItem item)
	throws FsException 
    {
	//purges the given item on behalf of the given user as part of a larger transaction.
        FSRepositoryItem repositoryItem = (FSRepositoryItem)item;
        try {
            repositoryItem.purgeSelf();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FsException(e);
        }
    }
    
    /**
     * restore the given item
     */
    public void restoreRepositoryItem(Transaction xact,
                                      User user,
                                      java.lang.String recurse, 
                                      RepositoryItem item)
    {
        //FIXME: not implemented
    }
    
    /**
     * sets the read-only status of the repository
     */
    public void setReadOnly(boolean readOnlyStatus)
    {
        //FIXME: not implemented
    }
    
    /**
     *  return a new object for managing commit/rollback on some set of actions on the repository
     */
    public Transaction startTransaction()
        throws FsException
    {
        try {
            return new FSTransaction();
        } catch (Exception ex) {
            throw new FsException(ex);
        }
    }
    
    public void updateLock(Transaction xact, ItemLock lock, User user)
    {//          updates a lock
    
        //FIXME: not implemented
    }
    
    /**
     * gets the files identified by paths to a jarfile, on behalf of the given user
     */
    public void writeJar(OutputStream jarStream, User user,
                         String[] paths, String basePath)
        throws FsException
    {   
        FSRepositoryUtil.writeJar(this, jarStream, user, paths, basePath);
    }
    
    boolean isCreatingMeta() 
    {
        return this.isCreatingMeta;
    }
    
    public Iterator getItemsPendingIndex(String collectionPathPrefix, String pathRegex, Date createdSince, Date createdThrough) 
	throws FsException
    {
        return null;
    }
    
    public  Iterator getItemsPendingIndexRemoval(String collectionPathPrefix, String pathRegex, Date createdSince, Date createdThrough) 
	throws FsException
    {
        return null;
    }

    public void markItemIndexed(RepositoryItem which) 
    {
        return;
    }

    public void markItemDeIndexed(RepositoryItem which)
    {
        return;
    }
}
