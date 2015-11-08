package com.blnz.fxpl.core;

import com.blnz.fxpl.fs.FS;
import com.blnz.fxpl.fs.FsRepository;
import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.FsException;

import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.security.User;

import com.blnz.xsl.om.ExtensionContext;

import org.xml.sax.ContentHandler;


/**
 * fetct the metadata for an item or folder in the repository
 */
public class ItemList extends FXRequestServerSide
{
    /**
     * evaluate the request
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
        throws Exception
    {
        FXContext ctx = extendContext((FXContext) context);

        try {
            FsRepository repo = null;
            
            repo = FS.getRepository();
            
            String path = (String) context.get("path");
            String depthStr = (String)context.get("depth");
            String foldersOnly = (String) context.get("foldersOnly");
            String itemStatus = ((String) context.get("itemStatus"));

            int depth = 1; 
            
            if (depthStr != null && !depthStr.equals("")){
                depth = Integer.parseInt(depthStr);
            }

            if ( itemStatus != null ) {
                itemStatus = itemStatus.toLowerCase();
            } else {
                itemStatus = "";
            }
            if (!itemStatus.equals("active") && 
                !itemStatus.equals("deleted") && 
                !itemStatus.equals("all")) {

                itemStatus = "active"; // default value
            }

            RepositoryItem dir = getItem(ctx);

            User user = getUser(ctx);

            if (dir == null) {
                throw new Exception(path + " not found");
            }
            
            // list active items
            if (itemStatus.equals("active")) {
                if (foldersOnly != null && "true".equals(foldersOnly)) {
                    dir.listFolders(user, responseTarget, depth);
                } else {
                    dir.list(user, responseTarget, depth);
                }
            }
            
            // list deleted items
            else if (itemStatus.equals("deleted")){
                repo.listDeletedRepositoryItems( user, responseTarget );
            }
            
            // list all items
            else {
                throw new FsException("Listing all items is not yet supported");
            }
            
        } catch (Exception ex) {
            // ex.printStackTrace();
            errorResponse(ex, responseTarget, ctx);
        }
       
     }
    
    /**
     * returns the base URI that should be associated
     * with the XML document which would result from
     * evaluating this XRAPRequest in the given context
     */
    public String getURI(FXContext context)
    {
        try {
            RepositoryItem item = getItem(extendContext(context));
            return item.getBaseURI();
        } catch (Exception e) {
            // FIXME: do something
            return "nullURI";
        }
    }
}
