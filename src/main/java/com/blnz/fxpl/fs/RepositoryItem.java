package com.blnz.fxpl.fs;

import com.blnz.fxpl.security.PermissionSet;
import com.blnz.fxpl.security.PermBits;
import com.blnz.fxpl.security.User;
import com.blnz.fxpl.fs.Transaction;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import java.sql.Timestamp;
import java.util.Date;

import java.io.Writer;
import java.io.Reader;
import java.io.OutputStream;
import java.io.InputStream;

/**
 * Provides those operations common to all objects in the repository
 */
public interface RepositoryItem extends PermissionSet
{

    /**
     * item is of type "FOLDER"
     */
    public static final int FOLDER = 1;

    /**
     * item is of type "XML"
     */
    public static final int XML = 2;

    /**
     * item is of type "BINARY"
     */
    public static final int BINARY = 3;

    /**
     * item is of type "TEXT"
     */
    public static final int TEXT = 4;

    public static final int STATUS_GONER = -1;
    public static final int STATUS_FS = 1;
    public static final int STATUS_DOCNODES = 2;

    public static final int STATUS_FS_AND_DOCNODES = 3;

    public static final int STATUS_CLOBS = 4;

    public static final int STATUS_FTI_PENDING = 8;
    public static final int STATUS_FTI_COMPLETE = 16;
    public static final int STATUS_FTI_REMOVAL_PENDING = 32;

    public static final int STATUS_PURGE_PENDING = 64;

    /**
     * @return the item's unique identifier
     */
    public String getItemID();

    /**
     * @return the unique ID of the item's owner
     */
    public String getOwnerID();

    /**
     * @return the Repository that manages this item
     */
    public FsRepository getOwnerRepository();
    
    /**
     *  set the item's ownership 
     */
    public void setOwnerID(String id) 
        throws FsException;
    /**
     *  set the item's ownership 
     */
    public void setOwnerID(Transaction xact, String id) 
        throws FsException;
    
    /**
     * returns the string identifier associated with the item's owning group
     */
    public String getOwningGroupID();
    
    /**
     * a bitmask authorization scheme
     */
    public PermBits getPermissions() ;

    /**
     * a bitmask authorization scheme
     */
    public void setPermissions(PermBits permissions) 
        throws FsException;

    /**
     * a bitmask authorization scheme
     */
    public void setPermissions(Transaction xact, PermBits permissions) 
        throws FsException;

    /**
     * set the item's owning group
     */
    public void setOwningGroupID(String id)
        throws FsException;

    /**
     * set the item's owning group
     */
    public void setOwningGroupID(Transaction xact, String id)
        throws FsException;

    /**
     * @return TEXT, XML, FOLDER or BINARY
     */
    public int getItemType();

    
    /**
     * set the item type identifier
     */
    public void setItemType(int type) throws FsException;

    /**
     * get the item's name
     */
    public String getName();

    /**
     *  return the normalized full path name for the item within the repository.
     *  e.g. "/testje/data/myDoc.xml"
     */
    public String getFullPath();

    /**
     * return a string representing the directory path in which
     * this item lives i.e. "/testje/data"
     */
    public String getDirNamePath() throws FsException;

    /**
     * sets the item's name
     */
    public void setName(String name) throws FsException;

    /**
     * changes the name and/or moves the item to another folder
     */
    public void move(User user, String fullpath) throws FsException;

    /**
     * @return the time at which this was created 
     */ 
    public String getCreateTime();

    /**
     * set the time at which this was created
     */
    public void setCreateTime(Date when) throws FsException;

    /**
     *  @return the time at which it was last modified
     */
    public String getUpdateTime();

    /**
     * sets the last modified time
     */
    public void setUpdateTime(Date updated) throws FsException;
    
    /**
     * get the item's MimeType
     */
    public String getMimeType();


    /**
     * @return all items that are children of this  FOLDER item
     */
    public RepositoryItem[] getChildren() 
        throws FsException;


    /**
     * @return all items that are children of this  FOLDER item
     *  that have been marked inactive and for which there is no
     *  currently active version
     */
    public RepositoryItem[] getDeletedChildren() 
        throws FsException;


    /**
     * @return all revisions of this item that are still
     * maintained in the system.
     */
    public RepositoryItem[] getAllVersions() 
        throws FsException;

    /**
     * emit the description of this item as XML.
     * @param dest a SAX2 ContentHandler to write the xml
     * @param depth in the case of folders, the 
     *   depth to recursively list children
     */
    public void list(User user, ContentHandler dest, int depth) 
        throws SAXException, FsException;


    /**
     * Associate an application object with a key for this item.
     *
     *  allows application code to associate any arbitrary object
     *  with any given RepositoryItem.  Implementations may refuse
     * to support this functionality.
     */
    public void setApplicationObject(Object key, Object value)
        throws UnsupportedOperationException;
    
    /**
     *  Return the application object identified by key for this item.
     *
     *  allows application code to associate any arbitrary object
     *  with any given RepositoryItem.  Implementations may refuse
     * to support this functionality.
     */
    public Object getApplicationObject(Object key)
        throws UnsupportedOperationException;

    /**
     * emit the description of this item, and its folder descendents as XML.
     * @param dest a SAX2 ContentHandler to write the xml
     * @param depth in the case of folders, the 
     *   depth to recursively list children
     */
    public void listFolders(User user, ContentHandler dest, int depth) 
        throws SAXException, FsException;

    /**
     *  creates a new FOLDER item as a child of this FOLDER 
     */
    public RepositoryItem createChildDirItem(User user, String name)
        throws FsException;

    /**
     *  creates a new XML item as a child of this FOLDER
     */
    public RepositoryItem createChildXMLItem(User user, String name, 
                                             String mimeType)
        throws FsException;
    
    /**
     *  creates a new XML item as a child of this FOLDER
     */
    public RepositoryItem createChildXMLItem(User user, String name, 
                                             String mimeType, 
                                             InputStream is )
        throws FsException;
    

    /**
     *  creates a new item as a child of this directory
     */
    public RepositoryItem createChildTextItem(User user, String name,
                                              String mimeType)
        throws FsException;

    /**
     *  creates a new item as a child of this directory
     */
    public RepositoryItem createChildTextItem(User user, String name,
                                              String mimeType, InputStream is )
        throws FsException;

    /**
     *  creates a new item as a child of this directory
     */
    public RepositoryItem createChildBinaryItem(User user, String name,
						String mimeType)
        throws FsException;
    
    /**
     *  creates a new item as a child of this directory
     */
    public RepositoryItem createChildBinaryItem(User user, String name,
						String mimeType,  InputStream is )
        throws FsException;
    
    /**
     * deletes the item from the repository
     */
    public int deleteSelf()
	throws Exception;
    
    
    /**
     * permanently purges the item from the repository
     */
    public int purgeSelf()
	throws Exception;
    
    /**
     *
     */
    public int deleteSelf(Timestamp deleteTime, boolean isSubItem )
    throws Exception;
    
    /**
     * return the base URI for this item suitable for
     *  resolving relative references
     */
    public String getBaseURI();
    
    /**
     * get a SAX ContententHandler for writing XML
     * content to the repository
     */
    public ContentHandler openXMLWriter()
        throws FsException;

    /**
     * get a Writer for writing character (text) content to
     * the repository item
     */
    public Writer openCharWriter()
        throws FsException;

    /**
     * get an OutputStream for writing binary data to
     * the repository item
     */
    public OutputStream getOutputStream()
        throws FsException;

    /**
     * get a SAX XMLReader for reading (parsing) the
     * repository XML content
     */
    public XMLReader openXMLReader()
        throws FsException;

    /**
     * gets a java character stream Reader for reading the
     * item's character content from the repository
     */
    public Reader openCharReader()
        throws FsException;

    /**
     * gets a java byte InputStream for reading the
     * binary data content from the repository
     */
    public InputStream getInputStream()
        throws FsException;

    /**
     *  get the named webdav property associated with this item
     */
    public String getDAVProperty(String propertyName);
    
    /**
     *  set the named webdav property associated with this item
     */
    public void setDAVProperty(String propertyName, String propertyValue)
	throws FsException;
    
    /**
     * returns the revision number of this item
     */
    public int getRevisionNumber();

    /**
     * true if this item represents the latest (current) revision of the item with this name
     */
    public boolean isLatest();
    
    /**
     * true if item with this name represents the checked out version of the item with this name
     */
    public boolean isCheckedoutVersion();
    
    /**
     * get version of an item with this name which has been already checked out, as  a part of a larger transaction 
     */
    public RepositoryItem getCheckedoutVersion(Transaction xact, User user)
	throws FsException;
    
    /**
     * checkout a version of the item with this name, as  a part of a larger transaction .
     *
     * @param xact transaction this operation participates in
     * @param user the User on whose behalf we're checking out
     * @return a checked out version which not accessible to other users
     */
    public RepositoryItem checkoutVersion(Transaction xact, User user)
	throws FsException;
    
    /**
     * checkin a checkedout version of the item with this name, as  a part of a larger transaction 
     *
     * @param xact transaction this operation participates in
     * @param user the User on whose behalf we're checking out
     */
    public void checkinCheckedoutVersion(Transaction xact, User user)
	throws FsException;
    
    /**
     * uncheckout (cancel a checkout) by removing a checkedout version of item with this name, 
     * as  a part of a larger transaction 
     *
     * @param xact transaction this operation participates in
     * @param user the User on whose behalf we're checking out
     */
    public void uncheckoutCheckedoutVersion(Transaction xact, User user)
	throws FsException;
    
    /**
     * if the repository implementation has an item cache associated with it, drop this item from
     * the cache.
     */
    public void dropFromCache();
}


