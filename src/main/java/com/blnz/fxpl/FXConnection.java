package com.blnz.fxpl;

import com.blnz.fxpl.security.User;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;

/**
 * A connection to an FXPL Processor for a given User.
 */
public interface FXConnection
{
    
    /**
     * @return the User associated with this connection
     */
    public User getConnectedUser();

    /**
     * returns the original  context, with bindings for the connection
     * parameters. Bindings made in this context persist for for the lifetime
     * of this connection.
     */
    public FXContext getBaseContext();


    /**
     * returns a new context, with bindings for the connection
     * parameters, and ready for extending with scoped bindings.
     */
    public FXContext getExtendedContext();


    /**
     * treats the string as a well-formed ECHO command, wraps it in
     * an FXPL Request object, and sends it to an ECHO processor
     * for evaluation
     */
    public void evalFXString(String request, ContentHandler response,
                             FXContext context)
        throws Exception;


    /**
     * evaluates the ECHO request with the current context.
     * writing the result as to the given ContentHandler 
     */
    public void evalFXToSAX(XMLReader request, InputSource src, 
                            ContentHandler result, FXContext context)
        throws FXException;


    /**
     * return the ECHO XProcessor for this connection.
     */
    public XProcessor getFX();

}
