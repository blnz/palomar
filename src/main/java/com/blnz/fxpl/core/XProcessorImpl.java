package com.blnz.fxpl.core;

import com.blnz.fxpl.*;

import com.blnz.fxpl.xform.XForm;
import com.blnz.fxpl.xform.TransformException;
import com.blnz.fxpl.xform.DOMWriter;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.NodeExtension;
import com.blnz.xsl.om.ExtensionContext;
import com.blnz.xsl.om.NodeExtensionFactory;
import com.blnz.xsl.sax2.SAXTwoOMBuilder;
import com.blnz.xsl.tr.LoadContext;

import com.blnz.fxpl.log.Logger;
import com.blnz.fxpl.log.Log;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;

import java.sql.Statement;

import java.text.DateFormat;


import java.util.Hashtable;
import java.util.Date;
import java.util.Enumeration;

import java.io.StringReader;
import java.io.IOException;

/**
 * The FX language processor configured with mappings
 * from Element names to classes which implement the
 * commands' semantics.
 */
public class XProcessorImpl implements XProcessor
{

    protected Logger _logger = null;
    protected FXRequest _request = null;

    protected SimpleElementFactory _myNodeExtensionFactory = null;
    protected SimpleElementFactory _myResponseFactory = null;

    // number of currently active "eval" calls
    protected static int _activeProcesses = 0;

    protected static Hashtable _processTable = new Hashtable();

    protected String _urlString  = null;

    protected String _userID = null;
    protected String _connectionID = null;
    protected String _certificate = null;


    /**
     *
     */
    public XProcessorImpl()
    { init(null); }

    /**
     * @param factory an NodeExtensionFactory for the FX Command 
     * objects appropriate to FX element names
     */
    public XProcessorImpl(SimpleElementFactory factory)
    { init(factory); }
    
    /**
     * @param factory an Object factory for the FX Command 
     * objects appropriate to FX element names
     */
    private void init(SimpleElementFactory factory)
    {
        try {
            _logger = Log.getLogger();
        } catch ( Exception ex) {
            ex.printStackTrace();
            // fatal maybe?
        }
        _myNodeExtensionFactory = factory;
        _myResponseFactory = factory;
    }

    /**
     *
     */
    public void setRequestFactory(SimpleElementFactory fact)
    { _myNodeExtensionFactory = fact; }
    
    /**
     *
     */
    public void setResponseFactory(SimpleElementFactory fact)
    { _myResponseFactory = fact; }


    public void setURL(String url)
    { _urlString = url; }

    /**
     *
     */
    public void setCredentials(String userID, String connectionID, String certificate)
    {
        _userID = userID;
        _connectionID = connectionID;
        _certificate = certificate;
    }
    /**
     * create an initial context for a request
     */
    public FXContext createContext()
    {
        return new FXContextImpl();
    }
    
    /**
     * returns the number of FX processes which are being processed
     * by instances of this class at this moment
     */
    public int getActiveProcessCount()
    { return _processTable.size(); }

    /**
     * evaluate the request
     */
    public void eval(FXContext context,
                     Node request,
                     ContentHandler responseTarget) 
	throws Exception
    {



        //        System.out.println("XprocessorImpl::eval(context, request, response) entry " + request.getName().getLocalPart() + " === " + request.getNodeExtension().getClass().getName());
        // note an active evaluation thread
        ++_activeProcesses;
        recordThread();
        responseTarget.startDocument();

        // System.out.println("XprocessorImpl::eval(context, request, response) 1");

        NodeExtension ne = request.getNodeExtension();
        // System.out.println("XprocessorImpl::eval(context, request, response) 3");
        ne.eval(responseTarget, context);

        // System.out.println("XprocessorImpl::eval(context, request, response) 4");
        responseTarget.endDocument();
        removeThread();
        // System.out.println("XprocessorImpl::eval(context, request, response) 6");
        // evaluation thread finished
        --_activeProcesses;
        // System.out.println("XprocessorImpl::eval(context, request, response) exit");
    }


    /**
     *
     */
    public Node compile(XMLReader reader, InputSource src)
        throws SAXException, TransformException, IOException
    {
        return getRequest(reader, src);
    }


    /**
     * evaluate the request
     */
    public void eval(FXContext context,
                     XMLReader reader,
                     InputSource src,
                     ContentHandler responseTarget) 
	throws Exception
    {
        if (reader == null) {
            throw new Exception("no reader for request");
        }
        
        Node request = getRequest(reader, src);

        if (request == null) {
            throw new Exception("null request");
        }
        eval(context, request, responseTarget);
    }

    /**
     * evaluate the request, constructing a OM result with
     * the default NodeExtensionFactory
     */
    public Node evalToResponse(FXContext ctx, Node request) 
	throws Exception
    { 
        if (request == null) {
            throw new Exception("FXRequest is null");
        }
        Node docNode = evalToOM(_myResponseFactory, ctx, request); 

        return Util.rootElement(docNode);

    }

    /**
     * evaluate the request, constructing a DOM result with
     * the default NodeExtensionFactory
     */
    public Node evalToResponse(FXContext ctx, XMLReader reader,
                               InputSource src)
	throws Exception
    { 
        return evalToResponse(ctx, getRequest(reader, src));
    }
    
    /**
     * evaluate the request, constructing a DOM result with
     * a custom elementFactory
     */
    private Node evalToOM(LoadContext factory, FXContext ctx, Node request) 
	throws Exception
    { 
        if (request == null) {
            throw new Exception("null FX request");
        }

        SAXTwoOMBuilder dw = XForm.createOMWriter(factory);
        eval(ctx, request, dw);
        return dw.getRootNode();
    }

    //
    //
    private Node getRequest(XMLReader sourceStream, 
                            InputSource input) 
        throws TransformException, SAXException, IOException
    {

        SAXTwoOMBuilder db =
            XForm.createOMWriter(_myNodeExtensionFactory);

        sourceStream.setContentHandler(db);
        sourceStream.parse(input);

        Node docNode = db.getRootNode();

        if (docNode == null) {
            throw new TransformException("unable to build OM from request");
        }
        return Util.rootElement(docNode);

    }

    /**
     *
     */
    private void removeThread()
    {

        Thread t = Thread.currentThread();

        ThreadInfo ti = (ThreadInfo) _processTable.get(t.getName());

        if (ti == null) {

        } else {
            ti.decrementProcessorCount();
            if (ti.getProcessorCount() == 0) {
                
                _processTable.remove(t.getName());
                
            }
        }
    }

    /**
     *
     */
    public void setThreadProperty(String propName, Object value)
    {
        Thread current = Thread.currentThread();
        String threadName = current.getName();
        
        ThreadInfo ti = (ThreadInfo) _processTable.get(threadName);

        if (ti == null) {

        } else {

            ti.setProperty(propName, value);
        }
    }

    /**
     *
     */
    public void listProcesses(ContentHandler ch)
        throws SAXException
    {

        AttributesImpl atts = new AttributesImpl();
        ch.startElement("", "activeProcesses", "activeProcesses", atts);

        Enumeration tiEnum = _processTable.elements();
        
        while (tiEnum.hasMoreElements()) {
            ThreadInfo ti = (ThreadInfo) tiEnum.nextElement();
            ti.asXML(ch);
        }
        
        ch.endElement("", "activeProcesses", "activeProcesses");

    }

    /**
     *
     */
    private void recordThread()
    {

        Thread t = Thread.currentThread();

        ThreadInfo ti = (ThreadInfo) _processTable.get(t.getName());
        if (ti == null) {
            ti = new ThreadInfo(t);
            _processTable.put(t.getName(), ti);

        }
        ti.addProcessorCount();
        
    }

    /**
     *  interrupts a running process
     */
    public void interruptProcess(String processName,
                                 String processId)
        throws Exception
    {

        ThreadInfo ti = (ThreadInfo) _processTable.get(processName);

        if (ti == null ) {
            throw new Exception("Process: " + processName + " not found");
        }

        if (!processId.equals("" + ti.getStartTime())) {
            throw new Exception("Process: " + processName + 
                                " does not have id: " + processId);

        }

        Thread t = ti.getThread();

        //        t.interrupt();

        if ( t == Thread.currentThread() ) {
            throw new Exception("a thread should not kill itself");
        }

        if (!t.isAlive()) {

        } else {
            Statement stmt = (Statement) ti.getProperty("statement");
            if (stmt != null) {
                stmt.cancel();
            }

            t.interrupt();
        }

        _processTable.remove(processName);

    }

    /**
     * records some information related to an FX Processor thread
     */
    private class ThreadInfo
    {
        private Hashtable _properties = new Hashtable();
        private long _startTime;
        private Thread _thread = null;
        private int _xProcessors = 0;

        /**
         *
         */
        public ThreadInfo(Thread t)
        {
            _thread = t;
            _startTime = System.currentTimeMillis();
        }

        /**
         *
         */
        public void setProperty(String key, Object value)
        {
            if (value == null) {
                _properties.put(key, value);
            } else {
                _properties.remove(key);
            }
        }

        /**
         *
         */
        public Object getProperty(String key)
        {
            return _properties.get(key);
        }

        /**
         *
         */
        public Thread getThread()
        {
            return _thread;
        }

        /**
         *
         */
        public long getStartTime()
        {
            return _startTime;
        }

        /**
         *
         */
        public void addProcessorCount()
        { ++ _xProcessors; }

        /**
         */
        public void decrementProcessorCount()
        { -- _xProcessors; }

        /**
         */
        public int getProcessorCount()
        {  return _xProcessors; }

        /**
         * emit an XML representation of this thread metadata
         */
        public void asXML(ContentHandler ch) throws SAXException
        {
            AttributesImpl atts = new AttributesImpl();

            atts.addAttribute("", "name", "name", "CDATA",
                              _thread.getName());

            Date date = new Date(_startTime);

            String since = DateFormat.getInstance().format(date);
            atts.addAttribute("", "startTime", "startTime", "CDATA",
                              since);

            atts.addAttribute("", "startTimeMillis", "startTimeMillis", "CDATA",
                              "" + _startTime);
                                 
            ch.startElement("", "thread", "thread", atts);

            ch.endElement("", "thread", "thread");
        }
    }
}

