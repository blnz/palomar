package com.blnz.fxpl.core;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.XProcessor;

import com.blnz.fxpl.util.NoClosePrintWriter;
import com.blnz.fxpl.xform.XForm;
import com.blnz.fxpl.xform.Transformer;

import java.util.logging.Logger;

import com.blnz.fxpl.util.ConfigProps;

import com.blnz.xsl.om.Name;
import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.NodeExtension;
import com.blnz.xsl.om.SafeNodeIterator;
import com.blnz.xsl.om.ExtensionContext;
import com.blnz.xsl.sax2.SaxFilterMaker;

import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.FXContext;

import java.io.StringReader;
import java.io.PrintWriter;
import java.util.Hashtable;

public class XSLTImpl extends FXRequestServerSide 
{   
    /**
     * evaluate the request to transform content through XSLT.
     *
     * This operator expects two children elements; the first
     *  is evaluated to yield an xslt
     *  stylesheet, the second is evaluated to yield the content
     *  to be transformed.
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
        throws Exception
    {

        Hashtable params = null;
        FXRequest styleSource = null;
        FXRequest inputSource = null;

        context = extendContext((FXContext) context);

        try {
            
            SafeNodeIterator kids = getChildNodes();
            char[] chars;

            Node n = kids.next();
            while (n != null) {

                NodeExtension ne = n.getNodeExtension();
                n = kids.next();

                if (ne instanceof FXRequest) {
                    
                    if (ne instanceof ParamSetImpl) {
                        params = ((ParamSetImpl)n).getHashtable((FXContext) context);

                    } else if (!(ne instanceof FXContextImpl) &&
                            !(ne instanceof ParamSetImpl) &&
                            !(ne instanceof Import) &&
                            !(ne instanceof DocumentationImpl) &&
                            !(ne instanceof NullTransformImpl)) {
 
                        if (styleSource == null) {
                            styleSource = (FXRequest) ne;
                        } else {
                            // FIXME -- do some validation here?
                            inputSource = (FXRequest) ne;
                        }
                    } else {
                        // we ignore these guys
                    }
                } 
            }
            
            if (params == null) {
                params = new Hashtable();
            }

            // cast the inputs as XMLReaders
            FXRequestReaderAdapter styleIn = 
                new FXRequestReaderAdapter((FXContext)context, styleSource);
            
            FXRequestReaderAdapter xformIn = 
                new FXRequestReaderAdapter((FXContext)context, inputSource);
            
            Transformer t = XForm.createXSLTTransformer(xformIn, styleIn);
            
            // kinda ugly, but we gotta try to be sure this property is defined
            // for sax ParserAdapter when called from stylesheets
            String pname = null;
            try {
                pname = System.getProperty("org.xml.sax.parser");
                
                if (pname == null || "".equals(pname)) {
                    System.setProperty("org.xml.sax.parser",
                                       ConfigProps.getProperty("org.xml.sax.parser", 
                                                               "com.blnz.xml.sax.Driver"));
                }
            } catch (Exception ex) {
                // well, we tried ...
            }
            
            params.put("com.jclark.xsl.sax.SaxFilterMaker",
                       new XRAPFilterMaker((FXContext) context));
            
            t.setParams(params);
            
            t.setContentHandler(responseTarget);
            
            t.parse(new InputSource(new StringReader("")));
            
        } catch (Exception ex) {

            //FIXME: lets catch more types of exceptions 
            ex.printStackTrace();
            errorResponse(ex, responseTarget, (FXContext) context);
            
            // print ECHO stacktrace to stdout
            PrintWriter out = new NoClosePrintWriter(new PrintWriter(System.out));
            ContentHandler ch = XForm.createCharacterContentWriter(out);
            ch.startDocument();
            errorResponse(ex, ch, (FXContext) context);            
            ch.endDocument();

        } catch (java.lang.OutOfMemoryError err) {
            LOGGER.severe("!!!... whoops, out of memory!"); 
            err.printStackTrace();
            throw new Exception("not enough memory!");   // ummm .. will this work?
        }
    }

    //
    private class XRAPFilterMaker implements SaxFilterMaker
    {

        private XProcessor _xrap;
        private FXContext _ctxt;

        public XRAPFilterMaker(FXContext ctxt)
        {
            _ctxt = ctxt;
            _xrap = FXHome.getXProcessor("local");
        }

        public XMLFilter getFilter()
        {
            XRAPFilter filt = new XRAPFilter(_ctxt, _xrap);
            return filt;
        }
    }

    /**
     *
     */
    private class XRAPFilter extends XMLFilterImpl
    {
        private FXContext _ctxt;
        private XProcessor _xrap;
       
        public XRAPFilter(FXContext ctxt, XProcessor xrap)
        {
            _ctxt = ctxt;
            _xrap = xrap;
        }

        public void parse(InputSource src) throws SAXException
        {
            if (getContentHandler() == null) {
                throw new SAXException("no ContentHandler");
            }
            try {
                _xrap.eval(_ctxt, getParent(), 
                           src,
                           getContentHandler());
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new SAXException(ex);
            }
        }

        public void parse(String uri) throws SAXException
        {
            parse(new InputSource(uri));
        }
    }

}
