package com.blnz.fxpl.fs.impl;

import com.blnz.fxpl.xform.impl.OutStreamContentHandler;
import com.blnz.fxpl.fs.FsRepository;
import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.FsException;

import com.blnz.fxpl.log.Log;

import com.blnz.fxpl.security.PermBits;

import com.blnz.fxpl.fs.RepositoryUtil;

import com.blnz.fxpl.security.Security;
import com.blnz.fxpl.security.User;
import com.blnz.fxpl.fs.Transaction;
import com.blnz.fxpl.util.NetUtils;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;

import java.util.SimpleTimeZone;
import java.util.Locale;
import java.util.Date;
import java.util.Hashtable;

import java.sql.Timestamp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.Reader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import com.blnz.fxpl.fs.helpers.FSRepositoryUtil;
/**
 * an entity object representing a repository item
 */
public class FSRepositoryItem implements RepositoryItem
{
    // what repository do we belong to?
    protected  FSRepositoryImpl _repository = null;
    
    private String _createTime = "";

    private String _updateTime = "";
    
    private long _lastLoaded;
    
    private String _resourcePath = "";
    
    private PermBits _permissions = null;
    
    private int _itemType = -1;
    
    private User _user = null;

    private String _mimeType = "";
    
    private String _ownerID = "";
    private String _owningGroupID = "";
    
    private int _itemStatus = RepositoryItem.STATUS_FS;
    private File resource = null;    
    private Hashtable _appObjectTable = null;    
    private long _length = -1;



    // Simple date format for the creation date ISO representation (partial).
    protected static final SimpleDateFormat creationDateFormat = 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    protected static final SimpleDateFormat dbDateFormat = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    
    protected static final SimpleDateFormat rfc1123DateFormat = 
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    

    
    //use FSRepositoryUtil to create a repository item
    public FSRepositoryItem(FSRepositoryImpl repository, User user, String resourcePath) 
        throws Exception
    {
        _repository = repository;
        init(user, resourcePath);
    }
    

    private void init(User user, String resourcePath)
        throws Exception
    {
        setFullPath(resourcePath);
        
        //      setRepository();
        
        _user = user;
        
        this._resourcePath = resourcePath;
        resource = FSConnection.getResource(resourcePath);
        _lastLoaded = new Date().getTime();
        
        FSRepositoryUtil.setFSRepositoryItemProperties(this, resource);
        readMeta();
    }
    
    /**
     *
     */
    public User getUser()
    {
        return _user;
    }
    
    //     /**
    //      *
    //      */
    //     private void setRepository()
    //     {
    //  try {
    
    //      if ( _repository == null ) {
    //          XfsRepository repository = XfsRepositoryHome.getRepository();
    
    //          if (repository instanceof FSRepositoryImpl ) {
    //              _repository = (FSRepositoryImpl)repository;
    //          }
    //      }
    
    //      if ( _repository == null ) {
    //          throw new FsException("Cannot find or configure a filesystem repository");
    //      }
    
    //  } catch (Exception e){
    //      System.out.println("Error in configuring repository : " + e.getMessage());
    //      //e.printStackTrace();
    //  }
    //     }
    
    /**
     *
     */
    public FsRepository getOwnerRepository() 
    {
        return _repository;
    }
    
    /**
     *
     */
    public RepositoryItem createChildBinaryItem(User user, String name,
                                                String mimeType)
    { 
        
        ByteArrayInputStream placeHolder 
            = new ByteArrayInputStream(new String("").getBytes());
        
        RepositoryItem repositoryItem = null;
        
        try {
            
            repositoryItem = makeChild(user, name, mimeType, placeHolder);
            
        } catch (Exception e) {
            System.out.println("Error: FSRepositoryItem : "+ e.getMessage());
            //e.printStackTrace();
        }
        
        return repositoryItem;
    }
    
    /**
     *
     */ 
    public RepositoryItem createChildBinaryItem(User user, String name,
                                                String mimeType,
                                                InputStream is)
    {//          creates a new item as a child of this directory
        
        RepositoryItem repositoryItem = null;
        
        try {
            
            repositoryItem = makeChild(user, name, mimeType, is);
            
        } catch (Exception e) {
            System.out.println("Error: FSRepositoryItem : "+ e.getMessage());
            //e.printStackTrace();
        }
        
        return repositoryItem;
    
    }
    
    /**
     * creates a new FOLDER item as a child of this FOLDER
     */
    public  RepositoryItem createChildDirItem(User user, String name)
    {
        
        RepositoryItem repositoryItem = null;
        
        try {
            
            String newPath = getFullPath() + "/" + name;
            
            //webdav put
            File file = FSConnection.getResource(newPath);
            file.mkdirs();
            
            //getRepositoryItem
            repositoryItem =  _repository.getRepositoryItemByPath(user, newPath);
            
        } catch (Exception e) {
            System.out.println("Error: FSRepositoryItem : "+ e.getMessage());
            //e.printStackTrace();
        }
        
        return repositoryItem;
        
    }
    
    /**
     *
     */
    public  RepositoryItem createChildTextItem(User user, String name,
                                               String mimeType)
    {
        
        RepositoryItem repositoryItem = null;
        
        try {
            
            InputStream is = new ByteArrayInputStream(new String("").getBytes());
            
            repositoryItem = makeChild(user, name, mimeType, is);
            
                
        } catch (Exception e) {
            System.out.println("Error: FSRepositoryItem : "+ e.getMessage());
            //e.printStackTrace();
        }
        
        return repositoryItem;
    }
    
    /**
     *          creates a new item as a child of this directory
     */
    public  RepositoryItem  createChildTextItem(User user, String name,
                                                String mimeType, InputStream is)
    {
        
        RepositoryItem repositoryItem = null;
        
        try {
            
            repositoryItem = makeChild(user, name, mimeType, is);
            
        } catch (Exception e) {
            System.out.println("Error: FSRepositoryItem : "+ e.getMessage());
            //e.printStackTrace();
        }
        
        return repositoryItem;
    }
    
    public  RepositoryItem createChildXMLItem(User user, String name,
                                              String mimeType)
    {//          creates a new XML item as a child of this FOLDER
        
        RepositoryItem repositoryItem = null;
        
        try {
            InputStream is = new ByteArrayInputStream(new String("<placeholder/>").getBytes());
            repositoryItem = makeChild(user, name, mimeType, is);
        } catch (Exception e) {
            System.out.println("Error: FSRepositoryItem : "+ e.getMessage());
            //e.printStackTrace();
        }
        
        return repositoryItem;
    }
    
    public  RepositoryItem createChildXMLItem(User user, String name, 
                                              String mimeType, InputStream is)
    {
        //creates a new XML item as a child of this FOLDER
        
        RepositoryItem repositoryItem = null;
        
        try {
            repositoryItem = makeChild(user, name, mimeType, is);
        } catch (Exception e) {
            System.out.println("Error: FSRepositoryItem : "+ e.getMessage());
            //e.printStackTrace();
        }
        
        return repositoryItem;
    }
    
    /**
     * mark this item as no longer active
     * @deprecated As of FDX 3.5, use {@link deleteSelf( Timestamp, boolean )}
     */
    public int deleteSelf()
        throws Exception
    {
        Timestamp deleteTime = new Timestamp((new java.util.Date()).getTime() );
        return deleteSelf( deleteTime, true );
    }
    
    
    public int deleteSelf(java.sql.Timestamp deleteTime, boolean isSubItem)
        throws Exception
    {
        // System.out.println("FSRepositoryItem : deleteSelf : item =" + getItemID());
        
        // if (_repository != null && _repository.isReadOnly()) {
        //             throw new FsException("repository is read-only");
        //         }
        resource.delete();
        
        return 0;
    }
    
    /**
     * permanantly delete this item from the repository
     */
    public int purgeSelf()
        throws Exception
    {
	// System.out.println("FSRepositoryItem : deleteSelf : item =" + getItemID());
        
        // if (_repository != null && _repository.isReadOnly()) {
        //             throw new FsException("repository is read-only");
        //         }
        
	resource.delete();
        
        return 0;
	
    }
    


    public RepositoryItem[] getAllVersions()
    {
        //       deletes row/s from the XStore Item table, if applicable.
        //FIXME: to be implemented
        return null;
    }
    
    public java.lang.String getBaseURI()
    {
        //          return the base URI for this item suitable for resolving relative references
        try {
            String fdxUri =  "file:" + resource;
            // System.out.println("fsRepositoryItem:: getBaseURI() is [" + fdxUri + "]");

            return fdxUri;
        } catch (Exception e) {
            //e.printStackTrace();
            return "unknown";
        }
    }
    
    /**
     * return null if no children
     */
    public RepositoryItem[] getChildren()
    {
        RepositoryItem[] children = null;
        
        try {
            
            children = FSRepositoryUtil.getChildren(this, resource);
            
        } catch (Exception e) {
            System.out.println("Error: FSRepositoryItem : getChildren()  for " + resource.toString() + "  " + e.getMessage());
            e.printStackTrace();
        }
        
        return children;
    }
    
    public String getCreateTime()
    {
        return _createTime;
    }
    

    private long getContentLength()
    {
        if (_length < 0) {
            _length = resource.length();
        }
        return _length;
    }

    /**
     *  
     */
    public String getDAVProperty(String prop) 
    {

        if ("creationdate".equals(prop)) {
            return getISODate(getCreateTime());
        } else if ("getcontenttype".equals(prop)) {
            return getMimeType();
        } else if ("getcontentlength".equals(prop)) {
            return "" + getContentLength();
        } else if ("getlastmodified".equals(prop)) {
            return getHTTPDate(getUpdateTime());
        } else if (getApplicationObject(prop) != null) {
            return (String) getApplicationObject(prop);
        }
        
        //FIXME: other DAV properties needed
        //protected properties
        //comment
        //creator-displayname
        //supported-method-set
        //supported-live-property-set
        //supported-report-set
        //checked-in
        //auto-version
        //checked-out
        //predecessor-set
        //version-name

        //computed properties
        //version-history
        //successor-set
        //checkout-set

        //other
        //checkout-fork
        //checkin-fork

        // FIXME: allow getting of arbritary DAV properties
        return "";
    }

    /**
     *  
     */
    public void setDAVProperty(String prop, String val) throws FsException {
        setApplicationObject(prop, val);
    }
    
    
    public RepositoryItem[] getDeletedChildren()
    {
        //FIXME: to be implemented
        return null;
    }
    
    
    /**
       return a string representing the directory path in which this item lives i.e.
    */
    public String getDirNamePath()
        throws FsException
    {
        String pparts[] = RepositoryUtil.splitPath(_resourcePath);
        return pparts[0];
    }
    
    /**
     * return the normalized full path name for the item within the repository.
     */
    public  String getFullPath()
    {     
        return _resourcePath;
    }
    
    /**
     * gets a java byte InputStream for reading the binary data content from the repository
     */
    public InputStream getInputStream()
    {
        InputStream itemIS = null;
        
        try {
            
            itemIS = new FileInputStream(resource);
            
        } catch (Exception e) {
            
            System.out.println("Error: FSRepositoryItem : "+ e.getMessage());
            e.printStackTrace();
        
        }
        
        return itemIS;
    }
    
    public String getItemID()
    {
        return _resourcePath;
    }
    
           
    public int getItemType()
    {
        return _itemType;
    }
    
    public int getItemStatus()
    {
        return _itemStatus;
    }
    
    /**
     * get the item's MimeType
     */
    public String getMimeType()
    {
        return _mimeType;
    }
    
    /**
     * get the item's name
     */
    public String getName()
    {
        String name = "";
        try {
            
            String pparts[] = RepositoryUtil.splitPath(_resourcePath);
            
            name = pparts[1];
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            //e.printStackTrace();
        }
        
        return name;
    }
    
    /**
     * get an OutputStream for writing binary data to the repository item
     */
    public OutputStream getOutputStream()
    {
        OutputStream itemOS = null;
        try {
            
            itemOS = new FileOutputStream(resource);
        
        } catch (Exception e) {
            System.out.println("Error: creating FSRepositoryItemOutStream : "+ e.getMessage());
            //e.printStackTrace();
        }
        
        return itemOS;
    }
    
    public String getOwnerID()
    {
        return _ownerID;
    }
    
    /**f
     * returns the integer identifier associated with the item's owning group
     */
    public String getOwningGroupID()
    {
        return _owningGroupID;
    }
    
    


    /**
     * get the parent path ?
     * change repository api to return a String instead of a int
     * get the ID of the item (folder) {//which contains this item
     */
    protected String getParentID()
    {
        String parent = "";
        
        try {
            parent = getDirNamePath();
        
        } catch (Exception e) {
            System.out.println("Error: FSRepositoryItem : getParentID : "+ e.getMessage());
            //e.printStackTrace();
        }
        
        return parent;
    }
    
    
    /**
     * a bitmask authorization scheme
     */
    public PermBits getPermissions()
    {
        return _permissions;
    }
    
    /**
     * returns the revision number of this item
     */
    public int  getRevisionNumber()
    {   
        //PROPFIND version-name
        
        //FIXME: to be implemented
        return 1;
    }
    
    public String getUpdateTime()
    {
        //
        return _updateTime;
    }
    
    /**
     * true if this item represents the latest (current) {//revision of the item with this name
     */
    public  boolean isLatest()
    {
        //FIXME: to be implemented
        return true;
        
    }
    
    public boolean isTemporaryVersion()
    {
        //true if this item represents the temporary version of the item with this name
        
        //FIXME: return is this version checked out ?
        return false;
        
    }
    
    /** emit the description of this item as XML.*/
    public void list(User user, ContentHandler dest, int depth)
        throws SAXException, FsException
    {
        listSome(user, dest, depth, false);
    }
    
    /**
     * emit the description of this item, and its folder descendents as XML.
     */
    public  void listFolders(User user, ContentHandler dest, int depth)
        throws SAXException, FsException
    {
        listSome(user, dest, depth, true);
    }
    
    
    /**
     * emit the description of this item
     */
    private void listSome(User user,
                          ContentHandler dest,
                          int depth,
                          boolean foldersOnly)
        throws SAXException, FsException
    {
                
        String tagname = null;
        RepositoryItem[] children = null;
                
        int type = getItemType();
        if (foldersOnly && type != FOLDER) {
            return;
        }
        switch (getItemType()) {
        case TEXT:
            tagname = "text";
            break;
        case XML:
            tagname = "doc";
            break;
        case FOLDER:
            tagname = "folder";
            if (isLatest() && (depth > 0)) {
                //&&
                //SecurityServiceHome.getSecurityService().checkRead(user,
                //this)
                children = getChildren();
            }
            break;
        case BINARY:
            tagname = "binary";
            break;
        default:
            tagname = "item";
        }
                
        Attributes attrs = listedAttributes(user);
                
        dest.startElement("", tagname, tagname, attrs);
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                if (foldersOnly && children[i].getItemType() != FOLDER) {
                    // skip it
                } else if (foldersOnly) {  
                    // we have a folder & that's what we're listing
                    children[i].listFolders(user, dest, depth - 1);
                } else {
                    // we have something that's not a folder, but we want to list it anywhoo
                    children[i].list(user, dest, depth - 1);
                }
            }
        }
                
        dest.endElement("", tagname, tagname);
    }
        
    /**
     * collect the attributes we'd put on an XML element representing this item
     */
    public Attributes listedAttributes(User user)
    {
        AttributesImpl attrs = new AttributesImpl();
                
        //id
        String id = getFullPath();
        if (id == null || id.equals("")){
            id = "";
        }
        attrs.addAttribute("", "id", "id",
                           "CDATA", id);
        //name
        String name = getName();
        if (name == null || name.equals("")){
            name = "";
        }
        attrs.addAttribute("", "name", "name",
                           "CDATA", name);
        
        //### ###
        
        //revision
        String version = getRevisionNumber() + "";
        if (version == null || version.equals("")){
            version = "";
        }
        attrs.addAttribute("", "revision", "revision",
                           "CDATA", version);
        
        // is it current or older version
        String latest = isLatest() ? "yes" : "no";
        attrs.addAttribute("", "current", "current",
                           "CDATA", latest);
                
        //mimeType
        String mimeType = getMimeType();
        if (mimeType == null || mimeType.equals("")) {
            mimeType = "";
        }
        attrs.addAttribute("", "mimeType", "mimeType",
                           "CDATA", mimeType);
        //docID
        String docID = getFullPath();//getDocID();
        if (docID == null || docID.equals("")){
            docID = "";
        }
        attrs.addAttribute("", "docID", "docID",
                           "CDATA", docID);
        //parentID
        String parentID = this.getParentID() + "";
        if (parentID == null || parentID.equals("")){
            parentID = "";
        }
        attrs.addAttribute("", "parent", "parent",
                           "CDATA", parentID);
        //Item Status
        String status = "" + getItemStatus();//FIXME: getItemStatus() + "";
        if (status == null || status.equals("")){
            status="";
        }
        attrs.addAttribute("", "status", "status",
                           "CDATA", status);
        //Owner
        String owner = getOwnerID() + "";
        if (owner == null || owner.equals("")){
            owner = "";
        }
        attrs.addAttribute("", "ownerID", "ownerID",
                           "CDATA", owner);
        //Group
        String group = getOwningGroupID()+"";
        if (group == null || group.equals("")) {
            group = "";
        }
        attrs.addAttribute("", "owningGroupID", "owningGroupID",
                           "CDATA", group);
                
        //CreateTime
        String createTime = getCreateTime();
        if (createTime == null || createTime.equals("")) {
            createTime = "";
        }
        attrs.addAttribute("", "createTime", "createTime",
                           "CDATA", createTime);
        //UpdateTime
        String updateTime = getUpdateTime();
        if (updateTime ==null || updateTime.equals("")) {
            updateTime= "";
        }
        attrs.addAttribute("", "lastUpdated", "lastUpdated",
                           "CDATA", updateTime);
        //Permissions
        String permissions = getPermissions().toInt()+"";
        if (permissions == null || permissions.equals("")) {
            permissions= "";
        }
        attrs.addAttribute("", "permissions", "permissions",
                           "CDATA", permissions);
                
        String dirPath = null;
        try {
            dirPath = getDirNamePath();
        } catch (Exception ex) {
        }
                
        if (dirPath == null || dirPath.equals("")){
            dirPath= "";
        }
        attrs.addAttribute("", "dirPath", "dirPath",
                           "CDATA", dirPath);
                
        //lte: provided so that no extra processing is needed for creating the view.
        attrs.addAttribute("", "read", "read", "CDATA",
                           Security.getSecurityService().checkRead(user, this)+"");
        attrs.addAttribute("", "write", "write", "CDATA",
                           Security.getSecurityService().checkWrite(user, this)+"");
        attrs.addAttribute("", "exe", "exe", "CDATA",
                           Security.getSecurityService().checkExecute(user, this)+"");
                
        return attrs;
    }
    
    public  void move(User user, String fullpath)
    { // changes the name and/or moves the item to another folder
        //FIXME: to be implemented
        
        
    }
    
    public  Reader openCharReader()
    {//          gets a java character stream Reader for reading the item's character content from the repository
        

        //FIXME: to be implemented
        return null;
        
    }
    
    public  Writer openCharWriter()
    {//          get a Writer for writing character (text) content to the repository item
        
        //FIXME: to be implemented
        return null;
    }
    

    /**
     * get a SAX XMLReader for reading (parsing) {//the repository XML content
     */
    public  XMLReader openXMLReader()
        throws FsException
    {
        try {
            
            if (getItemType() != RepositoryItem.XML){
                //
                //  throw new FsException("Cannot open a XML Reader: Item not XML: " +
                //                       getItemType());
            }
            
            //      XMLReader xitemReader = TransformServiceHome.getTransformService().
            //                 createInputSourceReader(new InputSource(resource.getCanonicalPath()));

            //      return xitemReader;

            return new FSItemXMLReader(this);

        } catch (Exception e) {
            throw new FsException(e);
        }
    }
    
    /**
     * get a SAX ContententHandler for writing XML content to the repository
     */
    public ContentHandler openXMLWriter() throws FsException
    {
        if (_repository != null && _repository.isReadOnly()) {
            throw new FsException("repository is read-only, writing not permitted");
        }
                
        try {
                        
        
            File f = FSConnection.getResource(getDirNamePath()
                                              + File.separator + getName());
        
        
            if (f == null || !f.exists()){
                throw new Exception("Unable to store the file [" + f + "] in the file system");
            }

            if (getItemType() == FOLDER){
                throw new FsException("Cannot open a XML Writer on [" + f + "]: as type is FOLDER");
            }

            OutputStream os = new FileOutputStream(f);

            ContentHandler handler = new OutStreamContentHandler(os);
        
            return handler;
        } catch(Exception e){
            throw new FsException(e);
        }
    }
    
    public void setMimeType(String mimeType)
    {
        _mimeType = mimeType;
    }
    
    /**
     * set the time at which this was created
     */
    public void setCreateTime(Date when)
    {
        _createTime = new Timestamp(when.getTime()).toString();
    }
    
    
    //FIXME: not to be implemented ever ?
    /**
     * take note of the system and (optionally) public identifiers for a DTD which may be associated with this XML doc
     */
    public  void setDTDRef(String systemID, java.lang.String publicID)
    {
        //FIXME: to be implemented
    }
    

    /**
     * set the item type identifier
     */
    public void setItemType(int type)
    { 
        _itemType = type;
    }

    
    /**
     * set the item type identifier
     */
    public void setItemStatus(int status)
    { 
        _itemStatus = status;
    }

    /**
     * sets the item's name
     */
    public void setName(String name)
    {
    }
    
    //FIXME: kill integer owner id ?
    /** set the item's ownership */
    public void setOwnerID(String id)
    {
        _ownerID = id;
        persistMeta();
    }
    
    /** set the item's ownership */
    public void setOwnerID(Transaction xact, String id)
    {
        _ownerID = id;
        persistMeta();
    }
    
    //FIXME: kill integer owning group id ?
    /**   set the item's owning group */
    public void setOwningGroupID(String id)
    { 
        _owningGroupID = id;
        persistMeta();
    }
    
    /**   set the item's owning group */
    public void setOwningGroupID(Transaction xact, String id)
    { 
        _owningGroupID = id;
        persistMeta();
    }
    
    
    /** set the ID of the item (folder) which contains this item */
    protected void setParentID(String id)
    {
        String name = getName();
        
        //set new resource path 
        _resourcePath = RepositoryUtil.normalizePath(id, name );
    }
    

    
    /**set the location of the repository item */
    public void setFullPath(String resourcePath)
    {   
        try {
            _resourcePath = RepositoryUtil.normalizePath(resourcePath, "" );
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            //e.printStackTrace();
        }
    }


    public void setPermissions(PermBits permissions)
    {
        //          a bitmask authorization scheme
        _permissions = permissions;
        persistMeta();
    }
    
    public void setPermissions(Transaction xact, PermBits permissions)
    {
        //          a bitmask authorization scheme
        _permissions = permissions;
        persistMeta();
    }
    
    /** 
     * sets the last modified time
     */
    public void setUpdateTime(Date updated)
    {
        _updateTime = new Timestamp(updated.getTime()).toString();
    }
    
    /**
     *  upload content to the DAV repository
     */
    private RepositoryItem makeChild (User user, String name,
                                      String mimeType,
                                      InputStream is)
        throws Exception        
    {
        if (name== null || "".equals(name)) {
            throw new Exception("Cannot create child with empty name");
        }
        
        //check if the current repository item is a folder

        
        //check write permissions
        
            
        //ignore mimetype ?
        
        String newPath = getFullPath() + "/" + name;
        
        FSRepositoryUtil.transportItem(is, new FileOutputStream(FSConnection.getResource(newPath)));
        
        //getRepositoryItem
        RepositoryItem repositoryItem = _repository.getRepositoryItemByPath(user, newPath);
        
        return repositoryItem;
    }
    
    private PermBits getDefaultPermissions()
    {
        /**  bit 0: owner Read
         * bit 1: owner Write
         * bit 3: owner Execute
         * bit 4: Reserved for future ussage
         * bit 5: group Read
         * bit 6: group Write
         * bit 7: group Execute
         * bit 8: Reserved for future usage
         * bit 9: World Read
         * bit 10: World Write
         * bit 11: World Execute
         * bit12-31: Reserved for future use
         */
        //W    G    O
        //0001 0001 0111 = 279
        
        PermBits permBits = new PermBits(279); 
        
        return permBits;
    }


    /**
     *
     */
    public String getDocID()
    {
        return getFullPath();
    }
    

    /**
     * true if item with this name represents the checked out version of the item with this name
     */
    public boolean isCheckedoutVersion()
    {
        return false;
    }
    
    /**
     * get version of an item with this name which has been already 
     *   checked out, as a part of a larger transaction
     */
    public RepositoryItem getCheckedoutVersion(Transaction xact,
                                               User user)
        throws FsException
    {
        return null;
    }
    

    /**
     * checkout a version of the item with this name, as a part of a larger transaction
     *   @return a checked out version which not accessible to other users 
     */
    public RepositoryItem checkoutVersion(Transaction xact,
                                          User user)
        throws FsException
    {
        return null;
    }
    
    
    /**
     * checkin a checkedout version of the item with this name, as a part of a larger transaction
     */
    public void checkinCheckedoutVersion(Transaction xact,
                                         User user)
        throws FsException
    {
    
    }
    
    /**
     * uncheckout (cancel a checkout) by removing a checkedout version of item with this name, as a part of a larger transaction
     */
    public void uncheckoutCheckedoutVersion(Transaction xact,
                                            User user)
        throws FsException
    {
        
    }
    
    public String toString()
    {
        StringBuffer toStr = new StringBuffer();
        toStr.append("FSRepositoryItem : itemID={"+getItemID()+"}");
        toStr.append(" fullPath={" + getFullPath() +"}");
        toStr.append(" dirNamePath={");
        try {
            toStr.append(getDirNamePath());
        } catch (Exception e) {
            toStr.append("exception "+e.getMessage());
        }
        toStr.append("}");
        toStr.append(" name={" + getName() +"}");
        toStr.append(" createTime={" + getCreateTime() +"}");
        toStr.append(" updateTime={" + getUpdateTime() +"}");
        toStr.append(" itemType={" + getItemType() +"}");
        toStr.append(" ownerID={" + getOwnerID() +"}");
        toStr.append(" owningGroupID={" + getOwningGroupID() +"}");
        
        return toStr.toString();
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
        if (_appObjectTable == null) {
            _appObjectTable = new Hashtable();
        }
        _appObjectTable.put(key, value);

        //        throw new UnsupportedOperationException();
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
        if (_appObjectTable == null) {
            return null;
        }
        return _appObjectTable.get(key);

    }

    public boolean exists() {
        if (resource == null) {
            return false;
        } else {
            return resource.exists();
        }
    }
    
    private void persistMeta() {
        try {
            File metaFile = getMetaFile();
            PrintWriter writer = new PrintWriter(new FileOutputStream(metaFile));
            writer.println("<meta>");
            writer.println("  <permissions>" + _permissions.toInt() + "</permissions>");
            writer.println("  <owner-id>" + _ownerID + "</owner-id>");
            writer.println("  <owning-group-id>" + _owningGroupID + "</owning-group-id>");
            writer.println("</meta>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Well, please excuse me. This is a real hack. uv
    private void readMeta() {
        try {
            _permissions = getDefaultPermissions();
            _ownerID = "admin";
            _owningGroupID = "admin";

            File metaFile = getMetaFile();
            if (!metaFile.exists()) {
                // System.out.println("no meta file, faking it");
                return;
            } else {
                // System.out.println("Thinking [" + metaFile + "] does exist");
                BufferedReader reader = new BufferedReader(new FileReader(metaFile));
                String line;
                while ((line = reader.readLine())!=null) {
                    if (line.indexOf("<permissions>")>-1) {
                        _permissions = new PermBits(getIntXMLValue(line));
                    } else if (line.indexOf("<owner-id>")>-1) {
                        this._ownerID = "" + getIntXMLValue(line);
                    } else if (line.indexOf("<owning-group-id>") >-1) {
                        this._owningGroupID = "" + getIntXMLValue(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getIntXMLValue(String line) {
        int firstGtPos = line.indexOf('>');
        String value = line.substring(firstGtPos+1, line.indexOf('<', firstGtPos)).trim();
        return Integer.parseInt(value);
    }


    private File getMetaFile() 
    {
        String containingFolder = NetUtils.removeFilename(resource.getAbsolutePath());
        String filename;
        if (getItemType() == FOLDER) {
            filename = ".folder";
        } else {
            filename = NetUtils.getFilename(_resourcePath);
        }

        // System.out.println("containingFolder is [" +  containingFolder + "]");
        if (! containingFolder.endsWith("/")) {
            containingFolder += "/";
        }

        File metaFolder = new File(containingFolder + "..xfsm");
        if (!metaFolder.exists() && _repository.isCreatingMeta()) {
            metaFolder.mkdir();
        }
        return new File(metaFolder, filename);
    }

    /**
     *
     */
    public boolean hasChanged() 
    {
        File newFile = FSConnection.getResource(getFullPath());
        return _lastLoaded < newFile.lastModified();
    }


    /**
     *  
     */
    private String getISODate(String dateStr) 
    {
        Date d;
        try {
            d = dbDateFormat.parse(dateStr, new ParsePosition(0));
        } catch (Exception ex) {
            Log.getLogger().warn("couldn't parse date: " + dateStr);
            return dateStr;
        }

        if (d == null) {
            return "";
        } else {
            StringBuffer creationDateValue = new StringBuffer(creationDateFormat.format(d));
            return creationDateValue.toString();
        }
    }

    /**
     *  
     */
    private String getHTTPDate(String dateStr) 
    {
        Date d;
        try {
            d = dbDateFormat.parse(dateStr, new ParsePosition(0));
        } catch (Exception ex) {
            Log.getLogger().warn("couldn't parse date: " + dateStr);
            return dateStr;
        }
        rfc1123DateFormat.setTimeZone(new SimpleTimeZone(0,"GMT"));
        StringBuffer dateval = new StringBuffer(rfc1123DateFormat.format(d));
        return dateval.toString();
    }


    public void dropFromCache() {
        // Null implementation
    }

}
