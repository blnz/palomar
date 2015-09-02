
package com.blnz.fxpl.xform.impl;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.NodeIterator;
import com.blnz.xsl.om.SafeNodeIterator;

import com.blnz.xsl.om.XSLException;
import com.blnz.xsl.tr.ProcessContext;
import com.blnz.xsl.tr.Result;
import com.blnz.xsl.sax2.MultiNamespaceResult;

import java.io.IOException;

/**
 * An XMLREader (or filter) that only parses the InputSource given at construction time
 */
public class OMReader implements XMLReader

{
    private Node _src = null;
    private ContentHandler _contentHandler = null;
    private ErrorHandler _errorHandler = null;
    

    public OMReader(Node n) throws SAXException
    {
        _src = n;
    }

    /**
     *          Return the current content handler.
     */
    public ContentHandler  getContentHandler() 
    {
        return _contentHandler;
    }

    /**
     *          Return the current DTD handler.
     */
    public DTDHandler getDTDHandler() 
    {
        return null;
    }

    /**
     *          Return the current entity resolver.
     */
    public EntityResolver  getEntityResolver() 
    {
        return null;
    }

    /**
     *          Return the current error handler.
     */
    public ErrorHandler	getErrorHandler() 
    {
        return _errorHandler;
    }

    /**
     *          Look up the value of a feature flag.
     */
    public boolean	getFeature(String name) 
    {
        return false;
    }
    
    /**
     *          Look up the value of a property.
     */
    public Object	getProperty(String name) 
    {
        return null;
    }
    
    /**
     *          Parse an XML document.
     */
    public void	parse(InputSource input) throws SAXException
    {
        Result result = new MultiNamespaceResult(_contentHandler, _errorHandler);
        _contentHandler.startDocument();
        NodeIterator ni = new SafeNodeIterator() {
            Node _n = _src;   
            public Node next() { Node current = _n; _n = null; return current; }
            
        };
        try {
            copyNodes(ni, result);
        } catch (XSLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        _contentHandler.endDocument();

    }
    
    /**
     *          Parse an XML document from a system identifier (URI).
     */
    public void	parse(String systemId) throws SAXException
    {
        parse(new InputSource(systemId));
        
    }

    /**
     *          Allow an application to register a content event handler.
     */
    public void	setContentHandler(ContentHandler handler) 
    {
            _contentHandler = handler;
    }
    
    /**
     *          Allow an application to register a DTD event handler.
     */
    public void	setDTDHandler(DTDHandler handler) 
    {}

    /**
     *          Allow an application to register an entity resolver.
     */
    public void	setEntityResolver(EntityResolver resolver) 
    {}
    
    /**
     *          Allow an application to register an error event handler.
     */
    public void	setErrorHandler(ErrorHandler handler) 
    {
        _errorHandler = handler;
    }
    
    /**
     *          Set the value of a feature flag.
     */
    public void	setFeature(String name, boolean value) 
    {}
    
    /**
     *          Set the value of a property.
     */
    public void	setProperty(String name, Object value) 
    {}
    
    /**
    *
    */
   private static void copyNodes(NodeIterator iter,
                                 Result result) throws XSLException
   {
       for (;;) {
           Node node = iter.next();
           if (node == null)
               break;
           switch (node.getType()) {

           case Node.ROOT:
               copyNodes(node.getChildren(), result);
               break;

           case Node.TEXT:
               result.characters(node.getData());
               break;

           case Node.ATTRIBUTE:
               result.attribute(node.getName(), node.getData());
               break;

           case Node.PROCESSING_INSTRUCTION:
               result.processingInstruction(node.getName().toString(),
                                            node.getData());
               break;

           case Node.COMMENT:
               result.comment(node.getData());
               break;

           case Node.ELEMENT:
               result.startElement(node.getName(),
                                   node.getNamespacePrefixMap());
               copyNodes(node.getAttributes(), result);
               copyNodes(node.getChildren(), result);
               result.endElement(node.getName());
               break;
           }
       }
   }
    
}
