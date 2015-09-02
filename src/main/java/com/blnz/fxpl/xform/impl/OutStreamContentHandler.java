package com.blnz.fxpl.xform.impl;

import com.blnz.fxpl.xform.TransformException;

import com.blnz.xsl.sax2.EncodingName;

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
 *
 */
public class OutStreamContentHandler implements ContentHandler
{

    Locator _locator = null;
    OutputStream _os = null;
    boolean hasStarted = false;
    
    private boolean		recreatedAttrs;
    private AttributesImpl	attributes = new AttributesImpl ();

    private String encoding;
    
    public OutStreamContentHandler(OutputStream os, boolean omitDecl)
    {
        init(os);
        omitXmlDeclaration = omitDecl;        
    }


    /**
     * construct with a stream to write to
     */
    public OutStreamContentHandler(OutputStream os)
    {
        init(os);
    }

    private void init(OutputStream os)
    {
        lineSeparator = System.getProperty("line.separator");
        this._os = os;
        out = os;
    }


    public String getEncoding() 
    {
        return encoding;
    }
    
    protected void setEncoding(String encoding) 
    {
        if (encoding == null)
            this.encoding = "UTF-8";
        else
            this.encoding = EncodingName.toIana(encoding);
    }

    public void setOmitDecl(boolean omitDecl) {
        this.omitXmlDeclaration = omitDecl;
    }
    
    public boolean isOmittingDecl() {
        return this.omitXmlDeclaration;
    }
    
    public boolean keepOpen() 
    {
        return keepOpen;
    }
    
    public Writer getWriter(String contentType, String encoding)
        throws IOException, UnsupportedEncodingException 
    {
        //        OutputStream out = getOutputStream(contentType, encoding);
        return new OutputStreamWriter(_os, EncodingName.toJava(getEncoding()));
    }


    /**
     * Filter a new document locator event.
     *
     * @param locator The document locator.
     * @see org.xml.sax.ContentHandler#setDocumentLocator
     */
    public void setDocumentLocator (Locator locator)
    {
	this._locator = locator;
    }


    /**
     * Filter a start document event.
     *
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    public void startDocument ()
	throws SAXException
    {
        if (!omitXmlDeclaration && !hasStarted) {
            writeRaw("<?xml version=\"1.0\" encoding=\"utf-8\"");
            if (standalone != null) {
                writeRaw(" standalone=\"");
                writeRaw(standalone);
                put((byte)'"');
            }
            writeRaw("?>");
            writeRaw(lineSeparator);
        }
        hasStarted = true;
    }


    /**
     * Filter an end document event.
     *
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public void endDocument ()
	throws SAXException
    {
        if (bufUsed != 0)
            flushBuf();
        try {
            if (out != null) {
                if (keepOpen)
                    out.flush();
                else
                    out.close();
                //   out = null;
            }
        }
        catch (java.io.IOException e) {
            throw new SAXException(e);
        }
        // out = null;
        // buf = null;
    }


    /**
     * Filter a start Namespace prefix mapping event.
     *
     * @param prefix The Namespace prefix.
     * @param uri The Namespace URI.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     */
    
    // SAX2
    public void startPrefixMapping (String prefix, String uri)
    {
        // reconstruct "xmlns" attributes deleted by all
        // SAX2 parsers without "namespace-prefixes" = true
        if ("".equals (prefix))
            attributes.addAttribute ("", "", "xmlns",
                                     "CDATA", uri);
        else
            attributes.addAttribute ("", "", "xmlns:" + prefix,
                                     "CDATA", uri);
        recreatedAttrs = true;
    }


    /**
     * Filter an end Namespace prefix mapping event.
     *
     * @param prefix The Namespace prefix.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     */
    public void endPrefixMapping (String prefix)
	throws SAXException
    {
        // FIXME do something

    }


    /**
     * Filter a start element event.
     *
     * @param uri The element's Namespace URI, or the empty string.
     * @param localName The element's local name, or the empty string.
     * @param qName The element's qualified (prefixed) name, or the empty
     *        string.
     * @param atts The element's attributes.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement (String uri, String localName, String qName,
			      Attributes atts)
	throws SAXException
    {
        if (inStartTag) {
            finishStartTag();
        }
        if (outputDoctype) {
            outputDoctype = false;
            writeRaw("<!DOCTYPE ");
            writeRaw(qName);
            if (doctypePublic != null) {
                writeRaw(" PUBLIC ");
                byte lit = doctypePublic.indexOf('"') >= 0 ? 
                    (byte)'\'' : 
                    (byte)'"';
                put(lit);
                writeRaw(doctypePublic);
                put(lit);
            }
            else
                writeRaw(" SYSTEM");
            if (doctypeSystem != null) {
                byte lit = doctypeSystem.indexOf('"') >= 0 ? 
                    (byte)'\'' :
                    (byte)'"';
                put((byte)' ');
                put(lit);
                writeRaw(doctypeSystem);
                put(lit);
            }
            put((byte)'>');
            writeRaw(lineSeparator);
        }
        put((byte)'<');
        
        // FIXME use prefix map?
        writeRaw(qName);

        // ... then any recreated ones (DOM deletes duplicates)
        if (recreatedAttrs) {
            recreatedAttrs = false;

            int len = attributes.getLength();
            for (int i = 0; i < len; ++i) {
                String qname = attributes.getQName(i);
                if (atts.getValue(qname) == null) {
                    put((byte)' ');
                    
                    // FIXME: URI prefix map?
                    writeRaw(attributes.getQName(i));
                    put((byte)'=');
                    put((byte)'"');
                    attributeValue(attributes.getValue(i));
                    put((byte)'"');
                }
            } 
            attributes.clear ();
        }

        // now the attributes
        int n = atts.getLength();
        for (int i = 0; i < n; i++) {
            put((byte)' ');

            // FIXME: URI prefix map?
            writeRaw(atts.getQName(i));
            put((byte)'=');
            put((byte)'"');
            attributeValue(atts.getValue(i));
            put((byte)'"');
        }
        inStartTag = true;
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
    public void endElement (String uri, String localName,
                            String qName)
	throws SAXException
    {
        if (inStartTag) {
            inStartTag = false;
            if (minimize != MINIMIZE_NONE) {
                if (minimize == MINIMIZE_EMPTY_ELEMENTS_HTML)
                    put((byte)' ');
                put((byte)'/');
                put((byte)'>');
                return;
            }
            put((byte)'>');
        }
        put((byte)'<');
        put((byte)'/');
        writeRaw(qName);
        put((byte)'>');
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
        }
        do {
            char c = cbuf[off++];
            switch (c) {
            case '\n':
                writeRaw(lineSeparator);
                break;
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
            }
        } while (--len > 0);
    }


    /**
     * Filter an ignorable whitespace event.
     *
     * @param ch An array of characters.
     * @param start The starting position in the array.
     * @param length The number of characters to use from the array.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#ignorableWhitespace
     */

    public void ignorableWhitespace (char ch[], int start, int length)
        throws SAXException {
        for (; length > 0; length--, start++)
            put((byte)ch[start]);
    }

    /**
     * Filter a processing instruction event.
     *
     * @param target The processing instruction target.
     * @param data The text following the target.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    public void processingInstruction(String target, String data)
        throws SAXException 
    {
        if (target == null) {
            comment(data);
            return;
        } else if ("xfyfs".equals(target)) {
            // do not propogate xfyfs target
            return;
        }
        if (inStartTag) {
            finishStartTag();
        }
        put((byte)'<');
        put((byte)'?');
        writeRaw(target);
        if (data.length() > 0) {
            put((byte)' ');
            writeMarkup(data);
        }
        put((byte)'?');
        put((byte)'>');
    }


    /**
     * Filter a skipped entity event.
     *
     * @param name The name of the skipped entity.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#skippedEntity
     */
    public void skippedEntity (String name)
	throws SAXException
    {
        // FIXME do something

    }
    ///////////////////////////////

    private OutputStream out = null;
    private boolean keepOpen = false;
    protected boolean inStartTag = false;
    private boolean omitXmlDeclaration = true;
    private String standalone;
    private static final int DEFAULT_BUF_LENGTH = 8*1024;
    private byte[] buf = new byte[DEFAULT_BUF_LENGTH];
    private int bufUsed = 0;
    protected String lineSeparator;
    private byte minimize = MINIMIZE_EMPTY_ELEMENTS;
    private String doctypeSystem;
    private String doctypePublic;
    private boolean outputDoctype = false;

    static final public byte MINIMIZE_NONE = 0;
    static final public byte MINIMIZE_EMPTY_ELEMENTS = 1;
    static final public byte MINIMIZE_EMPTY_ELEMENTS_HTML = 2;


//      public DocumentHandler init(Destination dest, AttributeList atts) throws IOException {
//          this.out = dest.getOutputStream("application/xml", null);
//          this.keepOpen = dest.keepOpen();
//          if ("yes".equals(atts.getValue("omit-xml-declaration")))
//              omitXmlDeclaration = true;
//          this.standalone = atts.getValue("standalone");
//          this.doctypeSystem = atts.getValue("doctype-system");
//          this.doctypePublic = atts.getValue("doctype-public");
//          if (this.doctypeSystem != null || this.doctypePublic != null)
//              outputDoctype = true;
//          if ("yes".equals(atts.getValue("indent")))
//              return new Indenter(this, this);
//          return this;
//      }

    public void setMinimize(byte minimize) {
        this.minimize = minimize;
    }

    
    public void rawCharacters(String chars) throws SAXException {
        if (inStartTag)
            finishStartTag();
        writeRaw(chars);
    }

    /**
     *
     */
    protected void writeRaw(String str) throws SAXException 
    {
        final int n = str.length();
        for (int i = 0; i < n; i++) {
            char c = str.charAt(i);
            if (c < 0x80)
                put((byte)c);
            else {
                try {
                    writeMB(str.charAt(i));
                }
                catch (CharConversionException e) {
                    if (++i == n)
                        throw new SAXException(e.getMessage());
                    writeSurrogatePair(c, str.charAt(i));
                }
            }
        }
    }

    /**
     *
     */
    protected final void writeMB(char c)
        throws SAXException, CharConversionException
    {
        switch (c & 0xF800) {
        case 0:
            put((byte)(((c >> 6) & 0x1F) | 0xC0));
            put((byte)((c & 0x3F) | 0x80));
            break;
        default:
            put((byte)(((c >> 12) & 0xF) | 0xE0));
            put((byte)(((c >> 6) & 0x3F) | 0x80));
            put((byte)((c & 0x3F) | 0x80));
            break;
        case 0xD800:
            throw new CharConversionException("invalid surrogate pair");
        }
    }
  
    /**
     *
     */
    protected final void writeSurrogatePair(char c1, char c2)
        throws SAXException 
    {
        if ((c1 & 0xFC00) != 0xD800 || (c2 & 0xFC00) != 0xDC00)
            throw new SAXException("invalid surrogate pair");
        int c = ((c1 & 0x3FF) << 10) | (c2 & 0x3FF);
        c += 0x10000;
        put((byte)(((c >> 18) & 0x7) | 0xF0));
        put((byte)(((c >> 12) & 0x3F) | 0x80));
        put((byte)(((c >> 6) & 0x3F) | 0x80));
        put((byte)((c & 0x3F) | 0x80));
    }

    /**
     *
     */
    protected void attributeValue(String value) throws SAXException 
    {
        int valueLength = value.length();
        for (int j = 0; j < valueLength; j++) {
            char c = value.charAt(j);
            switch (c) {
            case '\n':
                writeRaw("&#10;");
                break;
            case '&':
                writeRaw("&amp;");
                break;
            case '<':
                writeRaw("&lt;");
                break;
            case '"':
                writeRaw("&quot;");
                break;
            case '\r':
                writeRaw("&#13;");
                break;
            case '\t':
                writeRaw("&#9;");
                break;
            default:
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
                break;
            }
        }
    }

    /**
     *
     */
    protected final void finishStartTag() throws SAXException {
        inStartTag = false;
        put((byte)'>');
    }

    /**
     *
     */
    public void markup(String chars) throws SAXException {
        if (inStartTag)
            finishStartTag();
        writeMarkup(chars);
    }

    /**
     *
     */
    public void comment(String body) throws SAXException 
    {
        if (inStartTag) {
            finishStartTag();
        }
        writeRaw("<!--");
        writeMarkup(body);
        writeRaw("-->");
    }

    /**
     *
     */
    private void writeMarkup(String str) throws SAXException 
    {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == '\n')
                writeRaw(lineSeparator);
            else if (c < 0x80)
                put((byte)c);
            else {
                try {
                    writeMB(c);
                }
                catch (CharConversionException e) {
                    if (++i == len)
                        throw new SAXException(e);
                    writeSurrogatePair(c, str.charAt(i));
                }
            }
        }
    }

    /**
     *
     */
    protected final void put(byte b) throws SAXException 
    {
        if (bufUsed == buf.length) {
            flushBuf();
        }
        buf[bufUsed++] = b;
    }

    /**
     *
     */
    private final void flushBuf() throws SAXException 
    {
        try {
            out.write(buf, 0, bufUsed);
            bufUsed = 0;
        }
        catch (java.io.IOException e) {
            throw new SAXException(e);
        }
    }

}

