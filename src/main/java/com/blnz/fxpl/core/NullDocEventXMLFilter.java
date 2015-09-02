package com.blnz.fxpl.core;

import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.log.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.DocumentHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.Hashtable;

/**
 * provides a SAX 2 XMLReader interface on top of an FX Request 
 * element, while filtering out start/end document events
 */
public class NullDocEventXMLFilter extends FXRequestReaderAdapter
{

    public NullDocEventXMLFilter(FXContext ctxt, FXRequest req)
    {
        super(ctxt, req);
    }

    /**
     * does not propogate the SAX startDocument event to the downstream
     * consumer
     */
    public void startDocument()
    {}

    /**
     * does not propogate the SAX endDocument event to the downstream
     * consumer
     */
    public void endDocument()
    {}
    
}
