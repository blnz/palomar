package com.blnz.fxpl.xform.impl;


import org.xml.sax.helpers.XMLFilterImpl;

import java.io.OutputStream;

/**
 * a SAX2 content Handler that writes characters
 */
public class ByteStreamSerializer extends XMLFilterImpl
{
    
    private OutStreamContentHandler _handler = null;
    private NamespaceDeclAugmenter _nsfixer = null;
    private OutputStream os = null;
    private boolean omitDecl = true;
    
    /**
     * construct with a stream to write to
     */
    public ByteStreamSerializer(OutputStream os, boolean omitDecl)
    {
        init(os, omitDecl);
    }

    /**
     * construct with a stream to write to
     */
    public ByteStreamSerializer(OutputStream os)
    {
        init(os, true);
    }

    /**
     *
     */
    private void init(OutputStream os, boolean omitDecl)
    {
        // the actual work of writing chars is done by handler
        _handler = new OutStreamContentHandler(os, omitDecl);

        // we'll want to normalize namespace declarations on the way out
        _nsfixer = new NamespaceDeclAugmenter();
        _nsfixer.setContentHandler(_handler);

        // SAX events should be handled first by the namespace fixer, 
        // then pushed trhough the character writer
        super.setContentHandler(_nsfixer);
        this.os = os;
        this.omitDecl = omitDecl;
    }

    public void setEscapeXML(boolean escapeXML) {
        if (!escapeXML) {
            _handler = new NonEscapingOutStreamContentHandler(os, omitDecl);
            _nsfixer.setContentHandler(_handler);
        }
    }
    
    /**
     *
     */
    public String getEncoding() 
    {
        return _handler.getEncoding();
    }
    
    /**
     *
     */
    public void setEncoding(String encoding) 
    {
        _handler.setEncoding(encoding);
    }

    /**
     *
     */
    public void setMinimize(byte minimize) 
    {
        _handler.setMinimize(minimize);
    }

}
