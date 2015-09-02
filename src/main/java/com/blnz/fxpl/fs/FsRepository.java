
package com.blnz.fxpl.fs;

import com.blnz.fxpl.security.User;
import com.blnz.fxpl.fs.Transaction;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;
import java.util.Date;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *   content repository
 */
public interface FsRepository
{
    // repository object/file types
    public static final String FILE_TYPE_TEXT = "text";
    public static final String FILE_TYPE_BINARY = "binary";
    public static final String FILE_TYPE_XML = "xml";

    /**
     * sets the read-only status of the repository
     */
    public void setReadOnly(boolean readOnlyStatus);

    /**
     * gets the read-only status of the repository
     */
    public boolean isReadOnly();


    /**
     * a repository may wish to provide some services such as full-text indexing, 
     *  background processing, etc.  We need to decouple the from a repository's constructor
     */ 
    public void startServices();

    /**
     * retrieves the list of repository items and writes it to the given content
     * handler
     */
    public void listDeletedRepositoryItems(User user,
                                           ContentHandler dest ) throws FsException;

    /**
     *  gets the identified item on behalf of the given user
     */
    public RepositoryItem getRepositoryItem(User user, String itemID)
        throws FsException;
    
    /**
     *  returns the identified item, writeable under the given transaction
     */
    public RepositoryItem getRepositoryItem(Transaction xact ,
                                            User user, String itemID)
        throws FsException;
    
    /**
     * gets the named item on behalf of the given user
     */
    public RepositoryItem getRepositoryItemByPath(User user, String path)
        throws FsException;
    
    /**
     * gets the named folder on behalf of the given user for updating,
     *  create the folder if it doesn't exist
     */
    public RepositoryItem getOrCreateFolder(Transaction xact,
                                            User user, String path)
        throws FsException;
    
    /**
     * gets the named item on behalf of the given user for updating
     */
    public RepositoryItem getRepositoryItemByPath(Transaction xact,
    						  User user, String path)
        throws FsException;
    
    /**
     * deletes the given item on behalf of the given user as part of
     * a larger transaction. May simply mark the item as no longer
     * active, allowing later recovery of that item
     */
    public void deleteRepositoryItem(Transaction xact, 
                                     User user,
                                     String recurse,
    				     RepositoryItem item) 
        throws FsException;
    
    /**
     * permanantly deletes the given item.
     */
    public void purgeRepositoryItem(Transaction xact, 
                                    User user,
                                    String recurse,
                                    RepositoryItem item) 
        throws FsException;
    
    /**
     * permanantly deletes the given item and its versions.
     */
    public void purgeItemAndVersions(Transaction xact, 
                                     User user,
                                     String recurse,
                                     RepositoryItem item) 
        throws FsException;
    
    /**
     *  permanently delete the versions of an item excluding
     *  the latest version and also excluding few of the recent versions
     *
     * @param keepRecent excludes number of recent versions as specified in this argument, from deletion.
     */
    public void purgeItemVersions(Transaction xact, User user, 
				  int keepRecent, String recurse, 
				  RepositoryItem item)
        throws FsException;
    
    /**
     * restore the given item
     */
    public void restoreRepositoryItem(Transaction xact, 
                                      User user,
                                      String recurse,
                                      RepositoryItem item ) 
        throws FsException;
    
    
    /**
     * create a new (tentative) lock on an item in the repository
     */
    public ItemLock newLock(String path);
    
    /**
     * commits the (tentative) lock
     */
    public void addLock(Transaction xact,  ItemLock newLock, User user)
        throws FsException;
    
    /**
     * deletes a webdav lock
     */
    public void deleteLock(Transaction xact, ItemLock lock, User user)
        throws FsException;
    
    
    /**
     * updates a webdav lock
     */
    public void updateLock(Transaction xact, ItemLock lock, User user)
        throws FsException;
    
    
    /**
     * return all the locks on the given item
     */
    public ItemLock[] getItemLocks(Transaction xact, RepositoryItem item) throws FsException;
    
    /**
     * return first lock on the given item
     */
    public ItemLock getItemLock(Transaction xact, RepositoryItem item) throws FsException;
    
    /**
     *
     */
    public ItemLock[] getResourceLocks();
    
    /**
     *
     */
    public Hashtable getNullResourceLocks();
    
    /**
     *
     */
    public ItemLock[] getCollectionLocks(Transaction xact);
    
    
    /**
     * returns true if the user can have access to resource lock
     */
    public boolean canAccessLock(User user, ItemLock lock);
    
    /**
     * Check to see if a resource is currently write locked.
     *
     * @param item the RepositoryItem under consideration
     * @param ifHeader "If" HTTP header which was included in the request
     * @param user User on whose behalf we're checking
     * @return boolean true if the resource is locked (and no appropriate
     * lock token has been found for at least one of the non-shared locks which
     * are present on the resource).
     */
    public boolean isItemLocked(RepositoryItem item, String ifHeader, User user);
    
    /**
     * return true if item is already checked out 
     */
    public boolean isItemCheckedout(RepositoryItem item, User user);
    
    /** 
     * return true if the checked out item can be accessed by a user
     * @param item checked out version of an repository item
     * @param user User to test
     */
    public boolean canAccessCheckedoutItem(RepositoryItem item, User user);
    
    /**
     *  delete all expired locks from the ItemLocks table
     */
    public void deleteAllExpiredLocks();
    	
    //     /**
    //      * get the repository base path on the file system 
    //      */  
    //     public String getBaseXfsDir();
    
    /**
     * returns an interator over the items in a given branch with a
     *  given mimetype
     */
    public Iterator getBranchWalker(String path, String mimeType)
        throws FsException;

    /**
     * indexes docs of a given mime type in a given section of the repository
     */
    public void doBranchIndex(final String mimeType, final String path, 
                              final int skip, final int count,
                              final boolean renew)
        throws FsException;

    /**
     * return a new object for managing commit/rollback on some
     * set of actions on the repository
     */
    public Transaction startTransaction()
        throws FsException;

}


