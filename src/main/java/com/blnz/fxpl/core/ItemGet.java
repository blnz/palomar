package com.blnz.fxpl.core;

import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.FXException;
import com.blnz.fxpl.security.Security;
import com.blnz.fxpl.security.User;
import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.log.Logger;

import com.blnz.xsl.om.ExtensionContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * fetch an xml document from the repository, and evaluate it as FXPL
 */
public class ItemGet extends FXRequestServerSide
{

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
        
        boolean isBinary = toBoolean((String)context.get("asBinary"));

        
        try {
            RepositoryItem item = super.getItem(ctx);

            ctx.put("path", item.getDirNamePath());
            ctx.put("ctx:commandItem", item.getFullPath()); 
            FXRequest req = null;
 
           User user = getUser(ctx);
            
           if (! Security.getSecurityService().checkRead(user, item)) {
                throw new FXException("No READ permission for user " + 
                                       user.getUsername() + " on item: " + 
                                       item.getDirNamePath() + "/" +
                                       item.getName() );
            }
            if (isBinary) {
                OutputStream sink = (OutputStream) context.get("stdout");
                InputStream src = item.getInputStream();
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
            } else {
                XMLReader parser = null;
                try {
                    // FIXME: ensure item is XML before trying
    
                    parser = item.openXMLReader();
                } catch (Exception ex) {
                    // ex.printStackTrace();
                    throw new FXException("failed to open XMLReader " + 
                                           " on item: " + 
                                           item.getDirNamePath() + "/" +
                                           item.getName() );
                }
                parser.setContentHandler(responseTarget);
                parser.parse(item.getBaseURI());
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
