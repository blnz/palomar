
package com.blnz.fxpl.core;

import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;

import com.blnz.fxpl.xform.XForm;

import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.DocumentHandler;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;


/**
 * provides a SAX 2 XMLReader interface on top of an FX Request element
 */
public class FXRequestReaderAdapter extends XMLFilterImpl
{

    private NamespaceSupport _nsSupport;

    private boolean _parsing = false;
    private String _nameParts[] = new String[3];

    // send documentStart()/documentEnd() events?
    private boolean _sendDocEvents = false;


    private AttributesImpl atts = null;

    // Features
    private boolean _namespaces = true;
    private boolean _prefixes = false;
    
    private FXContext _ctxt = null;
    private FXRequest _req = null;
    private Hashtable _features = null;
    private boolean _firstParse = true;
    private XMLReader _backup = null;

    //
    // Internal constants for the sake of convenience.
    //
    private final static String FEATURES = "http://xml.org/sax/features/";
    private final static String NAMESPACES = FEATURES + "namespaces";
    private final static String NAMESPACE_PREFIXES = FEATURES + "namespace-prefixes";

    /**
     * construct with the FX request to be evaluated, and the
     *  context to be provided to that evaluation
     */    
    public FXRequestReaderAdapter(FXContext ctxt, FXRequest req)
    {
        _ctxt = ctxt;
        _req = req;
    }
    
    /**
     * evaluate the FX request object, routing it's SAX event
     * stream to our ContentHandler
     */
    public void parse(InputSource s) throws SAXException
    {
        try {

            // we may get called more than once by XT in loading xsl:includes
            if (_firstParse) {

                // Fixup SysID if not ready
                String sysId = s.getSystemId();
                if ( sysId == null || "".equals(sysId) ) {
                    s.setSystemId(_req.getURI(_ctxt));
                }

                if (_sendDocEvents) {
                    super.startDocument();
                }
                if (_req != null) {
                    _req.eval(this, _ctxt); // our base class will pass the SAX events through
                }
                if (_sendDocEvents) {
                    super.endDocument();
                }

                _firstParse = false;

            } else {

                // FIXME: replace with the right kind of parser
                if (_backup == null) {
                    _backup = XForm.createInputSourceReader();
                    _backup.setContentHandler(getContentHandler());
                }
                _backup.parse(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SAXException(ex);
        }
    }

    /**
     *
     */    
    public void setSendDocEvents(boolean state)
    {
        _sendDocEvents = state;
    }

    /**
     *
     */
    public boolean getSendDocEvents(boolean state)
    {
        return _sendDocEvents ;
    }

    /**
     * Set a feature flag for the parser.
     *
     * <p>The only features recognized are namespaces and 
     * namespace-prefixes.</p>
     *
     * @param name The feature name, as a complete URI.
     * @param value The requested feature value.
     * @exception SAXNotRecognizedException If the feature
     *            can't be assigned or retrieved.
     * @exception SAXNotSupportedException If the feature
     *            can't be assigned that value.
     * @see org.xml.sax.XMLReader#setFeature
     */
    public void setFeature(String name, boolean value)
	throws SAXNotRecognizedException, SAXNotSupportedException
    {
	if (name.equals(NAMESPACES)) {
	    checkNotParsing("feature", name);
	    _namespaces = value;
	    if (!_namespaces && !_prefixes) {
		_prefixes = true;
	    }
	} else if (name.equals(NAMESPACE_PREFIXES)) {
	    checkNotParsing("feature", name);
	    _prefixes = value;
	    if (!_prefixes && !_namespaces) {
		_namespaces = true;
	    }
	} else {
	    throw new SAXNotRecognizedException("Feature: " + name);
	}
    }

    /**
     * Throw an exception if we are parsing.
     *
     * <p>Use this method to detect illegal feature or
     * property changes.</p>
     *
     * @param type The type of thing (feature or property).
     * @param name The feature or property name.
     * @exception SAXNotSupportedException If a
     *            document is currently being parsed.
     */
    private void checkNotParsing (String type, String name)
	throws SAXNotSupportedException
    {
	if (_parsing) {
	    throw new SAXNotSupportedException("Cannot change " +
					       type + ' ' +
					       name + " while parsing");
            
	}
    }
    
    /**
     * Process a qualified (prefixed) name.
     *
     * <p>If the name has an undeclared prefix, use only the qname
     * and make an ErrorHandler.error callback in case the app is
     * interested.</p>
     *
     * @param qName The qualified (prefixed) name.
     * @param isAttribute true if this is an attribute name.
     * @return The name split into three parts.
     * @exception SAXException The client may throw
     *            an exception if there is an error callback.
     */
    private String[] processName (String qName, boolean isAttribute,
                                  boolean useException)
	throws SAXException
    {
	String parts[] = _nsSupport.processName(qName, _nameParts,
                                                isAttribute);
	if (parts == null) {
	    if (useException) {
		throw makeException("Undeclared prefix: " + qName);
            }
	    reportError("Undeclared prefix: " + qName);
	    parts = new String[3];
	    parts[0] = parts[1] = "";
	    parts[2] = qName.intern();
	}
	return parts;
    }
    
    /**
     * Report a non-fatal error.
     *
     * @param message The error message.
     * @exception SAXException The client may throw
     *            an exception.
     */
    void reportError (String message)
	throws SAXException
    {
	if (getErrorHandler() != null) {
	    getErrorHandler().error(makeException(message));
        }
    }
    
    /**
     * Construct an exception for the current context.
     *
     * @param message The error message.
     */
    private SAXParseException makeException (String message)
    {
        // 	if (getLocator() != null) {
        // 	    return new SAXParseException(message, getLocator());
        // 	} else {
        return new SAXParseException(message, null, null, -1, -1);
        //	}
    }
    
}
