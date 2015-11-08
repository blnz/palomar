package com.blnz.fxpl.core;

import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;

import com.blnz.fxpl.xform.XForm;

import com.blnz.xsl.om.ExtensionContext;
import com.blnz.xsl.sax2.SAXTwoOMBuilder;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import org.xml.sax.helpers.XMLFilterImpl;

/**
 * implements a "mapping function" over a record stream
 * 
 */
public class MapImpl extends FXRequestServerSide {

    public void eval(ContentHandler responseTarget, ExtensionContext context)
            throws Exception {

        FXContext ctx = extendContext((FXContext) context);

        if (ctx == null) {
            throw new Exception("null context");
        }

        String mapElementDepth = (String) ctx.get("mapElementDepth");
        int mapDepth = 1;

        if (mapElementDepth != null) {
            try {
                mapDepth = Integer.parseInt(mapElementDepth);
            } catch (Exception ex) {
                // no worries, we'll default
            }
        }

        try {
            FXRequest req = getSubRequest(1); // the guy we'll evaluate
            ECHOStreamIterator xsi = new ECHOStreamIterator(responseTarget,
                    req, ctx, mapDepth);

            FXRequest req2 = getSubRequest(2);
            req2.eval(xsi, ctx);

        } catch (Exception e) {
            errorResponse(e, responseTarget, ctx);
        }
    }
}

/**
 *
 */
class ECHOStreamIterator extends XMLFilterImpl {
    private FXRequest _req;
    private FXContext _context;
    private ContentHandler _target;

    private int _depth = 0;
    private int _targetDepth = 1;
    private SAXTwoOMBuilder _domWriter = null;

    /**
     *
     */
    public ECHOStreamIterator(ContentHandler responseTarget, FXRequest req,
            FXContext context, int depth) {
        _target = responseTarget;
        _req = req;
        _context = context;
        _targetDepth = depth;
    }

    /**
     *
     */
    public void startElement(String nsURI, String localName, String qName,
            Attributes qAtts) throws SAXException {

        if (_depth == _targetDepth) {

            try {
                _domWriter = XForm.createOMWriter();
            } catch (Exception ex) {
                throw new SAXException(ex);
            }
            _domWriter.startDocument();

        }

        ++_depth;

        if (_depth > _targetDepth) {

            _domWriter.startElement(nsURI, localName, qName, qAtts);

        }
    }

    /**
     *
     */
    public void characters(char[] buf, int start, int len) throws SAXException {
        if (_depth > _targetDepth) {
            _domWriter.characters(buf, start, len);
        }
    }

    /**
     *
     */
    public void endElement(String nsURI, String localName, String qName)
            throws SAXException {

        _depth--;

        if (_depth == _targetDepth) {

            _domWriter.endElement(nsURI, localName, qName);
            _domWriter.endDocument();

            try {
                XMLReader rdr = XForm.createOMSourceReader(_domWriter
                        .getRootNode());

                XMLFilter nsCleanup = new com.blnz.fxpl.xform.impl.NamespaceDeclAugmenter();
                nsCleanup.setParent(rdr);

                _context.put("StandardXMLIn", nsCleanup);

                _req.eval(_target, _context);
                _domWriter = null;

            } catch (Exception ex) {
                throw new SAXException(ex);
            }
        } else if (_depth > _targetDepth) {
            _domWriter.endElement(nsURI, localName, qName);
        }
    }
}
