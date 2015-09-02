package com.blnz.fxpl.core;

import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.FsException;


import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.XProcessor;

import com.blnz.fxpl.security.Security;
import com.blnz.fxpl.security.User;
import com.blnz.fxpl.util.ConfigProps;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.log.Logger;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.ExtensionContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


/**
 * imports the bindings in another file
 * 
 */
public class Import extends FXRequestServerSide 
{


    /**
     * evaluate the request
     */
    public FXContext getBindings(FXContext context) 
	throws Exception
    {
        context = extendContext(context);

        Logger logger = Log.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug(getTagName() + ": eval() entry ");
        }

        try {
            RepositoryItem item = super.getItem(context);

            context.put("path", item.getDirNamePath());
            context.put("ctx:commandItem", item.getFullPath()); 
            Node req = null;
            
            User user = getUser(context);
            
            if (! Security.getSecurityService().checkExecute(user,
                                                                        item)) {
                if (false) {
                    // FIXME: need to load proper permissions in repos before re-enabling
                    throw new FsException("No execute permission for user " + 
                                           user.getUsername() + " on item: " + 
                                           item.getDirNamePath() + "/" +
                                           item.getName() );
                }
            }

            try {
                req = (Node) item.getApplicationObject("echoRequest");
                if (DEBUG && req != null) {
                    System.out.println("found compiled req for " + (String) context.get("name"));
                }
            } catch (UnsupportedOperationException ex) {
                // no problem, our repository implementation 
            }

            // allow the script to reference other scripts with relative address
            if ("true".equals(ConfigProps.getProperty("ItemEvalSetRelativePaths"))) {
                context.put("path", item.getDirNamePath());
            }

            String uri = item.getBaseURI();
            if (req == null) {

                XMLReader parser = null;
                XProcessor newXP = null;

                try {
                    // FIXME: ensure item is XML before trying ?
                    parser = item.openXMLReader();
                } catch (Exception ex) {
                    throw new FsException("failed to open XMLReader " + 
                                           " on item: " + 
                                           item.getDirNamePath() + "/" +
                                           item.getName() );
                }
                
                newXP = FXHome.getXProcessor("local");

                try {
                    req = newXP.compile(parser, new InputSource(uri));
                    item.setApplicationObject("echoRequest", req);
                } catch (Exception ex) {
                    Log.getLogger().info("unable to compile and store request for " + item.getName());
                }

            }
            context = ((FXRequestServerSide) req.getNodeExtension()).extendContext(context);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return context;

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
