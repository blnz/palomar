// $Id: NamespaceDeclAugmenter.java 412 2005-08-15 21:48:15Z blindsey $

package com.blnz.fxpl.xform.impl;

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
 * ensures all namespaces are declared
 */
public class NamespaceDeclAugmenter extends XMLFilterImpl
{

    private boolean _parsing = false;
    private String _nameParts[] = new String[3];

    private AttributesImpl _atts = null;

    // Features
    private boolean _namespaces = true;
    private boolean _prefixes = true;

    // Handlers
    Locator locator;

    private Hashtable _features = null;
    private boolean _firstParse = true;
    private XMLReader _backup = null;
    private NamespaceSupport _nsSupport;

    private Hashtable _pending = new Hashtable();

    //
    // Internal constants for the sake of convenience.
    //
    private final static String FEATURES = "http://xml.org/sax/features/";
    private final static String NAMESPACES = FEATURES + "namespaces";
    private final static String NAMESPACE_PREFIXES = FEATURES + "namespace-prefixes";


    /**
     *
     */    
    public NamespaceDeclAugmenter()
    {
        _nsSupport = new NamespaceSupport();
        _atts = new AttributesImpl();
    }

    public void startPrefixMapping(String prefix, String uri)
    {
        //System.out.println("got map event for {" + uri + "} as {" + prefix + "}");
        _pending.put(prefix, uri);
    }


    public void endPrefixMapping()
    {

    }

    /**
     * <p>If necessary, perform Namespace processing.</p>
     *
     * @param qName The qualified (prefixed) name.
     * @param qAtts The XML 1.0 attribute list (with qnames).
     * @exception SAXException The client may raise a
     *            processing exception.
     */
    public void startElement (String nsURI, String localName, 
                               String qName, Attributes qAtts)
	throws SAXException
    {
        //         System.out.println("1-- start elem {" + nsURI + "}" + localName + 
        //                            " = {" + qName + "}"); 
        
        // These are exceptions from the
        // first pass; they should be
        // ignored if there's a second pass,
        // but reported otherwise.
	Vector exceptions = null;
        
        // If we're not doing Namespace
        // processing, dispatch this quickly.
	if (!_namespaces) {
	    if (getContentHandler() != null) {
                //                System.out.println("2-- no namespace processing, bailing");
		getContentHandler().startElement("", "", qName.intern(),
                                                 qAtts);
	    }
	    return;
	}
        
        
        // OK, we're doing Namespace processing.
	_nsSupport.pushContext();
	int length = qAtts.getLength();
	
        // First pass:  handle NS decls by recording them in the
        // namespace support object, and firing a startPrefixMapping event
	for (int i = 0; i < length; i++) {
            
	    String attQName = qAtts.getQName(i);
            //             System.out.println("3-- att: {" + qAtts.getURI(i) + "}" +
            //                                qAtts.getLocalName(i) + "={" + qAtts.getQName(i) +"}");
	    if (!attQName.startsWith("xmlns")) {
		continue;
            }
            // Could be a declaration...
	    String prefix;
	    int n = attQName.indexOf(':');
            
            // xmlns=...
	    if (n == -1 && attQName.length () == 5) {
		prefix = "";
	    } else if (n != 5) {
		// XML namespaces spec doesn't discuss "xmlnsf:oo"
		// (and similarly named) attributes ... at most, warn
                //       System.out.println("4-- don't know what to do with {" + attQName + "}");
		continue;
	    } else {		// xmlns:foo=...
		prefix = attQName.substring(n+1);
                // System.out.println("5-- found xmlns decl for prefix {" + prefix + "}");
            }
            
            String value = qAtts.getValue(i);
	    if (!_nsSupport.declarePrefix(prefix, value)) {
                reportError("nsSupport says Illegal Namespace prefix: " + prefix);
		continue;
	    } 
            if (getContentHandler() != null) {
                //    System.out.println("6-- se: start mapping {" + prefix + "} to {" + value + 
                //                        "} from decl");
		getContentHandler().startPrefixMapping(prefix, value);
            }
	}

 	_atts.clear();
        
        String prefix;
        int n = qName.indexOf(':');
        
        // 
        if (n == -1) {
            prefix = "";
        } else {
            prefix = qName.substring(0, n);
        }
        
        // next, has the prefix binding been declared yet for the element 
        //  name?
        String currentNamespace = _nsSupport.getURI(prefix);
        if (currentNamespace == null || ! currentNamespace.equals(nsURI)) {
            // no?, fake a declaration
            if ((nsURI != null) && (!"".equals(nsURI))) {
                if (!_nsSupport.declarePrefix(prefix, nsURI)) {
                    reportError("Illegal Namespace prefix: " + prefix);
                } else {
                    //                 System.out.println("7-- bound prefix {" + prefix + "} to {" + 
                    //                                    nsURI + "}");
                }
                if (getContentHandler() != null) {
                    //                System.out.println("8-- se: fake mapping {" + prefix +
                    //                                    "} to {" + nsURI + "}");
                    getContentHandler().startPrefixMapping(prefix, nsURI);
                }
                if (_prefixes) {
                    if ("".equals(prefix)) {
                        addOrReplaceAttribute(_atts, "", "", "xmlns", "CDATA", nsURI);
                    } else {
                        addOrReplaceAttribute(_atts, "", "", "xmlns:" + prefix, "CDATA", nsURI);
                    }
                }
            }
        } else {
            //     System.out.println("9-- _ns says we're okay with {" + prefix + "} as {" + nsURI +
            //                        "} for {" + qName + "}");
        }

        // now lets check each attribute name, and see if we need to
        // fake a namespace decl for it
        for (int i = 0; i < length; i++) {
	    String attQName = qAtts.getQName(i);
            String attURI = qAtts.getURI(i);

            n = attQName.indexOf(':');
            if (n == -1) {
                prefix = "";
            } else {
                prefix = attQName.substring(0, n);
            }

            // skip over any xmlns decls
            if ("xmlns".equals(prefix) || "xmlns".equals(attQName)) {
                continue;
            }

            // next, has the prefix binding been declared yet?
            currentNamespace = _nsSupport.getURI(prefix);

            if (currentNamespace == null || ! currentNamespace.equals(nsURI)) {
                // no?, fake a declaration
                if ((attURI != null) && (!"".equals(attURI))) {
                    if (! "xml".equals(prefix) && !_nsSupport.declarePrefix(prefix, attURI)) {
                        reportError("Illegal Namespace prefix: " + prefix);
                    } else {
                        //    System.out.println("10-- bound prefix {" + prefix + "} to {" + 
                        //                                      attURI + "}");
                    }
                    if (getContentHandler() != null) {
                        //           System.out.println("11-- se: fake mapping {" + prefix + "} to {" +
                        //                              attURI + "}");
                        getContentHandler().startPrefixMapping(prefix, attURI);
                    }
                    if (_prefixes) {
                        if ("".equals(prefix)) {
                            // default namespace
                            addOrReplaceAttribute(_atts, "", "", "xmlns", 
                                                  "CDATA", attURI);
                        } else {
                            addOrReplaceAttribute(_atts, "", "", "xmlns:" + prefix, 
                                                  "CDATA", attURI);
                        }
                    }
                }
            } else {
                //    System.out.println("12-- _ns says we're okay with {" + prefix + "} as {" + attURI +
                //                       "} for {" + attQName + "}");
            }
        }
	
        // final pass: copy all relevant
        // attributes into the SAX2 AttributeList
        // using updated prefix bindings
	for (int i = 0; i < length; i++) {
	    String attQName = qAtts.getQName(i);
	    String type = qAtts.getType(i);
	    String value = qAtts.getValue(i);

            // Declaration?
	    if (attQName.startsWith("xmlns")) {
		n = attQName.indexOf(':');

		if (n == -1 && attQName.length () == 5) {
		    prefix = "";
		} else if (n != 5) {
		    // XML namespaces spec doesn't discuss "xmlnsf:oo"
		    // (and similarly named) attributes ... ignore

		    prefix = null;
		} else {
		    prefix = attQName.substring(n + 1);
                    //                    System.out.println("13-- found prefix {" + prefix + "}");
		}
                // Yes, decl:  report or prune
		if (prefix != null) {
		    if (_prefixes) {
                        //   System.out.println("14-- se: carrying decl for {" + prefix + 
                        //                         "} as {" + value + "}");
                        if (_atts.getValue(attQName.intern()) == null) {
                            addOrReplaceAttribute(_atts, "", "", attQName.intern(),
                                                  type, value);
                        }
                    }
		    continue;
		}
	    } 

            // Not a declaration -- report
	    try {
		String attName[] = processName(attQName, true, true);
		addOrReplaceAttribute(_atts, attName[0], attName[1], attName[2],
                                      type, value);

	    } catch (SAXException e) {
                e.printStackTrace();
		if (exceptions == null) {
		    exceptions = new Vector();
                }
		exceptions.addElement(e);
                addOrReplaceAttribute(_atts, "", attQName, attQName, type, value);
	    }
	}


	// now handle the deferred exception reports
	if (exceptions != null && getErrorHandler() != null) {
	    for (int i = 0; i < exceptions.size(); i++) {
		getErrorHandler().error((SAXParseException)
                                        (exceptions.elementAt(i)));
            }
	}
        
        // OK, finally report the event.
	if (getContentHandler() != null) {
	    String[] name = _nsSupport.processName(qName, _nameParts, false);
            if (name == null) {
                //     System.out.println("namespaceSupoport couldn't process  qName {" + qName + "}");
                throw new SAXException("cannot namespace process qName {" + qName + "}");  
            } else {
                if (name[0] == null) {
                    //                    System.out.println("replacing null name[0]");
                    name[0] = "";
                }
                if (name[1] == null) {
                    //                    System.out.println("replacing null name[1]");
                    name[1] = "";
                }
                if (name[2] == null) {
                    //                    System.out.println("replacing null name[2]");
                    name[2] = "";
                }
            }
//             System.out.println("we're gonna propogate event with {" + 
//                                name[0]+ "/" + name[1] + "/" + name[2] + "}");
	    getContentHandler().startElement(name[0], name[1], name[2], _atts);
	}
    }

    /**
     * Adapter implementation method; do not call.
     * Adapt a SAX1 end element event.
     *
     * @param qName The qualified (prefixed) name.
     * @exception SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.DocumentHandler#endElement
     */
    public void endElement(String nsURI, String localName, String qName)
	throws SAXException
    {
        // If we're not doing Namespace
        // processing, dispatch this quickly.
        if (getContentHandler() != null) {
            getContentHandler().endElement(nsURI, localName, qName.intern());
        }
	if (!_namespaces) {
	    return;
	}

	if (getContentHandler() != null) {
	    Enumeration prefixes = _nsSupport.getDeclaredPrefixes();
	    while (prefixes.hasMoreElements()) {
		String prefix = (String)prefixes.nextElement();
		getContentHandler().endPrefixMapping(prefix);
	    }
	}
	_nsSupport.popContext();
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
    public void setFeature (String name, boolean value)
	throws SAXNotRecognizedException, SAXNotSupportedException
    {
// 	if (name.equals(NAMESPACES)) {
// 	    checkNotParsing("feature", name);
// 	    _namespaces = value;
// 	    if (!_namespaces && !_prefixes) {
// 		_prefixes = true;
// 	    }
// 	} else if (name.equals(NAMESPACE_PREFIXES)) {
// 	    checkNotParsing("feature", name);
// 	    _prefixes = value;
// 	    if (!_prefixes && !_namespaces) {
// 		_namespaces = true;
// 	    }
// 	} else {
// 	    throw new SAXNotRecognizedException("Feature: " + name);
//	}
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

    //
    private void addOrReplaceAttribute(AttributesImpl atts, String nsURI, 
                                       String attLocalName, String attQName, 
                                       String type, String value)
    {

        int index = atts.getIndex(attQName);
        if (index < 0) {
            atts.addAttribute(nsURI, attLocalName, attQName, type, value);
        } else {
            atts.setAttribute(index, nsURI, attLocalName, attQName, type, value);
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
        //        System.out.println("NamespaceDeclAugmenter error: " + message);
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
