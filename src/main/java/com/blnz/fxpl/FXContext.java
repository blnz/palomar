package com.blnz.fxpl;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * provides context information for an FXPL processor.
 *  FXContext objects provide the FXPL processor the ability
 *  to pass parameters to nested FXPL request handlers
 *  FXContext objects may contain a handle to a "parent"
 *  context.  If the requested key is not found in the requested
 *  context, it will attempt to find it in the parent context
 */
public interface FXContext  extends com.blnz.xsl.om.NodeExtension, com.blnz.xsl.om.ExtensionContext
{

    /**
     * a parent context to delegate queries
     */ 
    public void setParentContext(FXContext parentContext);

    /**
     * sends a trace of the nested hierarchy of contexts
     */
    public void sendStackTrace(ContentHandler target) throws
        SAXException;

    /**
     * a trace of the nested hierarchy of contexts
     */
    public void sendStackTrace(FXException e, ContentHandler target) throws
    SAXException;

    /**
     *
     */
    public FXContext extend();

}
