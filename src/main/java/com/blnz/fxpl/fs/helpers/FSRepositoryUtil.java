//
package com.blnz.fxpl.fs.helpers;

import javax.activation.MimetypesFileTypeMap;

import java.net.URL;

import com.blnz.fxpl.security.Security;
import com.blnz.fxpl.security.User;
import com.blnz.fxpl.util.ConfigProps;
import com.blnz.fxpl.fs.impl.FSRepositoryImpl;
import com.blnz.fxpl.fs.impl.FSRepositoryItem;
import com.blnz.fxpl.fs.Transaction;

import org.w3c.dom.*;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.blnz.fxpl.xform.XForm;

import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.RepositoryUtil;

import com.blnz.fxpl.fs.FsRepository;
import com.blnz.fxpl.fs.FsException;

import java.io.FilterInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
 
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * utility methods 
 */
public class FSRepositoryUtil
{
    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    private static final SimpleDateFormat creationDateFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    

    private static MimetypesFileTypeMap _typeMap;

    public static FSRepositoryItem setFSRepositoryItemProperties(FSRepositoryItem repositoryItem,
								 File fsResource)
	throws Exception
    {

	if (_typeMap == null) {
	    init();
	}

	String resourcePath = repositoryItem.getFullPath();
	
	//set create date
	//Date createTime = null;
	
	//System.out.println("FSRepositoryUtil : got create time : " + createTime);
	//repositoryItem.setCreateTime(createTime);
		
	//set update date 
	Date updateTime = new Date(fsResource.lastModified());
	
	repositoryItem.setUpdateTime(updateTime);
	//get resource type
	int itemType = -1;
	String mimeType = ""; 
	
	boolean isCollection = fsResource.isDirectory();
	if (isCollection){
	    repositoryItem.setItemType(RepositoryItem.FOLDER);
	} else {
	    itemType = getItemTypeFromName(fsResource.getName());
	    repositoryItem.setItemType(itemType);
	}
		
	//set mimetype
	if ( itemType == RepositoryItem.XML ) {
	    mimeType = "application/xml";
	} else if (repositoryItem.getName().endsWith(".zip")) {
	    mimeType = "application/x-zip-compressed";
	} else {
	    mimeType = _typeMap.getContentType(repositoryItem.getName());
	}
	repositoryItem.setMimeType(mimeType);
	
	return repositoryItem;
    }

    //
    private static void init()
    {
        
        String typeMapFile = ConfigProps.getProperty("mimetypes.map.file", "undefined");
        if ("undefined".equals(typeMapFile)) {
            System.out.println("no mimetypes.map.file specified, using javax activation default");
            _typeMap = new MimetypesFileTypeMap();
        } else {
            URL typeMapURL = ConfigProps.getResource(typeMapFile, ConfigProps.propsBaseURL());
            if (typeMapURL == null) {
                System.out.println("cannot build URL for " + typeMapFile + 
                                   " using javax activation default");
                _typeMap = new MimetypesFileTypeMap();
            }
            try {
                InputStream is = typeMapURL.openStream();
                _typeMap = new MimetypesFileTypeMap(is);
            } catch (Exception ex) {
                System.out.println("failed to load mimetype map from " + typeMapURL.toString() + 
                                   " using javax activation default");
                _typeMap = new MimetypesFileTypeMap();
            }
        }

    }

    /**
     *
     */    
    public static RepositoryItem[] getChildren(FSRepositoryItem repositoryItem,
					       File resource )
	throws Exception
    {
	
	String resourcePath = repositoryItem.getFullPath();
	FsRepository repository = repositoryItem.getOwnerRepository();
	User user = repositoryItem.getUser();

	RepositoryItem[] fsItems = new RepositoryItem[0];	

	String[] children = resource.list();

	if (children == null) {
	    return null;
	} else {
	    boolean haveMetaDir = false;
	    for ( int i = 0; i < children.length; i++){
		if ("..xfsm".equals(children[i])) {
		    haveMetaDir = true;
		    break;
		}
	    }
	    fsItems = new RepositoryItem[haveMetaDir ? (children.length - 1) : children.length];
	    //	    System.out.println("children length is " + fsItems.length); 
	    if (fsItems.length == 0) {
		return null;
	    }
	    for ( int i = 0, j = 0; i < children.length; i++ ) {
		String childName = children[i];

		if ( ! "..xfsm".equals(childName)) {
		    //    System.out.println("keeping child: " + childName);		    
		    String path = RepositoryUtil.normalizePath(resourcePath, childName );
		    fsItems[j++] = repository.getRepositoryItemByPath(user, path);
		} else {
		    // System.out.println("skipping child: " + childName);	    
		}
	    } 
	}

	return fsItems;
	
    }
        
    /**
     * Get creation date from string in ISO format.
     */
    private static Date getISOCreationDate(String creationDateStr)
    {
        
	Date date = creationDateFormat.parse(creationDateStr, new ParsePosition(0));

        return date;
    }

    
    //     /** retrieve text content of a node */
    //     private static String getText(Node node)
    // 	throws Exception
    //     {
    // 	NodeList childList = node.getChildNodes();
    
    // 	StringBuffer textBuf = new StringBuffer();
    
    // 	for (int i = 0; i < childList.getLength(); i++) {
    // 	    Node currentNode = childList.item(i);
    // 	    switch (currentNode.getNodeType()) {
    // 	    case Node.TEXT_NODE:
    // 		String str = ((Text)currentNode).getData();
    // 		textBuf.append(str);
    // 	    default:
    // 		break;
    // 	    }
    // 	}
    
    // 	return textBuf.toString();
    //     }
    
    //     /**
    //      * Return JAXP document builder instance.
    //      */
    //     private static DocumentBuilder getDocumentBuilder()
    //         throws Exception
    //     {
    //         DocumentBuilder documentBuilder = null;
    //         //        System.out.println("getting doc builder");
    //         try {
    //             documentBuilder =
    //                 DocumentBuilderFactory.newInstance().newDocumentBuilder();
    //         } catch(ParserConfigurationException e) {
    // 	    e.printStackTrace();
    // 	    throw e;
    // 	}
    //         return documentBuilder;
    //     }
    


    /** 
     * utility method to retrieve a item type from the filename suffix 
     * of a repository item
     * Possible Item types are:
     * <ul><li>RepositoryItem.BINARY</li>
     *  <li>RepositoryItem.FOLDER</li>
     *  <li>RepositoryItem.TEXT</li>
     *  <li>RepositoryItem.XML</li>
     * </ul>
     */
    public static int getItemTypeFromName(String filename)
    {
	int suffixIx = filename.lastIndexOf(".");
	String fileType = null;
	if (suffixIx > 0) {
	    // determine item category
	    //String fileExtension = f.getName().substring(suffixIx);
	    String fileExtension = filename.substring(suffixIx+1);
	    fileType = ConfigProps.getProperty("file.extension." +
					       fileExtension.toLowerCase(), fileExtension.toLowerCase());
	}
	
	if (fileType == null) {
	    return RepositoryItem.BINARY;
	}
	
	int itemCategory = -1;
	
	if (FsRepository.FILE_TYPE_XML.equals(fileType)) {
	    itemCategory =  RepositoryItem.XML;
	} else if (FsRepository.FILE_TYPE_TEXT.equals(fileType)) {
	    itemCategory = RepositoryItem.TEXT;
	} else if (FsRepository.FILE_TYPE_BINARY.equals(fileType)) {
	    itemCategory = RepositoryItem.BINARY;
	} else {
	    itemCategory = RepositoryItem.BINARY;
	}
	
	return itemCategory;
    }
    

    /**
     * gets the files identified by paths to a jarfile, on behalf of the given user
     */
    public static void loadJar(FsRepository repo, InputStream jarStream, User user, String basePath)
	throws FsException
    {
	//System.out.println("FSRepositoryUtil : loadJar called");
	
	// if (_readOnly) {
	//             throw new ("repository is set read-only, writing not permitted");
	//         }
        
	Transaction xact = null;
        
	try {
            xact = repo.startTransaction();
	    
            RepositoryItem item = repo.getRepositoryItemByPath(xact, user, basePath);
            
	    if (item == null) {
                throw new FsException ("jar basepath: {" + basePath + "} not found");
            }
	    
            MimetypesFileTypeMap typeMap = new MimetypesFileTypeMap();

            JarInputStream js = new JarInputStream(jarStream);
            Manifest man = js.getManifest();
            JarEntry entry;
            while ((entry = js.getNextJarEntry()) != null) {

                String entryName = entry.getName();
                //System.out.println("entry=" + entryName);

                int enl = entryName.length();
                if (entry.isDirectory()) {
                    entryName = entryName.substring(0, enl - 1); 
                } else {
                    
                }

                String[] pathParts = RepositoryUtil.splitPath(RepositoryUtil.normalizePath(basePath, entryName));
                if (! pathParts[0].equals(item.getDirNamePath() + "/" + item.getName())) {
                    item = repo.getRepositoryItemByPath(xact, user, pathParts[0]);
                }
		
                RepositoryItem child = null;

                int itemType = -1;
                String mimeType = typeMap.getContentType(entryName.toLowerCase());

                if (entry.isDirectory()) {
                    itemType = RepositoryItem.FOLDER;
                } else {
                    int idx = pathParts[1].lastIndexOf(".");
                    if (idx == -1) {
                        // no file extension.  regard it as binary file.
                        itemType = RepositoryItem.BINARY;
                    } else {
                        String ext = pathParts[1].substring(idx);
                        String fileType = ConfigProps.getProperty("file.extension" +
								  ext.toLowerCase());
                        if (fileType == null) {
                            // unknown file type. default as binary file.
                            itemType = RepositoryItem.BINARY;
                        } else {
                            if (fileType.equals(FsRepository.FILE_TYPE_TEXT)) {
                                itemType = RepositoryItem.TEXT;
                            } else if (fileType.equals(FsRepository.FILE_TYPE_BINARY)) {
                                itemType = RepositoryItem.BINARY;
                            } else if (fileType.equals(FsRepository.FILE_TYPE_XML)) {
                                itemType = RepositoryItem.XML;
                                mimeType = "application/xml";
                            } else {
                                // this shouldn't happen
                                itemType = RepositoryItem.BINARY;
                            }
                        }
                    }
                }

                if (RepositoryItem.FOLDER == itemType) {

                    try {
                        child = item.createChildDirItem(user, pathParts[1]);
                    } catch (FsException ex) {
                        // WDL temporary debug
                        ex.printStackTrace();
                        // folder exists, not to worry
                    }
                } else if (RepositoryItem.TEXT == itemType) {

                    child = item.createChildTextItem(user, pathParts[1], mimeType);
                    OutputStream os = child.getOutputStream();
                    byte[] buf = new byte[256];
                    int len = 0;
                    while ((len = js.read(buf, 0, 256)) > 0) {
                        os.write(buf, 0, len);
                    }
                    os.close();
                    
                } else if (RepositoryItem.BINARY == itemType) {

                    child = item.createChildBinaryItem(user, pathParts[1], mimeType);
                    OutputStream os = child.getOutputStream();
                    byte[] buf = new byte[256];
                    int len = 0;
                    while (( len = js.read(buf, 0, 256)) > 0) {
                        os.write(buf, 0, len);
                    }
                    os.close();

                } else if (RepositoryItem.XML == itemType) {

                    child = item.createChildXMLItem(user, pathParts[1],
                                                    mimeType);

                    try {
			
			OutputStream os = child.getOutputStream();
			byte[] buf = new byte[256];
			int len = 0;
			while ((len = js.read(buf, 0, 256)) > 0) {
			    os.write(buf, 0, len);
			}
			os.close();
                        
                    } catch (Exception e) {
                        
                        //                        Log.getLogger().error("store failed beacuse: " +
			//			     e.getMessage() +
			//			     " for " + 
			//			     pathParts[0] +
                        //				     "/" + pathParts[1]);
			//                         if (tempFile != null && tempFile.exists()) {
			//                             tempFile.delete();
			//                         }
                        throw e;
                    }

                } else {
                    throw new FsException("Invalid item type for " + entryName);
                }

                if (child == null) {
                    throw new FsException("child is null. cannot create " + entryName);
                }

                xact.commit();
            } // while

            js.close();

	} catch (Exception thex) {
            thex.printStackTrace();
	    //                 if (xact != null) {
	    //                     xact.rollback();
	    //                 }
	    //             } catch (Exception ex2) {
	    //             }
            if (thex instanceof FsException) {
                throw (FsException) thex;
            } else {
                throw new FsException(thex);
            }
        } finally {
            // try {
	    //                 if (xact != null) {
	    //                     xact.close();
	    //                 }
	    //             } catch (Exception ex2) {
	    //             }
        }
	
	//System.out.println("FSRepositoryUtil : loadJar done");
    
    }
    
    /**
     * gets the files identified by paths to
     * a jarfile, on behalf of the given user
     */
    public static void writeJar(FsRepository repo, OutputStream jarStream,
				User user, String[] paths,
				String basePath)
        throws FsException
    {
        RepositoryItem item = (RepositoryItem)repo.getRepositoryItemByPath(user, basePath);

        try {
	    RepositoryItem[] kids = item.getChildren();
            //check for READ permissions on each file (NOT tested)
            if (kids != null) {
                for (int i = 0; i < kids.length; ++i) {
                    if(! Security.getSecurityService().checkRead(user, kids[i])){
                        throw new FsException("No READ permission for user " + 
					       user.getUsername() + " on file " + kids[i].getName());
                    }
                }
            }
	    
            Manifest man = new Manifest();
            man.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, 
                                        "1.0");

            // write the metadata
            addFolderChildrenToManifest(man, item, paths, basePath);

            JarOutputStream js = new JarOutputStream(jarStream, man);
	    
	    // write the entries
            writeFolderChildrenContents(js, item, paths, basePath, user);
            js.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FsException(ex.getMessage(), ex);
        }

    }

    // recursively looks at a folder's children and writes their
    // content to a jarfile
    private static void writeFolderChildrenContents(JarOutputStream js,
						    RepositoryItem item,
						    String[] pathSpecs,
						    String basePath,
						    User user)
        throws Exception
    {
	RepositoryItem[] kids = item.getChildren();
        if (kids != null) {
            for (int i = 0; i < kids.length; ++i) {
                
                switch (kids[i].getItemType()) {
                case RepositoryItem.FOLDER:
		    //System.out.println("adding contents of folder {"+ kids[i].getName()+"}");
		    
		    if (Security.getSecurityService().checkRead(user, kids[i])) {
			js.putNextEntry(new JarEntry(entryName(kids[i],
							       basePath) + "/"));
			writeFolderChildrenContents(js, kids[i], pathSpecs, basePath, user);
		    }
		    break;
                case RepositoryItem.XML:
		    //System.out.println("adding xml {"+ kids[i].getName()+"}");
		    if (Security.getSecurityService().checkRead(user, kids[i])) {
			js.putNextEntry(new JarEntry(entryName(kids[i], basePath)));
			/*OutputStream os = new IrisNoCloseOutputStream(js);
			// OutputStream os = new IrisNoCloseOutputStream(System.out);
			XMLReader itemContents = kids[i].openXMLReader();
			ContentHandler outCH = TransformServiceHome.createOutputStreamContentWriter(os);
			itemContents.setContentHandler(outCH);
			itemContents.parse("dummy");
			*/
			InputStream is = kids[i].getInputStream();
			byte[] buf = new byte[256];
			int len = 0;
			while (( len = is.read(buf, 0, 256)) > 0) {
			    js.write(buf, 0, len);
			}
			is.close();
		    } else {
			continue;
		    }
		    break;
                case RepositoryItem.TEXT:
                case RepositoryItem.BINARY:
		    //System.out.println("adding binary/text {"+ kids[i].getName()+"}");
		    if (Security.getSecurityService().checkRead(user, kids[i])) {
			js.putNextEntry(new JarEntry(entryName(kids[i], basePath)));
			InputStream is = kids[i].getInputStream();
			byte[] buf = new byte[256];
			int len = 0;
			while (( len = is.read(buf, 0, 256)) > 0) {
			    js.write(buf, 0, len);
			}
			is.close();
		    } else {
			continue;
		    }
		    break;
                }
            }
        }
    }

    // recursively looks at a folder's children and adds them
    // to a JarFile's manifest
    private static void addFolderChildrenToManifest(Manifest man, RepositoryItem item,
						    String[] pathSpecs, 
						    String basePath)
        throws Exception
    {
        
        RepositoryItem[] kids = item.getChildren();
	Map entryMap = man.getEntries();
	Attributes atts = man.getMainAttributes();
	if (kids != null) {
	    for (int i = 0; i < kids.length; ++i) {
		String nm = entryName(kids[i], basePath);
		Attributes entAtts = new Attributes();
		int itemType = kids[i].getItemType();
		
		entAtts.put(new Attributes.Name("mimeType"), 
			    kids[i].getMimeType());
		
		entAtts.put(new Attributes.Name("itemType"), 
			    "" + kids[i].getItemType());
		
		entAtts.put(new Attributes.Name("permissions"), 
			    "" + kids[i].getPermissions().toInt() );
		
		entAtts.put(new Attributes.Name("ownerID"), 
			    "" + kids[i].getOwnerID() );
		
		entAtts.put(new Attributes.Name("owningGroupID"), 
			    "" + kids[i].getOwningGroupID() );
		
		if (itemType == RepositoryItem.FOLDER) {
		    entryMap.put(nm + "/", entAtts);
		    // recurse
		    addFolderChildrenToManifest(man, kids[i],
						pathSpecs, 
						basePath);
		} else {
		    entryMap.put(nm, entAtts);
		}
	    }
	    
	}
    }
    
    /** 
     * copy from inputstream to a outputstream
     */
    public static void transportItem(InputStream src, OutputStream sink)
	throws IOException 
    {
	try {
	    int b = -1;
	    while( ( b = src.read()) != -1){
		sink.write(b);
	    }
	} finally {
	    src.close();
	    sink.flush();
	    sink.close();
        }
	     
    }
    
    public static String entryName(RepositoryItem item, String basePath)
        throws Exception
    {
        String fullname = RepositoryUtil.normalizePath(item.getDirNamePath(), 
						       item.getName());
        return fullname.substring(basePath.length());
    }
    
}
