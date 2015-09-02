package com.blnz.fxpl.core;

import com.blnz.fxpl.*;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.Name;
import com.blnz.xsl.om.ExtensionContext;
import com.blnz.xsl.om.NameTable;
import com.blnz.xsl.om.SafeNodeIterator;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * the base class for representing all elements in the FX language.
 */
public class FXRequestImpl
    implements FXRequest
{

    private Node _node = null;

    public Node getNode() { return _node; }
    public void setNode(Node n) { _node = n; }

    /**
     * evaluate the request to an empty element.
     * @param responseTarget a SAX2 ContentHandler which will
     *  receive the response
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
	throws Exception
    {
        // a no-op
        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute("", "srcElName", "srcElName", "CDATA", getTagName());
        atts.addAttribute("", "srcElNS", "srcElNS", "CDATA", getNamespaceURI());

        responseTarget.startElement(FXHome.NAMESPACE, "empty" , 
                                    "empty", atts);
        responseTarget.endElement(FXHome.NAMESPACE, "empty" , 
                                  "empty");
    }

    /**
     * evaluate the request
     * @param responseTarget a SAX2 ContentHandler which will
     *  receive the response
     */
    public void eval(ContentHandler responseTarget, 
                     ExtensionContext context, InputSource s) 
	throws Exception
    {
        eval(responseTarget, context);
    }

    /**
     *
     */
    public String getURI(FXContext context)
    {
        return "internal";
    }

    // FIXME:  I think we want to include the namespace prefix
    public String getTagName() 
    {
        return _node == null ? null : _node.getName().getLocalPart();
    }


    public String getNamespaceURI() 
    {
        return _node == null ? null : _node.getName().getNamespace();
    }


    public SafeNodeIterator getChildNodes()
    {
        if (_node == null) {
            return null;
        }
        return _node.getChildren();
    }

    //  get our document's Name object for the given string
    public final Name name(String name)
    {
        // Node n = this.getNode();
        // if (n == null) {
        //     System.out.println("FXRequestImpl::name() null node");
        // }

        // Name nname = n.getName();
        // if (nname == null) {
        //     System.out.println("FXRequestImpl::name() null nodename");
        // }

        // NameTable nt = (NameTable) nname.getCreator();
        // if (nt == null) {
        //     System.out.println("FXRequestImpl::name() null nametable");
        // }


        return ((NameTable) this.getNode().getName().getCreator()).createName(name);
    }


}
