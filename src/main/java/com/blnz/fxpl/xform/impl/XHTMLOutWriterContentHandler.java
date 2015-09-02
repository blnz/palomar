package com.blnz.fxpl.xform.impl;

import com.blnz.fxpl.xform.TransformException;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
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
 * a SAX2 content Handler that writes characters as XHTML
 */
public class XHTMLOutWriterContentHandler extends OutWriterContentHandler
{

    private boolean inCdata = false;

    /**
     * construct with a stream to write to
     */
    public XHTMLOutWriterContentHandler(Writer os)
    {
        super(os);
    }

    public void startElement (String uri, String localName, String qName,
			      Attributes atts)
	throws SAXException
    {
        super.startElement(uri, localName, qName, atts);
        if ("script".equals(localName)) {
            inCdata = true;
        }
    }

    /**
     * Filter an end element event.
     *
     * @param uri The element's Namespace URI, or the empty string.
     * @param localName The element's local name, or the empty string.
     * @param qName The element's qualified (prefixed) name, or the empty
     *        string.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement (String uri, String localName, String qName)
	throws SAXException
    {
        if (inStartTag) {
            inStartTag = false;
            
            if ( "base".equals(localName)  ||
                 "meta".equals(localName)  ||
                 "link".equals(localName)  ||
                 "hr".equals(localName)  ||
                 "br".equals(localName)  ||
                 "basefont".equals(localName)  ||
                 "param".equals(localName)  ||
                 "img".equals(localName)  ||
                 "area".equals(localName)  ||
                 "input".equals(localName)  ||
                 "isindex".equals(localName)  ||
                 "col".equals(localName)  
                 ) {
                put('/');
                put('>');
                inCdata = false;
                return;
            }
            put('>');
        } else {
            if (inCdata) {
                put('\n');
                put('/');
                put('/');
                put(']');
                put(']');
                put('>');
                put('\n');
            }
        }
        inCdata = false;
        put('<');
        put('/');
        writeRaw(qName);
        put('>');
    } 

    /**
     * Filter a character data event.
     *
     * @param cbuf An array of characters.
     * @param off The starting position in the array.
     * @param len The number of characters to use from the array.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters (char cbuf[], int off, int len)
	throws SAXException
    {
        if (len == 0) {
            return;
        }
        if (inStartTag) {
            finishStartTag();
            if (inCdata) {
                put('\n');
                put('/');
                put('/');
                put('<');
                put('!');
                put('[');
                put('C');
                put('D');
                put('A');
                put('T');
                put('A');
                put('[');
                put('\n');
            }
        }
        if (inCdata) {
            writeRaw(new String(cbuf, off, len));
        } else {
            do {
                char c = cbuf[off++];
                if (c > 127) {
                    //                String s = "&#x" + Integer.toHexString(c) + ";";
                    writeRaw(getCharString(c));
                } else {
                    
                    switch (c) {
                    case '&':
                        writeRaw("&amp;");
                        break;
                    case '<':
                        writeRaw("&lt;");
                        break;
                    case  '>':
                        writeRaw("&gt;");
                        break;
                    default:
                        put(c);
                    }
                }
            } while (--len > 0);
        }
    }
}
