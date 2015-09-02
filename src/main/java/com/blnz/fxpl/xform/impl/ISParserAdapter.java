// $Id: ISParserAdapter.java 46 2004-12-11 00:39:16Z blindsey $

package com.blnz.fxpl.xform.impl;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.Parser;
import java.io.IOException;


public class ISParserAdapter extends ParserAdapter

{
    private InputSource _src;

    public ISParserAdapter (Parser p, InputSource src) throws SAXException
    {
        super(p);
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
