package com.blnz.fxpl.xform.impl;

import com.blnz.fxpl.xform.TransformException;


import java.io.CharConversionException;
import java.io.OutputStream;

import org.xml.sax.SAXException;

/**
 *
 */
public class NonEscapingOutStreamContentHandler extends OutStreamContentHandler
{

    public NonEscapingOutStreamContentHandler(OutputStream os, boolean omitDecl) {
        super(os, omitDecl);
    }

    public void characters (char cbuf[], int off, int len)
    throws SAXException
    {
        if (len == 0) {
            return;
        }
        if (inStartTag) {
            super.finishStartTag();
        }
        do {
            char c = cbuf[off++];
            if (c < 0x80)
                put((byte)c);
            else {
                try {
                    writeMB(c);
                }
                catch (CharConversionException e) {
                    if (len-- == 0)
                        throw new SAXException(e);
                    writeSurrogatePair(cbuf[off - 1], cbuf[off]);
                    off++;
                }
            }
        } while (--len > 0);
    }
    /**
    *
    */
   protected void attributeValue(String value) throws SAXException 
   {
       int valueLength = value.length();
       for (int j = 0; j < valueLength; j++) {
           char c = value.charAt(j);
           if (c < 0x80)
               put((byte)c);
           else {
               try {
                   writeMB(c);
               }
               catch (CharConversionException e) {
                   if (++j == valueLength)
                       throw new SAXException(e.getMessage());
                   writeSurrogatePair(value.charAt(j - 1), value.charAt(j));
               }
           }
       }
   }

}

