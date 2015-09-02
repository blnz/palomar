package com.blnz.fxpl.xform;

import org.xml.sax.ContentHandler;

import org.w3c.dom.Document;

/**
 * constructs a DOM from a SAX event stream
 */
public interface  DOMWriter extends ContentHandler
{
    /**
     * @return the Document which was created by sending
     *  the SAX events to this guy
     */
    public Document getDocument();
}
