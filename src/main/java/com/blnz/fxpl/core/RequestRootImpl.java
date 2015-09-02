package com.blnz.fxpl.core;

import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.FXRequest;

import com.blnz.xsl.om.ExtensionContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import org.xml.sax.helpers.AttributesImpl;;

public class RequestRootImpl extends FXRequestServerSide {

    /**
     * evaluate the request
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
        throws Exception
    {
        // FIXME: check for only one element child?
        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute("", "xmlns", "xmlns", "CDATA", FXHome.SESSION_NAMESPACE);

        responseTarget.startElement(FXHome.SESSION_NAMESPACE, 
                                    "response", "response", 
                                    atts);

        FXRequest child = getSubRequest(1);

        XMLReader reader = new NullDocEventXMLFilter((FXContext) context, child);

        String uri = child.getURI((FXContext)context);
        InputSource is = new InputSource(uri);

        reader.setContentHandler(responseTarget);

        reader.parse(is); // eval our child

        responseTarget.endElement(FXHome.SESSION_NAMESPACE, "response", "response");

    }
}
