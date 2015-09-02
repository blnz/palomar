package com.blnz.fxpl.core;

import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.XProcessor;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.log.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * for recursively processing FX request
 */
public class RequestReReader extends org.xml.sax.helpers.XMLFilterImpl
{
    private FXRequest _req;
    private FXContext _ctxt;
    private Hashtable nsMap = new Hashtable();
    
    public RequestReReader(FXRequest req, FXContext ctxt) 
    {
        _req = req;
        _ctxt = ctxt;
    }
    
    public void parse(InputSource s) throws SAXException
    {
        Logger logger = Log.getLogger();

        try {
            _req.eval(this, _ctxt);
        } catch (Exception ex) {
            // TEMP
            ex.printStackTrace();
            throw new SAXException(ex);
        }
        
    }
    
    public void startPrefixMapping (String prefix, String uri)
        throws SAXException
    {
        nsMap.put(prefix, uri);
        //                      super.startPrefixMapping (prefix, uri);
    }
    
    private void setNSDeclAsAttr(Attributes atts)
    {
        Enumeration keys = nsMap.keys();
        if (keys.hasMoreElements()) {
            if (! (atts instanceof AttributesImpl)) {
                atts = new AttributesImpl(atts);
            }
            while(keys.hasMoreElements()) {
                String prefix = (String) keys.nextElement();  
                String uri = (String) nsMap.get(prefix);
                if ("".equals (prefix)) {
                    int index = atts.getIndex("xmlns");
                    if (index == -1) {
                        
                        ((AttributesImpl)atts).addAttribute("", "", "xmlns", 
                                                            "CDATA", uri);
                    } else {
                        ((AttributesImpl)atts).setAttribute(index, "", "", "xmlns", 
                                                            "CDATA", uri);
                    }
                } else {
                    int index = atts.getIndex("xmlns:" + prefix);
                    if (index == -1) {
                        ((AttributesImpl)atts).addAttribute ("", "", "xmlns:" + prefix, 
                                                             "CDATA", uri);
                    } else {
                        ((AttributesImpl)atts).setAttribute (index, "", "", 
                                                             "xmlns:" + prefix, 
                                                             "CDATA", uri);
                    }
                }                       
            }
            nsMap.clear();
        }
    }
    
    public void startElement(java.lang.String uri, 
                             java.lang.String localName, java.lang.String qName, 
                             Attributes atts)
        throws SAXException
    {
        //emit namespace declarations as attributes
        setNSDeclAsAttr(atts);
        
        super.startElement( uri, localName, qName, atts);
    }
    
}
