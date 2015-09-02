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

import com.blnz.xsl.om.ExtensionContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


/**
 * fetch an xml document from the repository, and evaluate it as FXPL
 */
public class ItemEval extends FXRequestServerSide
{


    private boolean DEBUG = false;


    /**
     * evaluate the request
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
	throws Exception
    {
        FXContext ctx = extendContext((FXContext) context);

        Logger logger = Log.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug(getTagName() + ": eval() entry ");
        }

        try {
            RepositoryItem item = super.getItem(ctx);

            ctx.put("path", item.getDirNamePath());
            ctx.put("ctx:commandItem", item.getFullPath()); 
            FXRequest req = null;
            
            User user = getUser(ctx);
            
            if  (!Security.getSecurityService().checkExecute(user, item)) {
                throw new FsException("No execute permission for user " + 
                        user.getUsername() + " on item: " + 
                        item.getDirNamePath() + "/" +
                        item.getName() );
                
            }

            // bind child elements into the context as arg_1 ... arg_n
            FXRequest next;
            int i = 1;
            for (i = 1; (next = getSubRequest(i)) != null ; ++i) {
                ctx.put("arg_" + i, next);
            }
            ctx.put("arg_count", "" + (i - 1));

            try {
                req = (FXRequest) item.getApplicationObject("echoRequest");
                if (DEBUG && req != null) {
                    System.out.println("found compiled req for " + (String) ctx.get("name"));
                }
            } catch (UnsupportedOperationException ex) {
                // no problem, our repository implementation 
            }

            XMLReader parser = null;
            XProcessor newXP = null;
            String uri = item.getBaseURI();

            // allow the script to reference other scripts with relative address
            if ("true".equals(ConfigProps.getProperty("ItemEvalSetRelativePaths"))) {
                ctx.put("path", item.getDirNamePath());
            }

            if (req == null) {
                
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
                    req = (FXRequest) newXP.compile(parser, new InputSource(uri)).getNodeExtension();
                    item.setApplicationObject("echoRequest", req);
                } catch (Exception ex) {
                    Log.getLogger().info("unable to compile and store request for " + item.getName());
                }

            }
            if ( req == null) {
                newXP.eval(ctx, parser, new InputSource(uri), responseTarget);
            } else {
                req.eval(responseTarget, ctx);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
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
