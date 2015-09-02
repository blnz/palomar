package com.blnz.fxpl.core;

import java.io.StringReader;

import com.blnz.fxpl.FXConnection;
import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.FXException;

import com.blnz.fxpl.security.User;
import com.blnz.fxpl.XProcessor;
import com.blnz.fxpl.xform.XForm;

import com.blnz.xsl.om.Node;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * maintains state for a connection to an FX XProcessor
 */
public class FXConnectionImpl
    implements FXConnection
{

    private FXContext _context;

    private User _user;

    private XProcessor _echo = null;

    public FXConnectionImpl(XProcessor xProcessor, 
                            String userName, 
                            String userCredential)
        throws FXException
    {

        _echo = xProcessor;
        try {
            connectFX(userName, userCredential);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FXException(ex);
        }
    }

    /**
     *
     */
    public User getConnectedUser()
    {
        return _user;
    }

    /**
     *
     */
    public XProcessor getFX()
    {
        return _echo;
    }

    /**
     * returns a newly extended context
     */
    public FXContext getExtendedContext()
    {
        FXContext context = _echo.createContext();
        context.setParentContext(_context);
        return context;
    }

    public FXContext getBaseContext()
    {
        return _context;
    }

    /**
     * evaluates the FX script in the given request String.
     * 
     * This method binds the empty namespace to the FXHome NAMESPACE, parses
     * the string as XML into an abstract syntax tree model of the
     * script, then evaluates the script under the given Context.  The
     * XML result of the evaluation are streamed as SAX events to the provided
     * response ContentHandler.
     *
     */
    public void evalFXString(String request, ContentHandler response,
                             FXContext context)
        throws Exception
    {
        String req = "<echo xmlns='" + FXHome.NAMESPACE + "'>" + 
            request + "</echo>";

//        if (true) {
//            System.out.println("FXConnectionImpl::evalFXString{" + req + "}");
//        }
        try {
            _echo.eval(context, XForm.createInputSourceReader(),
                       new InputSource(new StringReader(req)), response);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * evaluates the FX request with the current context.
     * writing the result as to the given ContentHandler 
     */
    public void evalFXToSAX(XMLReader reader, InputSource src, 
                              ContentHandler result, FXContext context)
        throws FXException
    {
        try {
            _echo.eval(context, reader, src, result);
        } catch (Exception ex) {
            throw new FXException(ex);
        }
    }
    


    /**
     *
     */
    private void connectFX(String userName, String password)
        throws Exception
    {
        //        System.out.println("FXImpl::connectFX() entry");
        //        System.out.println("connectFX() userName " + userName);
        
        if (userName == null || password == null) {
            throw new Exception("invalid login");
        }
        
        String login = "<request xmlns='" + FXHome.SESSION_NAMESPACE + 
            "'><login userName='" + userName + "' " + 
            "password='" + password + "' /></request>";
        
        Node resp = 
            ((XProcessorImpl)_echo).evalToResponse(null, 
                                                   XForm.createInputSourceReader(),
                                                   new InputSource(new StringReader(login)));
        
        // NodeList contexts = resp.getElementsByTagName("context");
        
        // if (contexts != null && contexts.getLength() > 0) {
        //     _context = (FXContext) contexts.item(0);
        // } else {
        //     _context = _echo.createContext();
        // }
        
        _context = _echo.createContext();
        _context.put("path", "/");

        _echo.setCredentials((String) _context.get("userID"), (String) _context.get("connectionID"), 
                             (String) _context.get("credential"));

        
    }

    /**
     * examines a Response for errors, and re-throws
     * any exception which has been reported
     */
    private void checkResponse(Node resp) 
        throws FXException
    {
        // NodeList errors = resp.getElementsByTagName("error");
        // if (errors != null && errors.getLength() > 0) {
        //     throw new FXException("error from FX");
        // } 
    }
}



