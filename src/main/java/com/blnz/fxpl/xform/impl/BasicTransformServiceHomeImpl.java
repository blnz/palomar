package com.blnz.fxpl.xform.impl;

import com.blnz.fxpl.util.ConfigProps;
import com.blnz.fxpl.xform.*;

import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.core.SimpleElementFactory;

import com.blnz.xsl.tr.LoadContext;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.NameTableImpl;

import com.blnz.xsl.sax2.SAXTwoOMBuilder;
import com.blnz.xsl.sax2.XMLProcessorImpl;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.Parser;

import java.util.Hashtable;
import java.util.Dictionary;

import javax.xml.parsers.SAXParserFactory;

import javax.xml.transform.Source;

import java.util.logging.Logger;

import java.io.Writer;
import java.io.OutputStream;


/**
 * Factory for clients requesting <code>Transformer&apos;s</code>
 */
public class BasicTransformServiceHomeImpl implements TransformService
{
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private SAXParserFactory _rdrFactory = null;

    public BasicTransformServiceHomeImpl()
    { 

    }

    private void init()
    {
        _rdrFactory = SAXParserFactory.newInstance();
    }

    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     */
    public Transformer createXSLTTransformer()
    {
        return new StyleSheetTransformer();
    }

    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     */
    public Transformer createXSLTTransformer(XMLReader upstream)
    {
        return new StyleSheetTransformer(upstream);
    }

    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param styleSheet a SAX 2 XMLReader (parser) for the stylesheet
     */
    public Transformer createXSLTTransformer(XMLReader upstream, 
                                             XMLReader styleSheet)
        throws TransformException
    {
        return new StyleSheetTransformer(upstream, styleSheet);
    }

    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param stylesheet a URI for the stylesheet
     */
    public Transformer createXSLTTransformer(XMLReader upstream,
                                             RepositoryItem stylesheet)
        throws TransformException
    {
        StyleSheetTransformer xformer = null;
        try {
            xformer = 
                (StyleSheetTransformer) stylesheet.getApplicationObject("xslTransformer");
            if (xformer == null) {
                // System.out.println("building transformer for " + stylesheet.getName());
                XMLReader rdr = stylesheet.openXMLReader();
                xformer = new StyleSheetTransformer(upstream, rdr);
                stylesheet.setApplicationObject("xslTransformer", xformer);
            } else {
                // System.out.println("found transformer in " + stylesheet.getName());
                xformer = (StyleSheetTransformer) xformer.clone();
            }
            xformer.setParent(upstream);
        } catch (Exception ex) {
            // never mind
             System.out.println("unable to build or find transformer for " + stylesheet.getName());
             ex.printStackTrace();
        }

        if (xformer == null) {
            try { 
                XMLReader rdr = stylesheet.openXMLReader();
                xformer = new StyleSheetTransformer(upstream, rdr);
            } catch (Exception ex) {
                System.out.println("cannot get reader on item");
            }
        }
        return xformer;
    }

    
    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param styleSheet a SAX 2 XMLReader (parser) for the stylesheet
     */
    public Transformer createJAXPTransformer(Source upstream, 
                                             Source styleSheet)
        throws TransformException
    { 

        JAXPTransformer jt = null;

        try {
            // System.out.println("building JAXP transformer with sax stylesheet source");
            
            jt = new JAXPTransformer(upstream, styleSheet);

        } catch (Exception ex) {
            // never mind
             System.out.println("unable to build or find transformer for sax stream");
             ex.printStackTrace();
             throw new TransformException(ex);
        }

        return jt;
    }

    
    /**
     * @return an XSLT Transformation engine packaged as an 
     *  SAX2 XML Filter
     * @param upstream the SAX 2  XMLReader (parser) which delivers the
     *   input parse events to the Transformer
     * @param stylesheet the stylesheet 
     */
    public  Transformer createJAXPTransformer(Source upstream, 
                                              RepositoryItem stylesheet)
        throws TransformException
    { 
        JAXPTransformer jt = null;


        try {
            // System.out.println("building JAXP transformer with " + stylesheet.getName());
            
            jt = new JAXPTransformer(upstream, stylesheet);

        } catch (Exception ex) {
            // never mind
             System.out.println("unable to build or find transformer for " + stylesheet.getName());
             ex.printStackTrace();
             throw new TransformException(ex);
        }


        return jt;
    }

    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output to a UTF-8 byte stream
     */
    public ContentHandler createOutputStreamContentWriter(OutputStream s)
    {
        return new ByteStreamSerializer(s);
    }

    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output to a UTF-8 byte stream
     */
    public ContentHandler createOutputStreamContentWriter(OutputStream s, 
                                                          boolean addXMLDecl)
    {
        return new ByteStreamSerializer(s, ! addXMLDecl );
    }

    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output as html
     */
    public ContentHandler createHTMLContentWriter(Writer w)
    {
        return new HTMLWriterContentHandler(w);
    }

    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output to a character Writer
     */
    public ContentHandler createCharacterContentWriter(Writer w)
    {
        //        return new OutWriterContentHandler(w);
        return new CharStreamSerializer(w);
    }


    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output to a character Writer
     */
    public ContentHandler createXHTMLContentWriter(Writer w)
    {
        //        return new OutWriterContentHandler(w);
        return new XHTMLCharStreamSerializer(w);
    }

    /**
     * @return a SAX 2 XML Reader (parser) suitable for
     *   parsing a SAX input source
     */
    public XMLReader createInputSourceReader() throws TransformException
    {
        return createInputSourceReader(XForm.CHECK_NONE);
    }

    /**
     * @return a SAX 2 XML Reader (parser) suitable for
     *   parsing a SAX input source
     */
    public XMLReader createInputSourceReader(int validationLevel) throws TransformException
    {

        XMLReader rdr = newXMLReader(validationLevel);

        if (rdr == null) {
            rdr = wrappedInputSourceReader();
        } else {
            // System.out.println("got reader " + rdr.getClass().getName());
        } 

        return rdr;

    }


    /**
     * @return a SAX 2 XML Reader (parser) suitable for
     *   parsing a SAX input source
     */
    public XMLReader createInputSourceReader(InputSource src) 
        throws TransformException
    {

        return createInputSourceReader(src, XForm.CHECK_NONE);
    }

    /**
     * @return a SAX 2 XML Reader (parser) suitable for
     *   parsing a SAX input source
     */
    public XMLReader createInputSourceReader(InputSource src, int validationLevel) 
        throws TransformException
    {
        XMLReader rdr = newXMLReader(validationLevel);

        if (rdr == null) {
            rdr = wrappedInputSourceReader(src);
        } else {
            //            System.out.println("got reader " + rdr.getClass().getName());
            try {
                rdr = new ISXMLReaderFilter(rdr, src);
            } catch (SAXException ex) {
                throw new TransformException("cannot instantiate filter ISXMLReaderFilter",
                                             ex);
            }
        }
        return rdr;

    }


    /**
     * @return a SAX 2 XML Reader (parser) suitable for parsing a 
     * JSON stream packaged as a SAX InputSource
     */
    public XMLReader createJSONInputSourceReader()
        throws TransformException
    { return null; }


    
    /**
     * @return a SAX  ContentHandler for writing transformed
     *  output as JSON to a character Writer
     */
    public ContentHandler createJSONContentWriter(Writer w)
        throws TransformException
    { return null; }
    


    /**
     *
     */
    private XMLReader newXMLReader(int validationLevel)
    {
        XMLReader rdr = null;
        try {
            if (_rdrFactory == null) {
                init();
            }
            
            synchronized(this) {
                // rdr = _rdrFactory.newSAXParser().getXMLReader();
                
                if (validationLevel == XForm.CHECK_NONE) {
                    rdr =  new ParserAdapter(new com.blnz.xml.sax.CommentDriver()) 
                        {
                            public void processingInstruction(String target, String data) throws SAXException {
                                if (target == null) {
                                    ContentHandler ch = this.getContentHandler();
                                    if (ch instanceof org.xml.sax.ext.LexicalHandler) {
                                        char[] buf = data.toCharArray();
                                        ((org.xml.sax.ext.LexicalHandler)ch).comment(buf, 0, buf.length);
                                    }
                                } else {
                                    super.processingInstruction(target, data);
                                }
                            }
                        };
                } else {
                    rdr = _rdrFactory.newSAXParser().getXMLReader();
                }

                rdr.setFeature("http://xml.org/sax/features/namespace-prefixes",
                               true);
                rdr.setFeature("http://xml.org/sax/features/namespaces", true);
            }
        } catch (Exception ex) {
            LOGGER.warning("JAXP cannot get XMLReader for us:" + ex.toString());
        }
        return rdr;
    }

    /**
     * @return a SAX 2 XML Reader (parser) suitable for
     *   parsing a SAX input source
     */
    private XMLReader wrappedInputSourceReader() throws TransformException
    {
        
        try {
            Parser p = createParser();
            
            if (p == null) {
                throw new TransformException("cannot create parser");
            }
            
            ParserAdapter dp2 =  new ParserAdapter(p);
            // do we really want to do this? 
            dp2.setFeature("http://xml.org/sax/features/namespace-prefixes",
                           true);
            dp2.setFeature("http://xml.org/sax/features/namespaces", true);
            return dp2;

        } catch (Exception ex) {
            throw new TransformException("cannot create parser",
                                         ex);
        }
    }

    /**
     * @return a SAX 2 XML Reader (parser) suitable for
     *   parsing a SAX input source
     */
    private XMLReader wrappedInputSourceReader(InputSource src) 
        throws TransformException
    {
        
        try {
            Parser p = createParser();
            
            if (p == null) {
                throw new TransformException("cannot create parser");
            }
            
            ParserAdapter dp2 =  new ISParserAdapter(p, src);
            // do we really want to do this? 
            dp2.setFeature("http://xml.org/sax/features/namespace-prefixes",
                           true);
            dp2.setFeature("http://xml.org/sax/features/namespaces", true);
            return dp2;

        } catch (Exception ex) {
            throw new TransformException("cannot create parser", ex);
        }
    }

    /**
     *
     */
    public SAXTwoOMBuilder createOMWriter(LoadContext factory)
    {

        try {
            XMLProcessorImpl omb = new XMLProcessorImpl();

            SAXTwoOMBuilder builder = omb.getConfiguredOMBuilder( "", 0, factory, new NameTableImpl());
            return builder;
        } catch (Exception ex) {
            // FIXME -- do something?
            ex.printStackTrace();
        }
        return null;
    }

    /**
     *
     */
    public  SAXTwoOMBuilder  createOMWriter()
    {
        SimpleElementFactory factory = new SimpleElementFactory();
        Hashtable elementToClass = new Hashtable();
        elementToClass.put("*Element", "com.blnz.fxpl.core.FXRequestServerSide");
        
        factory.addMapping( (Dictionary)elementToClass, null );
        return createOMWriter(factory);
    }

    /**
     * @return an XMLReader which walks a DOM and is the 
     * source of a SAX2 event stream
     */
    public XMLReader createOMSourceReader(Node dom)
    {
        try {
            XMLReader dp2 =  new OMReader(dom);

            // do we really want to do this? 
            dp2.setFeature("http://xml.org/sax/features/namespace-prefixes",
                           true);

            dp2.setFeature("http://xml.org/sax/features/namespaces", true);
            return dp2;
        } catch (SAXException ex) {
            // FIXME: do something
            ex.printStackTrace();
        }
        return null;
    }

    /** 
     * @return a SAX-1 parser 
     */
    private Parser createParser()  throws Exception
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
     * @return a SAX-2 XMLReader 
     */
    private XMLReader createValidatingReader()  
        throws Exception
    {
        // FIXME: do we need to configure for different schema languages?
        String parserClass =
            ConfigProps.getProperty("com.blnz.fxpl.xform.validatingReader",
                                    "com.sun.xml.parser.ValidatingParser");
        XMLReader r = (XMLReader)Class.forName(parserClass).newInstance();
        return r;
    }

}

