package com.blnz.fxpl.xform;

import com.blnz.fxpl.util.ConfigProps;

import com.blnz.xsl.om.NodeExtensionFactory;
import com.blnz.fxpl.fs.RepositoryItem;

import org.xml.sax.Parser;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;


import com.blnz.xsl.om.Node;
import com.blnz.xsl.sax2.SAXTwoOMBuilder;
import com.blnz.xsl.tr.LoadContext;

import javax.xml.transform.Source;

import java.util.Hashtable;

import java.io.OutputStream;
import java.io.Writer;

/**
 * Service provider for XML transformation, parsing and serialization
 */
public class TransformServiceHome
{

    /**
     * XMLReader validation configuration flag: no validation is necessary
     */
    public static int CHECK_NONE = 0;

    /**
     * XMLReader validation configuration flag: well -formedness checking
     */
    public static int CHECK_WELL_FORMED = 1;

    /**
     * XMLReader validation configuration flag: schema validation
     */
    public static int CHECK_VALID = 2;


    private static TransformService _ts = null;

    /**
     * @return a SAX parser
     */
    public static Parser createParser()  throws Exception
    {
        String parserClass = ConfigProps.getProperty("com.blnz.xsl.sax.parser");
        if (parserClass == null) {
            parserClass = ConfigProps.getProperty("org.xml.sax.parser");
        }
        if (parserClass == null) {
            parserClass = "com.blnz.xml.sax.CommentDriver";
        }
        return (Parser)Class.forName(parserClass).newInstance();
    }

    /** 
     * gets the configured transform service
     */
    public static final TransformService getTransformService()
    { if (_ts == null) { init(); } return _ts; }
        
    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     */
    public static final Transformer createXSLTTransformer() 
        throws TransformException
    { return getTransformService().createXSLTTransformer(); }

    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     */
    public Transformer createXSLTTransformer(XMLReader upstream)
        throws TransformException
    { return getTransformService().createXSLTTransformer(upstream); }

    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param styleSheet a SAX 2 XMLReader (parser) for the stylesheet
     */
    public static final Transformer createXSLTTransformer(XMLReader upstream, 
                                                          XMLReader styleSheet)
        throws TransformException
    { 
        return getTransformService().createXSLTTransformer(upstream, 
                                                           styleSheet); 
    }

    
    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param stylesheet the stylesheet 
     */
    public static final Transformer createXSLTTransformer(XMLReader upstream, 
                                                          RepositoryItem stylesheet)
        throws TransformException
    { 
        return getTransformService().createXSLTTransformer( upstream, stylesheet);
    }

    
    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param stylesheet the stylesheet 
     */
    public static final Transformer createJAXPTransformer(Source upstream, 
                                                          RepositoryItem stylesheet)
        throws TransformException
    { 
        return getTransformService().createJAXPTransformer( upstream, stylesheet);
    }
    
    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param styleSheet a SAX 2 XMLReader (parser) for the stylesheet
     */
    public static final Transformer createJAXPTransformer(Source upstream, 
                                                          Source styleSheet)
        throws TransformException
    { 
        return getTransformService().createJAXPTransformer(upstream, 
                                                           styleSheet); 
    }


    // /**
    //  * @return an ElementFilter which can be configured to
    //  *  selectively remove elements from a SAX event stream
    //  */
    // public static final ElementFilter createElementFilter()
    // { return getTransformService().createElementFilter(); }

    // /**
    //  * @return an ElementFilter which can be configured to
    //  *  selectively remove attributes from a SAX event stream
    //  */
    // public static final ElementFilter createAttributesFilter(String attrNames)
    // { return getTransformService().createAttributesFilter(attrNames); }

    // /**
    //  * @return a compiled XSLT Transformation engine packaged as an 
    //  *  SAX2 XML Filter
    //  */
    // public static final Transformer createCompiledTransformer()
    //     throws TransformException
    // { return getTransformService().createCompiledTransformer(); }

    /**
     * @return a SAX  ContentHandler for writing XML
     *  output to a byte stream
     */
    public static final ContentHandler createOutputStreamContentWriter(OutputStream s)
        throws TransformException
    { return getTransformService().createOutputStreamContentWriter(s); }

    /**
     * @return a SAX 2 ContentHandler for writing XML
     *  output to a byte stream.
     *  @param addXMLDecl prepend the stream with the XML declaration
     */
    public static final ContentHandler createOutputStreamContentWriter(OutputStream s,
                                                                       boolean addXMLDecl)
        throws TransformException
    { return getTransformService().createOutputStreamContentWriter(s, addXMLDecl); }

    /**
     * @return a SAX 2  ContentHandler for writing transformed
     *  output to a character Writer
     */
    public static final ContentHandler createCharacterContentWriter(Writer w)
        throws TransformException
    { return getTransformService().createCharacterContentWriter(w); }

    /**
     * @return a SAX 2  ContentHandler for writing transformed
     *  output to a character Writer
     */
    public static final ContentHandler createXHTMLContentWriter(Writer w)
        throws TransformException
    { return getTransformService().createXHTMLContentWriter(w); }

    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output as html
     */
    public static final ContentHandler createHTMLContentWriter(Writer w)
        throws TransformException
    { return getTransformService().createHTMLContentWriter(w); }


    /**
     * @return a SAX 2 XMLReader (parser) suitable for 
     * parsing a SAX InputSource
     */
    public static final XMLReader createInputSourceReader()
        throws TransformException
    { return getTransformService().createInputSourceReader(); }

    /**
     * @return a SAX 2 XMLReader (parser) already wired for 
     * parsing the specified SAX InputSource
     */
    public static final XMLReader createInputSourceReader(InputSource src)
        throws TransformException
    { return getTransformService().createInputSourceReader(src); }


    /**
     * @return a DOM building ContentHandler as a target of a SAX2 event stream
     */
    public static final SAXTwoOMBuilder createOMWriter()
        throws TransformException
    { return getTransformService().createOMWriter(); }


    /**
     * @return a DOM building ContentHandler as a target of a SAX2 event stream
     */
    public static final SAXTwoOMBuilder createOMWriter(LoadContext factory)
        throws TransformException
    { return getTransformService().createOMWriter(factory); }

    /**
     * @return an XMLReader which walks a DOM and is the 
     * source of a SAX2 event stream
     */
    public static final XMLReader createOMSourceReader(Node node)
        throws TransformException
    { return getTransformService().createOMSourceReader(node); }


    private static synchronized void init()
    {
        if (_ts != null) {
            return; // another thread beat us
        }
        try {
            String implName = 
                ConfigProps.getProperty("com.blnz.fxpl.xform.TransformService", 
                                        "com.blnz.fxpl.xform.impl.BasicTransformServiceHomeImpl");
            if (implName == null) {
                _ts = new com.blnz.fxpl.xform.impl.BasicTransformServiceHomeImpl();
            } else {
                _ts = (TransformService) Class.forName(implName).newInstance();
            }
        } catch (Exception ex) {
            // we just want to have a fallback logger, and continue
            _ts = new com.blnz.fxpl.xform.impl.BasicTransformServiceHomeImpl();
        }
    }
}
