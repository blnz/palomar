package com.blnz.fxpl.xform;

import org.xml.sax.Attributes;

/**
 * created and used by ElementFilter
 */
public interface ElementTest
{

    public boolean testStartElement(String uri, String localName, 
                                    String qName, Attributes atts);

    public boolean isFinished();
}
