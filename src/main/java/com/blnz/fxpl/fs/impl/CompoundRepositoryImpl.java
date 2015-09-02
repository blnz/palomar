//
package com.blnz.fxpl.fs.impl;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.blnz.fxpl.security.User;
import com.blnz.fxpl.util.ConfigProps;

import com.blnz.fxpl.fs.ItemLock;
import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.RepositoryUtil;
import com.blnz.fxpl.fs.Transaction;
import com.blnz.fxpl.fs.FsException;
import com.blnz.fxpl.fs.FsRepository;

import com.blnz.fxpl.xform.XForm;

import com.blnz.fxpl.log.Log;

// import org.xmlecho.palomar.cache.Cache;
// import org.xmlecho.palomar.cache.CacheProxy;


import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.Iterator;
import java.util.Date;
import java.util.Hashtable;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;

import java.sql.Timestamp;


/**
 * A repository with mounted sub-repositories
 */
public class CompoundRepositoryImpl extends FSRepositoryImpl
{

    protected Hashtable _repositories = new Hashtable();

    /**
     * Repository of the lock-null resources.
     * <p>
     * Key : path of the collection containing the lock-null resource<br>
     * Value : Vector of lock-null resource which are members of the collection.
     * Each element of the Vector is the path associated with the lock-null
     * resource.
     */
    private Hashtable _nullResourceLocks = new Hashtable();
    
    private class MountPoint 
    {
	public String location;           // path within our set of names
	public String export;               // the location in the sub-repository's local namespace
	                                    //  which will be mapped to the mountpoint

	public FsRepository repository;    // the sub-repository's implementation

	public Pattern gPathRE;       
	public Pattern gIDRE;

	public IDStringLocalizer pathLocalizer;
	public IDStringLocalizer idLocalizer;

    }

    // ordered list
    private MountPoint[] _mounts = new MountPoint[1024];
    private int _mix = 0;

    /**
     *
     */
    public CompoundRepositoryImpl() 
    {
	super();
	init();
    }

    private void init()
    {

        String configFile = 
            ConfigProps.getProperty("org.xmlecho.palomar.CompoundRepositoryConfig");

        if (configFile == null) {
            configFile = "./repository.xml";
        }
        

        URL repositoryConfigURL = ConfigProps.getResource(configFile, ConfigProps.propsBaseURL());
	// System.out.println("loading config at [" + repositoryConfigURL.toString() + "]"); 
	
        try {
	    
            XMLReader r = XForm.getTransformService().createInputSourceReader();
	    
            InputSource s = new InputSource(repositoryConfigURL.toString());
	    
            r.setContentHandler(new CompoundRepositoryConfigurator(this));
            r.parse(s);

        } catch (Exception ex) {
           //  System.out.println("EchoConnectionFactoryImpl: failed to initialize tag map");
            ex.printStackTrace();
            // FIXME: do something   
        }

    }


    public void startServices()
    {
	Iterator it = _repositories.values().iterator();

	while (it.hasNext()) {
	    FsRepository repo = (FsRepository) it.next();
	    repo.startServices();
	}
    }
    
    /**
     *
     */
    private FsRepository repositoryForPath(String path)
    {
	for (int i = 0; (i < _mounts.length && _mounts[i] != null); ++i) {
	    if (_mounts[i].gPathRE.matcher(path).matches()) {
		return _mounts[i].repository;
	    }
	}
	return null;
    } 

    /**
     *
     */
    private FsRepository repositoryForItemID(String itemID)
    {
	for (int i = 0; (i < _mounts.length && _mounts[i] != null); ++i) {
	    if (_mounts[i].gIDRE.matcher(itemID).matches()) {
		return _mounts[i].repository;
	    }
	}
	return null;
    } 

    /**
     *
     */
    private MountPoint mountForPath(String path)
    {
	for (int i = 0; (i < _mounts.length && _mounts[i] != null); ++i) {
	    if (path.startsWith(_mounts[i].location)) {
		return _mounts[i];
	    }
	}
	return null;
    } 

    /**
     * for some sub-repository's canonical name, return the appropriate mountpoint 
     */
    private MountPoint mountForExport(String path)
    {
	for (int i = 0; (i < _mounts.length && _mounts[i] != null); ++i) {
	    if (path.startsWith(_mounts[i].export)) {
		return _mounts[i];
	    }
	}
	return null;
    } 

    /**
     *
     */
    private MountPoint mountForItemID(String itemID)
    {
	for (int i = 0; (i < _mounts.length && _mounts[i] != null); ++i) {
	    if (_mounts[i].gIDRE.matcher(itemID).matches()) {
		return _mounts[i];
	    }
	}
	return null;
    } 


    public void addLock(Transaction xact, ItemLock newLock, User user) 
        throws FsException
    {
        //commits the (tentative) lock
        //FIXME: not implemented
    }
    
    public boolean canAccessCheckedoutItem(RepositoryItem item, User user)
    {
	if (item.getOwnerRepository() == this) {
	    return true;
	} else {
	    return item.getOwnerRepository().canAccessCheckedoutItem(item, user);
	}
    }
    
    public boolean canAccessLock(User user, ItemLock lock)
    {
    
	// 	if (item.getOwnerRepository() == this) {
	// 	    return true;
	// 	} else {
	// 	    return item.getOwnerRepository().canAccessLock(item, user);
	// 	}
	
	return true;
    }
    
    public void deleteAllExpiredLocks()
    {
	//delete all expired locks from the ItemLocks table
        //FIXME: not implemented
    }
    
    public void deleteLock(Transaction xact, ItemLock newLock, User user)
    {
	//deletes a lock
        //FIXME: not implemented
    }
    
    public void deleteRepositoryItem(Transaction xact, User user, 
                                     java.lang.String recurse, RepositoryItem item) throws FsException 
    {
	if (item.getOwnerRepository() != this) {
	    item.getOwnerRepository().deleteRepositoryItem(xact, user, recurse, item);
	} else {
	    super.deleteRepositoryItem(xact, user, recurse, item);
	}
    }
    
    public void doBranchIndex(java.lang.String mimeType, java.lang.String path,
                              int skip, int count, boolean renew)
	throws FsException
    {
	MountPoint mp = mountForPath(path);
	if (mp != null) {
	    mp.repository.doBranchIndex(mimeType, mp.pathLocalizer.localize(path), skip, count, renew); 
	}

    }
    
    public java.util.Iterator getBranchWalker(java.lang.String path, 
                                              java.lang.String mimeType)
    {
	//returns an interator over the items in a given branch with a given mimetype
	return null;
    
    }
    
    public ItemLock[] getCollectionLocks(Transaction xact) 
    {
        //FIXME: not implemented
        return null;
    }
    
    //return first lock on the given item
    public ItemLock getItemLock(Transaction xact, RepositoryItem item)
	throws FsException
    {

	FsRepository repos = item.getOwnerRepository();
	if (repos != null && repos != this) {
        System.out.println("repos: " + repos);
        int i=0;
        System.out.println("this: " + this);
	    return repos.getItemLock(xact, item);
	}

        //FIXME: not implemented
        return null;
    }
     
    //return all the locks on the given item   
    public ItemLock[] getItemLocks(Transaction xact, RepositoryItem item)
	throws FsException
    {

	FsRepository repos = item.getOwnerRepository();
	if (repos != null) {
	    return repos.getItemLocks(xact, item);
	}

       return null;
    }
    
    /**
     * all the resource locks on items which don't yet exist
     */
    public Hashtable getNullResourceLocks() {
        return _nullResourceLocks;
    }

    //gets the named folder on behalf of the given user for updating,
    //create the folder if it doesn't exist
    public RepositoryItem getOrCreateFolder(Transaction xact, 
                                            User user, java.lang.String path)
	throws FsException
    {
	RepositoryItem folder = null;
	MountPoint mp = mountForPath(path);

	if (mp != null) {
	    folder = mp.repository.getOrCreateFolder(xact, user, mp.pathLocalizer.localize(path)); 
	    folder = new MountedItemWrapper(folder, mp.idLocalizer, mp.pathLocalizer);
	} else {
	    folder = super.getOrCreateFolder(xact, user, path);
	}
        return folder;
    }
    
    /**
     * returns the identified item, writeable under the given transaction
     */
    public RepositoryItem getRepositoryItem(Transaction xact, User user, String itemID)
	throws FsException
    {
	MountPoint mp = mountForItemID(itemID);
	RepositoryItem item = null;
	if (mp != null) {
	    item = 
		mp.repository.getRepositoryItem(xact, user, mp.idLocalizer.localize(itemID)); 

	    String itemName = item.getFullPath();
	    MountPoint canon = mountForExport(itemName);

	    item = new MountedItemWrapper(item, canon.idLocalizer, canon.pathLocalizer);
	} else {
	    item = super.getRepositoryItem(xact, user, itemID);
	}
	return item;
    }
    
    /**
     * gets the identified item on behalf of the given user (Note: item's itemID is assumed == item's path)
     */
    public RepositoryItem getRepositoryItem(User user, String itemID)
	throws FsException
    {
	RepositoryItem item = null;
	MountPoint mp = mountForItemID(itemID);
	if (mp != null) {
	    item = 
		mp.repository.getRepositoryItem(user, mp.idLocalizer.localize(itemID)); 

	    String itemName = item.getFullPath();
	    MountPoint canon = mountForExport(itemName);

	    item = new MountedItemWrapper(item, canon.idLocalizer, canon.pathLocalizer);
	    return item;

	} else {
	    return super.getRepositoryItem(user, itemID);
	}
    }
    
    /**
     * gets the named item on behalf of the given user for updating
     */
    public RepositoryItem getRepositoryItemByPath(Transaction xact, User user, 
                                                  java.lang.String path)
    	throws FsException
    {
	RepositoryItem item = null;
	MountPoint mp = mountForPath(path);
	if (mp != null) {
	    // System.out.println("get [" + path + "]");
	    item = 
		mp.repository.getRepositoryItemByPath(xact, user,  mp.pathLocalizer.localize(path));
	    if (item == null) {
		// System.out.println("1) no item at " + path);
		if (path.equals(mp.export)) {
		    //   System.out.println("but it matches export, so we're gonna create it");
		    item = mp.repository.getOrCreateFolder(xact, user, path); 
		} 
	    }
	    if (item == null) {
		// System.out.println("Still ... no item at " + path);
	    } else {
		item = new MountedItemWrapper(item, mp.idLocalizer, mp.pathLocalizer);
	    }
	    return item;
	} else {
	    return super.getRepositoryItemByPath(xact, user, path);
	}
    }
    
    
    /**
     * gets the named item on behalf of the given user
     */
    public RepositoryItem getRepositoryItemByPath(User user, String path )
	throws FsException
    { 
	RepositoryItem item = null;
	MountPoint mp = mountForPath(path);
	if (mp != null) {
	    item = 
		mp.repository.getRepositoryItemByPath(user,  mp.pathLocalizer.localize(path));
	    if (item == null) {
		// System.out.println("2) no item at " + path);
		if (path.equals(mp.export)) {
		    //  System.out.println("but it matches export, so we're gonna create it");
		    Transaction xact = startTransaction();
		    item = mp.repository.getOrCreateFolder(xact, user, path); 
		    try {
			xact.commit();
		    } catch (Exception ex) {
			throw new FsException(ex.getMessage());
		    }
		} 
	    }
	    if (item == null) {
		// System.out.println("Still ... no item at " + path);
	    } else {
		item = new MountedItemWrapper(item, mp.idLocalizer, mp.pathLocalizer);
	    }
	    return item;
	} else {
	    return super.getRepositoryItemByPath(user, path);
	}
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
    { 
	
        //FIXME: not implemented
        return null;
    }
    
    
    public boolean isItemCheckedout(RepositoryItem item, User user)
    {
        //FIXME: not implemented
        return false;
    }
    
    public boolean isItemLocked(RepositoryItem item, String ifHeader, User user)
    {
	//Check to see if a resource is currently write locked.
    
        //FIXME: not implemented
        return false;
    
    }
    
    public boolean isReadOnly()
    {
	//gets the read-only status of the repository
    
        //FIXME: not implemented
        return false;
    
    }
    
    public void listDeletedRepositoryItems(User user, ContentHandler dest)
    {
	//retrieves the list of repository items and writes it to the given content handler
    
        //FIXME: not implemented
    }
    
//    /**
//     * 
//     */
//    public void loadJar(InputStream jarStream, User user, String basePath)
//        throws FsException
//    {
//        RepositoryUtil.loadJar(this, jarStream, user, basePath);
//    }
    
    public ItemLock newLock(java.lang.String path)
    {
	//create a new (tentative) lock on an item in the repository
        //FIXME: not implemented
        return null;
    }
    
    public void purgeItemAndVersions(Transaction xact, User user,
                                     java.lang.String recurse, 
				     RepositoryItem item)
	throws FsException
    {
	//permanently deletes the given item and its versions.
	if (item.getOwnerRepository() != this) {
	    item.getOwnerRepository().purgeItemAndVersions(xact, user, 
							   recurse, item);
	} else {
	    super.purgeItemAndVersions(xact, user, recurse, item);
	}
    }
    

    public void purgeItemVersions(Transaction xact, User user,
                                  int keepRecent,
                                  java.lang.String recurse, RepositoryItem item)
	throws FsException
    {
	//permanently deletes the given item and its versions.
	if (item.getOwnerRepository() != this) {
	    item.getOwnerRepository().purgeItemVersions(xact, user, 
							keepRecent, 
							recurse, item);
	} else {
	    super.purgeItemVersions(xact, user, keepRecent, recurse, item);
	}
    }
    
    public void purgeRepositoryItem(Transaction xact, User user,
                                    java.lang.String recurse, RepositoryItem item)
        throws FsException
    {
	//permanantly deletes the given item.
	if (item.getOwnerRepository() != this) {
	    item.getOwnerRepository().purgeRepositoryItem(xact, user, recurse, item);
	} else {
	    super.purgeRepositoryItem(xact, user, recurse, item);
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
    
//    /**
//     * puts the files identified by paths to a jarfile, on behalf of the given user
//     */
//    public void writeJar(OutputStream jarStream, User user,
//                         String[] paths, String basePath)
//        throws FsException
//    {   
//        FSRepositoryUtil.writeJar(this, jarStream, user, paths, basePath);
//    }
    
    boolean isCreatingMeta() 
    {
        return this.isCreatingMeta;
    }

//    public Iterator getItemsPendingIndex(String collectionPathPrefix, String pathRegex, 
//					 Date createdSince, Date createdThrough) 
//	throws FsException
//    {
//	FsRepository repos = repositoryForPath(collectionPathPrefix);
//	if (repos != null) {
//	    return repos.getItemsPendingIndex(collectionPathPrefix, pathRegex,
//					      createdSince, createdThrough);
//	} else {
//	    // FIXME: recurse on each ot collectionPathPrefix's children folders
//	}
//	return null;
//    }
//
//    public  Iterator getItemsPendingIndexRemoval(String collectionPathPrefix, String pathRegex, 
//						 Date createdSince, Date createdThrough) 
//	throws FsException
//    {
//	
//	XfsRepository repos = repositoryForPath(collectionPathPrefix);
//	if (repos != null) {
//	    return repos.getItemsPendingIndexRemoval(collectionPathPrefix, pathRegex,
//						     createdSince, createdThrough);
//	} else {
//	}
//
//	return null;
//    }
//
//    public void markItemIndexed(RepositoryItem which) 
//    {
//	XfsRepository repos = which.getOwnerRepository();
//	if (repos == this) {
//	    return;
//	} else {
//	    repos.markItemIndexed(which);
//	}
//    }
//
//    public void markItemDeIndexed(RepositoryItem which)
//    {
//	XfsRepository repos = which.getOwnerRepository();
//	if (repos == this) {
//	    return;
//	} else {
//	    repos.markItemDeIndexed(which);
//	}
//    }
//    

    private class CompoundRepositoryConfigurator extends DefaultHandler
    {
	private CompoundRepositoryImpl _repository;

	public CompoundRepositoryConfigurator(CompoundRepositoryImpl repository)
	{
	    _repository = repository;
	}

	public void startDocument()
	{}

	public void endDocument()
	{}

	public void startElement(String namespace, String name, String qname, Attributes atts)
	{
	    //	    System.out.println("<" + qname + ">"); 
	    if ("repository".equals(name)) {
		String classname = atts.getValue("class");
		String reposID = atts.getValue("id");
		if (classname != null && reposID != null) {
		    try {
			
			FsRepository repos = 
			    (FsRepository) Class.forName(classname).newInstance();
			
			//			repos.startServices();
			_repositories.put(reposID, repos);
			System.out.println("CompoundRepositoryImpl:: added " + classname + " as " + reposID);

		    } catch (Exception ex) {
			ex.printStackTrace();
		    }
		}

	    } else if ("mountPoint".equals(name)) {
		String location = atts.getValue("location");
		String repository = atts.getValue("repository");
		String export = atts.getValue("export");

		String lpathRE = atts.getValue("localPathRE");
		String lpathReplace = atts.getValue("local2GlobalPathReplace");
		String lidRE = atts.getValue("localIDRE");
		String lidReplace = atts.getValue("local2GlobalIDReplace");

		String gpathRE = atts.getValue("globalPathRE");
		String gpathReplace = atts.getValue("global2LocalPathReplace");
		String gidRE = atts.getValue("globalIDRE");
		String gidReplace = atts.getValue("global2LocalIDReplace");


		FsRepository repos = (FsRepository) _repositories.get(repository);
		if (repos == null) {
		    System.out.println("cannot find repos " + repository + " for mounting at " + location); 
		} else {
		    MountPoint mp = new MountPoint();

		    mp.location = location;
		    mp.repository = repos;
		    mp.export = export;

		    Pattern lPathRE =  Pattern.compile(lpathRE);
		    mp.gPathRE =  Pattern.compile(gpathRE);

		    mp.pathLocalizer = new RELocalizer(lPathRE, lpathReplace,
						       mp.gPathRE, gpathReplace);
 
		    Pattern lIDRE = Pattern.compile(lidRE);
		    mp.gIDRE = Pattern.compile(gidRE);
		    mp.idLocalizer = new RELocalizer(lIDRE, lidReplace,
						     mp.gIDRE, gidReplace);
		    _mounts[_mix++] = mp;
		    System.out.println("adding mount from " + repository + ":" + export + " at " + location); 
 
		}
	    }
	}

	public void endElement(String namespace, String name, String qname)
	{
	    //	    System.out.println("</" + qname + ">"); 
	}
    }

    private class RELocalizer implements IDStringLocalizer
    {
	private Pattern _l2gRE;
	private String _l2gReplace;
	private Pattern _g2lRE;
	private String _g2lReplace;

	RELocalizer(Pattern l2gRE, String l2gReplace, Pattern g2lRE, String g2lReplace)
	{
	    _l2gRE = l2gRE;
	    _l2gReplace = l2gReplace;
	    _g2lRE = g2lRE;
	    _g2lReplace = g2lReplace;
	}

	public String localize(String global)
	{
	    //	System.out.println("localize [" + global + "] from [" + _g2lReplace + "]");
	    String s =  _g2lRE.matcher(global).replaceFirst(_g2lReplace);
	    // System.out.println("localize [" + global + "] to [" + s + "]");
	    return s;
	}

	public String globalize(String local)
	{
	    String s =  _l2gRE.matcher(local).replaceFirst(_l2gReplace);
	    // System.out.println("globalize [" + local + "] to [" + s + "]");
	    return s;
	}


    }
    
}
