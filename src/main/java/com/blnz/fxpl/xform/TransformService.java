//
package com.blnz.fxpl.xform;

import com.blnz.fxpl.fs.RepositoryItem;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import com.blnz.xsl.tr.LoadContext;
import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.NodeExtensionFactory;
import com.blnz.xsl.sax2.SAXTwoOMBuilder;


import java.util.Hashtable;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.transform.Source;

/**
 * service for adapting XML to and from various representations, e.g.
 * SAX events, byte streams, DOM trees, etc. and for transforming
 * XML documents
 */
public interface TransformService
{


    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     */
    public Transformer createXSLTTransformer() 
        throws TransformException;

    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     */
    public Transformer createXSLTTransformer(XMLReader upstream)
        throws TransformException;


    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param styleSheet a SAX 2 XMLReader (parser) for the stylesheet
     */
    public Transformer createXSLTTransformer(XMLReader upstream, 
                                             XMLReader styleSheet)
        throws TransformException;

    
    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param stylesheet the stylesheet 
     */
    public Transformer createXSLTTransformer(XMLReader upstream, 
                                             RepositoryItem stylesheet)
        throws TransformException;
    
    
    
    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param styleSheet a SAX 2 XMLReader (parser) for the stylesheet
     */
    public Transformer createJAXPTransformer(Source upstream, 
                                             Source styleSheet)
        throws TransformException;

    
    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param stylesheet the stylesheet 
     */
    public Transformer createJAXPTransformer(Source upstream, 
                                             RepositoryItem stylesheet)
        throws TransformException;


    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output to a utf-8 byte stream 
     */
    public ContentHandler createOutputStreamContentWriter(OutputStream s)
        throws TransformException;
    
    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output to a utf-8 byte stream
     */
    public  ContentHandler 
        createOutputStreamContentWriter(OutputStream s, boolean addXMLDecl)
        throws TransformException;
    
    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output to a character Writer
     */
    public ContentHandler createCharacterContentWriter(Writer w)
        throws TransformException;
    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output to a character Writer
     */
    public ContentHandler createXHTMLContentWriter(Writer w)
        throws TransformException;
    
    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output as JSON to a character Writer
     */
    public ContentHandler createJSONContentWriter(Writer w)
        throws TransformException;
    
    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output as html
     */
    public ContentHandler createHTMLContentWriter(Writer w)
        throws TransformException;
    
    /**
     * @return a SAX 2 XML Reader (parser) suitable for parsing a 
     * SAX InputSource
     */
    public XMLReader createInputSourceReader()
        throws TransformException;

    /**
     * @return a SAX 2 XML Reader (parser) suitable for parsing a 
     * JSON stream packaged as a SAX InputSource
     */
    public XMLReader createJSONInputSourceReader()
        throws TransformException;

    /**
     * @return a SAX 2 XML Reader (parser) already wired for 
     * parsing the specified SAX InputSource
     */
    public XMLReader createInputSourceReader(InputSource src)
        throws TransformException;

    /**
     * @return a SAX 2 XML Reader (parser) suitable for parsing a 
     * SAX InputSource, configured for the given level of validation
     */
    public XMLReader createInputSourceReader(int validationFlag)
        throws TransformException;

    /**
     * @return a SAX 2 XML Reader (parser) already wired for 
     * parsing the specified SAX InputSource
     */
    public XMLReader createInputSourceReader(InputSource src, int validationFlag)
        throws TransformException;

    
    /**
     * @return a OM building ContentHandler as a target of a SAX2 event stream
     */
    public SAXTwoOMBuilder createOMWriter()
        throws TransformException;

    /**
     * @return a OM building ContentHandler as a target of a SAX2 event stream
     */
    public SAXTwoOMBuilder createOMWriter(LoadContext factory)
        throws TransformException;


    /**
     * @return an XMLReader which walks an OM and is the source
     *  of a SAX2 event stream
     */
    public XMLReader createOMSourceReader(Node domNode)
        throws TransformException;
}
