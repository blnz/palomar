package com.blnz.fxpl.fs.impl;

import com.blnz.fxpl.fs.FsRepository;
import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.FsException;
import com.blnz.fxpl.fs.Transaction;

import com.blnz.fxpl.security.PermBits;

import com.blnz.fxpl.security.User;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import  org.xml.sax.helpers.XMLFilterImpl;

import java.util.Date;
import java.io.Writer;
import java.io.Reader;
import java.io.InputStream;
import java.io.OutputStream;
/**
 * an entity object representing a repository item
 */
public class MountedItemWrapper implements RepositoryItem
{
    public RepositoryItem _wrapped;
    IDStringLocalizer _ids;
    IDStringLocalizer _names;
    
    //
    public MountedItemWrapper(RepositoryItem wrapped, IDStringLocalizer ids, IDStringLocalizer names)
    {
	_wrapped = wrapped;
	_ids = ids;
	_names = names;
    }
    
    /**
     *
     */
    public RepositoryItem createChildBinaryItem(User user, String name,
						String mimeType)
	throws FsException
    { 
	return new MountedItemWrapper(_wrapped.createChildBinaryItem(user,  
								     _names.localize(name),
								     mimeType),
				      _ids, _names);
    }

    
    /**
     *
     */	
    public RepositoryItem createChildBinaryItem(User user, String name,
						String mimeType,
						InputStream is)
	throws FsException
    {	
	return new MountedItemWrapper(_wrapped.createChildBinaryItem(user,  
								     _names.localize(name),
								     mimeType, is),
				      _ids, _names);
    }
    
    /**
     * creates a new FOLDER item as a child of this FOLDER
     */
    public  RepositoryItem createChildDirItem(User user, String name)
	throws FsException
    {
	return new MountedItemWrapper(_wrapped.createChildDirItem(user,  
								  _names.localize(name)),
				      _ids, _names);
    }
    
    /**
     *
     */
    public  RepositoryItem createChildTextItem(User user, String name,
					       String mimeType)
	throws FsException
    {
	return new MountedItemWrapper(_wrapped.createChildTextItem(user,  
								   _names.localize(name),
								   mimeType),
				      _ids, _names);
    }
    
    /**
     *          creates a new item as a child of this directory
     */
    public  RepositoryItem  createChildTextItem(User user, String name,
						String mimeType, InputStream is)
	throws FsException
    {
	return new MountedItemWrapper(_wrapped.createChildTextItem(user,  
								   _names.localize(name),
								   mimeType, is),
				      _ids, _names);
    }
    
    public  RepositoryItem createChildXMLItem(User user, String name,
					      String mimeType)
	throws FsException
    {

	return new MountedItemWrapper(_wrapped.createChildXMLItem(user,  
								   _names.localize(name),
								   mimeType),
				      _ids, _names);

    }
    
    public  RepositoryItem createChildXMLItem(User user, String name, 
					      String mimeType, InputStream is)
	throws FsException
    {

	return new MountedItemWrapper(_wrapped.createChildXMLItem(user,  
								   _names.localize(name),
								   mimeType, is),
				      _ids, _names);

    }
    
    /**
     * mark this item as no longer active
     * @deprecated 
     */
    public int deleteSelf()
	throws Exception
    {
	return _wrapped.deleteSelf();
    }
    
    
    public int deleteSelf(java.sql.Timestamp deleteTime, boolean isSubItem)
	throws Exception
    {
	return _wrapped.deleteSelf(deleteTime, isSubItem);
    }
    
    public int purgeSelf()
	throws Exception
    {
	return _wrapped.purgeSelf();
    }
    
    private RepositoryItem[] wrapItemList(RepositoryItem[] list)
    {
        RepositoryItem[] newList=null;
        if (list != null) {
            newList = new RepositoryItem[list.length];
            for (int i = 0; i < list.length; ++i) {
                newList[i] = new MountedItemWrapper(list[i], _ids, _names);
            }
        }
        return newList;
    }

    public RepositoryItem[] getAllVersions()
	throws FsException
    {
	return wrapItemList(_wrapped.getAllVersions());
    }
    
    public java.lang.String getBaseURI()
    {
	return _wrapped.getBaseURI();
    }
    
    public RepositoryItem[] getChildren()
	throws FsException
    {
	return wrapItemList(_wrapped.getChildren());
    }
    

    //     public RepositoryItem[] getFolderChildren()
    //     {
    // 	return wrapItemList(_wrapped.getFolderChildren());
    //     }
    
    
    public String getCreateTime()
    {
	return _wrapped.getCreateTime();
    }
    
    
    public String getDAVProperty(String propertyName)
    {
	return _wrapped.getDAVProperty(propertyName);
    }
    
    public RepositoryItem[] getDeletedChildren()
	throws FsException
    {
	return wrapItemList(_wrapped.getDeletedChildren());
    }
    
    
    /**
       return a string representing the directory path in which this item lives i.e.
    */
    public String getDirNamePath()
	throws FsException
    {
	return _names.globalize(_wrapped.getDirNamePath());
    }
    
    /**
     * return the normalized full path name for the item within the repository.
     */
    public  String getFullPath()
    {     
	return _names.globalize(_wrapped.getFullPath());
    }
    
    /**
     * gets a java byte InputStream for reading the binary data content from the repository
     */
    public InputStream getInputStream()
	throws FsException
    {
	return _wrapped.getInputStream();
    }
    
    public String getItemID()
    {
	return _ids.globalize(_wrapped.getItemID());
    }
    
           
    public int getItemType()
    {
	return _wrapped.getItemType();
    }
    
    /**
     * get the item's MimeType
     */
    public String getMimeType()
    {
	return _wrapped.getMimeType();
    }
    
    /**
     * get the item's name
     */
    public String getName()
    {

	return _names.globalize(_wrapped.getName());
    }
    
    /**
     * get an OutputStream for writing binary data to the repository item
     */
    public final OutputStream getOutputStream()
	throws FsException
    {
	return _wrapped.getOutputStream();
    }
    
    public final String getOwnerID()
    {
	return _wrapped.getOwnerID();
    }
    
    /**
     * returns the integer identifier associated with the item's owning group
     */
    public final String getOwningGroupID()
    {
	return _wrapped.getOwningGroupID();
    }
    
    
    /**
     * a bitmask authorization scheme
     */
    public final PermBits getPermissions()
    {
	return _wrapped.getPermissions();
    }
    
    /**
     * returns the revision number of this item
     */
    public int 	getRevisionNumber()
    {	
	return _wrapped.getRevisionNumber();
    }
    
    public String getUpdateTime()
    {
	return _wrapped.getUpdateTime();
    }
    
    /**
     * true if this item represents the latest (current) {//revision of the item with this name
     */
    public  boolean isLatest()
    {
    	return _wrapped.isLatest();
	
    }
    
    
    /** emit the description of this item as XML.*/
    public void	list(User user, ContentHandler dest, int depth)
	throws SAXException, FsException
    {
	XMLFilterImpl filter = new LocalFilter(_ids, _names);
	filter.setContentHandler(dest);
	_wrapped.list(user, filter, depth);
    }
    
    /**
     * emit the description of this item, and its folder descendents as XML.
     */
    public  void listFolders(User user, ContentHandler dest, int depth)
	throws SAXException, FsException
    {
	XMLFilterImpl filter = new LocalFilter(_ids, _names);
	filter.setContentHandler(dest);
	_wrapped.listFolders(user, filter, depth);
    }
    
        
    /**
     * collect the attributes we'd put on an XML element representing this item
     */
    public Attributes listedAttributes(User user)
    {
 	AttributesImpl attrs = new AttributesImpl();
                
//         //id
//         String id = getFullPath();
//         if (id == null || id.equals("")){
//             id = "";
//         }
//         attrs.addAttribute("", "id", "id",
//                            "CDATA", id);
//         //name
//         String name = getName();
//         if (name == null || name.equals("")){
//             name = "";
//         }
//         attrs.addAttribute("", "name", "name",
//                            "CDATA", name);
        
//         //### ###
	
//         //revision
//         String version = getRevisionNumber() + "";
//         if (version == null || version.equals("")){
//             version = "";
//         }
//         attrs.addAttribute("", "revision", "revision",
//                            "CDATA", version);
	
// 	// is it current or older version
//         String latest = isLatest() ? "yes" : "no";
//         attrs.addAttribute("", "current", "current",
//                            "CDATA", latest);
                
//         //mimeType
//         String mimeType = getMimeType();
//         if (mimeType == null || mimeType.equals("")) {
//             mimeType = "";
//         }
//         attrs.addAttribute("", "mimeType", "mimeType",
//                            "CDATA", mimeType);
//         //docID
//         String docID = getFullPath();//getDocID();
//         if (docID == null || docID.equals("")){
//             docID = "";
//         }
//         attrs.addAttribute("", "docID", "docID",
//                            "CDATA", docID);
//         //parentID
//         String parentID = this.getParentID() + "";
//         if (parentID == null || parentID.equals("")){
//             parentID = "";
//         }
//         attrs.addAttribute("", "parent", "parent",
//                            "CDATA", parentID);
//         //Item Status
//         String status = "" + getItemStatus();//FIXME: getItemStatus() + "";
//         if (status == null || status.equals("")){
//             status="";
//         }
//         attrs.addAttribute("", "status", "status",
//                            "CDATA", status);
//         //Owner
//         String owner = getOwnerID() + "";
//         if (owner == null || owner.equals("")){
//             owner = "";
//         }
//         attrs.addAttribute("", "ownerID", "ownerID",
//                            "CDATA", owner);
//         //Group
//         String group = getOwningGroupID()+"";
//         if (group == null || group.equals("")) {
//             group = "";
//         }
//         attrs.addAttribute("", "owningGroupID", "owningGroupID",
//                            "CDATA", group);
                
//         //CreateTime
//         String createTime = getCreateTime();
//         if (createTime == null || createTime.equals("")) {
//             createTime = "";
//         }
//         attrs.addAttribute("", "createTime", "createTime",
//                            "CDATA", createTime);
//         //UpdateTime
//         String updateTime = getUpdateTime();
//         if (updateTime ==null || updateTime.equals("")) {
//             updateTime= "";
//         }
//         attrs.addAttribute("", "lastUpdated", "lastUpdated",
//                            "CDATA", updateTime);
//         //Permissions
//         String permissions = getPermissions().toInt()+"";
//         if (permissions == null || permissions.equals("")) {
//             permissions= "";
//         }
//         attrs.addAttribute("", "permissions", "permissions",
//                            "CDATA", permissions);
                
//         String dirPath = null;
//         try {
//             dirPath = getDirNamePath();
//         } catch (Exception ex) {
//         }
                
//         if (dirPath == null || dirPath.equals("")){
//             dirPath= "";
//         }
//         attrs.addAttribute("", "dirPath", "dirPath",
//                            "CDATA", dirPath);
                
//         //lte: provided so that no extra processing is needed for creating the view.
//         attrs.addAttribute("", "read", "read", "CDATA",
//                            SecurityServiceHome.getSecurityService().checkRead(user, this)+"");
//         attrs.addAttribute("", "write", "write", "CDATA",
//                            SecurityServiceHome.getSecurityService().checkWrite(user, this)+"");
//         attrs.addAttribute("", "exe", "exe", "CDATA",
//                            SecurityServiceHome.getSecurityService().checkExecute(user, this)+"");
                
        return attrs;
    }
    
    public  void move(User user, String fullpath)
	throws FsException
    { 
	_wrapped.move(user, _names.localize(fullpath));
    }
    
    public  Reader openCharReader()
	throws FsException
    {
	return _wrapped.openCharReader();
    }
    
    public  Writer openCharWriter()
	throws FsException
    {

	return _wrapped.openCharWriter();
    }
    

    /**
     * get a SAX XMLReader for reading (parsing) {//the repository XML content
     */
    public  XMLReader openXMLReader()
	throws FsException
    {
	return _wrapped.openXMLReader();
    }
    
    /**
     * get a SAX ContententHandler for writing XML content to the repository
     */
    public ContentHandler openXMLWriter() throws FsException
    {
	return _wrapped.openXMLWriter();
    }
    
    /**
     * set the time at which this was created
     */
    public void setCreateTime(Date when)
	throws FsException
    {
	_wrapped.setCreateTime(when);
    }
    
    
    /**
     * set the named webdav property associated with this item
     */
    public  void setDAVProperty(String propertyName, java.lang.String propertyValue)
	throws FsException
    {
	_wrapped.setDAVProperty(propertyName, propertyValue);
    }
    

    /**
     * set the item type identifier
     */
    public void setItemType(int type)
	throws FsException
    { 
	_wrapped.setItemType(type);
    }

    
    /**
     * sets the item's name
     */
    public void	setName(String name)
	throws FsException
    {
	_wrapped.setName(name);
    }
    
    //FIXME: kill integer owner id ?
    /** set the item's ownership */
    public void	setOwnerID(String id)
	throws FsException
    {
	_wrapped.setOwnerID(id);
    }
    
    //FIXME: kill integer owning group id ?
    /**   set the item's owning group */
    public void setOwningGroupID(String id)
	throws FsException
    { 
	_wrapped.setOwningGroupID(id);
    }
    
    /** set the item's ownership */
    public void	setOwnerID(Transaction xact, String id)
	throws FsException
    {
	_wrapped.setOwnerID(xact, id);
    }
    
    //FIXME: kill integer owning group id ?
    /**   set the item's owning group */
    public void setOwningGroupID(Transaction xact, String id)
	throws FsException
    { 
	_wrapped.setOwningGroupID(xact, id);
    }
    
    
    
    /**set the location of the repository item */
    public void	setFullPath(String resourcePath)
    {	
	//
    }


    public void	setPermissions(PermBits permissions)
	throws FsException
    {
	_wrapped.setPermissions(permissions);
    }

    public void	setPermissions(Transaction xact, PermBits permissions)
	throws FsException
    {
	_wrapped.setPermissions(xact, permissions);
    }
    
    /** 
     * sets the last modified time
     */
    public void	setUpdateTime(Date updated)
	throws FsException
    {
	_wrapped.setUpdateTime(updated);
    }

    /**
     * true if item with this name represents the checked out version of the item with this name
     */
    public boolean isCheckedoutVersion()
    {
	return _wrapped.isCheckedoutVersion();
    }
    
    /**
     * get version of an item with this name which has been already 
     *   checked out, as a part of a larger transaction
     */
    public RepositoryItem getCheckedoutVersion(Transaction xact,
					       User user)
	throws FsException
    {
	return _wrapped.getCheckedoutVersion(xact, user);
    }

    /**
     * checkout a version of the item with this name, as a part of a larger transaction
     *   @return a checked out version which not accessible to other users 
     */
    public RepositoryItem checkoutVersion(Transaction xact,
					  User user)
	throws FsException
    {
	return _wrapped.checkoutVersion(xact, user);    
    }
    
    /**
     * checkin a checkedout version of the item with this name, as a part of a larger transaction
     */
    public void checkinCheckedoutVersion(Transaction xact,
					 User user)
	throws FsException
    {
	_wrapped.checkinCheckedoutVersion(xact, user);    
    }
    
    /**
     * uncheckout (cancel a checkout) by removing a checkedout version of item with this name, as a part of a larger transaction
     */
    public void uncheckoutCheckedoutVersion(Transaction xact,
					    User user)
	throws FsException
    {
	_wrapped.uncheckoutCheckedoutVersion(xact, user);
     }
    
    public String toString()
    {
	return _wrapped.toString();
    }

    /**
     * Associate an application object with a key for this item.
     *
     *  allows application code to associate any arbitrary object
     *  with any given RepositoryItem.  Implementations may refuse
     * to support this functionality.
     */
    public void setApplicationObject(Object key, Object value)
        throws UnsupportedOperationException
    { 
	_wrapped.setApplicationObject(key, value);
    }
    
    /**
     *  Return the application object identified by key for this item.
     *
     *  allows application code to associate any arbitrary object
     *  with any given RepositoryItem.  Implementations may refuse
     * to support this functionality.
     */
    public Object getApplicationObject(Object key)
        throws UnsupportedOperationException
    {
        return _wrapped.getApplicationObject(key);
    }

    /**
     *
     */
    public FsRepository getOwnerRepository()
    {
	return _wrapped.getOwnerRepository();
    }


    private class LocalFilter extends org.xml.sax.helpers.XMLFilterImpl
    {
	IDStringLocalizer _ids;
	IDStringLocalizer _names;
	public LocalFilter( IDStringLocalizer ids, IDStringLocalizer names)
	{
	    _ids = ids;
	    _names = names;
	}

	public void startElement(String namespace, String localname, String qname,
				 Attributes attrs)
	    throws SAXException
	{
	    // globalize name and id, here
	    super.startElement(namespace, localname, qname, attrs);
	}

    }


    public void dropFromCache() {
        _wrapped.dropFromCache();
    }

}
