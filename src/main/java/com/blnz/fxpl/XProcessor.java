package com.blnz.fxpl;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.blnz.fxpl.xform.TransformException;

import com.blnz.xsl.om.Node;

import java.io.IOException;


/**
 <p> The top level interface to an FX Processor, which can
 operate either in server mode, client mode or both. </p>
 <p>In server mode, one typically obtains an FX request from a stream.
 Obtain an XMLReader for that stream, perhaps from the Transformation
 service, and then evaluate the request
 */
public interface XProcessor
{

    /**
     * create an initial context for a request
     */
    public FXContext createContext();


    /**
     *
     */
    public void setCredentials(String userID, String connectionID, String credential);

    /**
     * evaluate the request, under the given context,
     * returning the result as a stream of SAX events
     *  to <code>responseTarget</code>
     */
    public void eval(FXContext context, 
                     Node request, 
                     ContentHandler responseTarget) 
	throws Exception;


    /**
     *
     */
    public Node compile(XMLReader reader,
                             InputSource isrc)
	throws TransformException, SAXException, IOException;
    
    /**
     * evaluate the request, under the given context,
     * returning the result as a stream of SAX events
     *  to <code>responseTarget</code>
     */
    public void eval(FXContext ctx, 
                     XMLReader reader,
                     InputSource isrc,
                     ContentHandler responseTarget) 
	throws Exception;

    /**
     * returns the number of FX processes which are being processed
     * by instances of this class at this moment
     */
    public int getActiveProcessCount();

    /**
     * lists the running processes by emitting a SAX event stream
     * to the target ContentHandler
     */
    public void listProcesses(ContentHandler ch)
        throws SAXException;

    /**
     *  interrupts a running process
     */
    public void interruptProcess(String processName,
                                 String processId)
        throws Exception;

    /**
     * record state information for a thread
     */
    public void setThreadProperty(String propName,
                                  Object value);

}
