package com.blnz.fxpl.core;

import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.FXException;
import com.blnz.fxpl.FXHome;

import com.blnz.fxpl.expr.Expression;
import com.blnz.fxpl.expr.XRExp;

import com.blnz.xsl.expr.Variant;
import com.blnz.xsl.expr.KeyValuesTable;
import javax.xml.transform.SourceLocator;
import com.blnz.xsl.om.XSLException;
import com.blnz.xsl.om.SafeNodeIterator;
import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.NodeExtension;
import com.blnz.xsl.om.ExtensionContext;
import com.blnz.xsl.om.NodeIterator;
import com.blnz.xsl.om.Name;

import com.blnz.fxpl.security.Security;
import com.blnz.fxpl.security.SecurityService;
import com.blnz.fxpl.security.User;

import com.blnz.fxpl.fs.FS;
import com.blnz.fxpl.fs.FsRepository;
import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.RepositoryUtil;

import com.blnz.xsl.expr.ExprContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import java.net.URL;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.logging.Logger;


/**
 * Base class for server-side implementation of FX request objects.
 */
public class FXRequestServerSide extends FXRequestImpl

{
    protected final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected static SecurityService _sec = null;

    public static boolean DEBUG = false;

    //    public static boolean DEBUG = true;

    /**
     * Evaluate the request under the given context, sending the
     * results to the responseTarget ContentHandler.
     *
     * This base class exhibits a default behavior, whis is to copy
     * this element with its attributes to the output, and including
     * the results of evaluating its children elements. 
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
	throws Exception
    {
        FXContext ctx = (FXContext) context;

        if ("http://xte.blnz.com/fxpl/param".equals(this.getNamespaceURI())) {
            ctx = (FXContext) extendContext(ctx);
            
            //if (ctx == null) {
            //    throw new Exception("null context");
            //}
            FXRequest next;
            int i = 1;
            for (i = 1; (next = getSubRequest(i)) != null ; ++i) {
                ctx.put("arg_" + i, next);
            }
            ctx.put("arg_count", "" + (i - 1));

            ParamRefImpl.doEval(this.getTagName(), responseTarget, ctx);
        } else {
            // default behavior is to copy element and eval its contents
            evalCopy(responseTarget, ctx);
        }
    }


    /**
     * copy the element, and contents to the result
     */
    protected void evalCopy(ContentHandler responseTarget, FXContext context) 
	throws Exception
    {

//           System.out.println("FXRequestServerSide::evalCopy()");

        context = extendContext(context);
        if (context == null) {
            throw new Exception ("null context");
        }
        try {

            // copy the attributes
            AttributesImpl atts = new AttributesImpl();
            
            //            NamedNodeMap attsList = getAttributes();


            SafeNodeIterator ni = getNode().getAttributes();

            for ( ; ; ) {
                Node att = ni.next();
                if (att == null) {
                    break;
                }
                Name attName = att.getName();
                String attNS = attName.getNamespace(); 
                if (attNS == null) {
                    attNS = "";
                }

                String localName = attName.getLocalPart();
                String anp = attName.getPrefix();
                String qname = anp == null ? localName : (anp + ":" + localName);  
                if (localName == null) {
                    localName = qname;
                }
                String val = att.getData();
                try {
                    // FIXME: do not do unless needed
                    if (val.indexOf('{') >= 0) {
                        val = FXContextImpl.parseAttributeExpr(val, (FXContextImpl)context);
//
//                        Expression exp = 
//                            XRExp.parseExpr(new StringReader((String)val));
//                        StringWriter sw = new StringWriter();
//                        exp.eval(context, sw);
//                        val = sw.toString();
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    val = "** expression error: " + // ex.getMessage() + 
                        "in [" + val + "] **";
                    
                }
                
                atts.addAttribute(attNS, localName, qname, "CDATA", val);
            }

            String nsURI = getNamespaceURI();
            if (nsURI == null) {
                nsURI = "";
            }

            String tagName = getTagName();
            String localName = getNode().getName().getLocalPart(); //getLocalName();
            
            evalBody(responseTarget, context, nsURI, 
                     localName, tagName, atts);
            
        } catch (Exception ex) {
            ex.printStackTrace();
	    errorResponse(ex, responseTarget, context);
        }
    }


    /**
     * Copy the element, and  the results of its contents to the output.
     *
     * @param responseTarget  recieves the SAX events representing the results.
     * @param context the name/value bindings in effect.
     * @param nsURI the namespace name for this element's namespace/localName pair.
     * @param localName the localName for this element's namespace/localName pair.
     * @param qName the prefix/colon(:)/localName representation of the element's
     *    tagname.
     * @param atts attributes to send with the start element event
     */
    public void evalBody(ContentHandler responseTarget, FXContext context,
                         String  nsURI, String localName, 
                         String qName, Attributes atts) 
	throws Exception
    {
        //        context = extendContext(context);
        
//        System.out.println("FXRequestServerSide::EvalBody( target, context, " + nsURI + ", " + 
//                           localName + ", " + qName + ", attrs)");


        // insert code for attribute constructors
        
        if (qName != null) {
            responseTarget.startElement(nsURI, 
                                        localName, qName, 
                                        atts);
        }

        Name myName = this.getNode().getName();
       
        boolean trim =  ( FXHome.NAMESPACE.equals(myName.getNamespace() ) &&
                
                (("trim".equals(localName)) || 
                 ("echo".equals(localName)) ||
                 ("xrap".equals(localName)) ||
                 ("log".equals(localName)) ||
                 ("time".equals(localName)))) ? true : false;

        
        processChildren(responseTarget, context, trim);
        
         
        if (qName != null) {
            responseTarget.endElement(nsURI, 
                                      localName, qName); 
        }
        
    }

    
    public void processChildren(ContentHandler responseTarget, FXContext context, boolean trim)    throws Exception
    {
        SafeNodeIterator kids = getChildNodes();
        char[] chars;

        Node n = kids.next();
        while (n != null) {

            NodeExtension ne = n.getNodeExtension();

            if (ne instanceof FXRequest) {
                if (!(ne instanceof FXContextImpl) &&
                    !(ne instanceof ParamSetImpl) &&
                    !(ne instanceof Import) &&
                    !(ne instanceof DocumentationImpl) &&
                    !(ne instanceof NullTransformImpl)) {
                
                    FXRequest child = (FXRequest) ne;
                    XMLReader reader = 
                        new NullDocEventXMLFilter(context, child);                    
                    String uri = child.getURI(context);
                    InputSource is = new InputSource(uri);
                    reader.setContentHandler(responseTarget);
                
                    reader.parse(is);
                } else {
                    // we ignore these guys
                }
            } else {
                switch (n.getType()) {
                    
                case Node.PROCESSING_INSTRUCTION:
                    responseTarget.processingInstruction (n.getName().getLocalPart(), 
                                                          n.getData());
                    break;
                    
                case Node.TEXT:
                    chars = n.getData().toCharArray ();

                    if (trim) {
                        int cl = chars.length;
                        int j = 0;
                        for ( ; j < cl; ++j) {
                            if (! Character.isWhitespace(chars[j]) ) {
                                break;
                            }
                        }

                        for ( ; j < cl; --cl) {
                            if (! Character.isWhitespace(chars[cl - 1]) ) {
                                responseTarget.characters(chars, j, cl - j);
                                break;
                            }
                        }

                        
                    } else {
                        responseTarget.characters (chars, 0, chars.length);
                    }
                    break;
                    
                    
                case Node.COMMENT:
                    // 
                    if (responseTarget instanceof LexicalHandler) {
                        chars = n.getData().toCharArray ();
                        ((LexicalHandler)responseTarget).comment (chars, 0, chars.length);
                    }
                    break;
                    
                default:
                    throw new Exception("FX unable to process DOM node type"); 
                }
            }
            n = kids.next();
        }
    }
    
    /**
     *  adds a new context frame, attaching any locally bound
     * parameters
     */
    public FXContext extendContext(FXContext parentContext) 
	throws Exception
    {

        FXContext ctx = null;

        if (parentContext == null) {
            Name self = getNode().getName();
            System.out.println("no context in "+ self.getNamespace() + ":" + self.getLocalPart());
            throw new Exception ("null context in "+ self.getNamespace() + ":" + self.getLocalPart());
        }

        parentContext = parentContext.extend();
        ctx = parentContext;
        ctx.setNode(this.getNode());

        // first, we pick up attributes 

        SafeNodeIterator atts = this.getNode().getAttributes();
        // FIXME: do we really wanna always add attrs to context?
        // what about arbitrary, non-echo elements? 
        if (atts != null) {
            Node n = atts.next();
            while (n != null) {

                String attrName =  n.getName().getLocalPart();
                /// System.out.println("FXRequestServerSide:: extending context with : " + attrName);
                if (! attrName.equals("userID") &&
                    ! attrName.startsWith("xmlns")) {
                    if (! "{''}".equals(n.getData())) {
                        // new behavior resolves attribute expressions during extend context
                        ctx.put(n.getName().getLocalPart(), 
                                FXContextImpl.parseAttributeExpr(n.getData(), parentContext));
                    } else {
                        ctx.put(n.getName().getLocalPart(), n.getData());
                    }
                }
                n = atts.next();
            }
        }

        // process context and paramset children, in order
        SafeNodeIterator nl = this.getNode().getChildren();
        Node n = nl.next();

        while (n != null) {

            if (n.getType() == Node.ELEMENT) {
                // String tagname = ((Element) n).getTagName();
                NodeExtension ne = n.getNodeExtension();
                if (ne instanceof FXContext) {
                    ctx = ((FXContextImpl)parentContext).extend((FXContext) ne);
                    parentContext = ctx;
                } else if (ne instanceof Import) {
                    ctx = ((Import)ne).getBindings(parentContext);
                    parentContext = ctx;
                } else if (ne instanceof ParamSetImpl) {
                    //                     stashDefaultParams((FXContextImpl) parentContext,
                    //                                        ((Element) n).getElementsByTagNameNS(FXHome.NAMESPACE,
                    //                                                                             "param")); 
                    
                    //                     stashDefaultParams((FXContextImpl) parentContext,
                    //                                        ((Element) n).getElementsByTagNameNS("http://namespaces.snapbridge.com/xrap",
                    //                                                                             "param")); 
                    
                    //                     stashDefaultParams((FXContextImpl) parentContext,
                    //                                        ((Element) n).getElementsByTagNameNS(FXHome.NAMESPACE,
                    //                                                                             "function")); 
                    
                    //                     stashDefaultParams((FXContextImpl) parentContext,
                    //                                        ((Element) n).getElementsByTagNameNS("http://namespaces.snapbridge.com/xrap",
                    //                                                                             "function")); 
                    
                    //                     // FIXME: we should deprecate non-namespaced params?
                    
                    //                     stashDefaultParams((FXContextImpl) parentContext, 
                    //                                        ((Element) n).getElementsByTagName("param"));
                    
                }
            }
            n = nl.next();
        }

        parentContext.put("ctx:elementName", this.getTagName());
        parentContext.put("ctx:commandName", this.getClass().getName());

        SourceLocator loc = this.getNode();   //  FIXME:  location from node! this.getLocation();
        if (loc != null) {
            parentContext.put("ctx:sysID", "" + loc.getSystemId());
            parentContext.put("ctx:lineNo", "" + loc.getLineNumber());
            parentContext.put("ctx:colNo", "" + loc.getColumnNumber());
        }

        return parentContext;
    }

    //
    private void stashDefaultParams(FXContextImpl context, SafeNodeIterator nl)
    {
        
        Node n = nl.next();

        while (n != null) {

            String name = n.getAttributeValue(name("name")); 
            if (name != null && name.length() > 0) {
                Object val = FXContextImpl.getParamBindingValue(n, context);
                context.putDefault(name, val);
            }
            n = nl.next();
        }
    }

    public String getAttributeValue(String attName) 
    {
        Node mine = getNode();
        return mine.getAttributeValue(name(attName));
    }
        

    /**
     * returns a child request which is not a "context, paramSet, 
     * documentation or meta" element
     */
    public FXRequest getSubRequest(int position)
        throws Exception  // which kind
    {
        
        SafeNodeIterator nl = getChildNodes();

        if (nl == null) {
            return null;
        }

        Node n = nl.next(); 
        while (n != null) {
            if (n.getType() == Node.ELEMENT) {
                String tagname = ((Node) n).getName().getLocalPart();
                NodeExtension ne = n.getNodeExtension();
                if ( (ne instanceof FXContextImpl) ||
                     (ne instanceof Import) ||
                     (ne instanceof DocumentationImpl) ||
                     (ne instanceof ParamSetImpl) ||
                     "context".equals(tagname)) {
                    //skip the context, we already got it
                } else {
                    if (--position == 0) {
                        return (FXRequest) ne;
                    }
                }
            }
            n = nl.next();
        }
        return null;
    }

    /**
     *
     */
    protected void writeException(Throwable ex, ContentHandler target)
    {
        try {
            
            String exceptionClass = ex.getClass().getName();
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "type", "type", "CDATA",
                              exceptionClass);
            String message = ex.getMessage();
            if (message == null) {
                message = "?";
            }
            atts.addAttribute("", "message", "message", "CDATA",
                              message);
            startElement("exception", atts, target);
            if (DEBUG) {
                //            atts.clear();
                startElement( "trace", target); 
                
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                
                String s = sw.toString();
                char[] chars = new char[s.length()]; 
                s.getChars(0, s.length(), chars, 0);
                target.characters(chars, 0, chars.length);
                endElement("trace", target);
            }
            if (ex instanceof com.blnz.fxpl.FXException ) {
                ex = ((com.blnz.fxpl.FXException)ex).getException();
                if (ex != null) {
                    // atts.clear();
                    startElement( "contents", target);
                    writeException(ex, target);
                    endElement( "contents", target); 
                }
            }
            
            endElement("exception", target);
            
        } catch (SAXException e) {
            log(e);
        }
    }

    /**
     *
     */
    protected void writeException(Throwable ex, ContentHandler target, FXContext context)
    {
        try {
            String exceptionClass = ex.getClass().getName();
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "type", "type", "CDATA",
                              exceptionClass);
            String message = ex.getMessage();
            if (message == null) {
                message = "?";
            }
            atts.addAttribute("", "message", "message", "CDATA",
                              message);
            startElement("exception", atts, target);
            if (DEBUG) {
                //            atts.clear();
                startElement( "trace", target); 
                
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                
                String s = sw.toString();
                char[] chars = new char[s.length()]; 
                s.getChars(0, s.length(), chars, 0);
                target.characters(chars, 0, chars.length);
                endElement("trace", target);
            }
            if (ex instanceof com.blnz.fxpl.FXException ) {
                Exception inner = ((com.blnz.fxpl.FXException)ex).getException();
                if (inner != null) {
                    // atts.clear();
                    startElement( "contents", target);
                    writeException(inner, target);
                    endElement( "contents", target); 
                }
            } else {
                ex = new FXException(new Exception(ex));
            }
            
            endElement("exception", target);

            FXException echoEx = (FXException)ex;

            // always?
            context.sendStackTrace((FXException)echoEx, target);

            // always ?
            LOGGER.severe(echoEx.getMessage());
            
        } catch (SAXException e) {
            log(e);
        }
    }

    /**
     *
     */
    protected String getNamespacePrefix()
    { return ""; }

    /**
     *
     */
    protected void errorResponse(Throwable ex, ContentHandler responseTarget, 
                                 FXContext context)
        throws SAXException
    {
        AttributesImpl atts = new AttributesImpl();

        String msg = ex.getMessage();
        if (msg == null || "".equals(msg)) {
            msg = " ** no message ** ";
        }

        addAttribute(atts, "xmlns", FXHome.NAMESPACE);
        addAttribute(atts, "type", ex.getClass().getName());
        addAttribute(atts, "message", msg);
        addAttribute(atts, "action", this.getTagName());

        startElement("error", atts, responseTarget);
        writeException(ex, responseTarget, context);
        endElement("error", responseTarget);
    }

    /**
     * writes a cdata attribute with no namespace
     */
    protected static final void addAttribute(AttributesImpl atts, 
                                             String attname, String attvalue)
    {
        atts.addAttribute("", attname, attname, "CDATA", attvalue);
    }


    protected void startElement(String tagname, Attributes atts, 
                                ContentHandler dest)
        throws SAXException
    {
        dest.startElement(getNamespaceURI(), tagname, 
                          getNamespacePrefix() + tagname, atts);
    }
    
    
    protected void startElement(String tagname, 
                                ContentHandler dest)
        throws SAXException
    {
        dest.startElement(getNamespaceURI(), tagname, 
                          getNamespacePrefix() + tagname, 
                          new AttributesImpl());
    }
    
    
    protected void endElement(String tagname, 
                              ContentHandler dest)
        throws SAXException
    {
        dest.endElement(getNamespaceURI(), tagname, 
                        getNamespacePrefix() + tagname);
    }
    
    
    protected static final void log(Exception ex) 
    {
        try {
            LOGGER.warning(ex.toString());
        } catch (Exception e) {
            // FIXME: fatal?
        }
    }
    
    /**
     * convenience method obtains SecurityService singleton
     */
    protected static final SecurityService getSecurityService()
    {
        if (_sec == null) {
            try {
                _sec = Security.getSecurityService();
            } catch (Exception ex) {
                log(ex);
            }
        }
        return _sec;
    }
    
    
    protected User getUser(FXContext context)
        throws FXException
    {
        String userID = (String) context.get("userID");
        User user = null;
        try {
            user = getSecurityService().getUser(userID);
        } catch (Exception ex) {
                throw new FXException("failed to get User for " +
                                                 userID);
        }
        if (user == null) {
            throw new FXException("null User for " +
                                             userID);
        }
        return user;
    }

    // for debugging
    

    /**
     *  get the repository item from parameters in the context 
     */
    protected RepositoryItem getItem(FXContext context)
	throws Exception
    {
	if (context == null) {
	    throw new Exception("null context");
	}
	
	FsRepository dm = FS.getRepository();
	
	if (dm == null) { 
	    throw new Exception("null DocumentManagement"); 
	}
	
	User user = getUser(context);
	
	RepositoryItem item;
	
	String idstr = (String) context.get("itemID");
	String name = (String) context.get("name");
	String path = (String) context.get("path");
	
	if (idstr == null || "".equals(idstr)) {
	    if (name == null || "".equals(name)) {
		if (path == null || path.length() == 0) {
		    throw new Exception("no itemID, name, or path specified");
                }
		name = "";
	    }
	    
	    if (path == null || path.length() == 0) {
		path = "/";
	    } 
            path = RepositoryUtil.normalizePath(path, name);
	    
	    item = dm.getRepositoryItemByPath(user, path);
            if (item == null) {
                throw new Exception("item with name " + path + " not found");
            }
	} else {
	    //int id  = Integer.parseInt(idstr); 
	    item = dm.getRepositoryItem(user, idstr);
            if (item == null) {
                throw new Exception("item with ID: " + idstr + " not found");
            }
	}
	return item;
    }

    /** All output will be use this encoding */
    static final String outputEncoding = "UTF-8";

   
    protected boolean toBoolean(String str) {
        return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str);
    }

    protected boolean toBoolean(String str, boolean defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str);
        }
    }


}

