package com.blnz.fxpl.xform.impl;

import com.blnz.fxpl.xform.TransformException;

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.blnz.xsl.sax2.XSLProcessor;
import com.blnz.xsl.sax2.XSLProcessorImpl;
import com.blnz.xsl.sax2.SaxFilterMaker;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderAdapter;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.Parser;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 *  The StyleSheet Transformer.
 *  Transforms a document with the provided stylesheet.
 */
public class StyleSheetTransformer  extends TransformerImplBase implements Cloneable
{

    // Processor for transformation
    private XSLProcessor _xslProcessor = null;
    private XMLReader _styleReader = null;

    /** 
     * 
     */
    public StyleSheetTransformer() {}
    
    /**
     *
     */
    public StyleSheetTransformer(XMLReader upstream)
    {
        super(upstream); 
    }

    /**
     *
     */
    public Object clone()
    {
        StyleSheetTransformer newt = new StyleSheetTransformer();
        newt._xslProcessor = (XSLProcessor) _xslProcessor.clone();
        newt._styleReader = _styleReader;
        return newt;
    }

    /** 
     * 
     */
    public StyleSheetTransformer(XMLReader upstream, XMLReader styleSheet)
    {
        super(upstream);
        setStyleReader(styleSheet);
    }

    /**
     * @param a SAX Reader which will produce the events of reading
      the stylesheet
     */
    public void setStyleReader(XMLReader styleReader) 
    {
        _styleReader = styleReader;
    }

    
    /** complete any initializations after the client 
     * code has finished with all
     *	the setters for this  object and just before the transform is run
     */
    private void prepareProcessor() throws Exception
    {
        if (_xslProcessor == null) {
            try {
                _xslProcessor = new XSLProcessorImpl();
                
                _xslProcessor.setReaders(getParent(),
                                        _styleReader);
                
                if (_ssInputSource == null) {
                    _ssInputSource = new InputSource(new StringReader(""));
                } 
                
                //     System.out.println("StyleSheetTransformer::prepare() .. loading styleshhet from src {" +
                //                          _ssInputSource.getSystemId() + "}");
                
                _xslProcessor.loadStylesheet(_ssInputSource);   
            } catch (Exception ex) {
                _xslProcessor = null;
                throw ex;
            }
            
        } else {
            // we've already compiled stylesheet
            _xslProcessor.setSourceReader(getParent());

        }

        if (_params != null) {
            setXslProcessorParams(_params);
        }
        
        // attach an output handler
        // ParserAdapter pa = new ParserAdapter(_xslProcessor);
        // pa.setContentHandler(this);
        setParent(_xslProcessor);
    }
    
    /**
     * sets the requested run-time params in the XSLProcessor
     */
    private void setXslProcessorParams(Hashtable params)
    {
        for (Enumeration e = params.keys(); e.hasMoreElements() ; ) {
            String key = (String) e.nextElement();
            Object value = params.get(key);
            if ("com.blnz.xsl.sax2.SaxFilterMaker".equals(key) &&
                value instanceof SaxFilterMaker) {
                _xslProcessor.setSaxExtensionFilter("echo",
                                                    (SaxFilterMaker) value);
            } else {
                _xslProcessor.setParameter(key, params.get(key));
            }
        }
    }

    /**
     * run the transformation
     */
    public void parse(InputSource s) throws SAXException
    {
        try {
            prepareProcessor();
            super.parse(s);

        } catch (SAXException ex) {
            System.out.println("#### StyleSheetTransformer caught SAXException");
            // ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
            System.out.println("#### StyleSheetTransformer caught generic Exception");
            // ex.printStackTrace();
            throw new SAXException(ex);
        }
    }
}




