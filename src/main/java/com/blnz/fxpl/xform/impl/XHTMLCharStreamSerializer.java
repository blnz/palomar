package com.blnz.fxpl.xform.impl;

import com.blnz.fxpl.xform.TransformException;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;

/**
 * a SAX2 content Handler that writes characters
 */
public class XHTMLCharStreamSerializer extends XMLFilterImpl
{

    private OutWriterContentHandler _handler = null;
    private NamespaceDeclAugmenter _nsfixer = null;

    /**
     * construct with a stream to write to
     */
    public XHTMLCharStreamSerializer(Writer os)
    {
        // the actual work of writing chars is done by handler
        _handler = new XHTMLOutWriterContentHandler(os);

        // we'll want to normalize namespace declarations on the way out
        _nsfixer = new NamespaceDeclAugmenter();
        _nsfixer.setContentHandler(_handler);

        // SAX events should be handled first by the namespace fixer, 
        // then pushed trhough the character writer
        super.setContentHandler(_nsfixer);
    }


    public void setMinimize(byte minimize) 
    {
        _handler.setMinimize(minimize);
    }

    /**
     * set a flag indicating if we should be emitting html's character
     * entities instead of unicode code point entities
     */
    public void setSendHTMLCharEntities(boolean flag)
    {
        _handler.setSendHTMLCharEntities(flag);
    }

}
