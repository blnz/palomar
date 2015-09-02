package com.blnz.xsl.om;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

/**
 * allows for us to attach additional semantics, beyond that specified for XML, to Nodes in the parse tree
 */
public class NullNodeExtension implements NodeExtension
{
    private Node _node;

    public Node getNode()
    {
        return _node;
    }

    public void setNode(Node n)
    {
        _node = n;
    }

   /**
     * evaluate the request, writing results to responseTarget
     * @param responseTarget a SAX2 ContentHandler which will
     *  receive the response
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
	throws Exception
    {}

    /**
     * evaluate the request
     * @param responseTarget a SAX2 ContentHandler which will
     *  receive the response
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context, InputSource s) 
	throws Exception
    {}



}
