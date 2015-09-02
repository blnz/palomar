package com.blnz.fxpl.xform;

import java.util.Hashtable;
import java.io.Reader;
import java.io.Writer;
import java.io.OutputStream;

import org.xml.sax.DocumentHandler;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;

/**
 *  The Transformer
 *  Interface for all specific implementations of Transformation engine's.
 *  recieves SAX events from an upstream XMLReader, runs some sort of
 *  XML transformation, sends the result downstream to a receiving
 *  content handler.
 *
 */
public interface Transformer extends XMLFilter, EntityResolver, 
                                     DTDHandler, ContentHandler, ErrorHandler
{
    
    /** 
     * StyleSheet setter
     * @param styleSheet used as a file name 
     */
    public void setStylesheet(String styleSheet) throws TransformException;
    
    
    /** params setter 
     * @param params The parameters used by e.g. an XSLT engine 
     */
    public void setParams(Hashtable params);


}

