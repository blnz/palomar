package com.blnz.fxpl.core;

import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.FXException;
import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.FXRequest;

import com.blnz.fxpl.util.ConfigProps;
import com.blnz.fxpl.xform.XForm;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.NodeIterator;
import com.blnz.xsl.om.SafeNodeIterator;
import com.blnz.xsl.om.NodeExtension;
import com.blnz.xsl.om.ExtensionContext;
import com.blnz.xsl.om.NameTableImpl;
import com.blnz.xsl.om.Name;
import com.blnz.xsl.om.XSLException;

import com.blnz.xsl.expr.Expr2Parser;
import com.blnz.xsl.expr.ExprContext;
import com.blnz.xsl.expr.KeyValuesTable;
import com.blnz.xsl.expr.StringExpr;
import com.blnz.xsl.expr.StringVariant;
import com.blnz.xsl.expr.VariableSet;
import com.blnz.xsl.expr.VariantBase;
import com.blnz.xsl.expr.Variant;

import com.blnz.xsl.sax2.SAXTwoOMBuilder;

import java.util.Hashtable;
import java.util.Enumeration;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.text.DateFormat;
import java.util.Date;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import java.util.logging.Logger;

/**
 * Provides context information for the evaluation of
 * FX commands.
 *
 *  FXContext objects provide the FX processor the ability
 *  to pass parameters to the "eval()" method of nested FXRequest command
 *  implementations. 
 *  FXContext objects may contain a handle to a "parent"
 *  Context.  If the requested key is not found in the requested
 *  context, it will attempt to find it in the parent context
 */
public class FXContextImpl 
    extends NullTransformImpl
    implements FXContext, org.xml.sax.Locator, ExtensionContext, VariableSet, ExprContext
{

    private Hashtable _impl = null;
    private Hashtable _defaults = null;

    private FXContext _parent = null;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     *  constructor with no arguments, a top-level context
     */
    public FXContextImpl()
    {
        _impl = new Hashtable();
        _defaults = new Hashtable();
    }

    /** 
     * constructor with a parent context.
     */
    public FXContextImpl(FXContext parent)
    {
        this();
        _parent = parent;
        setNode(parent.getNode());
    }

    /**
     *
     */
    public FXContext extend()
    {
        return new FXContextImpl(this);
    }


    public synchronized FXContext extend(FXContext defs)
    {
        FXContext context = extend();
        ((FXContextImpl)context).loadParams(defs);
        return context;
    }
    
    /**
     * @return the value identified by <code>key</code>
     *  if the object is of type String, performs an expression
     *  evaluation of the string and returns the result of evaluation
     *
     * @param key the name of the requested value
     */
    public Object get(String key)
    {

        Object value = getRaw(key);

        if (value instanceof String) {

	    if ( ((String)value).indexOf('{') >= 0 ) {
		if ("{''}".equals(value)) {
                    return "";
                }
		try {
		    return parseAttributeExpr((String)value, this);
		} catch (Throwable ex) {
		    LOGGER.warning("Cannot parse context param expression {" + value + 
                                   "} with key {" + key  + "}");

		    LOGGER.warning(ex.toString());
		}
	    }
	}
	return value;
    }


    public static final String parseAttributeExpr(String val, FXContext ctx)
    {
        try {
            if (val.indexOf('{') >= 0) {
                
                // FIXME:  this part, we need only do one time.
                StringExpr expr = Expr2Parser.parseValueExpr( ((FXContextImpl)ctx).getNode(), val, ((FXContextImpl)ctx));
                return expr.eval(ctx.getNode(), (FXContextImpl)ctx);
            } else {
                return val;
            }
            
        } catch (Throwable ex) {
            LOGGER.warning("Cannot parse context param expression {" + val + "}");
            
            LOGGER.warning(ex.toString());
            // value = "** expression error: " + ex.getMessage() + 
            //    "in [" + value + "] **";
            
        }
        return val;

    }
    /**
     * @return the value identified by <code>key</code> without any attempt as expression evaluation
     * @param key the name of the requested value
     */
    public Object getRaw(String key)
    {

        Object value = null;

        value =  getAttribute(key);

        if (value == null || "".equals(value)) {
            value =  _impl.get(key);
        }
        
        if ((value == null || "".equals(value)) && _parent != null) {
            value =  ((FXContextImpl)_parent).getRaw(key);
        }
        
        if (value == null) {
            value = _defaults.get(key);
        }

        return value;
    }

    /**
     * Provides an XML representation of all the bindings in the
     *  context, including all the ancestor contexts
     */
    public void sendStackTrace(ContentHandler target) throws SAXException {
        sendStackTrace(null, target);
    }

    /**
     * Provides an XML representation of all the bindings in the
     *  context, including all the ancestor contexts
     */
    public void sendStackTrace(FXException ex, ContentHandler target) 
        throws SAXException
    {

        boolean verbose = "true".equals(getRaw("EchoErrorTrace"));

        sendStackTrace(ex, target, verbose);

    }


    /**
     * Provides an XML representation of all the bindings in the
     *  context, including all the ancestor contexts
     */
    public void sendStackTrace(FXException ex, ContentHandler target, boolean verbose) 
        throws SAXException
    {

        AttributesImpl atts = new AttributesImpl();

        String elName = (String) get("ctx:elementName");
        atts.addAttribute("", "elementName", "elementName",
                          "CDATA", elName == null ? "[anonymous]" : elName);

        String cName = (String) get("ctx:commandName");
        atts.addAttribute("", "commandName", "commandName",
                          "CDATA", cName == null ? "[unknown]" : cName );

        String iName =  (String) get("ctx:commandItem");
        atts.addAttribute("", "itemName", "itemName",
                          "CDATA", iName == null ? "[internal]" : iName );

        String sysID =  (String) get("ctx:sysID");
        atts.addAttribute("", "sysID", "sysID",
                          "CDATA", sysID == null ? "[internal]" : sysID );

        String lineNo =  (String) get("ctx:lineNo");
        atts.addAttribute("", "lineNo", "lineNo",
                          "CDATA", lineNo == null ? "-1" : lineNo );

        String colNo =  (String) get("ctx:colNo");
        atts.addAttribute("", "colNo", "colNo",
                          "CDATA", colNo == null ? "-1" : colNo );

        target.startElement("", "contextFrame", "contextFrame",
                           atts);
        atts.clear();

        if (ex != null) {
            ex.addLocation(this, elName);
        }

        if (verbose) {

            // do the bindings
            Enumeration keys = _impl.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                if (! key.startsWith("ctx:") && 
                    ! "password".equals(key)) {
                    atts.addAttribute("", "key", "key",
                                      "CDATA", key);
                    Object val = get(key);
                    if (val instanceof String) {
                        atts.addAttribute("", "value", "value",
                                          "CDATA", (String) val);
                    }
                    Object preval = _impl.get(key);
                    if ((preval instanceof String) && !preval.equals(val)) {
                        atts.addAttribute("", "expr", "expr",
                                          "CDATA", (String) preval);
                    }
                    target.startElement("", "bind", "bind",
                                        atts);
                    target.endElement("", "bind", "bind");
                    
                    atts.clear();
                    
                }
            }

            if (_parent != null) {
                ((FXContextImpl)_parent).sendStackTrace(ex, target, verbose);
            }
        }
        target.endElement("", "contextFrame", "contextFrame");
    }

    // Locator implementation

    public String getPublicId()
    { return null; }

    public String getSystemId() 
    {
        String sysID =  (String) get("ctx:sysID");
        return (sysID == null) ? "[internal]" : sysID ;
    }
    
    public int getLineNumber()
    {
        String lineNo =  (String) get("ctx:lineNo");
        return (lineNo == null) ? 1 : Integer.parseInt(lineNo);
    }

    public int getColumnNumber()
    {
        String colNo =  (String) get("ctx:colNo");
        return (colNo == null) ? 1 : Integer.parseInt(colNo);
    }





    /**
     * @return the value identified by <code>key</code>
     *  if the object is of type String, performs an expression
     *  evaluation of the string and returns the result of evaluation
     *
     * @param key the name of the requested value
     */
    private Object getSys(String key)
    {
        LOGGER.info("getSys: {" + key + "}");
        if ("date".equals(key)) {
            Date d = new Date();
            return DateFormat.getDateInstance().format(d);
        } else if ("time".equals(key)) {
            return null;
        }
        return null;
    }

    /**
     * @return the System property identified by <code>key</code>
     *
     * @param key the name of the requested value
     */
    private Object getSysProp(String key)
    {
        LOGGER.info("getSysProp: {" + key + "}");        
        try {
            System.getProperties().list(System.out);
        } catch (Exception ex) {
            LOGGER.warning("cannot list props");
        }

        return System.getProperty(key);
    }

    /**
     * @return the FDX configuration property identified by <code>key</code>
     *
     * @param key the name of the requested value
     */
    private Object getFdxProp(String key)
    {
        LOGGER.info("getFdxProp: {" + key + "}");        
        try {
            ConfigProps.getProperties().list(System.out);
        } catch (Exception ex) {
            LOGGER.warning("cannot list props");
        }
        return ConfigProps.getProperty(key);
    }


    /**
     * stores the value in the local context
     */
    public void put(String key, Object value)
    {
        _impl.put(key, value);
    }

    /**
     * removes an object from the context.
     */
    public Object remove(String key)
    {
        return _impl.remove(key);
    }

    /**
     * stores the default value in the local context
     */
    public void putDefault(String key, Object value)
    {
        _defaults.put(key, value);
    }
 
    /**
     * for delegating lookups
     */
    public void setParentContext(FXContext parent)
    {
        _parent = parent;
        //        this.loadParams();
    }

    public SafeNodeIterator getAttributes()
    {
        return (this.getNode() == null) ? null : this.getNode().getAttributes();
    }
        
    /**
     *
     */
    public String getAttribute(String attrName)
    {
        return (this.getNode() == null) ? null : this.getNode().getAttributeValue(name(attrName));
    }

    /**  
     * throw the params into our internal dictionary by key
     */ 
    protected void loadParams(NodeExtension ctx)
    {
        _impl.clear();

        // get context values from attributes
        
        SafeNodeIterator  attlist = ctx.getNode().getAttributes();

        Node n = attlist.next();
        while (n != null) {
            String name = n.getName().getLocalPart();
            String val = n.getData();
            _impl.put(name, val);
            n = attlist.next();
        }
        
        stashParams(ctx.getNode().getChildren());

    }

    //
    private void stashParams(SafeNodeIterator nl)
    {
        
        Node e = nl.next();
        while (e != null) {
            System.out.println("stashParams:: next is " + e.getClass().getName());
            if (e.getType() == Node.ELEMENT) {
                String tagName = e.getName().getLocalPart();
                if ("param".equals(tagName) ||
                    "function".equals(tagName) ) {
                    Name nName = name("name");
                    String name = e.getAttributeValue(nName); 
                    if (name != null && name.length() > 0) {
                        Object val = getParamBindingValue(e, this);
                        _impl.put(name, val);
                    }
                }
            }
            e = nl.next();
        }
    }

    /**
     * 
     */
    public static Object getParamBindingValue(Node e, FXContext context)
    {

        
        String name = e.getAttributeValue(((NameTableImpl) e.getParent().getName().getCreator()).createName("name"));
        Object val = e.getAttributeValue(((NameTableImpl) e.getParent().getName().getCreator()).createName("value"));
        String bindResult = e.getAttributeValue(((NameTableImpl) e.getParent().getName().getCreator()).createName("bindResult"));

        if (val == null || "".equals(val)) {
            SafeNodeIterator kids = e.getChildren();

            String textVal = null;

            Node n = kids.next();
            while (n != null) {
                if ( n.getType() == Node.TEXT ) {
                    textVal = n.getData();
                } else if ( n.getType() == Node.ELEMENT ) {
                    textVal = null;
                    
                    if (n.getNodeExtension() instanceof FXRequest ) {
                        
                        if ("true".equals(bindResult)) {
                            
                            String maybeVal = "";
                            
                            try {
                                StringWriter sw = new StringWriter();
                                ContentHandler ch = 
                                    XForm.getTransformService().createCharacterContentWriter(sw);
                                
                                ch.startDocument();
                                ((FXRequest)n).eval(ch, context);
                                ch.endDocument();
                                
                                maybeVal = sw.toString();
                                
                                if (! isWellFormed(maybeVal)) {
                                    val = unescape(maybeVal);
                                } else {
                                    try {
                                        SAXTwoOMBuilder ch3 = 
                                            XForm.getTransformService().createOMWriter();
                                        StringReader xsr = new StringReader(maybeVal);
                                        InputSource xIsrc = new InputSource(xsr);
                                        
                                        XMLReader xrdr = XForm.createInputSourceReader(xIsrc);
                                        xrdr.setContentHandler(ch3);
                                        xrdr.parse(xIsrc);
                                        
                                        //                                         ch3.startDocument();
                                        //                                         ((FXRequest)n).eval(ch3, context);
                                        //                                         ch3.endDocument();
                                        
                                        Node docRoot = ch3.getRootNode();
                                        if (docRoot == null ) {
                                            val = maybeVal;
                                        } else {
                                            XMLReader rdr = XForm.createOMSourceReader(docRoot);
                                            val = rdr;
                                        }
                                        
                                    } catch (Exception ex1) {
                                        ex1.printStackTrace();
                                        val = maybeVal;
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                val = "";
                            }
                        } else if ( ("function".equals(e.getName().getLocalPart( ))) && (FXHome.NAMESPACE.equals(e.getName().getNamespace()))) {
                            val = n;
                        } else {
                            // hmmm ... maybe this should be the request, itself
                            val = new RequestReReader(((FXRequest) n.getNodeExtension()), context);
                        }
                    }
                    break;
                }
                n = kids.next();
            }
            
        }
        return val;
    }
    
    
    //
    private static String unescape(String s)
    {
        if (s.indexOf('&') < 0) {
            return s;
        }
        
        try {
            StringReader sr = new StringReader("<x>" + s + "</x>");
            InputSource src = new InputSource (sr);
            XMLReader rdr = XForm.getTransformService().createInputSourceReader(src);
            CharsHandler ch = new CharsHandler();
            rdr.setContentHandler(ch);
            rdr.parse(src);
            
            return ch.getString();
        } catch (Exception ex) {
            System.out.println("^^unable to unescape [" + s + "] because: " + ex.getMessage());
        }
        return s;
    }

    /**
     *
     */
    private static boolean isWellFormed(String s)
    {

        if (s == null) {
            return false;
        }

        if (s.indexOf('<') < 0) {
            return false;
        }

        try {
            StringReader sr = new StringReader(s);
            InputSource src = new InputSource (sr);
            XMLReader rdr = XForm.getTransformService().createInputSourceReader(src, XForm.CHECK_NONE);
            ContentHandler ch = new DefaultHandler();
            rdr.setContentHandler(ch);
            rdr.parse(src);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    //
    //   XPATH ExprContext methods 
    //

    /**
     *
     */
    public int getPosition() throws XSLException 
    { return 1; }
    
    /**
     *
     */
    public int getLastPosition() throws XSLException 
    { return 1; }
    
    /**
     *
     */
    public com.blnz.xsl.expr.ExtensionContext getExtensionContext(String namespace)
        throws XSLException
    { return null; }
    
    /**
     * provides access to the system properties for the 
     * system-property() function in XSLT 1.0 section 12.4
     */
    public Variant getSystemProperty(Name name)
    { return null; }
    
    /**
     *
     */
    public Node getCurrent(Node contextNode)
    { return this.getNode(); }
    
    /**
     * returns a parsed representation of the document at the given
     * URL. ... enables  the "document()" function of XSLT 1.0 section 12.1
     */
    public NodeIterator getDocument(URL baseURL, String uriRef) throws XSLException
    { return null; }
    
    /**
     *  @return the indexed nodes for the named key in the node's document
     */
    public KeyValuesTable getKeyValuesTable(Name keyName, Node n)
    { return null; }
    
    /**
     *
     */
    public Node getTree(Variant v) throws XSLException
    { return null; }
    

        
    /** 
     * access to the stylesheet's global variables
     */ 
    public Variant getGlobalVariableValue(Name name) throws XSLException
    {   
        return  VariantBase.create(get(name.getLocalPart()));  
    }
    
    /**
     * access to the stylesheet's in-scope local variables
     */
    public Variant getLocalVariableValue(Name name) throws XSLException
    { return getGlobalVariableValue(name); }


    public boolean contains(Name name) {
        // TODO Auto-generated method stub
        return false;
    }
    


}


