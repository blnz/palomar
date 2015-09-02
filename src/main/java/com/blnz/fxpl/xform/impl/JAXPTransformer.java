package com.blnz.fxpl.xform.impl;

import com.blnz.fxpl.xform.TransformException;

import com.blnz.fxpl.fs.RepositoryItem;

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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

/**
 *  The StyleSheet Transformer.
 *  Transforms a document with the provided stylesheet.
 */
public class JAXPTransformer  extends TransformerImplBase
{

    // Processor for transformation

    private XMLReader _styleReader = null;
    private ParserAdapter _pa = null;

    private Transformer _transformer = null;
    private Templates _templates = null;

    private SAXSource _upstreamSource = null;
    private SAXSource _styleSource = null;

    Result _result  = null;
    private static Hashtable _transFactories = null;


    /** 
     * 
     */
    public JAXPTransformer(Source upstream, Source styleSource)
        throws Exception
    {
        super(((SAXSource)upstream).getXMLReader());
        _upstreamSource = (SAXSource) upstream;
        _styleSource = (SAXSource) styleSource;
        setStyleReader(_styleSource);
    }

    /** 
     * 
     */
    public JAXPTransformer(Source upstream, RepositoryItem styleSheet)
        throws Exception
    {
        super(((SAXSource)upstream).getXMLReader());
        //        setStyleReader(styleSheet);
        _upstreamSource = (SAXSource) upstream;

        XMLReader rdr = styleSheet.openXMLReader();
        _styleSource = new SAXSource(rdr, new InputSource(styleSheet.getBaseURI()));

        _templates = (Templates) styleSheet.getApplicationObject("jaxpTemplates");
        setStyleReader(_styleSource);
        styleSheet.setApplicationObject("jaxpTemplates", _templates);
    }


    public Templates getTemplates()
    {
        return _templates;
    }

    /**
     * @param styleSource contains a SAX Reader which will produce the events of reading
     * the stylesheet
     */
    public void setStyleReader(Source styleSource)  throws Exception
    {

        _styleReader = _styleSource.getXMLReader();

        if (_templates == null) {

            if (_transFactories == null) {
                _transFactories = new Hashtable();
            }
            
            TransformerFactory tf = (TransformerFactory) _transFactories.get(Thread.currentThread());
            if (tf == null) {
                tf = TransformerFactory.newInstance();
                _transFactories.put(Thread.currentThread(), tf);
            }
            _templates = tf.newTemplates(_styleSource);            
        } 

        _transformer = _templates.newTransformer();
    }

    
    /** complete any initializations after the client 
     * code has finished with all
     *	the setters for this  object and just before the transform is run
     */
    private void prepareProcessor() throws Exception
    {
        
        if (_params != null) {
            setXslProcessorParams(_params);
        }
        _result = new SAXResult(this);
    }
    
    /**
     * sets the requested run-time params in the XSLProcessor
     */
    private void setXslProcessorParams(Hashtable params)
    {
        for (Enumeration e = params.keys(); e.hasMoreElements() ; ) {
            String key = (String) e.nextElement();
            Object value = params.get(key);
            if ("com.blnz.xsl.sax.SaxFilterMaker".equals(key) &&
                value instanceof SaxFilterMaker) {
                // skip it
            } else {
                _transformer.setParameter(key, params.get(key));
            }
        }
    }

    /**
     * run the transformation
     */
    public void parse(InputSource s) throws SAXException
    {
        // System.out.println("#### transforming with JAXP");

        try {
            prepareProcessor();
            _transformer.transform(_upstreamSource, _result);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SAXException(ex);
        }
    }
}

