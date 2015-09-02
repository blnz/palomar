package com.blnz.fxpl.xform.impl;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.IOException;

/**
 * An XMLReader (or filter) that only parses the InputSource that was given at construction time
 */
public class ISXMLReaderFilter extends XMLFilterImpl

{
    private InputSource _src;

    public ISXMLReaderFilter(XMLReader rdr, InputSource src) throws SAXException
    {
        super(rdr);
        _src = src;
    }

    public void parse(InputSource src) 
        throws SAXException, IOException
    {
        super.parse(_src);
    }

    public void parse(String sysID) 
        throws SAXException,  IOException
    {
        super.parse(_src);
    }
    
}
